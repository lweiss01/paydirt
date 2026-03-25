package com.lweiss01.paydirt.ui.screens.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lweiss01.paydirt.domain.engine.PayoffEngine
import com.lweiss01.paydirt.domain.model.Card
import com.lweiss01.paydirt.domain.usecase.AddCardUseCase
import com.lweiss01.paydirt.domain.usecase.GetCardsUseCase
import com.lweiss01.paydirt.domain.usecase.UpdateCardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditCardUiState(
    val name: String = "",
    val balance: String = "",
    val apr: String = "",
    val minPayment: String = "",
    val colorTag: Int = 0,
    val estimatedMinPayment: String = "",
    val isSaving: Boolean = false,
    val isValid: Boolean = false
)

@HiltViewModel
class AddEditCardViewModel @Inject constructor(
    private val addCardUseCase: AddCardUseCase,
    private val updateCardUseCase: UpdateCardUseCase,
    private val getCardsUseCase: GetCardsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditCardUiState())
    val uiState: StateFlow<AddEditCardUiState> = _uiState.asStateFlow()

    fun loadCard(cardId: Long) {
        viewModelScope.launch {
            getCardsUseCase().firstOrNull()
                ?.firstOrNull { it.id == cardId }
                ?.let { card ->
                    _uiState.update {
                        it.copy(
                            name = card.name,
                            balance = card.currentBalance.toString(),
                            apr = card.apr.toString(),
                            minPayment = card.minPayment.toString(),
                            colorTag = card.colorTag
                        )
                    }
                    validate()
                }
        }
    }

    fun setName(v: String)       { _uiState.update { it.copy(name = v) };        validate() }
    fun setBalance(v: String)    { _uiState.update { it.copy(balance = v) };      estimateMin(); validate() }
    fun setApr(v: String)        { _uiState.update { it.copy(apr = v) };          estimateMin(); validate() }
    fun setMinPayment(v: String) { _uiState.update { it.copy(minPayment = v) };   validate() }
    fun setColorTag(v: Int)      { _uiState.update { it.copy(colorTag = v) } }

    fun useEstimatedMin() {
        _uiState.update { it.copy(minPayment = it.estimatedMinPayment) }
        validate()
    }

    private fun estimateMin() {
        val balance = _uiState.value.balance.toDoubleOrNull() ?: return
        val apr = _uiState.value.apr.toDoubleOrNull() ?: return
        val est = PayoffEngine.estimateMinPayment(balance, apr)
        _uiState.update { it.copy(estimatedMinPayment = "%.0f".format(est)) }
    }

    private fun validate() {
        val s = _uiState.value
        val valid = s.name.isNotBlank()
            && (s.balance.toDoubleOrNull() ?: 0.0) > 0
            && (s.apr.toDoubleOrNull() ?: 0.0) > 0
            && (s.minPayment.toDoubleOrNull() ?: 0.0) > 0
        _uiState.update { it.copy(isValid = valid) }
    }

    suspend fun save(existingCardId: Long?): Boolean {
        val s = _uiState.value
        if (!s.isValid) return false
        _uiState.update { it.copy(isSaving = true) }
        return try {
            val card = Card(
                id = existingCardId ?: 0,
                name = s.name.trim(),
                currentBalance = s.balance.toDouble(),
                originalBalance = s.balance.toDouble(),
                apr = s.apr.toDouble(),
                minPayment = s.minPayment.toDouble(),
                colorTag = s.colorTag
            )
            if (existingCardId != null) updateCardUseCase(card)
            else addCardUseCase(card)
            true
        } catch (e: Exception) {
            false
        } finally {
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}
