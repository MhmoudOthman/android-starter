package com.starter.app.core.designsystem.config

import android.content.res.Configuration
import android.text.TextUtils
import android.view.ContextThemeWrapper
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import java.util.Locale

const val LANGUAGE_ENGLISH = "en"
const val LANGUAGE_ARABIC = "ar"

/**
 * Runs [content] in the user's chosen language **and** night mode.
 *
 * The two travel together because they are one mechanism: a `Configuration` override on a context
 * wrapper. Neither goes through `AppCompatDelegate` — the app hosts its UI in a plain
 * `ComponentActivity`, and below API 33 AppCompat applies a per-app locale (and night mode) by
 * wrapping the activity's own base context, which only happens for an `AppCompatActivity`. On a bare
 * one the setting is stored and silently ignored, which is every handset in this fleet.
 *
 * **Language.** `stringResource` reads `LocalResources`, whose default is computed from [LocalContext]
 * and [LocalConfiguration]; providing both is what redirects every string in the app.
 * [LocalLayoutDirection] has to be provided separately — Compose takes it from the host view, which
 * still follows the device locale, so Arabic would render left-to-right without this.
 *
 * **Night mode.** [darkTheme] is already the flag [com.starter.app.core.designsystem.theme.AppTheme]
 * picks its `ColorScheme` from, so everything Compose draws follows the user's choice on its own.
 * What does *not* is anything the **resource system** resolves: a `-night` qualifier is matched
 * against the configuration, and the activity's configuration carries the *handset's* night mode.
 * The two logo vectors are the visible case — they fill from `@color/md_theme_onSurface` and
 * `@color/md_theme_tertiaryContainer`, so forcing Dark on a light handset left a near-black logo on
 * the dark surface it was drawn over. Putting the night bit in this same override is what makes
 * `values-night` follow the app rather than the phone.
 *
 * Note the bit is set as a *delta*: `UI_MODE_TYPE_*` is left undefined, so the base context's ui mode
 * type (television, car, watch) survives and only the night half is replaced.
 *
 * **The overriding context must wrap the activity, not replace it.** `Context.createConfigurationContext`
 * returns a bare `ContextImpl`, and the composition finds its owners — the activity-result registry,
 * the activity itself, the back-pressed dispatcher — by walking `ContextWrapper.baseContext` upwards
 * from [LocalContext] until it hits the `Activity`. Providing a context that is not a wrapper ends
 * that walk at the first step, and `rememberLauncherForActivityResult` throws on the spot ("No
 * ActivityResultRegistryOwner was provided"). A [ContextThemeWrapper] around the activity keeps the
 * chain intact while `applyOverrideConfiguration` redirects the resources.
 *
 * Three things stay outside its reach, all deliberate: anything drawn by another activity (zxing's
 * a third-party activity ships its own UI), anything read off `Locale.getDefault()`, which is untouched, and the
 * window decor itself — that is styled from XML at activity creation and is repainted separately by
 * `MainActivity.SystemBars`. The data layer therefore reads the *stored* language rather than the
 * process locale — see `OrdersRepoImpl.currentLanguage()` — and every formatter in `:core:common`
 * pins `Locale.ENGLISH`.
 */
@Composable
fun ProvideAppConfiguration(
    language: String,
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    // Read so the wrapper is rebuilt when the device configuration changes. A rotation or a
    // font-scale change would otherwise keep serving resources built for the old one.
    val deviceConfiguration = LocalConfiguration.current

    val locale = remember(language) { Locale.forLanguageTag(resolveTag(language)) }

    val appContext = remember(context, deviceConfiguration, locale, darkTheme) {
        // An override *delta*, not a full configuration: only the locale and the night bit are set,
        // and everything the platform sees unset here — font scale, ui mode type, screen size — is
        // inherited from the context being wrapped.
        val override = Configuration().apply {
            setLocale(locale)
            setLayoutDirection(locale)
            uiMode = if (darkTheme) {
                Configuration.UI_MODE_NIGHT_YES
            } else {
                Configuration.UI_MODE_NIGHT_NO
            }
        }
        // Theme resource 0: the wrapper copies the activity's own theme onto the overridden
        // resources rather than applying a different one.
        ContextThemeWrapper(context, 0).apply { applyOverrideConfiguration(override) }
    }

    val layoutDirection = remember(locale) {
        if (TextUtils.getLayoutDirectionFromLocale(locale) == View.LAYOUT_DIRECTION_RTL) {
            LayoutDirection.Rtl
        } else {
            LayoutDirection.Ltr
        }
    }

    CompositionLocalProvider(
        LocalContext provides appContext,
        LocalConfiguration provides appContext.resources.configuration,
        LocalLayoutDirection provides layoutDirection,
        content = content,
    )
}

/**
 * The app ships two languages, so anything that is not one of them — an unset preference, a value
 * written by an older build — lands on English.
 */
private fun resolveTag(language: String): String =
    if (language == LANGUAGE_ARABIC) LANGUAGE_ARABIC else LANGUAGE_ENGLISH
