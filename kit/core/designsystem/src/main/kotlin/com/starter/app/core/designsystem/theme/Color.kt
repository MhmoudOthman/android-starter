package com.starter.app.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// The Material colour scheme itself is the M3 baseline (see Theme.kt) and needs no values here.
// When a brand arrives, paste the Material Theme Builder export's `Color.kt` into this file and
// wire it into Theme.kt's lightColorScheme(...) / darkColorScheme(...) calls.

// --- Extended roles (Theme.kt / ExtendedColorScheme) ---------------------------------------------

// A softer error surface for banners and row backgrounds.
val errorBgLight = Color(0xFF9C4146)
val onErrorBgLight = Color(0xFFFFFFFF)
val errorBgContainerLight = Color(0xFFFFDAD9)
val onErrorBgContainerLight = Color(0xFF410006)
val errorBgDark = Color(0xFFFFB3B2)
val onErrorBgDark = Color(0xFF5F1318)
val errorBgContainerDark = Color(0xFF7D2A2E)
val onErrorBgContainerDark = Color(0xFFFFDAD9)

// "Needs attention, not broken" - amber.
val warningLight = Color(0xFF7A5900)
val onWarningLight = Color(0xFFFFFFFF)
val warningContainerLight = Color(0xFFFFDF9E)
val onWarningContainerLight = Color(0xFF261A00)
val warningDark = Color(0xFFF5C044)
val onWarningDark = Color(0xFF402D00)
val warningContainerDark = Color(0xFF5C4200)
val onWarningContainerDark = Color(0xFFFFDF9E)

// "Neither an action, a success nor a problem - read it" - blue.
val infoLight = Color(0xFF0B57D0)
val onInfoLight = Color(0xFFFFFFFF)
val infoContainerLight = Color(0xFFD3E3FD)
val onInfoContainerLight = Color(0xFF041E49)
val infoDark = Color(0xFFA8C7FA)
val onInfoDark = Color(0xFF062E6F)
val infoContainerDark = Color(0xFF0842A0)
val onInfoContainerDark = Color(0xFFD3E3FD)

// The fill behind a selected row/segment: the baseline primary, so it follows the scheme's accent.
val selectedContainerLight = Color(0xFF6750A4)
val selectedContainerDark = Color(0xFFD0BCFF)
