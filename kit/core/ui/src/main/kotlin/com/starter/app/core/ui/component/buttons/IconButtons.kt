@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.starter.app.core.ui.component.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_TYPE_NORMAL
import androidx.compose.ui.tooling.preview.Preview
import com.starter.app.core.designsystem.theme.AppTheme
import com.starter.app.core.designsystem.dimen.sdp

/**
 * Shared content slot for the app's icon buttons: the [icon] alone (no label), with a circular
 * [ContainedLoadingIndicator] overlaid while [isLoading] — the same treatment [ButtonContent]
 * gives the labelled buttons, so an icon button loads exactly like the button next to it.
 *
 * @param loadingContainerColor container color of the loading indicator (usually the button background).
 * @param loadingIndicatorColor color of the loading indicator drawn on top of [loadingContainerColor].
 */
@Composable
internal fun IconButtonContent(
    icon: @Composable (modifier: Modifier) -> Unit,
    loadingContainerColor: Color,
    loadingIndicatorColor: Color,
    isLoading: Boolean,
) {
    Box(
        contentAlignment = Alignment.Center
    ) {
        icon(Modifier.sizeIn(maxHeight = 24.sdp, maxWidth = 24.sdp))
        if (isLoading) {
            ContainedLoadingIndicator(
                modifier = Modifier
                    .matchParentSize()
                    .aspectRatio(1f, true),
                containerColor = loadingContainerColor,
                indicatorColor = loadingIndicatorColor,
                polygons = LoadingIndicatorDefaults.IndeterminateIndicatorPolygons
            )
        }
    }
}

/**
 * Base filled icon button: a solid [FilledIconButton] with a filled container and no border. All the
 * [FilledPrimaryIconButton] / [FilledSecondaryIconButton] / [FilledTertiaryIconButton] /
 * [FilledRedIconButton] variants delegate to this and only differ in the colors they pass.
 *
 * @param containerColor button background, also used as the loading indicator container.
 * @param contentColor icon color, also used as the loading indicator color.
 * @param disabledContainerAlpha alpha applied to [containerColor] when disabled.
 * @param disabledContentAlpha alpha applied to [contentColor] when disabled.
 * @param interactionSource the button's own interaction stream. Pass one to observe press state —
 *   e.g. `collectIsPressedAsState()` to drive a hold-to-repeat. Reading the press from here rather
 *   than layering a `pointerInput` over the button is what keeps it working: a gesture detector
 *   stacked on top competes with the button's internal `clickable` for the same pointer events.
 */
@Composable
fun FilledIconButtonBase(
    icon: @Composable (modifier: Modifier) -> Unit,
    containerColor: Color = LocalContentColor.current,
    contentColor: Color = contentColorFor(containerColor),
    modifier: Modifier = Modifier,
    disabledContainerAlpha: Float = 0.2f,
    disabledContentAlpha: Float = 0.5f,
    shape: Shape = CircleShape,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
    onClick: () -> Unit
) {
    FilledIconButton(
        onClick = onClick,
        modifier = Modifier
            .sizeIn(minWidth = 30.sdp, minHeight = 30.sdp)
            .then(modifier),
        enabled = enabled && !isLoading,
        shape = shape,
        interactionSource = interactionSource,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(disabledContainerAlpha),
            disabledContentColor = contentColor.copy(disabledContentAlpha),
        ),
    ) {
        IconButtonContent(
            icon = icon,
            loadingContainerColor = containerColor,
            loadingIndicatorColor = contentColor,
            isLoading = isLoading,
        )
    }
}

@Composable
fun FilledPrimaryIconButton(
    icon: @Composable (modifier: Modifier) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) = FilledIconButtonBase(
    icon = icon,
    containerColor = MaterialTheme.colorScheme.primary,
    disabledContentAlpha = 0.7f,
    modifier = modifier,
    shape = shape,
    enabled = enabled,
    isLoading = isLoading,
    onClick = onClick
)

