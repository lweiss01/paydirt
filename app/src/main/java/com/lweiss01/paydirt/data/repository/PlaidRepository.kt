package com.lweiss01.paydirt.data.repository

import android.content.Context
import com.lweiss01.paydirt.data.local.dao.CardDao
import com.lweiss01.paydirt.data.local.dao.LinkedAccountDao
import com.lweiss01.paydirt.data.local.entity.CardEntity
import com.lweiss01.paydirt.data.local.entity.LinkedAccountEntity
import com.lweiss01.paydirt.data.remote.ExchangeTokenRequest
import com.lweiss01.paydirt.data.remote.PlaidApiService
import com.lweiss01.paydirt.data.remote.RefreshAccountsRequest
import com.lweiss01.paydirt.domain.model.AprSource
import com.lweiss01.paydirt.domain.model.LinkedAccount
import com.lweiss01.paydirt.domain.model.PlaidLiabilityData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaidRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cardDao: CardDao,
    private val linkedAccountDao: LinkedAccountDao,
    private val plaidApiService: PlaidApiService,
) {

    fun getLinkedAccounts(): Flow<List<LinkedAccount>> =
        linkedAccountDao.getAllLinkedAccounts().map { list ->
            list.map { it.toDomain() }
        }

    fun getLinkedAccountCount(): Flow<Int> =
        linkedAccountDao.getLinkedAccountCount()

    suspend fun getAccountsNeedingReauth(): List<LinkedAccount> =
        linkedAccountDao.getAccountsNeedingReauth().map { it.toDomain() }

    suspend fun createLinkToken(userId: String): Result<String> {
        return try {
            val response = plaidApiService.createLinkToken(userId)
            Result.success(response.linkToken)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exchangePublicToken(
        publicToken: String,
        institutionId: String,
        institutionName: String,
        accountIds: List<String>,
    ): Result<String> {
        return try {
            val response = plaidApiService.exchangeToken(
                ExchangeTokenRequest(
                    publicToken = publicToken,
                    institutionId = institutionId,
                    institutionName = institutionName,
                    accountIds = accountIds,
                )
            )

            linkedAccountDao.insertLinkedAccount(
                LinkedAccountEntity(
                    itemId = response.itemId,
                    institutionId = institutionId,
                    institutionName = institutionName,
                    accessToken = response.encryptedAccessToken,
                    linkedAt = System.currentTimeMillis(),
                )
            )

            Result.success(response.itemId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshAllLinkedAccounts(): Result<List<String>> {
        val accountsNeedingReauth = linkedAccountDao.getAccountsNeedingReauth()
        if (accountsNeedingReauth.isNotEmpty()) {
            accountsNeedingReauth.forEach { cardDao.markItemNeedsReauth(it.itemId) }
        }

        return try {
            val linkedItems = linkedAccountDao.getAllLinkedAccounts().first()
            val updatedIds = mutableListOf<String>()

            linkedItems
                .filter { !it.needsReauth }
                .forEach { item ->
                    val refreshResult = refreshItem(item)
                    if (refreshResult.isSuccess) {
                        updatedIds.addAll(refreshResult.getOrDefault(emptyList()))
                        linkedAccountDao.markRefreshed(item.itemId, System.currentTimeMillis())
                    }
                }

            Result.success(updatedIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshItem(item: LinkedAccountEntity): Result<List<String>> {
        return try {
            val response = plaidApiService.refreshAccounts(
                RefreshAccountsRequest(
                    encryptedAccessToken = item.accessToken,
                    itemId = item.itemId,
                )
            )

            val updatedAccountIds = mutableListOf<String>()

            response.liabilities.forEach { liability ->
                val existingCard = cardDao.getCardByPlaidAccountId(liability.accountId)

                if (existingCard != null) {
                    updateCardFromLiability(existingCard, liability)
                } else {
                    insertCardFromLiability(liability, item.institutionName)
                }
                updatedAccountIds.add(liability.accountId)
            }

            Result.success(updatedAccountIds)
        } catch (e: Exception) {
            if (e.message?.contains("ITEM_LOGIN_REQUIRED") == true) {
                linkedAccountDao.markNeedsReauth(item.itemId)
                cardDao.markItemNeedsReauth(item.itemId)
            }
            Result.failure(e)
        }
    }

    private suspend fun updateCardFromLiability(
        existing: CardEntity,
        liability: PlaidLiabilityData,
    ) {
        val apr = liability.purchaseApr ?: existing.apr
        val aprSource = if (liability.purchaseApr != null) {
            AprSource.PLAID.key
        } else {
            existing.aprSource ?: AprSource.UNKNOWN.key
        }

        cardDao.updateFromPlaid(
            plaidAccountId = liability.accountId,
            balance = liability.currentBalance,
            minPayment = liability.minimumPaymentAmount ?: existing.minPayment,
            apr = apr,
            aprSource = aprSource,
            refreshedAt = System.currentTimeMillis(),
            statementBalance = liability.statementBalance,
            nextPaymentDueDate = liability.nextPaymentDueDate,
            dueDate = liability.dueDate,
        )
    }

    private suspend fun insertCardFromLiability(
        liability: PlaidLiabilityData,
        institutionName: String,
    ) {
        val apr = liability.purchaseApr ?: 0.0
        val aprSource = if (liability.purchaseApr != null) AprSource.PLAID.key else AprSource.UNKNOWN.key

        cardDao.insertCard(
            CardEntity(
                name = liability.name.ifBlank { institutionName },
                currentBalance = liability.currentBalance,
                originalBalance = liability.currentBalance,
                apr = apr,
                minPayment = liability.minimumPaymentAmount ?: 0.0,
                plaidAccountId = liability.accountId,
                plaidItemId = liability.itemId,
                plaidInstitutionName = institutionName,
                plaidLastRefreshed = System.currentTimeMillis(),
                aprSource = aprSource,
                statementBalance = liability.statementBalance,
                nextPaymentDueDate = liability.nextPaymentDueDate,
                dueDate = liability.dueDate,
            )
        )
    }

    suspend fun onReauthSuccess(itemId: String): Result<List<String>> {
        linkedAccountDao.markRefreshed(itemId, System.currentTimeMillis())
        val item = linkedAccountDao.getLinkedAccount(itemId)
            ?: return Result.failure(IllegalStateException("Item $itemId not found after reauth"))
        return refreshItem(item)
    }

    suspend fun unlinkItem(itemId: String) {
        try {
            plaidApiService.unlinkItem(itemId)
        } catch (_: Exception) {
        }

        linkedAccountDao.deleteLinkedAccount(itemId)

        val cards = cardDao.getCardsByItemId(itemId)
        cards.forEach { card ->
            cardDao.updateCard(
                card.copy(
                    plaidAccountId = null,
                    plaidItemId = null,
                    plaidInstitutionName = null,
                    plaidLastRefreshed = null,
                    plaidNeedsReauth = false,
                )
            )
        }
    }

    private fun LinkedAccountEntity.toDomain() = LinkedAccount(
        itemId = itemId,
        institutionId = institutionId,
        institutionName = institutionName,
        linkedAt = linkedAt,
        lastRefreshed = lastRefreshed,
        needsReauth = needsReauth,
        consentExpiresAt = consentExpiresAt,
    )
}
