package com.lweiss01.paydirt.ui.screens.optimizer

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

data class OptimizerUiState(
    val cards: List<Card> = emptyList(),
    val extraMonthly: String = "50",
    val strategy: PayoffEngine.Strategy = PayoffEngine.Strategy.AVALANCHE,
    val plan: PayoffPlan? = null,
    val isCalculating: Boolean = false
)

@HiltViewModel
class OptimizerViewModel @Inject constructor(
    private val getCardsUseCase: GetCardsUseCase,
    private val calculatePayoffPlanUseCase: CalculatePayoffPlanUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OptimizerUiState())
    val uiState: StateFlow<OptimizerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getCardsUseCase().collect { cards ->
                _uiState.update { it.copy(cards = cards) }
                recalculate()
            }
        }
    }

    fun setExtra(value: String) {
        _uiState.update { it.copy(extraMonthly = value) }
        recalculate()
    }

    fun setStrategy(strategy: PayoffEngine.Strategy) {
        _uiState.update { it.copy(strategy = strategy) }
        recalculate()
    }

    private fun recalculate() {
        val state = _uiState.value
        val extra = state.extraMonthly.toDoubleOrNull() ?: return
        if (state.cards.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCalculating = true) }
            val plan = calculatePayoffPlanUseCase(state.cards, extra, state.strategy)
            _uiState.update { it.copy(plan = plan, isCalculating = false) }
        }
    }
}
