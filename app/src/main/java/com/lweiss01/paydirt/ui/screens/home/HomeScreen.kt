package com.lweiss01.paydirt.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lweiss01.paydirt.domain.model.Card
import com.lweiss01.paydirt.domain.model.PayoffPlan
import com.lweiss01.paydirt.ui.theme.PayDirtColors
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddCard: () -> Unit,
    onCardClick: (Long) -> Unit,
    onOpenOptimizer: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = PayDirtColors.Background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCard,
                containerColor = PayDirtColors.Primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Card")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // ── Header ──
            item {
                HomeHeader(
                    totalBalance = state.totalBalance,
                    cardCount = state.cards.size
                )
            }

            // ── Quick Plan Banner ──
            if (state.quickPlan != null) {
                item {
                    QuickPlanBanner(
                        plan = state.quickPlan!!,
                        onClick = onOpenOptimizer
                    )
                }
            }

            // ── Section label ──
            item {
                Text(
                    text = "YOUR CARDS",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }

            // ── Card list ──
            if (state.cards.isEmpty()) {
                item { EmptyState(onAddCard = onAddCard) }
            } else {
                items(state.cards, key = { it.id }) { card ->
                    CardSummaryRow(
                        card = card,
                        onClick = { onCardClick(card.id) }
                    )
                }
            }
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
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💳", fontSize = 22.sp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "PayDirt",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Text(
                        text = "WHERE YOUR MONEY HITS HARDEST",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Total Debt",
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = formatCurrency(totalBalance),
                style = MaterialTheme.typography.displayMedium,
                color = Color.White
            )
            Text(
                text = "$cardCount card${if (cardCount != 1) "s" else ""}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun QuickPlanBanner(plan: PayoffPlan, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PayDirtColors.WinMuted)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Top accent bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(PayDirtColors.Win, PayDirtColors.Accent)
                        )
                    )
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "✦ RECOMMENDED",
                style = MaterialTheme.typography.labelSmall,
                color = PayDirtColors.Win
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Put extra on ${plan.recommendedTargetName}",
                style = MaterialTheme.typography.titleMedium,
                color = PayDirtColors.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MiniStat(label = "Saves", value = "+${formatCurrency(plan.interestSaved)}", color = PayDirtColors.Win)
                MiniStat(label = "Time Saved", value = "+${formatMonths(plan.monthsSaved)}", color = PayDirtColors.Accent)
                MiniStat(label = "Debt Free", value = formatDate(plan.debtFreeDate), color = PayDirtColors.TextSecondary)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Tap to see full plan →",
                style = MaterialTheme.typography.labelSmall,
                color = PayDirtColors.TextMuted
            )
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, color: Color) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun CardSummaryRow(card: Card, onClick: () -> Unit) {
    val cardColor = PayDirtColors.CardColors.getOrElse(card.colorTag) { PayDirtColors.Primary }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PayDirtColors.Surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(cardColor)
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = card.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${card.apr}% APR · Min ${formatCurrency(card.minPayment)}/mo",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(card.currentBalance),
                    style = MaterialTheme.typography.titleMedium,
                    color = PayDirtColors.TextPrimary
                )
                // Progress bar
                val progress = if (card.originalBalance > 0)
                    (1.0 - card.currentBalance / card.originalBalance).toFloat().coerceIn(0f, 1f)
                else 0f
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.width(80.dp).height(3.dp).clip(RoundedCornerShape(2.dp)),
                    color = cardColor,
                    trackColor = PayDirtColors.Border
                )
            }
        }
    }
}

@Composable
private fun EmptyState(onAddCard: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("💳", fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Add your first card",
            style = MaterialTheme.typography.titleMedium,
            color = PayDirtColors.TextPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "No account numbers needed.\nJust balance, APR, and minimum payment.",
            style = MaterialTheme.typography.bodyMedium,
            color = PayDirtColors.TextMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onAddCard,
            colors = ButtonDefaults.buttonColors(containerColor = PayDirtColors.Primary)
        ) {
            Text("Add a Card")
        }
    }
}

// ─── Formatters ──────────────────────────────────────────────────────────────

private fun formatCurrency(amount: Double): String =
    NumberFormat.getCurrencyInstance(Locale.US).apply {
        maximumFractionDigits = 0
    }.format(amount)

private fun formatMonths(months: Int): String {
    if (months <= 0) return "—"
    val y = months / 12
    val m = months % 12
    return when {
        y > 0 && m > 0 -> "${y}y ${m}mo"
        y > 0 -> "${y}yr"
        else -> "${m}mo"
    }
}

private fun formatDate(epochMillis: Long): String =
    SimpleDateFormat("MMM yyyy", Locale.US).format(Date(epochMillis))
