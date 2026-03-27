package com.lweiss01.paydirt.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lweiss01.paydirt.domain.engine.BehaviorEngine
import com.lweiss01.paydirt.domain.engine.PayoffEngine
import com.lweiss01.paydirt.domain.model.BehaviorState
import com.lweiss01.paydirt.domain.model.Card
import com.lweiss01.paydirt.domain.model.HomePaymentSummary
import com.lweiss01.paydirt.domain.model.PayoffPlan
import com.lweiss01.paydirt.domain.usecase.CalculatePayoffPlanUseCase
import com.lweiss01.paydirt.domain.usecase.GetCardsUseCase
import com.lweiss01.paydirt.domain.usecase.GetHomePaymentSummaryUseCase
import com.lweiss01.paydirt.domain.usecase.GetMonthlyGoalUseCase
import com.lweiss01.paydirt.domain.usecase.UpdateMonthlyGoalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

private const val DEFAULT_EXTRA_MONTHLY = 50.0
private val DEFAULT_STRATEGY = PayoffEngine.Strategy.AVALANCHE

data class HomeRecommendationUi(
    val cardId: Long,
    val cardName: String,
    val title: String,
    val body: String,
    val interestSavedLabel: String,
    val monthsSavedLabel: String,
    val debtFreeDateLabel: String,
)

data class HomeCalloutUi(
    val title: String,
    val body: String,
)

data class HomeProgressUi(
    val headline: String,
    val paceLabel: String,
    val totalsLabel: String,
    val momentumCallout: HomeCalloutUi,
    val recentPaymentCallout: HomeCalloutUi?,
    val nextOpportunityCallout: HomeCalloutUi?,
)

