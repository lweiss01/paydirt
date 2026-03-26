package com.lweiss01.paydirt.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lweiss01.paydirt.ui.screens.cards.AddEditCardScreen
import com.lweiss01.paydirt.ui.screens.cards.CardDetailScreen
import com.lweiss01.paydirt.ui.screens.home.HomeScreen
import com.lweiss01.paydirt.ui.screens.optimizer.OptimizerScreen
import com.lweiss01.paydirt.ui.screens.reward.RewardScreen

sealed class Screen(val route: String) {
    object Home       : Screen("home")
    object Optimizer  : Screen("optimizer")
    object AddCard    : Screen("add_card")
    object EditCard   : Screen("edit_card/{cardId}") {
        fun createRoute(cardId: Long) = "edit_card/$cardId"
    }
    object CardDetail : Screen("card_detail/{cardId}") {
        fun createRoute(cardId: Long) = "card_detail/$cardId"
    }
    object Reward : Screen("reward/{cardId}") {
        fun createRoute(cardId: Long) = "reward/$cardId"
    }
}

@Composable
fun PayDirtNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) { backStack ->
            val firstRecommendationReveal by backStack.savedStateHandle
                .getStateFlow(HOME_FIRST_RECOMMENDATION_REVEAL_KEY, false)
                .collectAsState()

            HomeScreen(
                onAddCard        = { navController.navigate(Screen.AddCard.route) },
                onCardClick      = { cardId -> navController.navigate(Screen.CardDetail.createRoute(cardId)) },
                onOpenOptimizer  = { navController.navigate(Screen.Optimizer.route) },
                onFirstRecommendationRevealConsumed = {
                    backStack.savedStateHandle[HOME_FIRST_RECOMMENDATION_REVEAL_KEY] = false
                },
                firstRecommendationReveal = firstRecommendationReveal,
            )
        }

        composable(Screen.Optimizer.route) {
            OptimizerScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AddCard.route) {
            AddEditCardScreen(
                cardId = null,
                onBack = { navController.popBackStack() },
                onSaved = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(HOME_FIRST_RECOMMENDATION_REVEAL_KEY, true)
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.EditCard.route,
            arguments = listOf(navArgument("cardId") { type = NavType.LongType })
        ) { backStack ->
            val cardId = backStack.arguments?.getLong("cardId")
            AddEditCardScreen(
                cardId = cardId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CardDetail.route,
            arguments = listOf(navArgument("cardId") { type = NavType.LongType })
        ) { backStack ->
            val cardId = backStack.arguments?.getLong("cardId") ?: return@composable
            val suggestedPaymentAmount by backStack.savedStateHandle
                .getStateFlow<Double?>(REWARD_PAYMENT_SUGGESTION_KEY, null)
                .collectAsState()
            val rewardReturnedToCard by backStack.savedStateHandle
                .getStateFlow(REWARD_RETURNED_TO_CARD_KEY, false)
                .collectAsState()
            val rewardDoneReturnedToCard by backStack.savedStateHandle
                .getStateFlow(REWARD_DONE_RETURNED_TO_CARD_KEY, false)
                .collectAsState()

            LaunchedEffect(rewardReturnedToCard) {
                if (rewardReturnedToCard) {
                    backStack.savedStateHandle.remove<RewardNavigationPayload>(REWARD_NAVIGATION_PAYLOAD_KEY)
                    backStack.savedStateHandle[REWARD_RETURNED_TO_CARD_KEY] = false
                }
            }

            CardDetailScreen(
                cardId = cardId,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Screen.EditCard.createRoute(cardId)) },
                suggestedPaymentAmount = suggestedPaymentAmount,
                onSuggestedPaymentHandled = {
                    backStack.savedStateHandle[REWARD_PAYMENT_SUGGESTION_KEY] = null
                },
                forceClosePaymentSheet = rewardDoneReturnedToCard,
                onForceClosePaymentSheetHandled = {
                    backStack.savedStateHandle[REWARD_DONE_RETURNED_TO_CARD_KEY] = false
                },
                onRewardReady = { impact, aprSource ->
                    backStack.savedStateHandle[REWARD_NAVIGATION_PAYLOAD_KEY] = RewardNavigationPayload(
                        cardId = cardId,
                        impact = impact,
                        aprSource = aprSource,
                    )
                    navController.navigate(Screen.Reward.createRoute(cardId)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Screen.Reward.route,
            arguments = listOf(navArgument("cardId") { type = NavType.LongType })
        ) { backStack ->
            val cardId = backStack.arguments?.getLong("cardId") ?: return@composable
            val sourceEntry = runCatching {
                navController.getBackStackEntry(Screen.CardDetail.createRoute(cardId))
            }.getOrNull() ?: run {
                navController.popBackStack()
                return@composable
            }
            val rewardPayload = remember(backStack, sourceEntry) {
                sourceEntry.savedStateHandle.get<RewardNavigationPayload>(REWARD_NAVIGATION_PAYLOAD_KEY)
            }

            if (rewardPayload == null || rewardPayload.cardId != cardId) {
                navController.popBackStack()
                return@composable
            }

            fun returnToCardDetail(isDone: Boolean) {
                sourceEntry.savedStateHandle[REWARD_RETURNED_TO_CARD_KEY] = true
                if (isDone) {
                    sourceEntry.savedStateHandle[REWARD_DONE_RETURNED_TO_CARD_KEY] = true
                }
                navController.popBackStack(Screen.CardDetail.createRoute(cardId), inclusive = false)
            }

            RewardScreen(
                impact = rewardPayload.impact,
                aprSource = rewardPayload.aprSource,
                onPayMore = { amount ->
                    sourceEntry.savedStateHandle[REWARD_PAYMENT_SUGGESTION_KEY] = amount
                    returnToCardDetail(isDone = false)
                },
                onDone = {
                    returnToCardDetail(isDone = true)
                },
            )
        }
    }
}
