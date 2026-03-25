package com.lweiss01.paydirt.ui.screens.optimizer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lweiss01.paydirt.domain.engine.PayoffEngine
import com.lweiss01.paydirt.domain.model.CardPayoffDetail
import com.lweiss01.paydirt.domain.model.PayoffPlan
import com.lweiss01.paydirt.ui.theme.PayDirtColors
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptimizerScreen(
    onBack: () -> Unit,
    viewModel: OptimizerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = PayDirtColors.Background,
        topBar = {
            TopAppBar(
                title = { Text("Optimizer", color = PayDirtColors.TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PayDirtColors.TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PayDirtColors.Background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                OptimizerInputCard(
                    extra = state.extraMonthly,
                    strategy = state.strategy,
                    onExtraChange = viewModel::setExtra,
                    onStrategyChange = viewModel::setStrategy
                )
            }

            if (state.isCalculating) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PayDirtColors.Primary)
                    }
                }
            } else if (state.plan != null) {
                val plan = state.plan!!

                item { RecommendationBanner(plan = plan, extra = state.extraMonthly) }
                item { ImpactStats(plan = plan) }
                item {
                    Text(
                        "PAYOFF ORDER",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(plan.cards.sortedBy { it.payoffOrder }) { card ->
                    PayoffCardRow(
                        card = card,
                        isTarget = card.cardId == plan.recommendedTargetId
                    )
                }
                item { HowItWorksCard() }
            }
        }
    }
}

@Composable
private fun OptimizerInputCard(
    extra: String,
    strategy: PayoffEngine.Strategy,
    onExtraChange: (String) -> Unit,
    onStrategyChange: (PayoffEngine.Strategy) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PayDirtColors.Surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Extra Monthly Payment", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = extra,
                onValueChange = onExtraChange,
                label = { Text("Amount ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PayDirtColors.Primary,
                    unfocusedBorderColor = PayDirtColors.Border,
                    focusedTextColor = PayDirtColors.TextPrimary,
                    unfocusedTextColor = PayDirtColors.TextPrimary,
                    cursorColor = PayDirtColors.Primary
                )
            )
            Spacer(Modifier.height(16.dp))
            Text("Strategy", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PayoffEngine.Strategy.values().forEach { s ->
                    val selected = strategy == s
                    FilterChip(
                        selected = selected,
                        onClick = { onStrategyChange(s) },
                        label = { Text(s.displayName(), fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PayDirtColors.Primary,
                            selectedLabelColor = Color.White,
                            containerColor = PayDirtColors.SurfaceVariant,
                            labelColor = PayDirtColors.TextSecondary
                        )
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = strategy.description(),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.sp,
                color = PayDirtColors.Primary
            )
        }
    }
}

@Composable
private fun RecommendationBanner(plan: PayoffPlan, extra: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PayDirtColors.WinMuted)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Brush.horizontalGradient(listOf(PayDirtColors.Win, PayDirtColors.Accent)))
            )
            Spacer(Modifier.height(14.dp))
            Text("✦ RECOMMENDED TARGET", style = MaterialTheme.typography.labelSmall, color = PayDirtColors.Win)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Put your extra ${formatCurrency(extra.toDoubleOrNull() ?: 0.0)} on",
                style = MaterialTheme.typography.bodyMedium,
                color = PayDirtColors.TextSecondary
            )
            Text(
                text = plan.recommendedTargetName,
                style = MaterialTheme.typography.displayMedium,
                color = PayDirtColors.Win,
                fontSize = 24.sp
            )
        }
    }
}

@Composable
private fun ImpactStats(plan: PayoffPlan) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(modifier = Modifier.weight(1f), label = "Interest Saved", value = "+${formatCurrency(plan.interestSaved)}", color = PayDirtColors.Win)
        StatCard(modifier = Modifier.weight(1f), label = "Time Saved", value = "+${formatMonths(plan.monthsSaved)}", color = PayDirtColors.Accent)
        StatCard(modifier = Modifier.weight(1f), label = "Debt Free", value = formatDate(plan.debtFreeDate), color = PayDirtColors.Warning)
    }
}

@Composable
private fun StatCard(modifier: Modifier, label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PayDirtColors.Surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, color = color, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun PayoffCardRow(card: CardPayoffDetail, isTarget: Boolean) {
    val accentColor = if (isTarget) PayDirtColors.Win else PayDirtColors.TextMuted

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isTarget) PayDirtColors.WinMuted else PayDirtColors.Surface
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isTarget) PayDirtColors.Win else PayDirtColors.Border),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = card.payoffOrder.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isTarget) Color.Black else PayDirtColors.TextMuted
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(card.name, style = MaterialTheme.typography.titleMedium)
                    if (isTarget) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "← extra here",
                            fontSize = 10.sp,
                            color = PayDirtColors.Win,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Text(
                    "${card.apr}% APR · ${formatCurrency(card.originalBalance)} balance",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatMonths(card.paidOffMonth), style = MaterialTheme.typography.titleMedium, color = accentColor)
                Text(formatCurrency(card.totalInterestPaid) + " int.", fontSize = 11.sp, color = PayDirtColors.Danger)
            }
        }
    }
}

@Composable
private fun HowItWorksCard() {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PayDirtColors.Surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("💡 HOW IT WORKS", style = MaterialTheme.typography.labelSmall, color = PayDirtColors.Primary)
            Spacer(Modifier.height(8.dp))
            Text(
                "As each card is paid off, its freed minimum payment stacks onto your extra — " +
                "so your paydown power grows automatically.",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
        }
    }
}

private fun PayoffEngine.Strategy.displayName() = when (this) {
    PayoffEngine.Strategy.AVALANCHE -> "Avalanche"
    PayoffEngine.Strategy.SNOWBALL  -> "Snowball"
    PayoffEngine.Strategy.HYBRID    -> "Hybrid"
}

private fun PayoffEngine.Strategy.description() = when (this) {
    PayoffEngine.Strategy.AVALANCHE -> "⚡ Highest APR first — saves the most money"
    PayoffEngine.Strategy.SNOWBALL  -> "🏔 Smallest balance first — fastest wins"
    PayoffEngine.Strategy.HYBRID    -> "🎯 Balances APR and size — smart targeting"
}

private fun formatCurrency(amount: Double) =
    NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 }.format(amount)

private fun formatMonths(months: Int): String {
    if (months <= 0) return "—"
    val y = months / 12; val m = months % 12
    return when { y > 0 && m > 0 -> "${y}y ${m}mo"; y > 0 -> "${y}yr"; else -> "${m}mo" }
}

private fun formatDate(epochMillis: Long) =
    SimpleDateFormat("MMM yyyy", Locale.US).format(Date(epochMillis))
