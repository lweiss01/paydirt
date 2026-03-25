package com.lweiss01.paydirt.domain.usecase

import com.lweiss01.paydirt.data.repository.PlaidRepository
import com.lweiss01.paydirt.domain.model.LinkedAccount
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LinkAccountUseCase @Inject constructor(
    private val plaidRepository: PlaidRepository
) {
    suspend fun getLinkToken(userId: String): Result<String> =
        plaidRepository.createLinkToken(userId)

    suspend fun onLinkSuccess(
        publicToken: String,
        institutionId: String,
        institutionName: String,
        accountIds: List<String>,
    ): Result<LinkResult> {
        val exchangeResult = plaidRepository.exchangePublicToken(
            publicToken = publicToken,
            institutionId = institutionId,
            institutionName = institutionName,
            accountIds = accountIds,
        )
        if (exchangeResult.isFailure) {
            return Result.failure(exchangeResult.exceptionOrNull()!!)
        }
        val itemId = exchangeResult.getOrThrow()
        return Result.success(
            LinkResult(
                itemId = itemId,
                institutionName = institutionName,
                cardsImported = accountIds.size
            )
        )
    }

    data class LinkResult(
        val itemId: String,
        val institutionName: String,
        val cardsImported: Int,
    )
}

class RefreshLinkedAccountsUseCase @Inject constructor(
    private val plaidRepository: PlaidRepository,
) {
    suspend operator fun invoke(forceRefresh: Boolean = false): RefreshResult {
        val result = plaidRepository.refreshAllLinkedAccounts()
        return if (result.isSuccess) {
            val updatedIds = result.getOrDefault(emptyList())
            RefreshResult.Success(
                updatedCardCount = updatedIds.size,
                reauthRequired = false
            )
        } else {
            RefreshResult.Failure(result.exceptionOrNull()?.message ?: "Unknown error")
        }
    }

    sealed class RefreshResult {
        data class Success(val updatedCardCount: Int, val reauthRequired: Boolean) : RefreshResult()
        data class Failure(val error: String) : RefreshResult()
    }
}

class ReauthAccountUseCase @Inject constructor(
    private val plaidRepository: PlaidRepository
) {
    suspend fun getReauthLinkToken(userId: String, itemId: String): Result<String> =
        plaidRepository.createLinkToken(userId)

    suspend fun onReauthSuccess(itemId: String): Result<Int> {
        val result = plaidRepository.onReauthSuccess(itemId)
        return result.map { it.size }
    }
}

class GetLinkedAccountsUseCase @Inject constructor(
    private val plaidRepository: PlaidRepository
) {
    operator fun invoke(): Flow<List<LinkedAccount>> =
        plaidRepository.getLinkedAccounts()
}

class UnlinkAccountUseCase @Inject constructor(
    private val plaidRepository: PlaidRepository
) {
    suspend operator fun invoke(itemId: String) =
        plaidRepository.unlinkItem(itemId)
}
