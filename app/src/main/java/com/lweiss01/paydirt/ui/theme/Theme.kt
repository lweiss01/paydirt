package com.lweiss01.paydirt.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ─── Brand Colors ─────────────────────────────────────────────────────────────

object PayDirtColors {
    val Background       = Color(0xFF0A0F1E)
    val Surface          = Color(0xFF0F1829)
    val SurfaceVariant   = Color(0xFF141E35)
    val Border           = Color(0xFF1E2D54)
    val BorderLight      = Color(0xFF2D3F6B)

    val Primary          = Color(0xFF3B82F6)   // Action blue
    val PrimaryVariant   = Color(0xFF2563EB)
    val Accent           = Color(0xFF06B6D4)   // Cyan

    val Win              = Color(0xFF22C55E)   // Green — savings, paid off
    val WinMuted         = Color(0xFF0F2A1E)   // Dark green background
    val WinBorder        = Color(0xFF1A4A32)

    val Danger           = Color(0xFFEF4444)   // Red — interest, debt
    val Warning          = Color(0xFFF59E0B)   // Amber — caution

    val TextPrimary      = Color(0xFFE8EAF2)
    val TextSecondary    = Color(0xFF94A3B8)
    val TextMuted        = Color(0xFF4A6080)
    val TextDisabled     = Color(0xFF2D3F6B)

    // Card color tags (0–5)
    val CardColors = listOf(
        Color(0xFF3B82F6),   // 0 Blue
        Color(0xFF22C55E),   // 1 Green
        Color(0xFFF59E0B),   // 2 Amber
        Color(0xFFEF4444),   // 3 Rose
        Color(0xFFA855F7),   // 4 Purple
        Color(0xFF06B6D4),   // 5 Cyan
    )
}

// ─── Typography ───────────────────────────────────────────────────────────────

// Using system fonts as fallback — swap in Sora + DM Sans after adding to assets/
val PayDirtTypography = androidx.compose.material3.Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        letterSpacing = (-1).sp,
        color = PayDirtColors.TextPrimary
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        letterSpacing = (-0.5).sp,
        color = PayDirtColors.TextPrimary
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = (-0.3).sp,
        color = PayDirtColors.TextPrimary
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        color = PayDirtColors.TextPrimary
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = PayDirtColors.TextSecondary
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        letterSpacing = 0.8.sp,
        color = PayDirtColors.TextMuted
    )
)

// ─── Color Scheme ─────────────────────────────────────────────────────────────

private val DarkColorScheme = darkColorScheme(
    primary          = PayDirtColors.Primary,
    onPrimary        = Color.White,
    primaryContainer = PayDirtColors.PrimaryVariant,
    secondary        = PayDirtColors.Accent,
    background       = PayDirtColors.Background,
    surface          = PayDirtColors.Surface,
    onBackground     = PayDirtColors.TextPrimary,
    onSurface        = PayDirtColors.TextPrimary,
    outline          = PayDirtColors.Border,
    error            = PayDirtColors.Danger
)

@Composable
fun PayDirtTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = PayDirtTypography,
        content = content
    )
}
