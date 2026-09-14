@file:OptIn(ExperimentalMaterial3Api::class)

package com.starter.app.core.ui.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_TYPE_NORMAL
import androidx.compose.ui.tooling.preview.Preview
import com.starter.app.core.designsystem.theme.AppTheme
import com.starter.app.core.designsystem.dimen.sdp

/**
 * Toolbar wrapper reproducing the Views `Widget.Toolbar` + `ThemeOverlay.Toolbar`:
 * primary-colored surface, `onPrimary` title/icons, and a 6sdp drop shadow (M3 bars are flat by default).
 *
 * The leading icon is described by [navigation] — pass whichever [TopBarNavigation] case the caller's
 * destination calls for. Deciding it is the caller's job: this module has no navigation dependency,
 * and picking the icon off the back stack is a guess that the app's own graph gets to make (see
 * `AppShell`).
 */
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigation: TopBarNavigation = TopBarNavigation.None,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
//        containerColor = MaterialTheme.colorScheme.primary,
//        scrolledContainerColor = MaterialTheme.colorScheme.primary,
//        titleContentColor = MaterialTheme.colorScheme.onPrimary,
//        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
//        actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
    ),
) {
//    Surface(shadowElevation = 6.sdp) {
        CenterAlignedTopAppBar(
            modifier = modifier,
            title = { Text(text = title, style = MaterialTheme.typography.titleLarge) },
            navigationIcon = {
                val icon = navigation.icon
                val onClick = navigation.onClick
                if (icon != null && onClick != null) {
                    IconButton(onClick = onClick) {
                        Icon(
                            imageVector = icon,
                            contentDescription = navigation.contentDescription(),
                        )
                    }
                }
            },
            actions = actions,
            colors = colors,
            scrollBehavior = scrollBehavior,
        )
//    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@Composable
private fun PreviewAppTopBar() {
    AppTheme {
        AppTopBar(title = "Starter App", navigation = TopBarNavigation.Drawer(onClick = {}))
    }
}
