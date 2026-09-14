@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.starter.app.core.ui.component.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_TYPE_NORMAL
import androidx.compose.ui.tooling.preview.Preview
import com.starter.app.core.designsystem.theme.AppTheme
import com.starter.app.core.designsystem.dimen.sdp

/**
 * Shared content slot for the app's buttons: an optional [icon] + bold [text] row, with a circular
 * [ContainedLoadingIndicator] overlaid (bounded to the button height) while [isLoading].
 *
 * @param loadingContainerColor container color of the loading indicator (usually the button background).
 * @param loadingIndicatorColor color of the loading indicator drawn on top of [loadingContainerColor].
 */
@Composable
internal fun ButtonContent(
    text: String,
    loadingContainerColor: Color,
    loadingIndicatorColor: Color,
    isLoading: Boolean,
    startIcon: (@Composable (modifier: Modifier) -> Unit)? = null,
    endIcon: (@Composable (modifier: Modifier) -> Unit)? = null,
) {
    Box(
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.sdp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            startIcon?.invoke(Modifier.sizeIn(maxHeight = 24.sdp, maxWidth = 24.sdp))
            Text(
                text = text,
                fontWeight = FontWeight.Bold
            )
            endIcon?.invoke(Modifier.sizeIn(maxHeight = 24.sdp, maxWidth = 24.sdp))

        }
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
 * Border used by the outlined / text button variants. Solid [containerColor] when [enabled],
 * fading to [disabledContainerColor] otherwise.
 */
@Composable
internal fun outlinedButtonBorder(
    containerColor: Color = MaterialTheme.colorScheme.outline,
    disabledContainerColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
    enabled: Boolean = true
): BorderStroke =
    BorderStroke(
        width = 1.sdp,
        color = if (enabled) containerColor else disabledContainerColor,
    )

enum class IconGravity {
    Start, End
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@Composable
private fun PreviewButtonContent() {
    AppTheme {
        Surface {
            Column(
                modifier = Modifier.padding(4.sdp),
                verticalArrangement = Arrangement.spacedBy(8.sdp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ButtonContent(
                    text = "Normal Button",
                    loadingContainerColor = MaterialTheme.colorScheme.primary,
                    loadingIndicatorColor = MaterialTheme.colorScheme.onPrimary,
                    isLoading = false,
                    startIcon = { modifier ->
                        Icon(
                            modifier = modifier,
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null
                        )
                    }
                )
                ButtonContent(
                    text = "Loading Button",
                    loadingContainerColor = MaterialTheme.colorScheme.primary,
                    loadingIndicatorColor = MaterialTheme.colorScheme.onPrimary,
                    isLoading = true,
                    startIcon = { modifier ->
                        Icon(
                            modifier = modifier,
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null
                        )
                    }
                )
            }
        }
    }
}