data class HomeUiState(
    val cards: List<Card> = emptyList(),
    val totalBalance: Double = 0.0,
    val quickPlan: PayoffPlan? = null,
    val recommendation: HomeRecommendationUi? = null,
    val behaviorState: BehaviorState? = null,
    val progress: HomeProgressUi? = null,
    val isLoading: Boolean = true,
    val extraMonthly: Double = DEFAULT_EXTRA_MONTHLY,
    val strategy: PayoffEngine.Strategy = DEFAULT_STRATEGY,
    val monthlyGoal: Double = DEFAULT_EXTRA_MONTHLY,
    val isEditingGoal: Boolean = false,
    val goalInput: String = formatGoalInput(DEFAULT_EXTRA_MONTHLY),
    val isSavingGoal: Boolean = false,
    val isShowingFirstRecommendationReveal: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCardsUseCase: GetCardsUseCase,
    private val getHomePaymentSummaryUseCase: GetHomePaymentSummaryUseCase,
    private val calculatePayoffPlanUseCase: CalculatePayoffPlanUseCase,
    private val getMonthlyGoalUseCase: GetMonthlyGoalUseCase,
    private val updateMonthlyGoalUseCase: UpdateMonthlyGoalUseCase,
) : ViewModel() {

    private val extraMonthly = MutableStateFlow(DEFAULT_EXTRA_MONTHLY)
    private val strategy = MutableStateFlow(DEFAULT_STRATEGY)
    private val firstRecommendationReveal = MutableStateFlow(false)
    private val isEditingGoal = MutableStateFlow(false)
    private val goalDraft = MutableStateFlow(formatGoalInput(DEFAULT_EXTRA_MONTHLY))
    private val isSavingGoal = MutableStateFlow(false)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeHomeState()
    }

    fun setExtraMonthly(amount: Double) {
        extraMonthly.value = amount
    }

    fun setStrategy(strategy: PayoffEngine.Strategy) {
        this.strategy.value = strategy
    }

    fun showFirstRecommendationReveal() {
        firstRecommendationReveal.value = true
    }

    fun dismissFirstRecommendationReveal() {
        firstRecommendationReveal.value = false
    }

    fun beginGoalEdit() {
        goalDraft.value = formatGoalInput(_uiState.value.monthlyGoal)
        isEditingGoal.value = true
    }

    fun updateGoalInput(input: String) {
        goalDraft.value = sanitizeGoalInput(input)
    }

    fun cancelGoalEdit() {
        isEditingGoal.value = false
    }

    fun saveGoal() {
        val parsedGoal = goalDraft.value.toDoubleOrNull()?.takeIf { it > 0.0 } ?: return
        viewModelScope.launch {
            isSavingGoal.value = true
            try {
                updateMonthlyGoalUseCase(parsedGoal)
                isEditingGoal.value = false
            } finally {
                isSavingGoal.value = false
            }
        }
    }

    private fun observeHomeState() {
        viewModelScope.launch {
            combine(
                getCardsUseCase(),
                getHomePaymentSummaryUseCase(),
                getMonthlyGoalUseCase(),
                extraMonthly,
                strategy,
                firstRecommendationReveal,
                isEditingGoal,
                goalDraft,
                isSavingGoal,
            ) { values ->
                @Suppress("UNCHECKED_CAST")
                val cards = values[0] as List<Card>
                val paymentSummary = values[1] as HomePaymentSummary
                val monthlyGoal = values[2] as Double
                val extraMonthly = values[3] as Double
                val strategy = values[4] as PayoffEngine.Strategy
                val firstRecommendationReveal = values[5] as Boolean
                val isEditingGoal = values[6] as Boolean
                val goalDraft = values[7] as String
                val isSavingGoal = values[8] as Boolean

                buildHomeUiState(
                    cards = cards,
                    paymentSummary = paymentSummary,
                    monthlyGoal = monthlyGoal,
                    extraMonthly = extraMonthly,
                    strategy = strategy,
                    isShowingFirstRecommendationReveal = firstRecommendationReveal,
                    isEditingGoal = isEditingGoal,
                    goalDraft = goalDraft,
                    isSavingGoal = isSavingGoal,
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private suspend fun buildHomeUiState(
        cards: List<Card>,
        paymentSummary: HomePaymentSummary,
        monthlyGoal: Double,
        extraMonthly: Double,
        strategy: PayoffEngine.Strategy,
        isShowingFirstRecommendationReveal: Boolean,
        isEditingGoal: Boolean,
        goalDraft: String,
        isSavingGoal: Boolean,
    ): HomeUiState {
        val totalBalance = cards.sumOf { it.currentBalance }
        val effectiveGoalInput = if (isEditingGoal) goalDraft else formatGoalInput(monthlyGoal)
        if (cards.isEmpty()) {
            return HomeUiState(
                cards = cards,
                totalBalance = totalBalance,
                isLoading = false,
                extraMonthly = extraMonthly,
                strategy = strategy,
                monthlyGoal = monthlyGoal,
                isEditingGoal = isEditingGoal,
                goalInput = effectiveGoalInput,
                isSavingGoal = isSavingGoal,
                isShowingFirstRecommendationReveal = false,
            )
        }

        val currentPlan = calculatePayoffPlanUseCase(
            cards = cards,
            extraMonthly = extraMonthly,
            strategy = strategy,
        )
        val originalPlan = calculatePayoffPlanUseCase(
            cards = cards.map { it.copy(currentBalance = it.originalBalance) },
            extraMonthly = extraMonthly,
            strategy = strategy,
        )

        val behaviorState = BehaviorEngine.computeHomeState(
            totalInterestSaved = maxOf(0.0, originalPlan.totalInterestPaid - currentPlan.totalInterestPaid),
            totalExtraPayments = paymentSummary.totalExtraPayments,
            extraThisMonth = paymentSummary.extraThisMonth,
            monthlyGoal = monthlyGoal,
            currentPlan = currentPlan,
            paymentCountThisMonth = paymentSummary.paymentCountThisMonth,
            lastPaymentAmount = paymentSummary.lastPaymentAmount,
            lastPaymentInterestSaved = estimateLastPaymentInterestSaved(
                lastPaymentAmount = paymentSummary.lastPaymentAmount,
                cards = cards,
                currentPlan = currentPlan,
            ),
        )

        return HomeUiState(
            cards = cards,
            totalBalance = totalBalance,
            quickPlan = currentPlan,
            recommendation = buildRecommendation(plan = currentPlan),
            behaviorState = behaviorState,
            progress = buildProgress(behaviorState),
            isLoading = false,
            extraMonthly = extraMonthly,
            strategy = strategy,
            monthlyGoal = monthlyGoal,
            isEditingGoal = isEditingGoal,
            goalInput = effectiveGoalInput,
            isSavingGoal = isSavingGoal,
            isShowingFirstRecommendationReveal = isShowingFirstRecommendationReveal,
        )
    }

    private fun estimateLastPaymentInterestSaved(
        lastPaymentAmount: Double?,
        cards: List<Card>,
        currentPlan: PayoffPlan,
    ): Double? {
        if (lastPaymentAmount == null || lastPaymentAmount <= 0.0) return null
        val targetCard = cards.firstOrNull { it.id == currentPlan.recommendedTargetId } ?: return null
        return PayoffEngine.monthlyInterestCharge(
            balance = lastPaymentAmount,
            apr = targetCard.apr,
        )
    }

    private fun buildRecommendation(plan: PayoffPlan): HomeRecommendationUi = HomeRecommendationUi(
        cardId = plan.recommendedTargetId,
        cardName = plan.recommendedTargetName,
        title = "Put extra on ${plan.recommendedTargetName}",
        body = when {
            plan.interestSaved > 0.0 && plan.monthsSaved > 0 ->
                "This is the clearest next move right now: save ${formatCurrency(plan.interestSaved)} and cut ${formatMonths(plan.monthsSaved)}."
            plan.interestSaved > 0.0 ->
                "This is the clearest next move right now: save ${formatCurrency(plan.interestSaved)} in projected interest."
            else -> "This card is still the clearest next move right now."
        },
        interestSavedLabel = formatCurrency(plan.interestSaved),
        monthsSavedLabel = formatMonths(plan.monthsSaved),
        debtFreeDateLabel = formatDate(plan.debtFreeDate),
    )

    private fun buildProgress(state: BehaviorState): HomeProgressUi {
        val momentumCallout = HomeCalloutUi(
            title = momentumTitle(state),
            body = buildMomentumBody(state),
        )
        val recentPaymentCallout = buildRecentPaymentCallout(state)
        val nextOpportunityCallout = buildNextOpportunityCallout(state)

        return HomeProgressUi(
            headline = when {
                state.totalExtraPayments <= 0.0 -> "No extra payments yet this month."
                state.isAheadOfGoal -> "You’re ahead of your monthly pace."
                state.goalProgress >= 0.6f -> "You’re within reach of this month’s goal."
                else -> "You’re building this month’s payoff momentum."
            },
            paceLabel = if (state.isAheadOfGoal) {
                "Goal cleared • ${formatCurrency(state.extraThisMonth)} extra this month"
            } else {
                "${formatCurrency(state.extraThisMonth)} of ${formatCurrency(state.monthlyGoal)} extra this month"
            },
            totalsLabel = "${formatCurrency(state.totalInterestSaved)} saved so far • ${formatCurrency(state.projectedTotalSavings)} projected",
            momentumCallout = momentumCallout,
            recentPaymentCallout = recentPaymentCallout,
            nextOpportunityCallout = nextOpportunityCallout,
        )
    }

    private fun buildMomentumBody(state: BehaviorState): String {
        val base = when (state.momentumScore) {
            com.lweiss01.paydirt.domain.model.MomentumScore.NONE ->
                "No extra payments are logged yet. The first extra payment will start the payoff signal here."
            com.lweiss01.paydirt.domain.model.MomentumScore.BUILDING ->
                "An extra payment is already logged this month. Keep the pace steady and the payoff effect starts to stack."
            com.lweiss01.paydirt.domain.model.MomentumScore.STRONG ->
                "This month already has a few extra payments logged. The payoff pace is holding."
            com.lweiss01.paydirt.domain.model.MomentumScore.COMPOUNDING ->
                "You’re past this month’s saved goal. Extra payments above it keep compounding the payoff."
        }
        val flexNudge = BehaviorEngine.buildFlexNudge(
            extraThisMonth = state.extraThisMonth,
            monthlyGoal = state.monthlyGoal,
        )
        return if (flexNudge != null) {
            "$base $flexNudge"
        } else {
            base
        }
    }

    private fun buildRecentPaymentCallout(state: BehaviorState): HomeCalloutUi? {
        val amount = state.lastPaymentAmount?.takeIf { it > 0.0 } ?: return null
        val interestSaved = state.lastPaymentInterestSaved?.takeIf { it > 0.0 } ?: return null
        return HomeCalloutUi(
            title = "Recent payment impact",
            body = "Your latest extra payment of ${formatCurrency(amount)} likely trimmed about ${formatCurrency(interestSaved)} in interest.",
        )
    }

    private fun buildNextOpportunityCallout(state: BehaviorState): HomeCalloutUi? {
        if (state.nextOpportunityInterestSaved <= 0.01) return null
        return HomeCalloutUi(
            title = "Next opportunity",
            body = "${formatCurrency(state.nextOpportunityAmount)} here could save about ${formatCurrency(state.nextOpportunityInterestSaved)} more.",
        )
    }

    private fun momentumTitle(state: BehaviorState): String = when (state.momentumScore) {
        com.lweiss01.paydirt.domain.model.MomentumScore.NONE -> "Momentum"
        com.lweiss01.paydirt.domain.model.MomentumScore.BUILDING -> "Momentum"
        com.lweiss01.paydirt.domain.model.MomentumScore.STRONG -> "On track"
        com.lweiss01.paydirt.domain.model.MomentumScore.COMPOUNDING -> "Compounding"
    }

    private fun formatCurrency(amount: Double): String =
        NumberFormat.getCurrencyInstance(Locale.US).apply {
            if (kotlin.math.abs(amount) < 10.0) {
                minimumFractionDigits = 2
                maximumFractionDigits = 2
            } else {
                maximumFractionDigits = 0
            }
        }.format(amount)

    private fun formatMonths(months: Int): String = when {
        months <= 0 -> "—"
        months >= 12 -> {
            val years = months / 12
            val remainingMonths = months % 12
            if (remainingMonths == 0) "${years}yr" else "${years}y ${remainingMonths}mo"
        }
        else -> "${months}mo"
    }

    private fun formatDate(epochMillis: Long): String =
        SimpleDateFormat("MMM yyyy", Locale.US).format(Date(epochMillis))

    companion object {
        const val LEGACY_DEFAULT_EXTRA_MONTHLY = DEFAULT_EXTRA_MONTHLY
        val LEGACY_DEFAULT_STRATEGY = DEFAULT_STRATEGY
    }
}

private fun formatGoalInput(amount: Double): String {
    val rounded = amount.toLong().toDouble()
    return if (amount == rounded) rounded.toLong().toString() else amount.toString()
}

private fun sanitizeGoalInput(input: String): String {
    val builder = StringBuilder()
    var seenDecimal = false
    input.forEach { char ->
        when {
            char.isDigit() -> builder.append(char)
            char == '.' && !seenDecimal -> {
                builder.append(char)
                seenDecimal = true
            }
        }
    }
    return builder.toString()
}
