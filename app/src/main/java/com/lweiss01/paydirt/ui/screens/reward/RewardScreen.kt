package com.lweiss01.paydirt.ui.screens.reward

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lweiss01.paydirt.domain.engine.BehaviorEngine.PaymentImpact
import com.lweiss01.paydirt.domain.model.MomentumScore
import com.lweiss01.paydirt.ui.theme.PayDirtColors
import java.text.NumberFormat
import java.util.Locale

/**
 * RewardScreen — the highest-ROI screen in the app.
 *
 * Four-line format from the behavior spec:
 *   Line 1 (headline):   "Nice hit."
 *   Line 2 (impact):     "+$1.08 saved"
 *   Line 3 (cumulative): "$14.32 total saved"
 *   Line 4 (projection): "On track to save ~$220"
 *
 * Plus: momentum, goal progress, next opportunity loop.
 *
 * Design rules (from spec):
 * ✓ Serious and satisfying
 * ✓ Real numbers, never rounded
 * ✗ No confetti
 * ✗ No "You're amazing!!"
 * ✗ No streak pressure
 */
@Composable
fun RewardScreen(
    impact: PaymentImpact,
    onPayMore: (Double) -> Unit,
    onDone: () -> Unit,
) {
    // Staggered reveal animations
    var showLine1 by remember { mutableStateOf(false) }
    var showLine2 by remember { mutableStateOf(false) }
    var showStats by remember { mutableStateOf(false) }
    var showGoal  by remember { mutableStateOf(false) }
    var showNext  by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showLine1 = true
        kotlinx.coroutines.delay(200)
        showLine2 = true
        kotlinx.coroutines.delay(300)
        showStats = true
        kotlinx.coroutines.delay(250)
        showGoal  = true
        kotlinx.coroutines.delay(200)
        showNext  = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PayDirtColors.Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))

        // ── Line 1: Headline ──
        AnimatedVisibility(
            visible = showLine1,
            enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 }
        ) {
            Text(
                text = impact.headlineText,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                color = Color.White,
                letterSpacing = (-1).sp,
            )
        }

        Spacer(Modifier.height(12.dp))

        // ── Line 2: Impact ──
        AnimatedVisibility(
            visible = showLine2,
            enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = impact.displayImpact,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PayDirtColors.Win,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    letterSpacing = (-2).sp,
                    lineHeight = 44.sp,
                )
                // Scaled projection
                if (impact.scaledProjection > 0.10) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Do that ${impact.scaledProjectionReps}× → ~${
                            formatCurrency(impact.scaledProjection)
                        } saved",
                        fontSize = 13.sp,
                        color = PayDirtColors.TextSecondary,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        // ── Stats row: cumulative + projection + momentum ──
        AnimatedVisibility(
            visible = showStats,
            enter = fadeIn(tween(500))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RewardStatRow(
                    label = "TOTAL SAVED",
                    value = impact.displayCumulative.removePrefix("").let {
                        formatCurrency(impact.cumulativeSaved)
                    },
                    valueColor = PayDirtColors.Win,
                )
                RewardStatRow(
                    label = "ON TRACK TO SAVE",
                    value = "~${formatCurrency(impact.projectedTotalSavings)}",
                    valueColor = PayDirtColors.Accent,
                )
                RewardStatRow(
                    label = "MOMENTUM",
                    value = impact.momentumScore.displayLabel,
                    valueColor = impact.momentumScore.displayColor,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Goal progress ──
        AnimatedVisibility(
            visible = showGoal && impact.monthlyGoal > 0,
            enter = fadeIn(tween(400))
        ) {
            GoalProgressCard(
                extraThisMonth = impact.extraThisMonth,
                monthlyGoal = impact.monthlyGoal,
                progress = impact.goalProgressAfter,
                isAheadOfGoal = impact.isAheadOfGoal,
            )
        }

        Spacer(Modifier.weight(1f))

        // ── Next opportunity ──
        AnimatedVisibility(
            visible = showNext,
            enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Next opportunity nudge
                if (impact.nextOpportunityInterestSaved > 0.01) {
                    NextOpportunityCard(
                        amount = impact.nextOpportunityAmount,
                        interestSaved = impact.nextOpportunityInterestSaved,
                        onTap = { onPayMore(impact.nextOpportunityAmount) }
                    )
                }

                // Action buttons
                PayMoreButtons(
                    suggestedAmount = impact.nextOpportunityAmount,
                    onPayMore = onPayMore,
                    onDone = onDone,
                )

                // Footer
                Text(
                    text = "Small hits, right target.",
                    fontSize = 11.sp,
                    color = PayDirtColors.TextMuted,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun RewardStatRow(
    label: String,
    value: String,
    valueColor: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(PayDirtColors.Surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = PayDirtColors.TextMuted,
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
        )
    }
}

@Composable
private fun GoalProgressCard(
    extraThisMonth: Double,
    monthlyGoal: Double,
    progress: Float,
    isAheadOfGoal: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isAheadOfGoal) Color(0xFF0A1810) else PayDirtColors.Surface)
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "MONTHLY GOAL",
                style = MaterialTheme.typography.labelSmall,
                color = PayDirtColors.TextMuted,
            )
            Text(
                text = if (isAheadOfGoal) "Goal hit ✓"
                       else "${formatCurrency(extraThisMonth)} / ${formatCurrency(monthlyGoal)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isAheadOfGoal) PayDirtColors.Win else PayDirtColors.TextSecondary,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            )
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = if (isAheadOfGoal) PayDirtColors.Win else PayDirtColors.Primary,
            trackColor = PayDirtColors.Border,
        )
    }
}

