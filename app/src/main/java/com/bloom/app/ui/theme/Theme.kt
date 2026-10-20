package com.bloom.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Palette sampled from the Figma screenshots.
val Cream = Color(0xFFFCF8F5)
val Rose = Color(0xFFAD5750)
val RoseDark = Color(0xFF8E3F3A)
val RoseSoft = Color(0xFFFBEBE8)
val RoseMid = Color(0xFFE8C5C0)
val Border = Color(0xFFF0E3DF)
val Ink = Color(0xFF4A2724)
val Muted = Color(0xFF8C7B77)
val Gold = Color(0xFFB98A3C)
val LinkBlue = Color(0xFF2F5FD0)
val TipBackground = Color(0xFFF6E7DD)

// One accent per goal (progress rings on the dashboard).
val GoalMealsColor = Color(0xFFAD5750)
val GoalMovementColor = Color(0xFF5E8C61)
val GoalWaterColor = Color(0xFF4F7CA8)
val GoalRelaxationColor = Color(0xFF8A6BB1)
val GoalSleepColor = Color(0xFF6B6A9C)
val GoalCycleColor = Color(0xFFC0564E)
val GoalSymptomsColor = Color(0xFFC98B4E)

private val Serif = FontFamily.Serif

private val BloomColorScheme = lightColorScheme(
    primary = Rose,
    onPrimary = Color.White,
    primaryContainer = RoseSoft,
    onPrimaryContainer = Ink,
    secondary = RoseDark,
    onSecondary = Color.White,
    background = Cream,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = RoseSoft,
    onSurfaceVariant = Muted,
    outline = Border,
    outlineVariant = Border,
)

// Headings use an italic serif (the Figma uses an editorial serif); body text is sans.
private val BloomTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = Serif, fontStyle = FontStyle.Italic, fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp, lineHeight = 36.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Serif, fontStyle = FontStyle.Italic, fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp, lineHeight = 30.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Serif, fontStyle = FontStyle.Italic, fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp, lineHeight = 23.sp,
    ),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 18.sp),
    bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontSize = 13.sp, lineHeight = 19.sp),
    bodySmall = TextStyle(fontSize = 11.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 1.sp),
)

@Composable
fun BloomTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BloomColorScheme,
        typography = BloomTypography,
        content = content,
    )
}
