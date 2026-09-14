package com.starter.app.core.ui.component.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
 * Base outlined button. All the [OutlinedPrimaryButton] / [OutlinedSecondaryButton] /
 * [OutlinedTertiaryButton] / [OutlinedRedButton] variants delegate to this and only differ
 * in the colors they pass.
 *
 * @param contentColor color used for the text, icon, border and the loading indicator container.
 * @param loadingIndicatorColor color of the loading indicator drawn on top of [contentColor].
 * @param disabledAlpha alpha applied to [contentColor] when the button is disabled.
 */
@Composable
fun OutlinedButtonBase(
    text: String,
    contentColor: Color,
    loadingIndicatorColor: Color,
    modifier: Modifier = Modifier,
    disabledAlpha: Float = 0.2f,
    shape: Shape = MaterialTheme.shapes.medium,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: (@Composable (modifier:Modifier) -> Unit)? = null,
    iconGravity: IconGravity = IconGravity.Start,
    onClick: () -> Unit
) {

    var startIcon:(@Composable (modifier:Modifier) -> Unit)? = null
    var endIcon:(@Composable (modifier:Modifier) -> Unit)? = null


    when(iconGravity){
        IconGravity.Start -> startIcon = icon
        IconGravity.End -> endIcon = icon
    }
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .heightIn(min = 30.sdp)
            .then(modifier),
        enabled = enabled && !isLoading,
        shape = shape,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = contentColor,
            disabledContentColor = contentColor.copy(disabledAlpha),
        ),
        border = outlinedButtonBorder(
            containerColor = contentColor,
            disabledContainerColor = contentColor.copy(disabledAlpha),
            enabled = enabled && !isLoading
        ),
    ) {
        ButtonContent(
            text = text,
            loadingContainerColor = contentColor,
            loadingIndicatorColor = loadingIndicatorColor,
            isLoading = isLoading,
            startIcon = startIcon,
            endIcon= endIcon
        )
    }
}

@Composable
fun OutlinedPrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: (@Composable (modifier:Modifier) -> Unit)? = null,
    iconGravity: IconGravity = IconGravity.Start,
    onClick: () -> Unit
) = OutlinedButtonBase(
    text = text,
    contentColor = MaterialTheme.colorScheme.primary,
    loadingIndicatorColor = MaterialTheme.colorScheme.onPrimary,
    modifier = modifier,
    enabled = enabled,
    isLoading = isLoading,
    icon = icon,
    iconGravity = iconGravity,
    onClick = onClick
)

@Composable
fun OutlinedSecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: (@Composable (modifier:Modifier) -> Unit)? = null,
    iconGravity: IconGravity = IconGravity.Start,
    onClick: () -> Unit
) = OutlinedButtonBase(
    text = text,
    contentColor = MaterialTheme.colorScheme.secondaryContainer,
    loadingIndicatorColor = MaterialTheme.colorScheme.onSecondaryContainer,
    disabledAlpha = 0.5f,
    modifier = modifier,
    enabled = enabled,
    isLoading = isLoading,
    icon = icon,
    iconGravity = iconGravity,
    onClick = onClick
)

@Composable
fun OutlinedTertiaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: (@Composable (modifier:Modifier) -> Unit)? = null,
    iconGravity: IconGravity = IconGravity.Start,
    onClick: () -> Unit
) = OutlinedButtonBase(
    text = text,
    contentColor = MaterialTheme.colorScheme.tertiaryContainer,
    loadingIndicatorColor = MaterialTheme.colorScheme.onTertiaryContainer,
    modifier = modifier,
    enabled = enabled,
    isLoading = isLoading,
    icon = icon,
    iconGravity = iconGravity,
    onClick = onClick
)

@Composable
fun OutlinedRedButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: (@Composable (modifier:Modifier) -> Unit)? = null,
    iconGravity: IconGravity = IconGravity.Start,
    onClick: () -> Unit
) = OutlinedButtonBase(
    text = text,
    contentColor = MaterialTheme.colorScheme.error,
    loadingIndicatorColor = MaterialTheme.colorScheme.onError,
    modifier = modifier,
    enabled = enabled,
    isLoading = isLoading,
    icon = icon,
    iconGravity = iconGravity,
    onClick = onClick
)

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@Composable
private fun PreviewOutlinedPrimaryButton() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.sdp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.sdp)
        ) {

            OutlinedPrimaryButton(
                "Click Me",
                enabled = true,
                isLoading = false,
                icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") }) {

            }
            OutlinedPrimaryButton(
                "Click Me",
                enabled = false,
                isLoading = false,
                icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") }) {

            }
            OutlinedPrimaryButton(
                "Click Me",
                enabled = false,
                isLoading = true,
                icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") }) {

            }

        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@Composable
private fun PreviewOutlinedSecondaryButton() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.sdp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.sdp)
        ) {

            OutlinedSecondaryButton(
                "Click Me",
                enabled = true,
                isLoading = false,
                icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") }) {

            }
            OutlinedSecondaryButton(
                "Click Me",
                enabled = false,
                isLoading = false,
                icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") }) {

            }
            OutlinedSecondaryButton(
                "Click Me",
                enabled = false,
                isLoading = true,
                icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") }) {

            }

        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@Composable
private fun PreviewOutlinedTertiaryButton() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.sdp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.sdp)
        ) {

            OutlinedTertiaryButton(
                "Click Me",
                enabled = true,
                isLoading = false,
                icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") }) {

            }
            OutlinedTertiaryButton(
                "Click Me",
                enabled = false,
                isLoading = false,
                icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") }) {

            }
            OutlinedTertiaryButton(
                "Click Me",
                enabled = false,
                isLoading = true,
                icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") }) {

            }

        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@Composable
private fun PreviewOutlinedRedButton() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.sdp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.sdp)
        ) {

            OutlinedRedButton(
                "Click Me",
                enabled = true,
                isLoading = false,
                icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") }) {

            }
            OutlinedRedButton(
                "Click Me",
                enabled = false,
                isLoading = false,
                icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") }) {

            }
            OutlinedRedButton(
                "Click Me",
                enabled = false,
                isLoading = true,
                icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") }) {

            }

        }
    }
}