@Composable
fun FilledSecondaryIconButton(
    icon: @Composable (modifier: Modifier) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) = FilledIconButtonBase(
    icon = icon,
    containerColor = MaterialTheme.colorScheme.secondaryContainer,
    modifier = modifier,
    shape = shape,
    enabled = enabled,
    isLoading = isLoading,
    onClick = onClick
)

@Composable
fun FilledTertiaryIconButton(
    icon: @Composable (modifier: Modifier) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) = FilledIconButtonBase(
    icon = icon,
    containerColor = MaterialTheme.colorScheme.tertiary,
    modifier = modifier,
    shape = shape,
    enabled = enabled,
    isLoading = isLoading,
    onClick = onClick
)

@Composable
fun FilledRedIconButton(
    icon: @Composable (modifier: Modifier) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) = FilledIconButtonBase(
    icon = icon,
    containerColor = MaterialTheme.colorScheme.error,
    modifier = modifier,
    shape = shape,
    enabled = enabled,
    isLoading = isLoading,
    onClick = onClick
)

/**
 * Base outlined icon button. All the [OutlinedPrimaryIconButton] / [OutlinedSecondaryIconButton] /
 * [OutlinedTertiaryIconButton] / [OutlinedRedIconButton] variants delegate to this and only differ
 * in the colors they pass.
 *
 * @param contentColor color used for the icon, border and the loading indicator container.
 * @param loadingIndicatorColor color of the loading indicator drawn on top of [contentColor].
 * @param disabledAlpha alpha applied to [contentColor] when the button is disabled.
 */
@Composable
fun OutlinedIconButtonBase(
    icon: @Composable (modifier: Modifier) -> Unit,
    contentColor: Color = LocalContentColor.current,
    loadingIndicatorColor: Color = LocalContentColor.current,
    modifier: Modifier = Modifier,
    disabledAlpha: Float = 0.2f,
    shape: Shape = CircleShape,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    OutlinedIconButton(
        onClick = onClick,
        modifier = Modifier
            .sizeIn(minWidth = 30.sdp, minHeight = 30.sdp)
            .then(modifier),
        enabled = enabled && !isLoading,
        shape = shape,
        colors = IconButtonDefaults.outlinedIconButtonColors(
            contentColor = contentColor,
            disabledContentColor = contentColor.copy(disabledAlpha),
        ),
        border = outlinedButtonBorder(
            containerColor = contentColor,
            disabledContainerColor = contentColor.copy(disabledAlpha),
            enabled = enabled && !isLoading
        ),
    ) {
        IconButtonContent(
            icon = icon,
            loadingContainerColor = contentColor,
            loadingIndicatorColor = loadingIndicatorColor,
            isLoading = isLoading,
        )
    }
}

@Composable
fun OutlinedPrimaryIconButton(
    icon: @Composable (modifier: Modifier) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) = OutlinedIconButtonBase(
    icon = icon,
    contentColor = MaterialTheme.colorScheme.primary,
    loadingIndicatorColor = MaterialTheme.colorScheme.onPrimary,
    modifier = modifier,
    shape = shape,
    enabled = enabled,
    isLoading = isLoading,
    onClick = onClick
)

@Composable
fun OutlinedSecondaryIconButton(
    icon: @Composable (modifier: Modifier) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) = OutlinedIconButtonBase(
    icon = icon,
    contentColor = MaterialTheme.colorScheme.secondaryContainer,
    loadingIndicatorColor = MaterialTheme.colorScheme.onSecondaryContainer,
    disabledAlpha = 0.5f,
    modifier = modifier,
    shape = shape,
    enabled = enabled,
    isLoading = isLoading,
    onClick = onClick
)

@Composable
fun OutlinedTertiaryIconButton(
    icon: @Composable (modifier: Modifier) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) = OutlinedIconButtonBase(
    icon = icon,
    contentColor = MaterialTheme.colorScheme.tertiary,
    loadingIndicatorColor = MaterialTheme.colorScheme.onTertiary,
    modifier = modifier,
    shape = shape,
    enabled = enabled,
    isLoading = isLoading,
    onClick = onClick
)

