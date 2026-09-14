package com.starter.app.core.ui.component.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_TYPE_NORMAL
import androidx.compose.ui.tooling.preview.Preview
import com.starter.app.core.designsystem.theme.AppTheme
import com.starter.app.core.designsystem.dimen.sdp

/**
 * Base filled button: a solid [Button] with a filled container and no border. All the
 * [FilledPrimaryButton] / [FilledSecondaryButton] / [FilledTertiaryButton] / [FilledRedButton]
 * variants delegate to this and only differ in the colors they pass.
 *
 * @param containerColor button background, also used as the loading indicator container.
 * @param contentColor text/icon color, also used as the loading indicator color.
 * @param disabledContainerAlpha alpha applied to [containerColor] when disabled.
 * @param disabledContentAlpha alpha applied to [contentColor] when disabled.
 */
@Composable
fun FilledButtonBase(
    text: String,
    containerColor: Color,
    contentColor: Color = contentColorFor(containerColor),
    modifier: Modifier = Modifier,
    disabledContainerAlpha: Float = 0.2f,
    disabledContentAlpha: Float = 0.5f,
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
    Button(
        onClick = onClick,
        modifier = Modifier
            .heightIn(min = 30.sdp)
            .then(modifier),
        enabled = enabled && !isLoading,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(disabledContainerAlpha),
            disabledContentColor = contentColor.copy(disabledContentAlpha),
        ),
    ) {
        ButtonContent(
            text = text,
            loadingContainerColor = containerColor,
            loadingIndicatorColor = contentColor,
            isLoading = isLoading,
            startIcon = startIcon,
            endIcon= endIcon
        )
    }
}

@Composable
fun FilledPrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: (@Composable (modifier:Modifier) -> Unit)? = null,
    iconGravity: IconGravity = IconGravity.Start,
    onClick: () -> Unit
) = FilledButtonBase(
    text = text,
    containerColor = MaterialTheme.colorScheme.primary,
    disabledContentAlpha = 0.7f,
    modifier = modifier,
    enabled = enabled,
    isLoading = isLoading,
    icon = icon,
    iconGravity = iconGravity,
    onClick = onClick
)

@Composable
fun FilledSecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: (@Composable (modifier:Modifier) -> Unit)? = null,
    iconGravity: IconGravity = IconGravity.Start,
    onClick: () -> Unit
) = FilledButtonBase(
    text = text,
    containerColor = MaterialTheme.colorScheme.secondaryContainer,
    modifier = modifier,
    enabled = enabled,
    isLoading = isLoading,
    icon = icon,
    iconGravity = iconGravity,
    onClick = onClick
)

@Composable
fun FilledTertiaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: (@Composable (modifier:Modifier) -> Unit)? = null,
    iconGravity: IconGravity = IconGravity.Start,
    onClick: () -> Unit
) = FilledButtonBase(
    text = text,
    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
    modifier = modifier,
    enabled = enabled,
    isLoading = isLoading,
    icon = icon,
    iconGravity = iconGravity,
    onClick = onClick
)

@Composable
fun FilledRedButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: (@Composable (modifier:Modifier) -> Unit)? = null,
    iconGravity: IconGravity = IconGravity.Start,
    onClick: () -> Unit
) = FilledButtonBase(
    text = text,
    containerColor = MaterialTheme.colorScheme.error,
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
private fun PreviewFilledPrimaryButton() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.sdp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.sdp)
        ) {

            FilledPrimaryButton(
                "Click Me",
                enabled = true,
                isLoading = false,
                icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") }) {

            }
            FilledPrimaryButton(
                "Click Me",
                enabled = false,
                isLoading = false,
                icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") }) {

            }
            FilledPrimaryButton(
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
private fun PreviewFilledSecondaryButton() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.sdp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.sdp)
        ) {

            FilledSecondaryButton(
                "Click Me",
                enabled = true,
                isLoading = false,
                icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") }) {

            }
            FilledSecondaryButton(
                "Click Me",
                enabled = false,
                isLoading = false,
                icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") }) {

            }
            FilledSecondaryButton(
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
private fun PreviewFilledTertiaryButton() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.sdp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.sdp)
        ) {

            FilledTertiaryButton(
                "Click Me",
                enabled = true,
                isLoading = false,
                icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") }) {

            }
            FilledTertiaryButton(
                "Click Me",
                enabled = false,
                isLoading = false,
                icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") }) {

            }
            FilledTertiaryButton(
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
private fun PreviewFilledRedButton() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.sdp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.sdp)
        ) {

            FilledRedButton(
                "Click Me",
                enabled = true,
                isLoading = false,
                icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") }) {

            }
            FilledRedButton(
                "Click Me",
                enabled = false,
                isLoading = false,
                icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") }) {

            }
            FilledRedButton(
                "Click Me",
                enabled = false,
                isLoading = true,
                icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") }) {

            }

        }
    }
}
