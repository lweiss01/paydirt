package com.lweiss01.paydirt.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lweiss01.paydirt.domain.engine.SmartAPREngine
import com.lweiss01.paydirt.domain.model.AprSource
import com.lweiss01.paydirt.ui.theme.PayDirtColors

/**
 * APRConfidenceBadge — Shows APR with a confidence indicator.
 *
 * Examples:
 *   24.99%  [✓ confirmed]
 *   ~22.5%  [≈ estimated]   ← tap to see how we got here
 *   ~19%    [? low confidence]
 *   APR unknown  [+ enter APR]
 */
@Composable
fun APRConfidenceBadge(
    estimate: SmartAPREngine.APREstimate,
    onConfirmTap: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val (bgColor, borderColor, textColor, icon, label) = when (estimate.confidence) {
        SmartAPREngine.Confidence.HIGH -> ConfidenceStyle(
            bg     = Color(0xFF0F2A1E),
            border = Color(0xFF1A4A32),
            text   = PayDirtColors.Win,
            icon   = if (estimate.dataPointsUsed == -1) "✓" else "≈",
            label  = if (estimate.dataPointsUsed == -1) "confirmed" else "estimated ✓"
        )
        SmartAPREngine.Confidence.MEDIUM -> ConfidenceStyle(
            bg     = Color(0xFF1A1A0F),
            border = Color(0xFF3A3A1A),
            text   = PayDirtColors.Warning,
            icon   = "≈",
            label  = "estimated"
        )
        SmartAPREngine.Confidence.LOW -> ConfidenceStyle(
            bg     = Color(0xFF1A0F0F),
            border = Color(0xFF3A1A1A),
            text   = Color(0xFFFF8A65),
            icon   = "?",
            label  = "low confidence"
        )
        SmartAPREngine.Confidence.INSUFFICIENT -> ConfidenceStyle(
            bg     = PayDirtColors.SurfaceVariant,
            border = PayDirtColors.Border,
            text   = PayDirtColors.TextMuted,
            icon   = "+",
            label  = "tap to enter"
        )
    }

    var showDetail by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(bgColor)
                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                .then(
                    if (estimate.confidence != SmartAPREngine.Confidence.HIGH ||
                        estimate.dataPointsUsed != -1
                    ) {
                        Modifier.clickable { showDetail = !showDetail }
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (estimate.confidence == SmartAPREngine.Confidence.INSUFFICIENT) {
                Text(
                    "APR unknown",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            } else {
                Text(
                    "${estimate.estimatedAPR}%",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(borderColor)
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    "$icon $label",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    letterSpacing = 0.3.sp
                )
            }
        }

        AnimatedVisibility(
            visible = showDetail,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                    .background(PayDirtColors.Surface)
                    .border(
                        1.dp,
                        PayDirtColors.Border,
                        RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)
                    )
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "HOW WE ESTIMATED THIS",
                    style = MaterialTheme.typography.labelSmall,
                    color = PayDirtColors.Primary
                )

                if (estimate.monthlyEstimates.isNotEmpty()) {
                    estimate.monthlyEstimates
                        .filter { !it.isOutlier }
                        .take(3)
                        .forEach { month ->
                            MonthlyDataRow(month)
                        }
                    if (estimate.monthlyEstimates.any { it.isOutlier }) {
                        Text(
                            "Some months excluded as outliers (balance transfer, fee, or rate change)",
                            fontSize = 11.sp,
                            color = PayDirtColors.TextMuted
                        )
                    }
                }

                estimate.notes.take(2).forEach { note ->
                    Text(
                        "• $note",
                        fontSize = 12.sp,
                        color = PayDirtColors.TextSecondary,
                        lineHeight = 17.sp
                    )
                }

                if (onConfirmTap != null &&
                    estimate.confidence != SmartAPREngine.Confidence.INSUFFICIENT
                ) {
                    Spacer(Modifier.width(2.dp))
                    TextButton(
                        onClick = onConfirmTap,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "Enter exact APR from your statement →",
                            fontSize = 12.sp,
                            color = PayDirtColors.Primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyDataRow(month: SmartAPREngine.MonthlyEstimate) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            month.monthLabel,
            fontSize = 12.sp,
            color = PayDirtColors.TextMuted,
            modifier = Modifier.weight(1f)
        )
        Text(
            "Balance: $${month.openingBalance.toInt()}",
            fontSize = 12.sp,
            color = PayDirtColors.TextMuted,
            modifier = Modifier.weight(1.5f)
        )
        Text(
            "Interest: $${String.format("%.2f", month.interestCharged)}",
            fontSize = 12.sp,
            color = PayDirtColors.Danger,
            modifier = Modifier.weight(1.5f)
        )
        Text(
            "→ ${String.format("%.1f", month.impliedAPR)}%",
            fontSize = 12.sp,
            color = PayDirtColors.TextSecondary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private data class ConfidenceStyle(
    val bg: Color,
    val border: Color,
    val text: Color,
    val icon: String,
    val label: String
)

data class AprTrustCopy(
    val badgeLabel: String,
    val helperText: String,
)

internal fun aprTrustCopyForSource(aprSource: AprSource): AprTrustCopy = when (aprSource) {
    AprSource.USER -> AprTrustCopy(
        badgeLabel = "APR confirmed",
        helperText = "This recommendation is using the APR you entered for this card."
    )
    AprSource.PLAID -> AprTrustCopy(
        badgeLabel = "APR confirmed",
        helperText = "This recommendation is using APR data synced from your linked account."
    )
    AprSource.INFERRED -> AprTrustCopy(
        badgeLabel = "APR estimated",
        helperText = "This recommendation is directionally useful, but the APR was inferred from account history."
    )
    AprSource.UNKNOWN -> AprTrustCopy(
        badgeLabel = "APR not confirmed",
        helperText = "This recommendation may sharpen once you add the exact APR from a statement."
    )
}

@Composable
fun APRTrustBadge(
    aprSource: AprSource,
    modifier: Modifier = Modifier,
) {
    val copy = aprTrustCopyForSource(aprSource)
    val (background, border, text) = when (aprSource) {
        AprSource.USER, AprSource.PLAID -> Triple(
            PayDirtColors.WinMuted,
            PayDirtColors.WinBorder,
            PayDirtColors.Win,
        )
        AprSource.INFERRED -> Triple(
            Color(0xFF1A1A0F),
            Color(0xFF3A3A1A),
            PayDirtColors.Warning,
        )
        AprSource.UNKNOWN -> Triple(
            PayDirtColors.SurfaceVariant,
            PayDirtColors.Border,
            PayDirtColors.TextSecondary,
        )
    }

    Text(
        text = copy.badgeLabel,
        color = text,
        style = MaterialTheme.typography.labelSmall,
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(background)
            .border(1.dp, border, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    )
}

/**
 * Compact inline APR display for use in card summary rows.
 * Shows just "22.5%" or "~19%" without the expandable detail.
 */
@Composable
fun InlineAPRChip(
    estimate: SmartAPREngine.APREstimate,
    modifier: Modifier = Modifier
) {
    val (text, color) = when (estimate.confidence) {
        SmartAPREngine.Confidence.HIGH ->
            (if (estimate.dataPointsUsed == -1) "${estimate.estimatedAPR}%"
            else "~${estimate.estimatedAPR}%") to PayDirtColors.Win
        SmartAPREngine.Confidence.MEDIUM ->
            "~${estimate.estimatedAPR}%" to PayDirtColors.Warning
        SmartAPREngine.Confidence.LOW ->
            "~${estimate.estimatedAPR}%?" to Color(0xFFFF8A65)
        SmartAPREngine.Confidence.INSUFFICIENT ->
            "APR?" to PayDirtColors.TextMuted
    }

    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = color,
        modifier = modifier
    )
}
