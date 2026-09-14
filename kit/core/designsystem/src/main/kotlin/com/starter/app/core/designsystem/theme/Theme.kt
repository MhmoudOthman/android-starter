package com.starter.app.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * The app's Material 3 colour schemes.
 *
 * **The kit ships the Material 3 baseline palette on purpose** — `lightColorScheme()` and
 * `darkColorScheme()` with no overrides — so nothing here belongs to any brand. To apply a brand:
 * export a theme from the Material Theme Builder (https://m3.material.io/theme-builder), paste its
 * `Color.kt` values into this module's `Color.kt`, and pass them into the two calls below
 * (`lightColorScheme(primary = primaryLight, …)`). Mirror the same values into
 * `res/values/colors.xml` and `res/values-night/colors.xml`, which style the window before Compose
 * draws (status/navigation bars, the launch background).
 */
private val lightScheme = lightColorScheme()
private val darkScheme = darkColorScheme()

/**
 * Colours Material has no role for. Each family is filled in for light and dark; a family added
 * here has to be filled in for both, and a miss is a compile error rather than a silent black.
 *
 * - [errorBgColor]: a softer error surface for banners and row backgrounds, where `error` itself
 *   is too loud.
 * - [warning]: "needs attention, not broken" — amber.
 * - [info]: "neither an action, a success nor a problem — read it" — blue. Borrowing `primary`
 *   or `tertiary` for it would give the message a meaning it does not have.
 * - [selectedContainer]: the fill behind a selected row/segment when `secondaryContainer` is
 *   already spoken for on the same screen.
 */
@Immutable
data class ExtendedColorScheme(
    val errorBgColor: ColorFamily,
    val warning: ColorFamily,
    val info: ColorFamily,
    val selectedContainer: Color,
)

@Immutable
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color,
)

val extendedLight = ExtendedColorScheme(
    errorBgColor = ColorFamily(errorBgLight, onErrorBgLight, errorBgContainerLight, onErrorBgContainerLight),
    warning = ColorFamily(warningLight, onWarningLight, warningContainerLight, onWarningContainerLight),
    info = ColorFamily(infoLight, onInfoLight, infoContainerLight, onInfoContainerLight),
    selectedContainer = selectedContainerLight,
)

val extendedDark = ExtendedColorScheme(
    errorBgColor = ColorFamily(errorBgDark, onErrorBgDark, errorBgContainerDark, onErrorBgContainerDark),
    warning = ColorFamily(warningDark, onWarningDark, warningContainerDark, onWarningContainerDark),
    info = ColorFamily(infoDark, onInfoDark, infoContainerDark, onInfoContainerDark),
    selectedContainer = selectedContainerDark,
)

/**
 * Exposes [ExtendedColorScheme] to composables. Provided by [AppTheme]; read via
 * `LocalExtendedColorScheme.current`.
 */
val LocalExtendedColorScheme = staticCompositionLocalOf { extendedLight }

val unspecified_scheme = ColorFamily(
    Color.Unspecified, Color.Unspecified, Color.Unspecified, Color.Unspecified
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkScheme
        else -> lightScheme
    }

    val extendedColorScheme = if (darkTheme) extendedDark else extendedLight

    CompositionLocalProvider(LocalExtendedColorScheme provides extendedColorScheme) {
        MaterialExpressiveTheme(
            motionScheme = MotionScheme.expressive(),
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = appShapes(),
            content = content,
        )
    }
}