@Composable
fun OutlinedRedIconButton(
    icon: @Composable (modifier: Modifier) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) = OutlinedIconButtonBase(
    icon = icon,
    contentColor = MaterialTheme.colorScheme.error,
    loadingIndicatorColor = MaterialTheme.colorScheme.onError,
    modifier = modifier,
    shape = shape,
    enabled = enabled,
    isLoading = isLoading,
    onClick = onClick
)

/**
 * Base standard icon button: a flat, borderless [IconButton] with no container — the icon-only
 * counterpart of [TextButtonBase]. All the [PrimaryIconButton] / [SecondaryIconButton] /
 * [TertiaryIconButton] / [RedIconButton] variants delegate to this and only differ in the colors
 * they pass.
 *
 * @param contentColor color used for the icon and the loading indicator container.
 * @param loadingIndicatorColor color of the loading indicator drawn on top of [contentColor].
 * @param disabledAlpha alpha applied to [contentColor] when the button is disabled.
 */
@Composable
fun IconButtonBase(
    icon: @Composable (modifier: Modifier) -> Unit,
    contentColor: Color = LocalContentColor.current,
    loadingIndicatorColor: Color = LocalContentColor.current,
    modifier: Modifier = Modifier,
    disabledAlpha: Float = 0.2f,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .sizeIn(minWidth = 30.sdp, minHeight = 30.sdp)
            .then(modifier),
        enabled = enabled && !isLoading,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = contentColor,
            disabledContentColor = contentColor.copy(disabledAlpha),
        ),
    ) {
        IconButtonContent(
            icon = icon,
            loadingContainerColor = contentColor,
            loadingIndicatorColor = loadingIndicatorColor,
            isLoading = isLoading,
        )
    }
}

@Composable
fun PrimaryIconButton(
    icon: @Composable (modifier: Modifier) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) = IconButtonBase(
    icon = icon,
    contentColor = MaterialTheme.colorScheme.primary,
    loadingIndicatorColor = MaterialTheme.colorScheme.onPrimary,
    modifier = modifier,
    enabled = enabled,
    isLoading = isLoading,
    onClick = onClick
)

@Composable
fun SecondaryIconButton(
    icon: @Composable (modifier: Modifier) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) = IconButtonBase(
    icon = icon,
    contentColor = MaterialTheme.colorScheme.secondaryContainer,
    loadingIndicatorColor = MaterialTheme.colorScheme.onSecondaryContainer,
    disabledAlpha = 0.5f,
    modifier = modifier,
    enabled = enabled,
    isLoading = isLoading,
    onClick = onClick
)

@Composable
fun TertiaryIconButton(
    icon: @Composable (modifier: Modifier) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) = IconButtonBase(
    icon = icon,
    contentColor = MaterialTheme.colorScheme.tertiary,
    loadingIndicatorColor = MaterialTheme.colorScheme.onTertiary,
    modifier = modifier,
    enabled = enabled,
    isLoading = isLoading,
    onClick = onClick
)

