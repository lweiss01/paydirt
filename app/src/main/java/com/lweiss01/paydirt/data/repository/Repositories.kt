package com.lweiss01.paydirt.data.repository

import com.lweiss01.paydirt.data.local.dao.CardDao
import com.lweiss01.paydirt.data.local.dao.GoalSettingsDao
import com.lweiss01.paydirt.data.local.dao.PaymentDao
import com.lweiss01.paydirt.data.local.entity.DEFAULT_MONTHLY_GOAL
import com.lweiss01.paydirt.data.local.entity.GoalSettingsEntity
import com.lweiss01.paydirt.data.local.mapper.toDomain
import com.lweiss01.paydirt.data.local.mapper.toEntity
import com.lweiss01.paydirt.domain.model.Card
import com.lweiss01.paydirt.domain.model.HomePaymentSummary
import com.lweiss01.paydirt.domain.model.Payment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardRepository @Inject constructor(
    private val cardDao: CardDao
) {
    fun getActiveCards(): Flow<List<Card>> =
        cardDao.getActiveCards().map { list -> list.map { it.toDomain() } }

    suspend fun getCardById(id: Long): Card? =
        cardDao.getCardById(id)?.toDomain()

    suspend fun insertCard(card: Card): Long =
        cardDao.insertCard(card.toEntity())

    suspend fun updateCard(card: Card) =
        cardDao.updateCard(card.toEntity())

    suspend fun updateBalance(id: Long, newBalance: Double) =
        cardDao.updateBalance(id, newBalance)

    suspend fun archiveCard(id: Long) =
        cardDao.archiveCard(id)

    fun getTotalBalance(): Flow<Double> =
        cardDao.getTotalBalance().map { it ?: 0.0 }

    fun getActiveCardCount(): Flow<Int> =
        cardDao.getActiveCardCount()
}

@Singleton
class PaymentRepository @Inject constructor(
    private val paymentDao: PaymentDao
) {
    fun getPaymentsForCard(cardId: Long): Flow<List<Payment>> =
        paymentDao.getPaymentsForCard(cardId).map { list -> list.map { it.toDomain() } }

    fun getRecentPayments(limit: Int = 20): Flow<List<Payment>> =
        paymentDao.getRecentPayments(limit).map { list -> list.map { it.toDomain() } }

    fun getHomePaymentSummary(
        limit: Int = HOME_PAYMENT_LOOKBACK,
        nowProvider: () -> Long = { System.currentTimeMillis() },
    ): Flow<HomePaymentSummary> =
        paymentDao.getRecentPayments(limit).map { entities ->
            val payments = entities.map { it.toDomain() }
            val now = Calendar.getInstance().apply { timeInMillis = nowProvider() }
            val thisMonthPayments = payments.filter { payment -> payment.isInSameMonth(now) }

            HomePaymentSummary(
                recentPayments = payments,
                totalExtraPayments = payments.filter { it.isExtraPayment }.sumOf { it.amount },
                extraThisMonth = thisMonthPayments.filter { it.isExtraPayment }.sumOf { it.amount },
                paymentCountThisMonth = thisMonthPayments.size,
                lastPaymentAmount = payments.firstOrNull()?.amount,
            )
        }

    fun getTotalPaidForCard(cardId: Long): Flow<Double> =
        paymentDao.getTotalPaidForCard(cardId).map { it ?: 0.0 }

    suspend fun insertPayment(payment: Payment): Long =
        paymentDao.insertPayment(payment.toEntity())

    suspend fun deletePaymentById(id: Long) =
        paymentDao.deletePaymentById(id)

    private fun Payment.isInSameMonth(reference: Calendar): Boolean {
        val paymentDate = Calendar.getInstance().apply { timeInMillis = paidAt }
        return paymentDate.get(Calendar.YEAR) == reference.get(Calendar.YEAR) &&
            paymentDate.get(Calendar.MONTH) == reference.get(Calendar.MONTH)
    }

    private companion object {
        const val HOME_PAYMENT_LOOKBACK = 500
    }
}

@Singleton
class GoalSettingsRepository @Inject constructor(
    private val goalSettingsDao: GoalSettingsDao,
) {
    fun observeMonthlyGoal(): Flow<Double> =
        goalSettingsDao.observeGoalSettings().map { settings ->
            settings?.monthlyGoal ?: DEFAULT_MONTHLY_GOAL
        }

    suspend fun getMonthlyGoal(): Double =
        goalSettingsDao.getGoalSettings()?.monthlyGoal ?: DEFAULT_MONTHLY_GOAL

    suspend fun updateMonthlyGoal(monthlyGoal: Double) {
        goalSettingsDao.upsertGoalSettings(
            GoalSettingsEntity(monthlyGoal = monthlyGoal)
        )
    }
}
