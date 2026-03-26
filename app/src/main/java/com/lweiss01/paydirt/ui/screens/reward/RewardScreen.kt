package com.lweiss01.paydirt.ui.screens.reward

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lweiss01.paydirt.domain.engine.BehaviorEngine.PaymentImpact
import com.lweiss01.paydirt.domain.model.AprSource
import com.lweiss01.paydirt.domain.model.MomentumScore
import com.lweiss01.paydirt.ui.components.APRTrustBadge
import com.lweiss01.paydirt.ui.components.aprTrustCopyForSource
import com.lweiss01.paydirt.ui.theme.PayDirtColors
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun RewardScreen(
    impact: PaymentImpact,
    aprSource: AprSource,
    onPayMore: (Double) -> Unit,
    onDone: () -> Unit,
) {
    var showHero by remember { mutableStateOf(false) }
    var showStats by remember { mutableStateOf(false) }
    var showGoal by remember { mutableStateOf(false) }
    var showNextMove by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showHero = true
        delay(180)
        showStats = true
        delay(160)
        showGoal = true
        delay(140)
        showNextMove = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reward_screen")
            .background(PayDirtColors.Background)
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(20.dp))

        AnimatedVisibility(
            visible = showHero,
            enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 },
        ) {
            HeroSection(impact = impact)
        }

        Spacer(Modifier.height(24.dp))

        AnimatedVisibility(
            visible = showStats,
            enter = fadeIn(tween(300)),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RewardStatRow(
                    label = "TOTAL SAVED",
                    value = formatCurrency(impact.cumulativeSaved),
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

        if (impact.monthlyGoal > 0) {
            Spacer(Modifier.height(14.dp))
            AnimatedVisibility(
                visible = showGoal,
                enter = fadeIn(tween(300)),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    GoalProgressCard(
                        extraThisMonth = impact.extraThisMonth,
                        monthlyGoal = impact.monthlyGoal,
                        progress = impact.goalProgressAfter,
                        isAheadOfGoal = impact.isAheadOfGoal,
                    )
                    RewardAprTrustCard(aprSource = aprSource)
                }
            }
        }

        Spacer(Modifier.weight(1f))

        AnimatedVisibility(
            visible = showNextMove,
            enter = fadeIn(tween(320)) + slideInVertically(tween(320)) { it / 3 },
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                NextMoveCard(impact = impact)
                PayMoreButtons(
                    suggestedAmount = impact.nextOpportunityAmount,
                    canPayMore = impact.nextOpportunityInterestSaved > 0.01 && impact.nextOpportunityAmount > 0.0,
                    onPayMore = onPayMore,
                    onDone = onDone,
                )
                Text(
                    text = "Result first. Next move if it helps.",
                    fontSize = 11.sp,
                    color = PayDirtColors.TextMuted,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.4.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
    }
}

@Composable
private fun HeroSection(impact: PaymentImpact) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "INTEREST CUT",
            style = MaterialTheme.typography.labelMedium,
            color = PayDirtColors.TextMuted,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.1.sp,
        )
        Text(
            text = impact.displayImpact,
            fontSize = 46.sp,
            lineHeight = 46.sp,
            fontWeight = FontWeight.ExtraBold,
            color = PayDirtColors.Win,
            fontFamily = FontFamily.Monospace,
        )
        Text(
            text = impact.headlineText,
            style = MaterialTheme.typography.titleMedium,
            color = PayDirtColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = impact.rewardBodyText,
            style = MaterialTheme.typography.bodyMedium,
            color = PayDirtColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        if (impact.scaledProjection > 0.10) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = PayDirtColors.Surface,
            ) {
                Text(
                    text = "Repeat a move like this ${impact.scaledProjectionReps}× and you save about ${formatCurrency(impact.scaledProjection)} more.",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    fontSize = 12.sp,
                    color = PayDirtColors.TextSecondary,
                    textAlign = TextAlign.Center,
                )
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
            .clip(RoundedCornerShape(12.dp))
            .background(PayDirtColors.Surface)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = PayDirtColors.TextMuted,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.8.sp,
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            fontFamily = FontFamily.Monospace,
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
            .testTag("reward_goal_card")
            .clip(RoundedCornerShape(12.dp))
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
                fontFamily = FontFamily.Monospace,
            )
            Text(
                text = if (isAheadOfGoal) {
                    "Ahead • ${formatCurrency(extraThisMonth)}"
                } else {
                    "${formatCurrency(extraThisMonth)} of ${formatCurrency(monthlyGoal)}"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isAheadOfGoal) PayDirtColors.Win else PayDirtColors.TextSecondary,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.testTag("reward_goal_status"),
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
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (isAheadOfGoal) {
                "This payment keeps you ahead of your ${formatCurrency(monthlyGoal)} monthly extra-payment goal."
            } else {
                "This payment brings you to ${formatCurrency(extraThisMonth)} of your ${formatCurrency(monthlyGoal)} monthly extra-payment goal."
            },
            style = MaterialTheme.typography.bodySmall,
            color = PayDirtColors.TextSecondary,
            lineHeight = 18.sp,
        )
    }
}

