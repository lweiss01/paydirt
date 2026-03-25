package com.lweiss01.paydirt.domain.engine

import kotlin.math.ln1p

/**
 * PayoffEngine — Pure Kotlin debt payoff calculator.
 * Zero Android dependencies. Fully unit-testable.
 *
 * Supports three strategies:
 *  - AVALANCHE: Target highest APR first (mathematically optimal, saves most interest)
 *  - SNOWBALL:  Target lowest balance first (psychologically motivating, quick wins)
 *  - HYBRID:    APR-weighted by balance size (balanced targeting)
 *
 * As each card is paid off, its freed minimum payment is automatically
 * stacked onto the extra pool — the "avalanche acceleration" effect.
 */
object PayoffEngine {

    enum class Strategy {
        AVALANCHE,
        SNOWBALL,
        HYBRID
    }

    data class CardInput(
        val id: Long,
        val name: String,
        val balance: Double,
        val apr: Double,         // annual percentage rate, e.g. 22.99
        val minPayment: Double
    )

    data class CardResult(
        val cardId: Long,
        val name: String,
        val originalBalance: Double,
        val apr: Double,
        val paidOffMonth: Int,
        val totalInterestPaid: Double,
        val payoffOrder: Int
    )

    data class PayoffResult(
        val cards: List<CardResult>,
        val totalMonths: Int,
        val totalInterestPaid: Double,
        val recommendedTargetId: Long
    )

    data class OptimizationResult(
        val plan: PayoffResult,
        val minOnlyPlan: PayoffResult,
        val interestSaved: Double,
        val monthsSaved: Int,
        val recommendedTargetId: Long,
        val recommendedTargetName: String
    )

    private const val MAX_MONTHS = 600  // 50 years — safety ceiling
    private const val ZERO_THRESHOLD = 0.01

    // ─── Public API ──────────────────────────────────────────────────────────

    /**
     * Full optimization: calculates plan WITH extra payment and min-only baseline,
     * then returns the delta (interest saved, months saved, recommended target).
     */
    fun optimize(
        cards: List<CardInput>,
        extraMonthly: Double,
        strategy: Strategy
    ): OptimizationResult {
        require(cards.isNotEmpty()) { "Must provide at least one card" }
        require(extraMonthly >= 0) { "Extra monthly payment cannot be negative" }

        val plan = calculate(cards, extraMonthly, strategy)
        val minOnly = calculate(cards, 0.0, strategy)

        val interestSaved = minOnly.totalInterestPaid - plan.totalInterestPaid
        val monthsSaved = minOnly.totalMonths - plan.totalMonths
        val targetCard = cards.firstOrNull { it.id == plan.recommendedTargetId }

        return OptimizationResult(
            plan = plan,
            minOnlyPlan = minOnly,
            interestSaved = maxOf(0.0, interestSaved),
            monthsSaved = maxOf(0, monthsSaved),
            recommendedTargetId = plan.recommendedTargetId,
            recommendedTargetName = targetCard?.name ?: ""
        )
    }

    /**
     * Calculate a payoff plan for given cards, extra monthly payment, and strategy.
     */
    fun calculate(
        cards: List<CardInput>,
        extraMonthly: Double,
        strategy: Strategy
    ): PayoffResult {
        val working = cards.map { WorkingCard(it) }.toMutableList()
        var month = 0
        val payoffOrder = mutableMapOf<Long, Int>()
        var orderCounter = 1

        // Determine initial target (first card to receive extra payment)
        val initialTarget = getTarget(working, strategy)

        while (working.any { it.remaining > ZERO_THRESHOLD } && month < MAX_MONTHS) {
            month++

            // Freed minimums from paid-off cards stack onto extra pool
            val freedMinimums = working
                .filter { it.paidOff }
                .sumOf { it.original.minPayment }
            val extraPool = extraMonthly + freedMinimums

            val target = getTarget(working, strategy)

            for (card in working) {
                if (card.paidOff || card.remaining <= ZERO_THRESHOLD) continue

                val monthlyRate = card.original.apr / 100.0 / 12.0
                val interest = card.remaining * monthlyRate
                var payment = card.original.minPayment

                if (target != null && card.original.id == target.original.id) {
                    payment += extraPool
                }

                // Don't overpay
                payment = minOf(payment, card.remaining + interest)

                val principal = payment - interest
                card.remaining = maxOf(0.0, card.remaining - principal)
                card.totalInterest += interest

                if (card.remaining <= ZERO_THRESHOLD && !card.paidOff) {
                    card.paidOff = true
                    card.paidOffMonth = month
                    payoffOrder[card.original.id] = orderCounter++
                }
            }
        }

        val results = working.map { w ->
            CardResult(
                cardId = w.original.id,
                name = w.original.name,
                originalBalance = w.original.balance,
                apr = w.original.apr,
                paidOffMonth = w.paidOffMonth ?: month,
                totalInterestPaid = w.totalInterest,
                payoffOrder = payoffOrder[w.original.id] ?: orderCounter
            )
        }.sortedBy { it.payoffOrder }

        return PayoffResult(
            cards = results,
            totalMonths = month,
            totalInterestPaid = working.sumOf { it.totalInterest },
            recommendedTargetId = initialTarget?.original?.id ?: cards.first().id
        )
    }

    // ─── Strategy targeting ───────────────────────────────────────────────────

    private fun getTarget(cards: List<WorkingCard>, strategy: Strategy): WorkingCard? {
        val active = cards.filter { !it.paidOff && it.remaining > ZERO_THRESHOLD }
        if (active.isEmpty()) return null

        return when (strategy) {
            Strategy.AVALANCHE -> active.maxByOrNull { it.original.apr }

            Strategy.SNOWBALL -> active.minByOrNull { it.remaining }

            Strategy.HYBRID -> active.maxByOrNull { card ->
                // Score = APR / ln(1 + balance)
                // High APR cards score well, but smaller balances boost the score
                card.original.apr / ln1p(card.remaining)
            }
        }
    }

    // ─── Internal working state ───────────────────────────────────────────────

    private class WorkingCard(val original: CardInput) {
        var remaining: Double = original.balance
        var totalInterest: Double = 0.0
        var paidOff: Boolean = false
        var paidOffMonth: Int? = null
    }

    // ─── Utility ──────────────────────────────────────────────────────────────

    /**
     * Returns monthly interest charge for a given balance and APR.
     * Useful for UI previews without running a full simulation.
     */
    fun monthlyInterestCharge(balance: Double, apr: Double): Double {
        return balance * (apr / 100.0 / 12.0)
    }

    /**
     * Estimate minimum payment for a new card if user doesn't know it.
     * Common issuer formula: max($25, 1% of balance + monthly interest)
     */
    fun estimateMinPayment(balance: Double, apr: Double): Double {
        val monthlyInterest = monthlyInterestCharge(balance, apr)
        return maxOf(25.0, balance * 0.01 + monthlyInterest)
    }
}
