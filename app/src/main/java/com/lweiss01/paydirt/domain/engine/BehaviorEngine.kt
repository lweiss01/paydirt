package com.lweiss01.paydirt.domain.engine

import com.lweiss01.paydirt.domain.model.BehaviorState
import com.lweiss01.paydirt.domain.model.MomentumScore
import com.lweiss01.paydirt.domain.model.PayoffPlan
import java.util.Calendar
import kotlin.math.roundToInt

/**
 * BehaviorEngine — computes the state that drives the reward screen,
 * home screen summary, and all retention mechanics.
 *
 * This is pure logic, no Android deps.
 * Called by HomeViewModel after every payment or refresh.
 *
 * Implements the Hook Model reward layer:
 * - Immediate reward: interest saved on THIS payment
 * - Scaled reward: "do that 10x → ~$X total"
 * - Progress reward: goal completion %
 * - Identity reward: momentum score label
 */
object BehaviorEngine {

    // ─── Core computation ─────────────────────────────────────────────────

    data class PaymentImpact(
        val paymentAmount: Double,
        val interestSaved: Double,           // on this specific payment
        val scaledProjection: Double,         // "do that 10x → ~$X"
        val scaledProjectionReps: Int,        // how many reps = 10 (or fewer if big payment)
        val cumulativeSaved: Double,          // all time, including this payment
        val projectedTotalSavings: Double,    // full payoff engine output
        val nextOpportunityAmount: Double,    // suggested follow-up
        val nextOpportunityInterestSaved: Double,
        val goalProgressAfter: Float,         // 0f–1f
        val monthlyGoal: Double,
        val extraThisMonth: Double,
        val isAheadOfGoal: Boolean,
        val momentumScore: MomentumScore,

        // Formatted display strings — pre-computed so ViewModel is clean
        val displayImpact: String,           // "+$1.08 saved"
        val displayCumulative: String,       // "$14.32 total saved"
        val displayProjection: String,       // "On track to save ~$220"
        val displayMomentum: String,         // "Momentum: Building ↑"
        val displayNextOpp: String,          // "Another $5 right now saves $1.05"
        val displayGoalStatus: String,       // "$32 / $50 this month" or "Goal hit ✓"
        val headlineText: String,            // "Nice hit." or "Solid move." etc.
        val rewardBodyText: String,          // The full 4-line reward block
    )

