package com.lweiss01.paydirt.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lweiss01.paydirt.domain.model.BehaviorState
import com.lweiss01.paydirt.domain.model.Card
import com.lweiss01.paydirt.ui.components.APRTrustBadge
import com.lweiss01.paydirt.ui.components.aprTrustCopyForSource
import com.lweiss01.paydirt.ui.theme.PayDirtColors
import java.text.NumberFormat
import java.util.Locale

private enum class HomeTab(val label: String) {
    OVERVIEW("Overview"),
    CARDS("Cards")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddCard: () -> Unit,
    onCardClick: (Long) -> Unit,
    onOpenOptimizer: () -> Unit,
    firstRecommendationReveal: Boolean = false,
    onFirstRecommendationRevealConsumed: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(HomeTab.OVERVIEW) }
    val recommendationCard = state.recommendation?.let { recommendation ->
        state.cards.firstOrNull { it.id == recommendation.cardId }
    }

    LaunchedEffect(firstRecommendationReveal) {
        if (firstRecommendationReveal) {
            viewModel.showFirstRecommendationReveal()
            onFirstRecommendationRevealConsumed()
        }
    }

    LaunchedEffect(state.cards.isEmpty()) {
        if (state.cards.isEmpty() && state.isShowingFirstRecommendationReveal) {
            viewModel.dismissFirstRecommendationReveal()
        }
        if (state.cards.isEmpty()) {
            selectedTab = HomeTab.OVERVIEW
        }
    }

    Scaffold(
        containerColor = PayDirtColors.Background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCard,
                containerColor = PayDirtColors.Primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add card")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                HomeHeader(
                    totalBalance = state.totalBalance,
                    cardCount = state.cards.size
                )
            }

            if (state.cards.isEmpty()) {
                item { EmptyState(onAddCard = onAddCard) }
            } else {
                item {
                    HomeTabRow(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )
                }

                if (selectedTab == HomeTab.OVERVIEW) {
                    if (state.recommendation != null && recommendationCard != null) {
                        if (state.isShowingFirstRecommendationReveal) {
                            item {
                                FirstRecommendationRevealBanner(
                                    monthlyGoal = state.monthlyGoal,
                                    onDismiss = viewModel::dismissFirstRecommendationReveal
                                )
                            }
                        }

                        item {
                            NextMoveHero(
                                recommendation = state.recommendation!!,
                                targetCard = recommendationCard,
                                isFirstRecommendationReveal = state.isShowingFirstRecommendationReveal,
                                onOpenOptimizer = onOpenOptimizer
                            )
                        }
                    }

                    if (state.behaviorState != null) {
                        item {
                            MonthlyGoalCard(
                                monthlyGoal = state.monthlyGoal,
                                extraThisMonth = state.behaviorState!!.extraThisMonth,
                                isAheadOfGoal = state.behaviorState!!.isAheadOfGoal,
                                goalRemainingAmount = state.behaviorState!!.goalRemainingAmount,
                                isEditingGoal = state.isEditingGoal,
                                goalInput = state.goalInput,
                                isSavingGoal = state.isSavingGoal,
                                onBeginEdit = viewModel::beginGoalEdit,
                                onGoalInputChanged = viewModel::updateGoalInput,
                                onSaveGoal = viewModel::saveGoal,
                                onCancelEdit = viewModel::cancelGoalEdit,
                            )
                        }
                    }

                    if (state.progress != null && state.behaviorState != null) {
                        item {
                            ProgressSection(
                                progress = state.progress!!,
                                behaviorState = state.behaviorState!!
                            )
                        }
                    }
                } else {
                    item {
                        SectionHeading(
                            eyebrow = "CARDS",
                            title = "Your balances"
                        )
                    }

                    items(state.cards, key = { it.id }) { card ->
                        CardSummaryRow(
                            card = card,
                            isRecommended = state.recommendation?.cardId == card.id,
                            onClick = { onCardClick(card.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTabRow(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
) {
    val tabs = HomeTab.values()
    TabRow(
        selectedTabIndex = tabs.indexOf(selectedTab),
        containerColor = PayDirtColors.Surface,
        contentColor = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .testTag("home_tab_row")
    ) {
        tabs.forEach { tab ->
            Tab(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tab.label,
                        color = if (tab == selectedTab) Color.White else PayDirtColors.TextMuted,
                        fontWeight = if (tab == selectedTab) FontWeight.Bold else FontWeight.Medium
                    )
                }
            )
        }
    }
}

@Composable
private fun HomeHeader(totalBalance: Double, cardCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D1B3E), PayDirtColors.Background)
                )
            )
            .padding(horizontal = 8.dp, vertical = 24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "HOME",
                    style = MaterialTheme.typography.labelSmall,
                    color = PayDirtColors.Accent
                )
                Text(
                    text = "Put today’s extra dollars where they help most.",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryChip(label = "Total debt", value = formatCurrency(totalBalance))
                SummaryChip(label = "Open cards", value = cardCount.toString())
            }
        }
    }
}

