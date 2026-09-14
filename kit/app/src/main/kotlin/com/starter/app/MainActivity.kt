package com.starter.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.starter.app.core.designsystem.config.ProvideAppConfiguration
import com.starter.app.core.designsystem.theme.AppTheme
import com.starter.app.navigation.AppNavHost

/**
 * Hosts the single [AppNavHost]. Kept deliberately thin: theme, system bars, navigation.
 *
 * When the app grows a language/theme preference, resolve both here (outside the provider — a
 * `SYSTEM` theme reading `isSystemInDarkTheme()` from inside would be asking about the override
 * rather than the handset) and gate the first frame on a bounded loader. See CLAUDE.md §8.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val darkTheme = isSystemInDarkTheme()
            // "en" until a stored preference exists. ProvideAppConfiguration is what applies a
            // chosen language + night mode inside the composition without AppCompatDelegate.
            ProvideAppConfiguration(language = "en", darkTheme = darkTheme) {
                AppTheme(darkTheme = darkTheme) {
                    SystemBars(darkTheme)
                    Surface(modifier = Modifier.fillMaxSize()) {
                        AppNavHost(navController = rememberNavController())
                    }
                }
            }
        }
    }

    /**
     * The window decor is styled from XML at creation and follows the *system's* night mode, so a
     * theme forced from inside the app would leave a mismatched strip above it. No-op colour
     * setters from API 35 (bars are transparent there); the appearance flags carry it.
     */
    @Composable
    private fun SystemBars(darkTheme: Boolean) {
        val view = LocalView.current
        val barColor = MaterialTheme.colorScheme.surface

        SideEffect {
            @Suppress("DEPRECATION")
            window.statusBarColor = barColor.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = barColor.toArgb()

            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
}
