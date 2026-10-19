package com.bloom.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Palette (placeholder until matched to the Figma tokens).
val Rose = Color(0xFFE85D8A)
val RoseLight = Color(0xFFFFE4EC)
val Lavender = Color(0xFF8E7CC3)
val LavenderLight = Color(0xFFEDE7F8)
val Peach = Color(0xFFFFB38A)
val PeachLight = Color(0xFFFFEDE3)
val Mint = Color(0xFF5FB89A)
val MintLight = Color(0xFFE2F5EE)
val Cream = Color(0xFFFFF8FA)
val Ink = Color(0xFF2D1B24)
val InkMuted = Color(0xFF7A6670)

private val BloomColors = lightColorScheme(
    primary = Rose,
    onPrimary = Color.White,
    primaryContainer = RoseLight,
    onPrimaryContainer = Ink,
    secondary = Lavender,
    onSecondary = Color.White,
    secondaryContainer = LavenderLight,
    onSecondaryContainer = Ink,
    tertiary = Peach,
    background = Cream,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = RoseLight,
    onSurfaceVariant = InkMuted,
    outlineVariant = Color(0xFFF0DDE4),
)

private val BloomTypography = Typography(
    headlineLarge = TextStyle(fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
)

@Composable
fun BloomTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BloomColors,
        typography = BloomTypography,
        content = content,
    )
}