@Composable
private fun SummaryChip(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = PayDirtColors.Surface.copy(alpha = 0.96f),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall)
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun NextMoveHero(
    recommendation: HomeRecommendationUi,
    targetCard: Card,
    isFirstRecommendationReveal: Boolean,
    onOpenOptimizer: () -> Unit,
) {
    val trustCopy = aprTrustCopyForSource(targetCard.aprSource)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_next_move_hero"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PayDirtColors.SurfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isFirstRecommendationReveal) "FIRST RECOMMENDATION" else "NEXT MOVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = PayDirtColors.Accent
                )
                Text(
                    text = recommendation.title,
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White
                )
                Text(
                    text = recommendation.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PayDirtColors.TextSecondary,
                    lineHeight = 20.sp
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                APRTrustBadge(aprSource = targetCard.aprSource)
                Text(
                    text = trustCopy.helperText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PayDirtColors.TextSecondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HeroMetric(
                    modifier = Modifier.weight(1f),
                    label = "Projected interest saved",
                    value = recommendation.interestSavedLabel,
                    accent = PayDirtColors.Win
                )
                HeroMetric(
                    modifier = Modifier.weight(1f),
                    label = "Time saved",
                    value = recommendation.monthsSavedLabel,
                    accent = PayDirtColors.Accent
                )
            }

            HeroMetric(
                label = "Debt-free target",
                value = recommendation.debtFreeDateLabel,
                accent = Color.White
            )

            Button(
                onClick = onOpenOptimizer,
                colors = ButtonDefaults.buttonColors(containerColor = PayDirtColors.Primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("See full payoff plan")
            }
        }
    }
}

@Composable
private fun FirstRecommendationRevealBanner(monthlyGoal: Double, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_first_recommendation_banner"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PayDirtColors.Surface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "FIRST LOOK",
                style = MaterialTheme.typography.labelSmall,
                color = PayDirtColors.Accent
            )
            Text(
                text = "Your first card is saved. Your monthly extra-payment goal starts at ${formatCurrency(monthlyGoal)}.",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Home will keep this loop calm and explicit: the recommendation stays grounded, and your progress below compares every extra payment to that saved goal.",
                style = MaterialTheme.typography.bodyMedium,
                color = PayDirtColors.TextSecondary,
                lineHeight = 20.sp
            )
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("home_first_recommendation_dismiss")
            ) {
                Text("Got it")
            }
        }
    }
}

