package com.starter.app.core.ui.adaptive

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass

/**
 * The window's width class, as Material 3 defines it — the one thing a screen branches its layout
 * on. Never the device type, and never the orientation: the same tablet is *expanded* in landscape
 * and *medium* in portrait, a split screen can make a tablet *compact*, and Android 16 ignores the
 * app's orientation lock on large screens (targetSdk 36+), so any of these can happen.
 *
 * - [COMPACT] (< 600dp): phones — one column, full width.
 * - [MEDIUM] (600–839dp): portrait tablets, foldables — one column, width-capped and centred.
 * - [EXPANDED] (≥ 840dp): landscape tablets — two panes.
 */
enum class WidthClass { COMPACT, MEDIUM, EXPANDED }

@Composable
fun currentWidthClass(): WidthClass {
    val sizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
    return when {
        sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> WidthClass.EXPANDED
        sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> WidthClass.MEDIUM
        else -> WidthClass.COMPACT
    }
}