    /**
     * Calculate the full impact of a payment for the reward screen.
     *
     * @param paymentAmount the amount just paid
     * @param isExtraPayment whether it's above the minimum
     * @param currentPlan the PayoffPlan BEFORE this payment
     * @param updatedPlan the PayoffPlan AFTER this payment
     * @param cumulativeSavedBefore total interest saved before this payment
     * @param extraThisMonthBefore extra payments this month before this one
     * @param monthlyGoal user's monthly extra payment goal
     * @param paymentCountThisMonth number of payments made this month (including this one)
     */
    fun calculatePaymentImpact(
        paymentAmount: Double,
        isExtraPayment: Boolean,
        currentPlan: PayoffPlan,
        updatedPlan: PayoffPlan,
        cumulativeSavedBefore: Double,
        extraThisMonthBefore: Double,
        monthlyGoal: Double,
        paymentCountThisMonth: Int,
    ): PaymentImpact {
        // Interest saved = difference in total interest between plans
        val interestSaved = maxOf(0.0, currentPlan.totalInterestPaid - updatedPlan.totalInterestPaid)
        val cumulativeSaved = cumulativeSavedBefore + interestSaved

        // Scaled projection: "do that N times → ~$X"
        val repsToScale = when {
            paymentAmount >= 100 -> 5
            paymentAmount >= 50  -> 8
            else                 -> 10
        }
        val scaledProjection = interestSaved * repsToScale

        // Next opportunity: suggest the same amount or $5 if large payment
        val nextOppAmount = when {
            paymentAmount <= 10  -> paymentAmount
            paymentAmount <= 50  -> 5.0
            else                 -> 10.0
        }
        // Approximate next opp savings proportionally
        val nextOppSaved = if (paymentAmount > 0)
            interestSaved * (nextOppAmount / paymentAmount) * 0.9 // slight decay
        else 0.0

        // Goal tracking
        val extraThisMonthAfter = if (isExtraPayment) extraThisMonthBefore + paymentAmount
                                  else extraThisMonthBefore
        val goalProgress = if (monthlyGoal > 0)
            (extraThisMonthAfter / monthlyGoal).toFloat().coerceIn(0f, 1f)
        else 0f
        val isAheadOfGoal = extraThisMonthAfter >= monthlyGoal

        // Momentum
        val momentum = calculateMomentum(paymentCountThisMonth, goalProgress, monthlyGoal)

        // Display strings
        val displayImpact = "+${formatCurrency(interestSaved)} saved"
        val displayCumulative = "${formatCurrency(cumulativeSaved)} total saved"
        val displayProjection = "On track to save ~${formatCurrency(updatedPlan.totalInterestPaid.let {
            cumulativeSaved + (currentPlan.interestSaved - interestSaved)
        })}"
        val displayMomentum = "Momentum: ${momentum.label} ${momentum.arrow}"
        val displayNextOpp = "Another ${formatCurrency(nextOppAmount)} right now saves ${formatCurrency(nextOppSaved)}"
        val displayGoalStatus = buildGoalStatus(extraThisMonthAfter, monthlyGoal, isAheadOfGoal)
        val headlineText = buildHeadline(interestSaved, paymentCountThisMonth)

        val rewardBodyText = buildRewardBody(
            interestSaved, cumulativeSaved, updatedPlan, isAheadOfGoal
        )

        return PaymentImpact(
            paymentAmount = paymentAmount,
            interestSaved = interestSaved,
            scaledProjection = scaledProjection,
            scaledProjectionReps = repsToScale,
            cumulativeSaved = cumulativeSaved,
            projectedTotalSavings = updatedPlan.totalInterestPaid,
            nextOpportunityAmount = nextOppAmount,
            nextOpportunityInterestSaved = nextOppSaved,
            goalProgressAfter = goalProgress,
            monthlyGoal = monthlyGoal,
            extraThisMonth = extraThisMonthAfter,
            isAheadOfGoal = isAheadOfGoal,
            momentumScore = momentum,
            displayImpact = displayImpact,
            displayCumulative = displayCumulative,
            displayProjection = displayProjection,
            displayMomentum = displayMomentum,
            displayNextOpp = displayNextOpp,
            displayGoalStatus = displayGoalStatus,
            headlineText = headlineText,
            rewardBodyText = rewardBodyText,
        )
    }

    /**
     * Compute BehaviorState for home screen — no payment context, just current state.
     */
    fun computeHomeState(
        totalInterestSaved: Double,
        totalExtraPayments: Double,
        extraThisMonth: Double,
        monthlyGoal: Double,
        currentPlan: PayoffPlan?,
        paymentCountThisMonth: Int,
        lastPaymentAmount: Double?,
        lastPaymentInterestSaved: Double?,
    ): BehaviorState {
        val momentum = calculateMomentum(
            paymentCountThisMonth,
            if (monthlyGoal > 0) (extraThisMonth / monthlyGoal).toFloat() else 0f,
            monthlyGoal
        )

        // Next opportunity — suggest $5 or $25 based on goal
        val nextOppAmount = when {
            monthlyGoal <= 0    -> 5.0
            extraThisMonth == 0.0 -> minOf(25.0, monthlyGoal)
            else                -> minOf(5.0, monthlyGoal - extraThisMonth).coerceAtLeast(5.0)
        }

        val nextOppSaved = currentPlan?.let {
            // Rough estimate: interest saved is proportional to payment
            val totalMin = it.cards.sumOf { c -> c.totalInterestPaid }
            if (totalMin > 0) nextOppAmount * 0.02 else 0.0
        } ?: 0.0

        return BehaviorState(
            totalInterestSaved = totalInterestSaved,
            totalExtraPayments = totalExtraPayments,
            extraThisMonth = extraThisMonth,
            monthlyGoal = monthlyGoal,
            projectedTotalSavings = currentPlan?.interestSaved ?: 0.0,
            momentumScore = momentum,
            lastPaymentAmount = lastPaymentAmount,
            lastPaymentInterestSaved = lastPaymentInterestSaved,
            nextOpportunityAmount = nextOppAmount,
            nextOpportunityInterestSaved = nextOppSaved,
        )
    }

    // ─── Flex nudge copy ──────────────────────────────────────────────────

    /**
     * Returns a flex nudge string when user is behind on their goal.
     * Returns null if no nudge is warranted.
     *
     * Examples:
     * "You're $18 behind your goal — catch up with $5?"
     * "Just $8 left to hit your goal this month."
     * null (when ahead or no goal set)
     */
    fun buildFlexNudge(
        extraThisMonth: Double,
        monthlyGoal: Double,
    ): String? {
        if (monthlyGoal <= 0) return null
        val remaining = monthlyGoal - extraThisMonth
        if (remaining <= 0) return null

        return when {
            remaining <= 5   -> "Just ${formatCurrency(remaining)} left to hit your goal this month."
            remaining <= 25  -> "You're ${formatCurrency(remaining)} behind your goal — catch up with ${formatCurrency(5.0)}?"
            remaining <= 50  -> "You're ${formatCurrency(remaining)} behind your goal — a quick ${formatCurrency(10.0)} helps."
            else             -> "Your goal is ${formatCurrency(monthlyGoal)} — ${formatCurrency(remaining)} to go this month."
        }
    }

    // ─── Private helpers ──────────────────────────────────────────────────

    private fun calculateMomentum(
        paymentCount: Int,
        goalProgress: Float,
        monthlyGoal: Double,
    ): MomentumScore = when {
        paymentCount == 0          -> MomentumScore.NONE
        goalProgress >= 1.0f       -> MomentumScore.COMPOUNDING
        paymentCount >= 3          -> MomentumScore.STRONG
        paymentCount >= 1          -> MomentumScore.BUILDING
        else                       -> MomentumScore.NONE
    }

    private fun buildHeadline(interestSaved: Double, paymentCount: Int): String {
        // Vary headlines slightly to avoid staleness — never "You're amazing!"
        return when {
            paymentCount == 1   -> "Nice hit."
            paymentCount == 5   -> "Consistent."
            paymentCount == 10  -> "Building momentum."
            interestSaved > 5   -> "Good move."
            interestSaved > 2   -> "Nice hit."
            else                -> "Small hit, right target."
        }
    }

    private fun buildGoalStatus(
        extraThisMonth: Double,
        monthlyGoal: Double,
        isAheadOfGoal: Boolean,
    ): String {
        if (monthlyGoal <= 0) return ""
        return if (isAheadOfGoal) {
            "Goal hit ✓ (${formatCurrency(extraThisMonth)} this month)"
        } else {
            "${formatCurrency(extraThisMonth)} / ${formatCurrency(monthlyGoal)} this month"
        }
    }

    private fun buildRewardBody(
        interestSaved: Double,
        cumulativeSaved: Double,
        plan: PayoffPlan,
        isAheadOfGoal: Boolean,
    ): String {
        val lines = mutableListOf<String>()
        lines.add("That move saved ${formatCurrency(interestSaved)} in interest.")
        if (cumulativeSaved > interestSaved) {
            lines.add("You've saved ${formatCurrency(cumulativeSaved)} total.")
        }
        if (isAheadOfGoal) {
            lines.add("You're ahead of your plan.")
        } else {
            lines.add("You're now slightly ahead of your baseline plan.")
        }
        return lines.joinToString("\n")
    }

    private fun formatCurrency(amount: Double): String {
        val rounded = (amount * 100).roundToInt() / 100.0
        return if (amount < 10) "$${String.format("%.2f", rounded)}"
               else "$${String.format("%.0f", rounded)}"
    }

    // Extension properties for MomentumScore display
    private val MomentumScore.label: String get() = when (this) {
        MomentumScore.NONE        -> "None"
        MomentumScore.BUILDING    -> "Building"
        MomentumScore.STRONG      -> "Strong"
        MomentumScore.COMPOUNDING -> "Compounding"
    }

    private val MomentumScore.arrow: String get() = when (this) {
        MomentumScore.NONE        -> ""
        MomentumScore.BUILDING    -> "↑"
        MomentumScore.STRONG      -> "↑↑"
        MomentumScore.COMPOUNDING -> "↑↑↑"
    }
}