@Composable
private fun MonthlyGoalCard(
    monthlyGoal: Double,
    extraThisMonth: Double,
    isAheadOfGoal: Boolean,
    goalRemainingAmount: Double,
    isEditingGoal: Boolean,
    goalInput: String,
    isSavingGoal: Boolean,
    onBeginEdit: () -> Unit,
    onGoalInputChanged: (String) -> Unit,
    onSaveGoal: () -> Unit,
    onCancelEdit: () -> Unit,
) {
    val goalInputValue = goalInput.toDoubleOrNull()
    val isGoalInputValid = goalInputValue != null && goalInputValue > 0.0
    val statusCopy = when {
        isAheadOfGoal -> "You've already logged ${formatCurrency(extraThisMonth)} this month, which puts you ahead of your saved pace."
        extraThisMonth > 0.0 -> "You've logged ${formatCurrency(extraThisMonth)} so far, so ${formatCurrency(goalRemainingAmount)} remains to reach this month's goal."
        else -> "Nothing extra is logged yet. The recommendation and progress below will frame every extra payment against this saved goal."
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_goal_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PayDirtColors.Surface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "MONTHLY GOAL",
                style = MaterialTheme.typography.labelSmall,
                color = PayDirtColors.Accent
            )

            if (isEditingGoal) {
                Text(
                    text = "Set the extra-payment pace you want this month.",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = goalInput,
                    onValueChange = onGoalInputChanged,
                    label = { Text("Monthly extra-payment goal ($)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_goal_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PayDirtColors.Primary,
                        unfocusedBorderColor = PayDirtColors.Border,
                        focusedLabelColor = PayDirtColors.Primary,
                        unfocusedLabelColor = PayDirtColors.TextMuted,
                        focusedTextColor = PayDirtColors.TextPrimary,
                        unfocusedTextColor = PayDirtColors.TextPrimary,
                        cursorColor = PayDirtColors.Primary,
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                Text(
                    text = if (isGoalInputValid) {
                        "Home and the post-payment reward card both use this same saved goal."
                    } else {
                        "Enter a goal above $0 to save it."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = PayDirtColors.TextMuted,
                    lineHeight = 18.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onSaveGoal,
                        enabled = isGoalInputValid && !isSavingGoal,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_goal_save_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PayDirtColors.Primary)
                    ) {
                        Text(if (isSavingGoal) "Saving…" else "Save goal")
                    }
                    TextButton(
                        onClick = onCancelEdit,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_goal_cancel_button")
                    ) {
                        Text("Cancel")
                    }
                }
            } else {
                Text(
                    text = "Current goal: ${formatCurrency(monthlyGoal)} extra this month.",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("home_goal_current")
                )
                Text(
                    text = statusCopy,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PayDirtColors.TextSecondary,
                    lineHeight = 20.sp,
                    modifier = Modifier.testTag("home_goal_status")
                )
                TextButton(
                    onClick = onBeginEdit,
                    modifier = Modifier.testTag("home_goal_edit_button")
                ) {
                    Text("Edit monthly goal")
                }
            }
        }
    }
}

