package com.lweiss01.paydirt.domain.model

/**
 * Card — updated with Plaid linking state and APR source tracking.
 */
data class Card(
    val id: Long = 0,
    val name: String,
    val currentBalance: Double,
    val originalBalance: Double,
    val apr: Double,
    val minPayment: Double,
    val colorTag: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,

    // Plaid state
    val plaidAccountId: String? = null,
    val plaidItemId: String? = null,
    val plaidInstitutionName: String? = null,
    val plaidLastRefreshed: Long? = null,
    val plaidNeedsReauth: Boolean = false,
    val aprSource: AprSource = AprSource.UNKNOWN,
    val dueDate: Int? = null,
    val statementBalance: Double? = null,
    val nextPaymentDueDate: Long? = null,
) {
    val isLinked: Boolean get() = plaidAccountId != null
    val isStale: Boolean get() = isLinked &&
        (plaidLastRefreshed == null ||
        System.currentTimeMillis() - plaidLastRefreshed > STALE_THRESHOLD_MS)

    companion object {
        // 7 days — if linked card hasn't refreshed in a week, nudge user
        const val STALE_THRESHOLD_MS = 7 * 24 * 60 * 60 * 1000L
    }
}

enum class AprSource(val key: String) {
    USER("user"),           // User entered it — confirmed
    PLAID("plaid"),         // From Plaid liabilities endpoint — confirmed
    INFERRED("inferred"),   // SmartAPREngine estimate
    UNKNOWN("unknown");     // Not set yet

    val isConfirmed: Boolean get() = this == USER || this == PLAID

    companion object {
        fun fromKey(key: String?) = values().firstOrNull { it.key == key } ?: UNKNOWN
    }
}

data class Payment(
    val id: Long = 0,
    val cardId: Long,
    val amount: Double,
    val isExtraPayment: Boolean = false,
    val note: String? = null,
    val paidAt: Long = System.currentTimeMillis()
)

data class PayoffPlan(
    val cards: List<CardPayoffDetail>,
    val totalMonths: Int,
    val totalInterestPaid: Double,
    val interestSaved: Double,
    val monthsSaved: Int,
    val recommendedTargetId: Long,
    val recommendedTargetName: String,
    val debtFreeDate: Long
)

data class CardPayoffDetail(
    val cardId: Long,
    val name: String,
    val originalBalance: Double,
    val apr: Double,
    val paidOffMonth: Int,
    val totalInterestPaid: Double,
    val payoffOrder: Int,
    val paidOffDate: Long
)

// Color tags
enum class CardColor(val tag: Int) {
    BLUE(0), GREEN(1), AMBER(2), ROSE(3), PURPLE(4), CYAN(5)
}

/**
 * LinkedAccount — a Plaid Item (one per institution).
 */
data class LinkedAccount(
    val itemId: String,
    val institutionId: String,
    val institutionName: String,
    val linkedAt: Long,
    val lastRefreshed: Long?,
    val needsReauth: Boolean,
    val consentExpiresAt: Long?,
)

/**
 * PlaidLiabilityData — raw data returned from Plaid /liabilities endpoint
 * for a single credit card account. Used to update CardEntity.
 */
data class PlaidLiabilityData(
    val accountId: String,
    val name: String,
    val institutionName: String,
    val itemId: String,
    val currentBalance: Double,
    val statementBalance: Double?,
    val minimumPaymentAmount: Double?,
    val nextPaymentDueDate: Long?,    // epoch millis
    val lastPaymentAmount: Double?,
    val lastPaymentDate: Long?,
    val purchaseApr: Double?,         // may be null — SmartAPR fills in
    val dueDate: Int?,                // day of month
)

/**
 * BehaviorState — drives the reward screen and home screen summary.
 * Computed by BehaviorEngine, observed by HomeViewModel.
 */
data class BehaviorState(
    val totalInterestSaved: Double,       // all time, cumulative
    val totalExtraPayments: Double,       // all time
    val extraThisMonth: Double,           // current calendar month
    val monthlyGoal: Double,              // user-set target
    val projectedTotalSavings: Double,    // PayoffEngine output
    val momentumScore: MomentumScore,
    val lastPaymentAmount: Double?,
    val lastPaymentInterestSaved: Double?,
    val nextOpportunityAmount: Double,    // suggested next micro-payment
    val nextOpportunityInterestSaved: Double,
) {
    val goalProgress: Float
        get() = if (monthlyGoal > 0)
            (extraThisMonth / monthlyGoal).toFloat().coerceIn(0f, 1f)
        else 0f

    val isAheadOfGoal: Boolean get() = extraThisMonth >= monthlyGoal
    val goalRemainingAmount: Double get() = maxOf(0.0, monthlyGoal - extraThisMonth)
}

enum class MomentumScore {
    NONE,       // No payments yet
    BUILDING,   // 1–2 payments this month
    STRONG,     // 3–5 payments, or goal on track
    COMPOUNDING // Goal exceeded, or consistent for 2+ months
}
