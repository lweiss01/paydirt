package com.lweiss01.paydirt.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lweiss01.paydirt.domain.engine.PayoffEngine
import com.lweiss01.paydirt.domain.model.Card
import com.lweiss01.paydirt.domain.model.PayoffPlan
import com.lweiss01.paydirt.domain.usecase.CalculatePayoffPlanUseCase
import com.lweiss01.paydirt.domain.usecase.GetCardsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val cards: List<Card> = emptyList(),
    val totalBalance: Double = 0.0,
    val quickPlan: PayoffPlan? = null,
    val isLoading: Boolean = true,
    val extraMonthly: Double = 50.0,
    val strategy: PayoffEngine.Strategy = PayoffEngine.Strategy.AVALANCHE
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCardsUseCase: GetCardsUseCase,
    private val calculatePayoffPlanUseCase: CalculatePayoffPlanUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeCards()
    }

    private fun observeCards() {
        viewModelScope.launch {
            getCardsUseCase()
                .collect { cards ->
                    val totalBalance = cards.sumOf { it.currentBalance }
                    _uiState.update { it.copy(
                        cards = cards,
                        totalBalance = totalBalance,
                        isLoading = false
                    )}
                    recalculate(cards)
                }
        }
    }

    fun setExtraMonthly(amount: Double) {
        _uiState.update { it.copy(extraMonthly = amount) }
        recalculate(_uiState.value.cards)
    }

    fun setStrategy(strategy: PayoffEngine.Strategy) {
        _uiState.update { it.copy(strategy = strategy) }
        recalculate(_uiState.value.cards)
    }

    private fun recalculate(cards: List<Card>) {
        if (cards.isEmpty()) {
            _uiState.update { it.copy(quickPlan = null) }
            return
        }
        viewModelScope.launch {
            val plan = calculatePayoffPlanUseCase(
                cards = cards,
                extraMonthly = _uiState.value.extraMonthly,
                strategy = _uiState.value.strategy
            )
            _uiState.update { it.copy(quickPlan = plan) }
        }
    }
}
