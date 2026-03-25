package com.lweiss01.paydirt.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PayoffEngineTest {

    private val singleCard = listOf(
        PayoffEngine.CardInput(id = 1, name = "Chase", balance = 1200.0, apr = 22.99, minPayment = 30.0)
    )

    private val multiCards = listOf(
        PayoffEngine.CardInput(id = 1, name = "Chase", balance = 4200.0, apr = 22.99, minPayment = 84.0),
        PayoffEngine.CardInput(id = 2, name = "Capital", balance = 1850.0, apr = 28.49, minPayment = 37.0),
        PayoffEngine.CardInput(id = 3, name = "Citi", balance = 6100.0, apr = 19.74, minPayment = 122.0),
    )

    @Test
    fun `single card pays off with minimum payments`() {
        val result = PayoffEngine.calculate(singleCard, extraMonthly = 0.0, strategy = PayoffEngine.Strategy.AVALANCHE)
        assertTrue("Should pay off", result.totalMonths in 1..600)
        assertTrue("Should have positive interest", result.totalInterestPaid > 0)
        assertEquals(1, result.cards.size)
    }

    @Test
    fun `extra payment reduces months and interest`() {
        val withExtra = PayoffEngine.calculate(multiCards, extraMonthly = 100.0, strategy = PayoffEngine.Strategy.AVALANCHE)
        val minOnly = PayoffEngine.calculate(multiCards, extraMonthly = 0.0, strategy = PayoffEngine.Strategy.AVALANCHE)

        assertTrue("Extra payment should reduce total months", withExtra.totalMonths < minOnly.totalMonths)
        assertTrue("Extra payment should reduce total interest", withExtra.totalInterestPaid < minOnly.totalInterestPaid)
    }

    @Test
    fun `avalanche targets highest APR card`() {
        val result = PayoffEngine.calculate(multiCards, extraMonthly = 50.0, strategy = PayoffEngine.Strategy.AVALANCHE)
        assertEquals(2L, result.recommendedTargetId)
    }

    @Test
    fun `snowball targets lowest balance card`() {
        val result = PayoffEngine.calculate(multiCards, extraMonthly = 50.0, strategy = PayoffEngine.Strategy.SNOWBALL)
        assertEquals(2L, result.recommendedTargetId)
    }

    @Test
    fun `avalanche saves more interest than snowball`() {
        val avalanche = PayoffEngine.calculate(multiCards, extraMonthly = 50.0, strategy = PayoffEngine.Strategy.AVALANCHE)
        val snowball = PayoffEngine.calculate(multiCards, extraMonthly = 50.0, strategy = PayoffEngine.Strategy.SNOWBALL)

        assertTrue("Avalanche should pay less total interest than snowball", avalanche.totalInterestPaid <= snowball.totalInterestPaid)
    }

    @Test
    fun `freed minimums accelerate payoff`() {
        val result = PayoffEngine.calculate(multiCards, extraMonthly = 0.0, strategy = PayoffEngine.Strategy.AVALANCHE)
        val resultWithFreeing = PayoffEngine.calculate(multiCards, extraMonthly = 50.0, strategy = PayoffEngine.Strategy.AVALANCHE)
        assertTrue(resultWithFreeing.totalMonths < result.totalMonths)
    }

    @Test
    fun `optimize returns positive interest saved`() {
        val result = PayoffEngine.optimize(multiCards, extraMonthly = 50.0, strategy = PayoffEngine.Strategy.AVALANCHE)
        assertTrue("Interest saved should be positive", result.interestSaved > 0)
        assertTrue("Months saved should be non-negative", result.monthsSaved >= 0)
    }

    @Test
    fun `optimize with zero extra has zero savings`() {
        val result = PayoffEngine.optimize(multiCards, extraMonthly = 0.0, strategy = PayoffEngine.Strategy.AVALANCHE)
        assertEquals(0.0, result.interestSaved, 0.01)
        assertEquals(0, result.monthsSaved)
    }

    @Test
    fun `monthly interest charge is correct`() {
        val interest = PayoffEngine.monthlyInterestCharge(balance = 1000.0, apr = 12.0)
        assertEquals(10.0, interest, 0.001)
    }

    @Test
    fun `estimate min payment is at least 25`() {
        val min = PayoffEngine.estimateMinPayment(balance = 100.0, apr = 5.0)
        assertTrue("Min payment should be at least $25", min >= 25.0)
    }

    @Test
    fun `all cards have paid off months`() {
        val result = PayoffEngine.calculate(multiCards, extraMonthly = 50.0, strategy = PayoffEngine.Strategy.AVALANCHE)
        result.cards.forEach { card ->
            assertTrue("${card.name} should have a payoff month > 0", card.paidOffMonth > 0)
        }
    }

    @Test
    fun `single card optimize works correctly`() {
        val result = PayoffEngine.optimize(singleCard, extraMonthly = 25.0, strategy = PayoffEngine.Strategy.AVALANCHE)
        assertEquals(1L, result.recommendedTargetId)
        assertTrue(result.interestSaved > 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty cards list throws`() {
        PayoffEngine.optimize(emptyList(), extraMonthly = 50.0, strategy = PayoffEngine.Strategy.AVALANCHE)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative extra throws`() {
        PayoffEngine.optimize(singleCard, extraMonthly = -10.0, strategy = PayoffEngine.Strategy.AVALANCHE)
    }
}