@Composable
fun RedIconButton(
    icon: @Composable (modifier: Modifier) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) = IconButtonBase(
    icon = icon,
    contentColor = MaterialTheme.colorScheme.error,
    loadingIndicatorColor = MaterialTheme.colorScheme.onError,
    modifier = modifier,
    enabled = enabled,
    isLoading = isLoading,
    onClick = onClick
)

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@Composable
private fun PreviewFilledIconButtons() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.sdp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.sdp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.sdp)) {
                FilledPrimaryIconButton(icon = { PreviewSearchIcon(it) }, enabled = true) {}
                FilledPrimaryIconButton(icon = { PreviewSearchIcon(it) }, enabled = false) {}
                FilledPrimaryIconButton(icon = { PreviewSearchIcon(it) }, isLoading = true) {}
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.sdp)) {
                FilledSecondaryIconButton(icon = { PreviewSearchIcon(it) }, enabled = true) {}
                FilledSecondaryIconButton(icon = { PreviewSearchIcon(it) }, enabled = false) {}
                FilledSecondaryIconButton(icon = { PreviewSearchIcon(it) }, isLoading = true) {}
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.sdp)) {
                FilledTertiaryIconButton(icon = { PreviewSearchIcon(it) }, enabled = true) {}
                FilledTertiaryIconButton(icon = { PreviewSearchIcon(it) }, enabled = false) {}
                FilledTertiaryIconButton(icon = { PreviewSearchIcon(it) }, isLoading = true) {}
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.sdp)) {
                FilledRedIconButton(icon = { PreviewSearchIcon(it) }, enabled = true) {}
                FilledRedIconButton(icon = { PreviewSearchIcon(it) }, enabled = false) {}
                FilledRedIconButton(icon = { PreviewSearchIcon(it) }, isLoading = true) {}
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@Composable
private fun PreviewOutlinedIconButtons() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.sdp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.sdp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.sdp)) {
                OutlinedPrimaryIconButton(icon = { PreviewSearchIcon(it) }, enabled = true) {}
                OutlinedPrimaryIconButton(icon = { PreviewSearchIcon(it) }, enabled = false) {}
                OutlinedPrimaryIconButton(icon = { PreviewSearchIcon(it) }, isLoading = true) {}
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.sdp)) {
                OutlinedSecondaryIconButton(icon = { PreviewSearchIcon(it) }, enabled = true) {}
                OutlinedSecondaryIconButton(icon = { PreviewSearchIcon(it) }, enabled = false) {}
                OutlinedSecondaryIconButton(icon = { PreviewSearchIcon(it) }, isLoading = true) {}
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.sdp)) {
                OutlinedTertiaryIconButton(icon = { PreviewSearchIcon(it) }, enabled = true) {}
                OutlinedTertiaryIconButton(icon = { PreviewSearchIcon(it) }, enabled = false) {}
                OutlinedTertiaryIconButton(icon = { PreviewSearchIcon(it) }, isLoading = true) {}
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.sdp)) {
                OutlinedRedIconButton(icon = { PreviewSearchIcon(it) }, enabled = true) {}
                OutlinedRedIconButton(icon = { PreviewSearchIcon(it) }, enabled = false) {}
                OutlinedRedIconButton(icon = { PreviewSearchIcon(it) }, isLoading = true) {}
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@Composable
private fun PreviewStandardIconButtons() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.sdp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.sdp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.sdp)) {
                PrimaryIconButton(icon = { PreviewSearchIcon(it) }, enabled = true) {}
                PrimaryIconButton(icon = { PreviewSearchIcon(it) }, enabled = false) {}
                PrimaryIconButton(icon = { PreviewSearchIcon(it) }, isLoading = true) {}
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.sdp)) {
                SecondaryIconButton(icon = { PreviewSearchIcon(it) }, enabled = true) {}
                SecondaryIconButton(icon = { PreviewSearchIcon(it) }, enabled = false) {}
                SecondaryIconButton(icon = { PreviewSearchIcon(it) }, isLoading = true) {}
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.sdp)) {
                TertiaryIconButton(icon = { PreviewSearchIcon(it) }, enabled = true) {}
                TertiaryIconButton(icon = { PreviewSearchIcon(it) }, enabled = false) {}
                TertiaryIconButton(icon = { PreviewSearchIcon(it) }, isLoading = true) {}
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.sdp)) {
                RedIconButton(icon = { PreviewSearchIcon(it) }, enabled = true) {}
                RedIconButton(icon = { PreviewSearchIcon(it) }, enabled = false) {}
                RedIconButton(icon = { PreviewSearchIcon(it) }, isLoading = true) {}
            }
        }
    }
}

@Composable
private fun PreviewSearchIcon(modifier: Modifier) = Icon(
    modifier = modifier,
    imageVector = Icons.Rounded.Search,
    contentDescription = "Search"
)
