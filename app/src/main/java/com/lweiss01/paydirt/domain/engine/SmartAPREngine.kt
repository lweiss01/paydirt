package com.lweiss01.paydirt.domain.engine

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * SmartAPREngine — Infers effective APR from real transaction history.
 *
 * When a user doesn't know (or hasn't entered) their APR, this engine
 * analyzes their credit card transaction history to back-calculate an
 * effective interest rate from actual interest charges posted to the account.
 *
 * Confidence levels:
 *  - HIGH:   3+ months of clean interest charge data → reliable estimate
 *  - MEDIUM: 1-2 months of data, or some noise in the signal
 *  - LOW:    Single data point, or high variance between months
 *  - INSUFFICIENT: Not enough data to make any estimate
 *
 * The engine also handles edge cases:
 *  - Promotional 0% APR periods (detected and flagged)
 *  - Fee contamination (late fees, annual fees filtered out)
 *  - Grace period months (full payment, no interest — excluded)
 *  - Balance transfer rates (detected as anomalies)
 */
object SmartAPREngine {

    // ─── Public API ──────────────────────────────────────────────────────────

    enum class Confidence {
        HIGH,           // ±2% accuracy, 3+ clean data points
        MEDIUM,         // ±5% accuracy, 1-2 data points or some noise
        LOW,            // ±10% accuracy, single point or high variance
        INSUFFICIENT    // Cannot estimate — no interest charges found
    }

    data class APREstimate(
        val estimatedAPR: Double,           // e.g. 22.47
        val confidence: Confidence,
        val dataPointsUsed: Int,            // months of data analyzed
        val monthlyEstimates: List<MonthlyEstimate>,
        val notes: List<String>,            // human-readable explanation
        val isPromoPeriodSuspected: Boolean,
        val displayLabel: String            // "~22.5% (estimated)" or "24.99% (confirmed)"
    )

    data class MonthlyEstimate(
        val monthLabel: String,             // "Jan 2025"
        val openingBalance: Double,
        val interestCharged: Double,
        val effectiveMonthlyRate: Double,
        val impliedAPR: Double,
        val isOutlier: Boolean
    )

    data class TransactionRecord(
        val amountCents: Long,              // positive = charge, negative = payment/credit
        val description: String,
        val postedDate: Long,               // epoch millis
        val category: String?               // from MX/Plaid categorization
    )

    data class MonthlySnapshot(
        val monthEpoch: Long,               // first day of month, epoch millis
        val openingBalance: Double,
        val closingBalance: Double,
        val transactions: List<TransactionRecord>
    )

    /**
     * Main entry point. Given monthly balance snapshots and their transactions,
     * infer the effective APR.
     */
    fun estimate(snapshots: List<MonthlySnapshot>): APREstimate {
        if (snapshots.isEmpty()) {
            return insufficientData("No transaction history available.")
        }

        val monthly = snapshots
            .sortedBy { it.monthEpoch }
            .mapNotNull { analyzeMonth(it) }

        if (monthly.isEmpty()) {
            return insufficientData("No interest charges detected in transaction history. " +
                "Card may be in a 0% promo period, or always paid in full.")
        }

        // Filter outliers (balance transfers, fee contamination, promo rate changes)
        val clean = removeOutliers(monthly)
        val used = clean.filter { !it.isOutlier }

        if (used.isEmpty()) {
            return APREstimate(
                estimatedAPR = monthly.map { it.impliedAPR }.average(),
                confidence = Confidence.LOW,
                dataPointsUsed = monthly.size,
                monthlyEstimates = monthly,
                notes = listOf(
                    "High variance detected between months.",
                    "This card may have multiple rates or a recent rate change.",
                    "Consider entering your APR manually from your statement."
                ),
                isPromoPeriodSuspected = false,
                displayLabel = "~${monthly.map { it.impliedAPR }.average().roundToOneDecimal()}% (low confidence)"
            )
        }

        val avgAPR = used.map { it.impliedAPR }.average()
        val variance = used.map { it.impliedAPR }.standardDeviation()
        val isPromoPeriod = snapshots.any { isLikelyPromoPeriod(it) }

        val confidence = when {
            used.size >= 3 && variance < 2.0  -> Confidence.HIGH
            used.size >= 2 && variance < 4.0  -> Confidence.MEDIUM
            used.size >= 1                    -> Confidence.LOW
            else                              -> Confidence.INSUFFICIENT
        }

        val notes = buildNotes(used.size, variance, isPromoPeriod, monthly.size - used.size)

        return APREstimate(
            estimatedAPR = avgAPR.roundToOneDecimal(),
            confidence = confidence,
            dataPointsUsed = used.size,
            monthlyEstimates = clean,
            notes = notes,
            isPromoPeriodSuspected = isPromoPeriod,
            displayLabel = buildDisplayLabel(avgAPR, confidence)
        )
    }

