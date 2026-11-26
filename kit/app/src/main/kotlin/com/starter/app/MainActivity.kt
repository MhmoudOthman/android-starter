package com.starter.app

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.starter.app.core.designsystem.theme.AppTheme
import com.starter.app.launch.LaunchViewModel
import com.starter.app.navigation.AppNavHost
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Hosts the single [AppNavHost]. Kept deliberately thin, and built the way Android recommends:
 *
 * - **`AppCompatActivity`**, so the per-app language (`AppCompatDelegate.setApplicationLocales`)
 *   and night mode (`setDefaultNightMode`) are applied to it by the platform. A change recreates
 *   the activity; every string, `-night` resource, dialog and popup follows without Compose being
 *   told. `isSystemInDarkTheme()` therefore already reports the *app's* choice. See CLAUDE.md §8.
 * - **SplashScreen API** — the system splash, held by [LaunchViewModel] until the first screen is
 *   known, instead of a second splash of our own after it.
 * - **Edge to edge** — mandatory from targetSdk 35; bars are transparent and screens take insets.
 */
class MainActivity : AppCompatActivity() {

    private val launchViewModel: LaunchViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        lockOrientation()
        super.onCreate(savedInstanceState)
        splash.setKeepOnScreenCondition { launchViewModel.startDestination.value == null }
        edgeToEdge()

        setContent {
            val startDestination by launchViewModel.startDestination.collectAsStateWithLifecycle()
            AppTheme(darkTheme = isSystemInDarkTheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    startDestination?.let {
                        AppNavHost(navController = rememberNavController(), startDestination = it)
                    }
                }
            }
        }
    }

    /**
     * Transparent bars, with icons dark on the light theme and light on the dark one. The night bit
     * read here is the app's (AppCompat put it there), and a theme change recreates the activity,
     * so reading it once per instance is enough.
     */
    private fun edgeToEdge() {
        WindowCompat.enableEdgeToEdge(window)
        val night = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
            Configuration.UI_MODE_NIGHT_YES
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = !night
            isAppearanceLightNavigationBars = !night
        }
    }

    /**
     * Phones portrait, tablets landscape — **as a preference, not a guarantee.** For apps targeting
     * API 36+, Android 16 ignores orientation requests on screens 600dp and wider, and there is no
     * opt-out at targetSdk 37. So this holds on tablets up to Android 15, and every screen must still
     * lay itself out by window size class (`currentWidthClass()`), never by assuming the orientation.
     *
     * `smallestScreenWidthDp` is the short side, so it does not change as the device turns.
     */
    private fun lockOrientation() {
        requestedOrientation = if (resources.configuration.smallestScreenWidthDp >= TABLET_MIN_WIDTH_DP) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        }
    }

    private companion object {
        const val TABLET_MIN_WIDTH_DP = 600
    }
}
