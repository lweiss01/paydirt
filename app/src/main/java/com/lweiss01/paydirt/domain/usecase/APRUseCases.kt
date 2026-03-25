package com.lweiss01.paydirt.domain.usecase

import com.lweiss01.paydirt.data.repository.CardRepository
import com.lweiss01.paydirt.data.repository.PaymentRepository
import com.lweiss01.paydirt.domain.engine.SmartAPREngine
import com.lweiss01.paydirt.domain.model.Card
import com.lweiss01.paydirt.domain.model.Payment
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar
import javax.inject.Inject

/**
 * InferAPRUseCase — Bridges PayDirt's payment history with SmartAPREngine.
 *
 * Since PayDirt tracks payments locally (before MX/Plaid integration),
 * this use case builds MonthlySnapshots from our own Room data.
 *
 * When MX/Plaid is integrated in V2, this will be replaced by
 * InferAPRFromLinkedAccountUseCase which uses real transaction feeds.
 */
class InferAPRFromPaymentHistoryUseCase @Inject constructor(
    private val cardRepository: CardRepository,
    private val paymentRepository: PaymentRepository
) {
    suspend operator fun invoke(cardId: Long): SmartAPREngine.APREstimate {
        val card = cardRepository.getCardById(cardId)
            ?: return SmartAPREngine.APREstimate(
                estimatedAPR = 0.0,
                confidence = SmartAPREngine.Confidence.INSUFFICIENT,
                dataPointsUsed = 0,
                monthlyEstimates = emptyList(),
                notes = listOf("Card not found."),
                isPromoPeriodSuspected = false,
                displayLabel = "APR unknown"
            )

        // If user already confirmed their APR, return it as confirmed
        if (card.apr > 0.0) {
            return SmartAPREngine.confirmed(card.apr)
        }

        // Build snapshots from payment history
        val payments = paymentRepository.getPaymentsForCard(cardId).firstOrNull() ?: emptyList()
        val snapshots = buildSnapshots(card, payments)

        return SmartAPREngine.estimate(snapshots)
    }

    private fun buildSnapshots(
        card: Card,
        payments: List<Payment>
    ): List<SmartAPREngine.MonthlySnapshot> {
        if (payments.isEmpty()) return emptyList()

        // Group payments by month
        val byMonth = payments.groupBy { payment ->
            val cal = Calendar.getInstance().apply { timeInMillis = payment.paidAt }
            Pair(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
        }

        return byMonth.entries
            .sortedWith(compareBy({ it.key.first }, { it.key.second }))
            .map { (monthKey, monthPayments) ->
                val monthStart = Calendar.getInstance().apply {
                    set(monthKey.first, monthKey.second, 1, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                // Convert payments to TransactionRecords
                val transactions = monthPayments.map { payment ->
                    SmartAPREngine.TransactionRecord(
                        amountCents = (payment.amount * 100).toLong(),
                        description = payment.note ?: "Payment",
                        postedDate = payment.paidAt,
                        category = if (payment.isExtraPayment) "Payment" else "Minimum Payment"
                    )
                }

                SmartAPREngine.MonthlySnapshot(
                    monthEpoch = monthStart,
                    openingBalance = card.originalBalance,   // best approximation pre-MX
                    closingBalance = card.currentBalance,
                    transactions = transactions
                )
            }
    }
}

/**
 * Placeholder for V2 MX/Plaid integration.
 * When linked accounts are available, this replaces the above use case.
 *
 * The interface is identical — the rest of the app doesn't care
 * where the snapshots come from.
 */
class InferAPRFromLinkedAccountUseCase @Inject constructor(
    // V2: inject MX/Plaid repository here
) {
    suspend operator fun invoke(
        cardId: Long,
        linkedAccountId: String
    ): SmartAPREngine.APREstimate {
        // V2 implementation:
        // 1. Fetch last 6 months of transactions from MX/Plaid
        // 2. Group into MonthlySnapshots
        // 3. Pass to SmartAPREngine.estimate()
        // 4. Cache result in Room to avoid re-fetching on every open
        TODO("Implemented in V2 with MX/Plaid integration")
    }
}

/**
 * GetAPREstimateUseCase — The single public entry point for APR data.
 *
 * Precedence:
 *  1. User-confirmed APR (always wins)
 *  2. Linked account inference (V2)
 *  3. Payment history inference (V1)
 *  4. Insufficient data
 */
class GetAPREstimateUseCase @Inject constructor(
    private val inferFromHistory: InferAPRFromPaymentHistoryUseCase
) {
    suspend operator fun invoke(cardId: Long): SmartAPREngine.APREstimate {
        // V1: use payment history
        // V2: check if linked account exists → use InferAPRFromLinkedAccountUseCase first
        return inferFromHistory(cardId)
    }
}