@Composable
private fun HeroMetric(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = PayDirtColors.Surface.copy(alpha = 0.95f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall)
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = accent,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ProgressSection(progress: HomeProgressUi, behaviorState: BehaviorState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeading(
            eyebrow = "PROGRESS",
            title = progress.headline
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PayDirtColors.Surface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProgressMeter(
                    progress = behaviorState.goalProgress,
                    label = progress.paceLabel,
                    supportingText = paceSupportingText(behaviorState)
                )

                CalloutRow(
                    title = progress.momentumCallout.title,
                    body = progress.momentumCallout.body,
                    accent = PayDirtColors.Accent
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PayDirtColors.Surface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Payoff impact",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = progress.totalsLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PayDirtColors.TextSecondary,
                    lineHeight = 20.sp
                )
                if (progress.recentPaymentCallout != null) {
                    CalloutRow(
                        title = progress.recentPaymentCallout.title,
                        body = progress.recentPaymentCallout.body,
                        accent = PayDirtColors.Primary
                    )
                }
                if (progress.nextOpportunityCallout != null) {
                    CalloutRow(
                        title = progress.nextOpportunityCallout.title,
                        body = progress.nextOpportunityCallout.body,
                        accent = PayDirtColors.Win
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressMeter(progress: Float, label: String, supportingText: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Monthly pace",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = PayDirtColors.TextSecondary
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp)),
            color = if (progress >= 1f) PayDirtColors.Win else PayDirtColors.Primary,
            trackColor = PayDirtColors.Border
        )
        Text(
            text = supportingText,
            style = MaterialTheme.typography.bodyMedium,
            color = PayDirtColors.TextMuted
        )
    }
}

@Composable
private fun CalloutRow(title: String, body: String, accent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PayDirtColors.SurfaceVariant)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = PayDirtColors.TextSecondary,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun SectionHeading(eyebrow: String, title: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = eyebrow,
            style = MaterialTheme.typography.labelSmall,
            color = PayDirtColors.Accent
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CardSummaryRow(card: Card, isRecommended: Boolean, onClick: () -> Unit) {
    val cardColor = PayDirtColors.CardColors.getOrElse(card.colorTag) { PayDirtColors.Primary }
    val progress = if (card.originalBalance > 0) {
        (1.0 - card.currentBalance / card.originalBalance).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRecommended) {
                PayDirtColors.SurfaceVariant
            } else {
                PayDirtColors.Surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(cardColor)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        text = if (isRecommended) "Recommended target right now" else "Tap for details and payment history",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isRecommended) PayDirtColors.Accent else PayDirtColors.TextMuted
                    )
                }
                APRTrustBadge(aprSource = card.aprSource)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CardDetailStat(
                    modifier = Modifier.weight(1f),
                    label = "Balance",
                    value = formatCurrency(card.currentBalance)
                )
                CardDetailStat(
                    modifier = Modifier.weight(1f),
                    label = "Minimum",
                    value = "${formatCurrency(card.minPayment)}/mo"
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Paydown progress",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PayDirtColors.TextSecondary
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = cardColor,
                    trackColor = PayDirtColors.Border
                )
                Text(
                    text = "${percentLabel(progress)} paid down from the starting balance",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PayDirtColors.TextMuted
                )
            }
        }
    }
}

@Composable
private fun CardDetailStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyState(onAddCard: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PayDirtColors.Surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("💳", fontSize = 44.sp)
            Text(
                text = "Start with one card. Manual is fine.",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Text(
                text = "Everything stays local to this device. You do not need account numbers or a linked bank connection to get started.",
                style = MaterialTheme.typography.bodyMedium,
                color = PayDirtColors.TextSecondary,
                lineHeight = 20.sp
            )
            Text(
                text = "For the first save, just enter a card name and current balance. Home will come back with the clearest next payoff move and show when the APR is still unconfirmed.",
                style = MaterialTheme.typography.bodyMedium,
                color = PayDirtColors.TextSecondary,
                lineHeight = 20.sp
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OnboardingBullet("Required now: card name + current balance.")
                OnboardingBullet("Optional for now: APR and minimum payment if you know them.")
                OnboardingBullet("After your first save: a recommended next move appears here, with honest confidence wording.")
            }
            Button(
                onClick = onAddCard,
                colors = ButtonDefaults.buttonColors(containerColor = PayDirtColors.Primary)
            ) {
                Text("Add your first card")
            }
        }
    }
}

@Composable
private fun OnboardingBullet(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(PayDirtColors.Accent)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = PayDirtColors.TextSecondary,
            lineHeight = 20.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun paceSupportingText(state: BehaviorState): String = when {
    state.isAheadOfGoal -> "Goal met. Every extra dollar above this pace compounds your payoff savings."
    state.goalRemainingAmount > 0.0 -> "${formatCurrency(state.goalRemainingAmount)} more would complete this month’s extra-payment goal."
    else -> "Keep the pace steady to stay on track this month."
}

private fun percentLabel(progress: Float): String = "${(progress * 100).toInt()}%"

private fun formatCurrency(amount: Double): String =
    NumberFormat.getCurrencyInstance(Locale.US).apply {
        if (kotlin.math.abs(amount) < 10.0) {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        } else {
            maximumFractionDigits = 0
        }
    }.format(amount)