@Composable
private fun NextOpportunityCard(
    amount: Double,
    interestSaved: Double,
    onTap: () -> Unit,
) {
    TextButton(
        onClick = onTap,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF080E1E)),
        contentPadding = PaddingValues(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Another ${formatCurrency(amount)} right now saves ${formatCurrency(interestSaved)}",
                fontSize = 13.sp,
                color = PayDirtColors.Accent,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "→",
                fontSize = 16.sp,
                color = PayDirtColors.Accent,
            )
        }
    }
}

@Composable
private fun PayMoreButtons(
    suggestedAmount: Double,
    onPayMore: (Double) -> Unit,
    onDone: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Button(
            onClick = { onPayMore(suggestedAmount) },
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PayDirtColors.Primary),
        ) {
            Text(
                text = "Pay ${formatCurrency(suggestedAmount)} More",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        OutlinedButton(
            onClick = onDone,
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = PayDirtColors.TextSecondary
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, PayDirtColors.Border),
        ) {
            Text("Done for now", fontSize = 13.sp)
        }
    }
}

// ─── MomentumScore display extensions ────────────────────────────────────────

private val MomentumScore.displayLabel: String get() = when (this) {
    MomentumScore.NONE        -> "—"
    MomentumScore.BUILDING    -> "Building ↑"
    MomentumScore.STRONG      -> "Strong ↑↑"
    MomentumScore.COMPOUNDING -> "Compounding ↑↑↑"
}

private val MomentumScore.displayColor: Color get() = when (this) {
    MomentumScore.NONE        -> PayDirtColors.TextMuted
    MomentumScore.BUILDING    -> PayDirtColors.Warning
    MomentumScore.STRONG      -> PayDirtColors.Accent
    MomentumScore.COMPOUNDING -> PayDirtColors.Win
}

// ─── Formatter ───────────────────────────────────────────────────────────────

private fun formatCurrency(amount: Double): String =
    if (amount < 10)
        "$${String.format("%.2f", amount)}"
    else
        NumberFormat.getCurrencyInstance(Locale.US).apply {
            maximumFractionDigits = 0
        }.format(amount)
