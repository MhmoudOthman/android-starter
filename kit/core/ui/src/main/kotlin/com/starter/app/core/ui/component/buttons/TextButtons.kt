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
import androidx.compose.material3.TextButton
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
 * Base text button: a flat, borderless [TextButton] with no container. All the
 * [TextPrimaryButton] / [TextSecondaryButton] / [TextTertiaryButton] / [TextRedButton] variants
 * delegate to this and only differ in the colors they pass.
 *
 * @param contentColor color used for the text, icon and the loading indicator container.
 * @param loadingIndicatorColor color of the loading indicator drawn on top of [contentColor].
 * @param disabledAlpha alpha applied to [contentColor] when the button is disabled.
 */
@Composable
fun TextButtonBase(
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
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .heightIn(min = 30.sdp)
            .then(modifier),
        enabled = enabled && !isLoading,
        shape = shape,
        colors = ButtonDefaults.textButtonColors(
            contentColor = contentColor,
            disabledContentColor = contentColor.copy(disabledAlpha),
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
fun TextPrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: (@Composable (modifier:Modifier) -> Unit)? = null,
    iconGravity: IconGravity = IconGravity.Start,
    onClick: () -> Unit
) = TextButtonBase(
    text = text,
    contentColor = MaterialTheme.colorScheme.primary,
    loadingIndicatorColor = MaterialTheme.colorScheme.onPrimary,
    modifier = modifier,
    enabled = enabled,
    isLoading = isLoading,
    icon = icon,
    onClick = onClick
)

@Composable
fun TextSecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: (@Composable (modifier:Modifier) -> Unit)? = null,
    iconGravity: IconGravity = IconGravity.Start,
    onClick: () -> Unit
) = TextButtonBase(
    text = text,
    contentColor = MaterialTheme.colorScheme.secondaryContainer,
    loadingIndicatorColor = MaterialTheme.colorScheme.onSecondaryContainer,
    disabledAlpha = 0.5f,
    modifier = modifier,
    enabled = enabled,
    isLoading = isLoading,
    icon = icon,
    onClick = onClick
)

@Composable
fun TextTertiaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: (@Composable (modifier:Modifier) -> Unit)? = null,
    iconGravity: IconGravity = IconGravity.Start,
    onClick: () -> Unit
) = TextButtonBase(
    text = text,
    contentColor = MaterialTheme.colorScheme.tertiaryContainer,
    loadingIndicatorColor = MaterialTheme.colorScheme.onTertiaryContainer,
    modifier = modifier,
    enabled = enabled,
    isLoading = isLoading,
    icon = icon,
    onClick = onClick
)

@Composable
fun TextRedButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: (@Composable (modifier:Modifier) -> Unit)? = null,
    iconGravity: IconGravity = IconGravity.Start,
    onClick: () -> Unit
) = TextButtonBase(
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
private fun PreviewTextPrimaryButton() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.sdp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.sdp)
        ) {

            TextPrimaryButton(
                "Click Me",
                enabled = true,
                isLoading = false,
                icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") }) {

            }
            TextPrimaryButton(
                "Click Me",
                enabled = false,
                isLoading = false,
                icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") }) {

            }
            TextPrimaryButton(
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
private fun PreviewTextSecondaryButton() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.sdp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.sdp)
        ) {

            TextSecondaryButton(
                "Click Me",
                enabled = true,
                isLoading = false,
                icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") }) {

            }
            TextSecondaryButton(
                "Click Me",
                enabled = false,
                isLoading = false,
                icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") }) {

            }
            TextSecondaryButton(
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
private fun PreviewTextTertiaryButton() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.sdp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.sdp)
        ) {

            TextTertiaryButton(
                "Click Me",
                enabled = true,
                isLoading = false,
                icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") }) {

            }
            TextTertiaryButton(
                "Click Me",
                enabled = false,
                isLoading = false,
                icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") }) {

            }
            TextTertiaryButton(
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
private fun PreviewTextRedButton() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.sdp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.sdp)
        ) {

            TextRedButton(
                "Click Me",
                enabled = true,
                isLoading = false,
                icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") }) {

            }
            TextRedButton(
                "Click Me",
                enabled = false,
                isLoading = false,
                icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") }) {

            }
            TextRedButton(
                "Click Me",
                enabled = false,
                isLoading = true,
                icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") }) {

            }

        }
    }
}
