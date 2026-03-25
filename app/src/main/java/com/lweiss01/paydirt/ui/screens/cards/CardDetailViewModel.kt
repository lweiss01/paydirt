package com.lweiss01.paydirt.ui.screens.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lweiss01.paydirt.data.repository.CardRepository
import com.lweiss01.paydirt.domain.model.Card
import com.lweiss01.paydirt.domain.model.Payment
import com.lweiss01.paydirt.domain.usecase.GetPaymentsForCardUseCase
import com.lweiss01.paydirt.domain.usecase.LogPaymentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CardDetailUiState(
    val card: Card? = null,
    val payments: List<Payment> = emptyList(),
    val totalPaid: Double = 0.0,
    val isLoading: Boolean = true
)

@HiltViewModel
class CardDetailViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    private val getPaymentsForCardUseCase: GetPaymentsForCardUseCase,
    private val logPaymentUseCase: LogPaymentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardDetailUiState())
    val uiState: StateFlow<CardDetailUiState> = _uiState.asStateFlow()

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
            logPaymentUseCase(
                Payment(
                    cardId = cardId,
                    amount = amount,
                    isExtraPayment = isExtra,
                    note = note?.ifBlank { null }
                )
            )
        }
    }
}