    /**
     * Lightweight estimate from a single month snapshot.
     * Used when full history isn't available yet.
     */
    fun estimateSingleMonth(
        openingBalance: Double,
        interestCharged: Double
    ): APREstimate? {
        if (openingBalance <= 0 || interestCharged <= 0) return null

        val monthlyRate = interestCharged / openingBalance
        val impliedAPR = monthlyRate * 12 * 100

        // Sanity check — reject obviously wrong numbers
        if (impliedAPR < 0.5 || impliedAPR > 60.0) return null

        return APREstimate(
            estimatedAPR = impliedAPR.roundToOneDecimal(),
            confidence = Confidence.LOW,
            dataPointsUsed = 1,
            monthlyEstimates = listOf(
                MonthlyEstimate(
                    monthLabel = "Latest month",
                    openingBalance = openingBalance,
                    interestCharged = interestCharged,
                    effectiveMonthlyRate = monthlyRate,
                    impliedAPR = impliedAPR,
                    isOutlier = false
                )
            ),
            notes = listOf(
                "Based on a single month of data.",
                "Accuracy improves with more history.",
                "Enter your actual APR from your statement when possible."
            ),
            isPromoPeriodSuspected = false,
            displayLabel = "~${impliedAPR.roundToOneDecimal()}% (1 month)"
        )
    }

    /**
     * Given a confirmed APR, return a "confirmed" APREstimate for UI consistency.
     */
    fun confirmed(apr: Double): APREstimate = APREstimate(
        estimatedAPR = apr,
        confidence = Confidence.HIGH,
        dataPointsUsed = -1,   // sentinel: user-entered
        monthlyEstimates = emptyList(),
        notes = listOf("APR confirmed by user."),
        isPromoPeriodSuspected = false,
        displayLabel = "${apr}% (confirmed)"
    )

    // ─── Analysis ─────────────────────────────────────────────────────────────

    private fun analyzeMonth(snapshot: MonthlySnapshot): MonthlyEstimate? {
        val interestCharges = snapshot.transactions
            .filter { isInterestCharge(it) }
            .sumOf { it.amountCents / 100.0 }

        // No interest = either promo, grace period, or no balance — skip
        if (interestCharges <= 0.01) return null

        // Opening balance must be positive to calculate rate
        if (snapshot.openingBalance <= 0) return null

        val monthlyRate = interestCharges / snapshot.openingBalance
        val impliedAPR = monthlyRate * 12 * 100

        // Reject physically impossible APRs
        if (impliedAPR < 0.5 || impliedAPR > 79.99) return null

        return MonthlyEstimate(
            monthLabel = formatMonthLabel(snapshot.monthEpoch),
            openingBalance = snapshot.openingBalance,
            interestCharged = interestCharges,
            effectiveMonthlyRate = monthlyRate,
            impliedAPR = impliedAPR,
            isOutlier = false
        )
    }