@Composable
private fun RewardAprTrustCard(aprSource: AprSource) {
    val trustCopy = aprTrustCopyForSource(aprSource)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reward_apr_trust_card")
            .clip(RoundedCornerShape(12.dp))
            .background(PayDirtColors.Surface)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "APR CONFIDENCE",
            style = MaterialTheme.typography.labelSmall,
            color = PayDirtColors.TextMuted,
            fontFamily = FontFamily.Monospace,
        )
        APRTrustBadge(aprSource = aprSource)
        Text(
            text = trustCopy.helperText,
            style = MaterialTheme.typography.bodySmall,
            color = PayDirtColors.TextSecondary,
            lineHeight = 18.sp,
        )
    }
}

@Composable
private fun NextMoveCard(impact: PaymentImpact) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF080E1E))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "BEST NEXT MOVE",
            style = MaterialTheme.typography.labelSmall,
            color = PayDirtColors.TextMuted,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.8.sp,
        )
        if (impact.nextOpportunityInterestSaved > 0.01 && impact.nextOpportunityAmount > 0.0) {
            Text(
                text = "Another ${formatCurrency(impact.nextOpportunityAmount)} here saves about ${formatCurrency(impact.nextOpportunityInterestSaved)} more.",
                style = MaterialTheme.typography.bodyLarge,
                color = PayDirtColors.Accent,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "It is the cleanest next payoff move from this screen — not a requirement.",
                style = MaterialTheme.typography.bodySmall,
                color = PayDirtColors.TextSecondary,
            )
        } else {
            Text(
                text = "You already covered the clearest next move with this payment.",
                style = MaterialTheme.typography.bodyLarge,
                color = PayDirtColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Back out when you're ready. The math is already on your side.",
                style = MaterialTheme.typography.bodySmall,
                color = PayDirtColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun PayMoreButtons(
    suggestedAmount: Double,
    canPayMore: Boolean,
    onPayMore: (Double) -> Unit,
    onDone: () -> Unit,
) {
    if (!canPayMore) {
        OutlinedButton(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("reward_done_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = PayDirtColors.TextSecondary,
            ),
            border = BorderStroke(1.dp, PayDirtColors.Border),
        ) {
            Text("Back to card", fontSize = 13.sp)
        }
        Box(modifier = Modifier.testTag("reward_pay_more_button"))
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Button(
            onClick = { onPayMore(suggestedAmount) },
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .testTag("reward_pay_more_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PayDirtColors.Primary),
            contentPadding = PaddingValues(horizontal = 14.dp),
        ) {
            Text(
                text = "Use ${formatCurrency(suggestedAmount)} next",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
        OutlinedButton(
            onClick = onDone,
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .testTag("reward_done_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = PayDirtColors.TextSecondary,
            ),
            border = BorderStroke(1.dp, PayDirtColors.Border),
        ) {
            Text("Back to card", fontSize = 13.sp)
        }
    }
}

private val MomentumScore.displayLabel: String
    get() = when (this) {
        MomentumScore.NONE -> "—"
        MomentumScore.BUILDING -> "Building ↑"
        MomentumScore.STRONG -> "Strong ↑↑"
        MomentumScore.COMPOUNDING -> "Compounding ↑↑↑"
    }

private val MomentumScore.displayColor: Color
    get() = when (this) {
        MomentumScore.NONE -> PayDirtColors.TextMuted
        MomentumScore.BUILDING -> PayDirtColors.Warning
        MomentumScore.STRONG -> PayDirtColors.Accent
        MomentumScore.COMPOUNDING -> PayDirtColors.Win
    }

private fun formatCurrency(amount: Double): String =
    if (amount < 10) {
        "$${String.format("%.2f", amount)}"
    } else {
        NumberFormat.getCurrencyInstance(Locale.US).apply {
            maximumFractionDigits = 0
        }.format(amount)
    }
