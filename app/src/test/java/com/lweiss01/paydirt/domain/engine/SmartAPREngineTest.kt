package com.lweiss01.paydirt.domain.engine

import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class SmartAPREngineTest {

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun epochForMonth(year: Int, month: Int): Long {
        return Calendar.getInstance().apply {
            set(year, month - 1, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun interestTx(amountCents: Long, desc: String = "Purchase Interest Charge") =
        SmartAPREngine.TransactionRecord(
            amountCents = amountCents,
            description = desc,
            postedDate = System.currentTimeMillis(),
            category = "Interest Charge"
        )

    private fun purchaseTx(amountCents: Long) =
        SmartAPREngine.TransactionRecord(
            amountCents = amountCents,
            description = "Amazon.com",
            postedDate = System.currentTimeMillis(),
            category = "Shopping"
        )

    private fun paymentTx(amountCents: Long) =
        SmartAPREngine.TransactionRecord(
            amountCents = -amountCents,
            description = "Payment Thank You",
            postedDate = System.currentTimeMillis(),
            category = "Payment"
        )

    private fun snapshot(
        year: Int, month: Int,
        openingBalance: Double, closingBalance: Double,
        transactions: List<SmartAPREngine.TransactionRecord>
    ) = SmartAPREngine.MonthlySnapshot(
        monthEpoch = epochForMonth(year, month),
        openingBalance = openingBalance,
        closingBalance = closingBalance,
        transactions = transactions
    )

    // ─── Core APR inference ───────────────────────────────────────────────────

    @Test
    fun `infers ~24pct APR from clean 3-month data`() {
        // 24% APR = 2% monthly rate
        // $3000 balance → ~$60 interest/month
        val snapshots = listOf(
            snapshot(2025, 1, 3000.0, 3010.0, listOf(interestTx(6000), purchaseTx(7000))),
            snapshot(2025, 2, 3000.0, 3010.0, listOf(interestTx(6000), purchaseTx(6000))),
            snapshot(2025, 3, 3000.0, 3008.0, listOf(interestTx(5900), purchaseTx(5500)))
        )

        val result = SmartAPREngine.estimate(snapshots)

        assertNotEquals(SmartAPREngine.Confidence.INSUFFICIENT, result.confidence)
        assertTrue("APR should be near 24%", result.estimatedAPR in 22.0..26.0)
        assertEquals(3, result.dataPointsUsed)
    }

    @Test
    fun `infers ~22pct APR from single month`() {
        // 22% APR ≈ 1.833% monthly
        // $4200 balance → ~$77 interest
        val result = SmartAPREngine.estimateSingleMonth(
            openingBalance = 4200.0,
            interestCharged = 77.0
        )

        assertNotNull(result)
        assertTrue("APR should be near 22%", result!!.estimatedAPR in 19.0..25.0)
        assertEquals(SmartAPREngine.Confidence.LOW, result.confidence)
    }

    @Test
    fun `returns HIGH confidence for 3+ consistent months`() {
        val snapshots = listOf(
            snapshot(2025, 1, 5000.0, 5050.0, listOf(interestTx(9500))),
            snapshot(2025, 2, 5000.0, 5050.0, listOf(interestTx(9500))),
            snapshot(2025, 3, 5000.0, 5050.0, listOf(interestTx(9600)))
        )

        val result = SmartAPREngine.estimate(snapshots)
        assertEquals(SmartAPREngine.Confidence.HIGH, result.confidence)
    }

    @Test
    fun `returns MEDIUM confidence for 2 months`() {
        val snapshots = listOf(
            snapshot(2025, 1, 3000.0, 3060.0, listOf(interestTx(6000))),
            snapshot(2025, 2, 3000.0, 3060.0, listOf(interestTx(5800)))
        )

        val result = SmartAPREngine.estimate(snapshots)
        assertTrue(result.confidence in listOf(
            SmartAPREngine.Confidence.MEDIUM, SmartAPREngine.Confidence.HIGH
        ))
    }

    // ─── Edge cases ───────────────────────────────────────────────────────────

    @Test
    fun `returns INSUFFICIENT when no interest charges found`() {
        // Card always paid in full — no interest charges
        val snapshots = listOf(
            snapshot(2025, 1, 1000.0, 0.0, listOf(
                purchaseTx(50000), paymentTx(100000)
            )),
            snapshot(2025, 2, 500.0, 0.0, listOf(
                purchaseTx(30000), paymentTx(80000)
            ))
        )

        val result = SmartAPREngine.estimate(snapshots)
        assertEquals(SmartAPREngine.Confidence.INSUFFICIENT, result.confidence)
    }

    @Test
    fun `returns INSUFFICIENT for empty snapshots`() {
        val result = SmartAPREngine.estimate(emptyList())
        assertEquals(SmartAPREngine.Confidence.INSUFFICIENT, result.confidence)
        assertEquals(0.0, result.estimatedAPR, 0.001)
    }

    @Test
    fun `detects promo period when balance exists but no interest`() {
        val snapshots = listOf(
            snapshot(2025, 1, 5000.0, 5000.0, listOf(purchaseTx(10000))), // promo — no interest
            snapshot(2025, 2, 5000.0, 5000.0, listOf(purchaseTx(10000))), // promo — no interest
            snapshot(2025, 3, 5000.0, 5090.0, listOf(interestTx(9500)))   // promo ended
        )

        val result = SmartAPREngine.estimate(snapshots)
        assertTrue("Should suspect promo period", result.isPromoPeriodSuspected)
        assertNotEquals(SmartAPREngine.Confidence.INSUFFICIENT, result.confidence)
    }

    @Test
    fun `rejects physically impossible APR values`() {
        // Tiny interest on huge balance = near-zero APR (reject)
        val resultLow = SmartAPREngine.estimateSingleMonth(
            openingBalance = 10000.0,
            interestCharged = 0.10   // 0.012% monthly = 0.14% APR — impossible
        )
        assertNull("Should reject impossibly low APR", resultLow)

        // Enormous interest on small balance = 200%+ APR (reject)
        val resultHigh = SmartAPREngine.estimateSingleMonth(
            openingBalance = 100.0,
            interestCharged = 50.0   // 50% monthly = 600% APR — reject
        )
        assertNull("Should reject impossibly high APR", resultHigh)
    }

    @Test
    fun `confirmed APR always returns HIGH confidence`() {
        val result = SmartAPREngine.confirmed(22.99)
        assertEquals(22.99, result.estimatedAPR, 0.001)
        assertEquals(SmartAPREngine.Confidence.HIGH, result.confidence)
        assertTrue(result.displayLabel.contains("confirmed"))
    }

    @Test
    fun `display label reflects confidence level`() {
        val snapshots3months = listOf(
            snapshot(2025, 1, 3000.0, 3060.0, listOf(interestTx(6000))),
            snapshot(2025, 2, 3000.0, 3060.0, listOf(interestTx(6000))),
            snapshot(2025, 3, 3000.0, 3060.0, listOf(interestTx(6000)))
        )
        val result = SmartAPREngine.estimate(snapshots3months)
        assertTrue("Label should say estimated", result.displayLabel.contains("estimated"))
    }

    @Test
    fun `interest keyword detection works across common issuer formats`() {
        val keywords = listOf(
            "Purchase Interest Charge",
            "INTEREST CHARGE",
            "Finance Charge",
            "Periodic Rate Interest",
            "INT CHARGE APR",
            "Cash Advance Interest"
        )

        // Each keyword should be detected as an interest charge
        // We test this indirectly through the estimateSingleMonth function
        // by building a snapshot with each transaction description
        keywords.forEach { keyword ->
            val tx = SmartAPREngine.TransactionRecord(
                amountCents = 5000,
                description = keyword,
                postedDate = System.currentTimeMillis(),
                category = null  // no category — must detect from description
            )
            val snap = snapshot(2025, 1, 3000.0, 3050.0, listOf(tx))
            val result = SmartAPREngine.estimate(listOf(snap))
            assertNotEquals(
                "Should detect interest from '$keyword'",
                SmartAPREngine.Confidence.INSUFFICIENT, result.confidence
            )
        }
    }

    // ─── PayDirt integration scenario ────────────────────────────────────────

    @Test
    fun `realistic Chase Sapphire scenario returns usable estimate`() {
        // Real-world: $4,200 balance, ~22.99% APR
        // Monthly interest ≈ $80.46
        val snapshots = listOf(
            snapshot(2025, 1, 4200.0, 4230.0, listOf(
                interestTx(8046, "PURCHASE INTEREST CHARGE"),
                purchaseTx(15000),
                paymentTx(12000)
            )),
            snapshot(2025, 2, 4250.0, 4280.0, listOf(
                interestTx(8140, "PURCHASE INTEREST CHARGE"),
                purchaseTx(12000),
                paymentTx(10000)
            )),
            snapshot(2025, 3, 4300.0, 4310.0, listOf(
                interestTx(8230, "PURCHASE INTEREST CHARGE"),
                purchaseTx(8000),
                paymentTx(8000)
            ))
        )

        val result = SmartAPREngine.estimate(snapshots)

        assertTrue("APR should be near 22.99%", result.estimatedAPR in 20.0..26.0)
        assertTrue("Confidence should be MEDIUM or HIGH",
            result.confidence in listOf(SmartAPREngine.Confidence.HIGH, SmartAPREngine.Confidence.MEDIUM))
        assertTrue("Should have notes", result.notes.isNotEmpty())
        assertFalse("Should not suspect promo period", result.isPromoPeriodSuspected)

        println("Chase estimate: ${result.displayLabel}")
        println("Notes: ${result.notes.joinToString("\n  - ", prefix = "\n  - ")}")
    }
}
