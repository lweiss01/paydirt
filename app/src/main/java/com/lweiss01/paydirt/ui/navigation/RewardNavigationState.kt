package com.lweiss01.paydirt.ui.navigation

import com.lweiss01.paydirt.domain.engine.BehaviorEngine
import com.lweiss01.paydirt.domain.model.AprSource
import java.io.Serializable

const val REWARD_NAVIGATION_PAYLOAD_KEY = "reward_navigation_payload"
const val REWARD_PAYMENT_SUGGESTION_KEY = "reward_payment_suggestion"
const val REWARD_RETURNED_TO_CARD_KEY = "reward_returned_to_card"
const val REWARD_DONE_RETURNED_TO_CARD_KEY = "reward_done_returned_to_card"
const val HOME_FIRST_RECOMMENDATION_REVEAL_KEY = "home_first_recommendation_reveal"

data class RewardNavigationPayload(
    val cardId: Long,
    val impact: BehaviorEngine.PaymentImpact,
    val aprSource: AprSource,
) : Serializable
