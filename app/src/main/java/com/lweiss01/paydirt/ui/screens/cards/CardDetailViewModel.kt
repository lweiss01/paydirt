package com.lweiss01.paydirt.ui.screens.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lweiss01.paydirt.data.repository.CardRepository
import com.lweiss01.paydirt.data.repository.PaymentRepository
import com.lweiss01.paydirt.domain.engine.BehaviorEngine
import com.lweiss01.paydirt.domain.engine.PayoffEngine
import com.lweiss01.paydirt.domain.model.Card
import com.lweiss01.paydirt.domain.model.Payment
import com.lweiss01.paydirt.domain.usecase.CalculatePayoffPlanUseCase
import com.lweiss01.paydirt.domain.usecase.GetMonthlyGoalUseCase
import com.lweiss01.paydirt.domain.usecase.GetPaymentsForCardUseCase
import com.lweiss01.paydirt.domain.usecase.LogPaymentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CardDetailUiState(
    val card: Card? = null,
    val payments: List<Payment> = emptyList(),
    val totalPaid: Double = 0.0,
    val isLoading: Boolean = true
)

sealed interface CardDetailEvent {
    data class RewardReady(
        val cardId: Long,
        val impact: BehaviorEngine.PaymentImpact,
    ) : CardDetailEvent
}

@HiltViewModel
class CardDetailViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    private val paymentRepository: PaymentRepository,
    private val getPaymentsForCardUseCase: GetPaymentsForCardUseCase,
    private val logPaymentUseCase: LogPaymentUseCase,
    private val calculatePayoffPlanUseCase: CalculatePayoffPlanUseCase,
    private val getMonthlyGoalUseCase: GetMonthlyGoalUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardDetailUiState())
    val uiState: StateFlow<CardDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CardDetailEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<CardDetailEvent> = _events.asSharedFlow()

    fun loadCard(cardId: Long) {
        viewModelScope.launch {
            launch {
                cardRepository.getActiveCards()
                    .collect { cards ->
                        val card = cards.firstOrNull { it.id == cardId }
                        _uiState.update { it.copy(card = card, isLoading = false) }
                    }
            }
            launch {
                getPaymentsForCardUseCase(cardId).collect { payments ->
                    val total = payments.sumOf { it.amount }
                    _uiState.update { it.copy(payments = payments, totalPaid = total) }
                }
            }
        }
    }

    fun logPayment(cardId: Long, amount: Double, isExtra: Boolean, note: String?) {
        viewModelScope.launch {
            val payment = Payment(
                cardId = cardId,
                amount = amount,
                isExtraPayment = isExtra,
                note = note?.ifBlank { null }
            )

            val cardsBefore = cardRepository.getActiveCards().first()
            val cardBefore = cardsBefore.firstOrNull { it.id == cardId }
            val recentPaymentsBefore = paymentRepository.getRecentPayments(limit = RECENT_PAYMENT_LOOKBACK).first()

            logPaymentUseCase(payment)

            if (cardBefore == null || cardsBefore.isEmpty()) {
                return@launch
            }

            val updatedCard = cardRepository.getCardById(cardId) ?: return@launch
            val cardsAfter = cardsBefore.map { card ->
                if (card.id == cardId) updatedCard else card
            }

            val rewardImpact = buildRewardImpact(
                payment = payment,
                cardsBefore = cardsBefore,
                cardsAfter = cardsAfter,
                paymentsBefore = recentPaymentsBefore,
            )

            _events.tryEmit(CardDetailEvent.RewardReady(cardId = cardId, impact = rewardImpact))
        }
    }

    private suspend fun buildRewardImpact(
        payment: Payment,
        cardsBefore: List<Card>,
        cardsAfter: List<Card>,
        paymentsBefore: List<Payment>,
    ): BehaviorEngine.PaymentImpact {
        val currentPlan = calculatePayoffPlanUseCase(
            cards = cardsBefore,
            extraMonthly = REWARD_PLAN_EXTRA_MONTHLY,
            strategy = REWARD_PLAN_STRATEGY,
        )
        val updatedPlan = calculatePayoffPlanUseCase(
            cards = cardsAfter,
            extraMonthly = REWARD_PLAN_EXTRA_MONTHLY,
            strategy = REWARD_PLAN_STRATEGY,
        )
        val originalPlan = calculatePayoffPlanUseCase(
            cards = cardsBefore.map { it.copy(currentBalance = it.originalBalance) },
            extraMonthly = REWARD_PLAN_EXTRA_MONTHLY,
            strategy = REWARD_PLAN_STRATEGY,
        )

        val thisMonthPaymentsBefore = paymentsBefore.filter { isInCurrentMonth(it.paidAt) }
        val extraThisMonthBefore = thisMonthPaymentsBefore
            .filter { it.isExtraPayment }
            .sumOf { it.amount }
        val paymentCountThisMonth = thisMonthPaymentsBefore.size + 1
        val cumulativeSavedBefore = maxOf(
            0.0,
            originalPlan.totalInterestPaid - currentPlan.totalInterestPaid,
        )
        val monthlyGoal = getMonthlyGoalUseCase.current()

        return BehaviorEngine.calculatePaymentImpact(
            paymentAmount = payment.amount,
            isExtraPayment = payment.isExtraPayment,
            currentPlan = currentPlan,
            updatedPlan = updatedPlan,
            cumulativeSavedBefore = cumulativeSavedBefore,
            extraThisMonthBefore = extraThisMonthBefore,
            monthlyGoal = monthlyGoal,
            paymentCountThisMonth = paymentCountThisMonth,
        )
    }

    private fun isInCurrentMonth(epochMillis: Long): Boolean {
        val paymentDate = Calendar.getInstance().apply { timeInMillis = epochMillis }
        val now = Calendar.getInstance()
        return paymentDate.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
            paymentDate.get(Calendar.MONTH) == now.get(Calendar.MONTH)
    }

    private companion object {
        const val REWARD_PLAN_EXTRA_MONTHLY = 50.0
        const val RECENT_PAYMENT_LOOKBACK = 500
        val REWARD_PLAN_STRATEGY = PayoffEngine.Strategy.AVALANCHE
    }
}
