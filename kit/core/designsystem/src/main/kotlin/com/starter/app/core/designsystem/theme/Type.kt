package com.starter.app.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.starter.app.core.designsystem.R


val cairoFamily = FontFamily(
    Font(R.font.cairo_extra_light, FontWeight.ExtraLight),
    Font(R.font.cairo_light, FontWeight.Light),
    Font(R.font.cairo, FontWeight.Normal),
    Font(R.font.cairo_medium, FontWeight.Medium),
    Font(R.font.cairo_semi_bold, FontWeight.SemiBold),
    Font(R.font.cairo_bold, FontWeight.Bold),
    Font(R.font.cairo_extra_bold, FontWeight.ExtraBold),
    Font(R.font.cairo_black, FontWeight.Black),
)
val baseline = Typography()

val AppTypography = Typography(
    displayLarge = baseline.displayLarge.copy(
        fontFamily = cairoFamily,
        fontWeight = FontWeight.ExtraBold
    ),
    displayMedium = baseline.displayMedium.copy(
        fontFamily = cairoFamily,
        fontWeight = FontWeight.ExtraBold
    ),
    displaySmall = baseline.displaySmall.copy(
        fontFamily = cairoFamily,
        fontWeight = FontWeight.ExtraBold
    ),
    headlineLarge = baseline.headlineLarge.copy(
        fontFamily = cairoFamily,
        fontWeight = FontWeight.Bold
    ),
    headlineMedium = baseline.headlineMedium.copy(
        fontFamily = cairoFamily,
        fontWeight = FontWeight.Bold
    ),
    headlineSmall = baseline.headlineSmall.copy(
        fontFamily = cairoFamily,
        fontWeight = FontWeight.Bold
    ),
    titleLarge = baseline.titleLarge.copy(
        fontFamily = cairoFamily,
        fontWeight = FontWeight.SemiBold
    ),
    titleMedium = baseline.titleMedium.copy(
        fontFamily = cairoFamily,
        fontWeight = FontWeight.SemiBold
    ),
    titleSmall = baseline.titleSmall.copy(
        fontFamily = cairoFamily,
        fontWeight = FontWeight.SemiBold
    ),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = cairoFamily, fontWeight = FontWeight.Normal),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = cairoFamily, fontWeight = FontWeight.Normal),
    bodySmall = baseline.bodySmall.copy(fontFamily = cairoFamily, fontWeight = FontWeight.Normal),
    labelLarge = baseline.labelLarge.copy(fontFamily = cairoFamily, fontWeight = FontWeight.Medium),
    labelMedium = baseline.labelMedium.copy(
        fontFamily = cairoFamily,
        fontWeight = FontWeight.Medium
    ),
    labelSmall = baseline.labelSmall.copy(fontFamily = cairoFamily, fontWeight = FontWeight.Medium),
)