    private fun isInterestCharge(tx: TransactionRecord): Boolean {
        val desc = tx.description.lowercase()
        val cat = tx.category?.lowercase() ?: ""

        // Must be a positive amount (charge to account)
        if (tx.amountCents <= 0) return false

        // Category-based detection (MX/Plaid categorize these)
        if (cat.contains("interest") || cat.contains("finance charge")) return true

        // Description-based detection
        val interestKeywords = listOf(
            "interest charge", "finance charge", "periodic rate",
            "interest fee", "purchase interest", "cash advance interest",
            "interest charged", "int charge", "fin chg", "interest apr"
        )
        return interestKeywords.any { desc.contains(it) }
    }

    private fun isLikelyPromoPeriod(snapshot: MonthlySnapshot): Boolean {
        val hasBalance = snapshot.openingBalance > 100
        val hasNoInterest = snapshot.transactions.none { isInterestCharge(it) }
        return hasBalance && hasNoInterest
    }

    private fun removeOutliers(estimates: List<MonthlyEstimate>): List<MonthlyEstimate> {
        if (estimates.size < 3) return estimates

        val avg = estimates.map { it.impliedAPR }.average()
        val std = estimates.map { it.impliedAPR }.standardDeviation()

        // Mark as outlier if more than 2 standard deviations from mean
        // This catches balance transfer rate periods, fee contamination, etc.
        return estimates.map { estimate ->
            val isOutlier = abs(estimate.impliedAPR - avg) > (2 * std) && std > 2.0
            estimate.copy(isOutlier = isOutlier)
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun buildNotes(
        cleanPoints: Int,
        variance: Double,
        isPromoPeriod: Boolean,
        outliersRemoved: Int
    ): List<String> {
        val notes = mutableListOf<String>()

        when (cleanPoints) {
            1    -> notes.add("Based on 1 month of interest charge data.")
            2    -> notes.add("Based on 2 months of interest charge data.")
            else -> notes.add("Based on $cleanPoints months of interest charge data.")
        }

        if (variance < 1.0) {
            notes.add("Very consistent rate — high confidence estimate.")
        } else if (variance < 3.0) {
            notes.add("Slight variance between months — likely minor balance fluctuations.")
        } else {
            notes.add("Notable variance detected — rate may have changed recently.")
        }

        if (isPromoPeriod) {
            notes.add("⚠️ Some months show no interest despite a balance. " +
                "You may have had a 0% promo period. Estimate reflects non-promo months only.")
        }

        if (outliersRemoved > 0) {
            notes.add("$outliersRemoved month(s) excluded as likely outliers " +
                "(balance transfer, fee, or rate change period).")
        }

        notes.add("For maximum accuracy, confirm your APR from your card statement or issuer app.")

        return notes
    }

    private fun buildDisplayLabel(apr: Double, confidence: Confidence): String {
        val rounded = apr.roundToOneDecimal()
        return when (confidence) {
            Confidence.HIGH         -> "~$rounded% (estimated ✓)"
            Confidence.MEDIUM       -> "~$rounded% (estimated)"
            Confidence.LOW          -> "~$rounded% (low confidence)"
            Confidence.INSUFFICIENT -> "APR unknown"
        }
    }

    private fun insufficientData(reason: String) = APREstimate(
        estimatedAPR = 0.0,
        confidence = Confidence.INSUFFICIENT,
        dataPointsUsed = 0,
        monthlyEstimates = emptyList(),
        notes = listOf(reason, "Link your card account to enable APR estimation."),
        isPromoPeriodSuspected = false,
        displayLabel = "APR unknown"
    )

    private fun formatMonthLabel(epochMillis: Long): String {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = epochMillis }
        val months = listOf("Jan","Feb","Mar","Apr","May","Jun",
                            "Jul","Aug","Sep","Oct","Nov","Dec")
        return "${months[cal.get(java.util.Calendar.MONTH)]} ${cal.get(java.util.Calendar.YEAR)}"
    }

    private fun Double.roundToOneDecimal(): Double =
        (this * 10).roundToInt() / 10.0

    private fun List<Double>.standardDeviation(): Double {
        if (size <= 1) return 0.0
        val avg = average()
        return kotlin.math.sqrt(sumOf { (it - avg) * (it - avg) } / size)
    }
}
