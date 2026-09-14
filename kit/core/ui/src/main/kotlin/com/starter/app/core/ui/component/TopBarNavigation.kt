package com.starter.app.core.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.starter.app.core.designsystem.R as DesignSystemR

/**
 * What [AppTopBar]'s leading slot does. Modelled as a type rather than a nullable click lambda so
 * "this bar has no navigation icon" is a choice you can read at the call site — [None] is the
 * default, and every other case carries the handler it needs.
 */
sealed interface TopBarNavigation {
    /** No leading icon at all. */
    data object None : TopBarNavigation

    /** Back arrow — auto-mirrored, so it points the right way in Arabic. */
    data class Up(val onClick: () -> Unit) : TopBarNavigation

    /** Hamburger that opens the navigation drawer. */
    data class Drawer(val onClick: () -> Unit) : TopBarNavigation

    data class Custom(
        val icon: ImageVector,
        val contentDescription: String?,
        val onClick: () -> Unit,
    ) : TopBarNavigation
}

/** The icon to render, or `null` for [TopBarNavigation.None]. */
internal val TopBarNavigation.icon: ImageVector?
    get() = when (this) {
        TopBarNavigation.None -> null
        is TopBarNavigation.Up -> Icons.AutoMirrored.Rounded.ArrowBack
        is TopBarNavigation.Drawer -> Icons.Rounded.Menu
        is TopBarNavigation.Custom -> icon
    }

@Composable
internal fun TopBarNavigation.contentDescription(): String? = when (this) {
    TopBarNavigation.None -> null
    is TopBarNavigation.Up -> stringResource(id = DesignSystemR.string.back)
    is TopBarNavigation.Drawer -> stringResource(id = DesignSystemR.string.drawer_open)
    is TopBarNavigation.Custom -> contentDescription
}

internal val TopBarNavigation.onClick: (() -> Unit)?
    get() = when (this) {
        TopBarNavigation.None -> null
        is TopBarNavigation.Up -> onClick
        is TopBarNavigation.Drawer -> onClick
        is TopBarNavigation.Custom -> onClick
    }
