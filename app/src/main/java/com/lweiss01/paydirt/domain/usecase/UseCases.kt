package com.lweiss01.paydirt.domain.usecase

import com.lweiss01.paydirt.data.repository.CardRepository
import com.lweiss01.paydirt.data.repository.GoalSettingsRepository
import com.lweiss01.paydirt.data.repository.PaymentRepository
import com.lweiss01.paydirt.domain.engine.PayoffEngine
import com.lweiss01.paydirt.domain.model.Card
import com.lweiss01.paydirt.domain.model.CardPayoffDetail
import com.lweiss01.paydirt.domain.model.HomePaymentSummary
import com.lweiss01.paydirt.domain.model.Payment
import com.lweiss01.paydirt.domain.model.PayoffPlan
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetCardsUseCase @Inject constructor(
    private val cardRepository: CardRepository
) {
    operator fun invoke() = cardRepository.getActiveCards()
}

class AddCardUseCase @Inject constructor(
    private val cardRepository: CardRepository
) {
    suspend operator fun invoke(card: Card): Long =
        cardRepository.insertCard(card.copy(originalBalance = card.currentBalance))
}

class UpdateCardUseCase @Inject constructor(
    private val cardRepository: CardRepository
) {
    suspend operator fun invoke(card: Card) =
        cardRepository.updateCard(card)
}

class ArchiveCardUseCase @Inject constructor(
    private val cardRepository: CardRepository
) {
    suspend operator fun invoke(cardId: Long) =
        cardRepository.archiveCard(cardId)
}

class LogPaymentUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val cardRepository: CardRepository
) {
    suspend operator fun invoke(payment: Payment) {
        // Insert the payment record
        paymentRepository.insertPayment(payment)

        // Deduct from card's current balance
        val card = cardRepository.getCardById(payment.cardId) ?: return
        val newBalance = maxOf(0.0, card.currentBalance - payment.amount)
        cardRepository.updateBalance(payment.cardId, newBalance)
    }
}

class GetPaymentsForCardUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository
) {
    operator fun invoke(cardId: Long) =
        paymentRepository.getPaymentsForCard(cardId)
}

class GetHomePaymentSummaryUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository
) {
    operator fun invoke(): Flow<HomePaymentSummary> =
        paymentRepository.getHomePaymentSummary()
}

class GetMonthlyGoalUseCase @Inject constructor(
    private val goalSettingsRepository: GoalSettingsRepository,
) {
    operator fun invoke(): Flow<Double> = goalSettingsRepository.observeMonthlyGoal()

    suspend fun current(): Double = goalSettingsRepository.getMonthlyGoal()
}

class UpdateMonthlyGoalUseCase @Inject constructor(
    private val goalSettingsRepository: GoalSettingsRepository,
) {
    suspend operator fun invoke(monthlyGoal: Double) =
        goalSettingsRepository.updateMonthlyGoal(monthlyGoal)
}

class CalculatePayoffPlanUseCase @Inject constructor(
    private val cardRepository: CardRepository
) {
    suspend operator fun invoke(
        cards: List<Card>,
        extraMonthly: Double,
        strategy: PayoffEngine.Strategy
    ): PayoffPlan {
        val inputs = cards.map { card ->
            PayoffEngine.CardInput(
                id = card.id,
                name = card.name,
                balance = card.currentBalance,
                apr = card.apr,
                minPayment = card.minPayment
            )
        }

        val result = PayoffEngine.optimize(inputs, extraMonthly, strategy)
        val now = System.currentTimeMillis()

        val cardDetails = result.plan.cards.map { cr ->
            CardPayoffDetail(
                cardId = cr.cardId,
                name = cr.name,
                originalBalance = cr.originalBalance,
                apr = cr.apr,
                paidOffMonth = cr.paidOffMonth,
                totalInterestPaid = cr.totalInterestPaid,
                payoffOrder = cr.payoffOrder,
                paidOffDate = addMonths(now, cr.paidOffMonth)
            )
        }

        return PayoffPlan(
            cards = cardDetails,
            totalMonths = result.plan.totalMonths,
            totalInterestPaid = result.plan.totalInterestPaid,
            interestSaved = result.interestSaved,
            monthsSaved = result.monthsSaved,
            recommendedTargetId = result.recommendedTargetId,
            recommendedTargetName = result.recommendedTargetName,
            debtFreeDate = addMonths(now, result.plan.totalMonths)
        )
    }

    private fun addMonths(epochMillis: Long, months: Int): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = epochMillis
            add(Calendar.MONTH, months)
        }
        return cal.timeInMillis
    }
}
