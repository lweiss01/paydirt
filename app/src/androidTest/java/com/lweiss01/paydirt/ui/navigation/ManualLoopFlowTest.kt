package com.lweiss01.paydirt.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lweiss01.paydirt.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ManualLoopFlowTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun assembledManualLoopKeepsRecommendationTrustRewardAndReturnFlowCoherent() {
        composeRule.onNodeWithText("Start with one card. Manual is fine.").assertIsDisplayed()
        composeRule.onNodeWithText("Add your first card").performClick()

        composeRule.onNodeWithTag("add_card_name_input").performTextInput("Starter Visa")
        composeRule.onNodeWithTag("add_card_balance_input").performTextInput("1200")
        composeRule.onNodeWithTag("add_card_save_button").assertIsEnabled().performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("home_tab_row", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("home_tab_row").assertIsDisplayed()
        composeRule.onNodeWithText("Overview").assertIsDisplayed()
        composeRule.onNodeWithText("Cards").assertIsDisplayed()
        composeRule.onNodeWithTag("home_next_move_hero").assertIsDisplayed()
        composeRule.onNodeWithText("Put extra on Starter Visa").assertIsDisplayed()
        composeRule.onNodeWithText("APR not confirmed").assertIsDisplayed()
        composeRule.onNodeWithText(
            "This recommendation may sharpen once you add the exact APR from a statement."
        ).assertIsDisplayed()
        composeRule.onNodeWithText("Current goal: \$50 extra this month.").assertIsDisplayed()

        composeRule.onNodeWithText("Starter Visa").performClick()

        composeRule.onNodeWithText("APR CONFIDENCE").assertIsDisplayed()
        composeRule.onNodeWithText("APR not confirmed").assertIsDisplayed()
        composeRule.onNodeWithText(
            "This recommendation may sharpen once you add the exact APR from a statement."
        ).assertIsDisplayed()
        composeRule.onNodeWithTag("card_detail_log_payment_fab").performClick()
        composeRule.onNodeWithTag("card_detail_payment_amount_input").performTextClearance()
        composeRule.onNodeWithTag("card_detail_payment_amount_input").performTextInput("20")
        composeRule.onNodeWithTag("card_detail_payment_extra_switch").performClick()
        composeRule.onNodeWithTag("card_detail_payment_submit").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("reward_screen", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("reward_goal_card").assertIsDisplayed()
        composeRule.onNodeWithTag("reward_apr_trust_card").assertIsDisplayed()
        composeRule.onNodeWithText("INTEREST CUT").assertIsDisplayed()
        composeRule.onNodeWithText("MONTHLY GOAL").assertIsDisplayed()
        composeRule.onNodeWithText("BEST NEXT MOVE").assertIsDisplayed()
        composeRule.onNodeWithText("APR CONFIDENCE").assertIsDisplayed()
        composeRule.onNodeWithText("APR not confirmed").assertIsDisplayed()
        composeRule.onNodeWithText("\$20 of \$50").assertIsDisplayed()
        composeRule.onNodeWithText(
            "This recommendation may sharpen once you add the exact APR from a statement."
        ).assertIsDisplayed()
        composeRule.onNodeWithTag("reward_done_button").assertIsDisplayed().performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("card_detail_log_payment_fab", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("APR CONFIDENCE").assertIsDisplayed()
        composeRule.onNodeWithTag("card_detail_log_payment_fab").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Back").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("home_next_move_hero", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("home_tab_row").assertIsDisplayed()
        composeRule.onNodeWithTag("home_next_move_hero").assertIsDisplayed()
        composeRule.onNodeWithText("APR not confirmed").assertIsDisplayed()
        composeRule.onNodeWithText("Current goal: \$50 extra this month.").assertIsDisplayed()
    }
}
