@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.starter.app.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.starter.app.core.designsystem.dimen.sdp
import com.starter.app.core.designsystem.previews.PreviewLightDarkDeviceSystemUi
import com.starter.app.core.designsystem.theme.AppTheme
import com.starter.app.core.ui.component.cards.AppElevatedCard

/**
 * The app's only *blocking* loading visual: a non-dismissible full-screen dialog holding a card
 * with a Material loading indicator and an optional label. Reach for it when the user is waiting on
 * the work and nothing else on screen is usable meanwhile — a submit, a status change. A refresh
 * the user can keep scrolling behind belongs in the control itself (a button's `isLoading`, a
 * pull-to-refresh hint), not here.
 *
 * [color] is the card; the indicator and text take [contentColor], so the three theme wrappers
 * below stay readable on their own container. To swap the indicator for branded artwork, replace
 * the [ContainedLoadingIndicator] with an [AppLottieAnimation] — that composable already tints a
 * Lottie asset from theme colours, see [SuccessDialog] for the pattern.
 */
@Composable
fun LoadingOverlay(
    modifier: Modifier = Modifier,
    text: String? = null,
    color: Color = MaterialTheme.colorScheme.secondary,
    contentColor: Color = contentColorFor(color),
) {
    Dialog(
        onDismissRequest = {}, // nothing to dismiss to: the work behind it has to finish
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        AppElevatedCard(containerColor = color, contentColor = contentColor) {
            Row(
                modifier = modifier.padding(8.sdp),
                horizontalArrangement = Arrangement.spacedBy(8.sdp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ContainedLoadingIndicator(
                    modifier = Modifier.size(36.sdp),
                    containerColor = contentColor.copy(alpha = 0.15f),
                    indicatorColor = contentColor,
                )
                text?.let {
                    Text(
                        modifier = Modifier.padding(end = 4.sdp),
                        text = it,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
fun SecondaryLoadingOverlay(text: String? = null) =
    LoadingOverlay(text = text, color = MaterialTheme.colorScheme.secondary)

@Composable
fun TertiaryLoadingOverlay(text: String? = null) =
    LoadingOverlay(text = text, color = MaterialTheme.colorScheme.tertiaryContainer)

@Composable
fun RedLoadingOverlay(text: String? = null) =
    LoadingOverlay(text = text, color = MaterialTheme.colorScheme.error)

@PreviewLightDarkDeviceSystemUi
@Composable
private fun PreviewLoadingOverlay() {
    AppTheme {
        LoadingOverlay(text = "Loading")
    }
}
