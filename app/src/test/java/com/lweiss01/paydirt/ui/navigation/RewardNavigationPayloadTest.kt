package com.lweiss01.paydirt.ui.navigation

import com.lweiss01.paydirt.domain.engine.BehaviorEngine
import com.lweiss01.paydirt.domain.model.AprSource
import com.lweiss01.paydirt.domain.model.MomentumScore
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RewardNavigationPayloadTest {

    @Test
    fun rewardNavigationPayload_serializesPaymentImpactForSavedStateRecovery() {
        val payload = RewardNavigationPayload(
            cardId = 42L,
            impact = BehaviorEngine.PaymentImpact(
                paymentAmount = 75.0,
                interestSaved = 3.42,
                scaledProjection = 27.36,
                scaledProjectionReps = 8,
                cumulativeSaved = 14.18,
                projectedTotalSavings = 218.0,
                nextOpportunityAmount = 5.0,
                nextOpportunityInterestSaved = 0.21,
                goalProgressAfter = 0.64f,
                monthlyGoal = 125.0,
                extraThisMonth = 32.0,
                isAheadOfGoal = false,
                momentumScore = MomentumScore.BUILDING,
                displayImpact = "+$3.42 saved",
                displayCumulative = "$14.18 total saved",
                displayProjection = "On track to save ~$218",
                displayMomentum = "Momentum: Building ↑",
                displayNextOpp = "Another $5 right now saves $0.21",
                displayGoalStatus = "$32 / $125 this month",
                headlineText = "Nice hit.",
                rewardBodyText = "That move saved $3.42 in interest.",
            ),
            aprSource = AprSource.UNKNOWN,
        )

        val restored = payload.roundTrip()

        assertEquals(42L, restored.cardId)
        assertEquals(payload.impact.displayImpact, restored.impact.displayImpact)
        assertEquals(125.0, restored.impact.monthlyGoal, 0.0)
        assertEquals(payload.impact.nextOpportunityAmount, restored.impact.nextOpportunityAmount, 0.0)
        assertEquals(payload.impact.momentumScore, restored.impact.momentumScore)
        assertEquals(AprSource.UNKNOWN, restored.aprSource)
        assertTrue(restored.impact.projectedTotalSavings > restored.impact.interestSaved)
    }

    private fun RewardNavigationPayload.roundTrip(): RewardNavigationPayload {
        val output = ByteArrayOutputStream()
        ObjectOutputStream(output).use { it.writeObject(this) }
        return ObjectInputStream(ByteArrayInputStream(output.toByteArray())).use {
            it.readObject() as RewardNavigationPayload
        }
    }
}
