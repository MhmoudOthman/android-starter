@file:OptIn(ExperimentalMaterial3Api::class)

package com.starter.app.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_TYPE_NORMAL
import androidx.compose.ui.tooling.preview.Preview
import com.starter.app.core.designsystem.R as DesignSystemR
import com.starter.app.core.designsystem.theme.AppTheme
import com.starter.app.core.designsystem.dimen.sdp
import androidx.compose.material3.R as MaterialR


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorSnackBar(
    snackBarData: SnackbarData,
    modifier: Modifier = Modifier,
    actionOnNewLine: Boolean = true,
) {
    val actionContentColor = MaterialTheme.colorScheme.onError
    val dismissActionContentColor = MaterialTheme.colorScheme.onError
    val containerColor = MaterialTheme.colorScheme.error
    val contentColor = MaterialTheme.colorScheme.onError

    val actionLabel = snackBarData.visuals.actionLabel
    val actionComposable: (@Composable () -> Unit)? =
        if (actionLabel != null) {
            @Composable {
                TextButton(
                    colors = ButtonDefaults.textButtonColors(contentColor = actionContentColor),
                    onClick = { snackBarData.performAction() },
                    content = { Text(actionLabel) },
                )
            }
        } else {
            null
        }
    val dismissActionComposable: (@Composable () -> Unit)? =
        if (snackBarData.visuals.withDismissAction) {
            @Composable {
                val contentDescription = stringResource(DesignSystemR.string.close)
                TooltipBox(
                    positionProvider =
                        TooltipDefaults.rememberTooltipPositionProvider(
                            TooltipAnchorPosition.Above
                        ),
                    tooltip = {
                        PlainTooltip(
                            modifier =
                                Modifier.semantics {
                                    liveRegion = LiveRegionMode.Assertive
                                    paneTitle = contentDescription
                                },
                            caretShape = null,
                            maxWidth = TooltipDefaults.plainTooltipMaxWidth,
                            shape = TooltipDefaults.plainTooltipContainerShape,
                            contentColor = TooltipDefaults.plainTooltipContentColor,
                            containerColor = TooltipDefaults.plainTooltipContainerColor,
                            tonalElevation = 0.sdp,
                            shadowElevation = 0.sdp,
                        ) { Text(contentDescription) }
                    },
                    state = rememberTooltipState(),
                ) {
                    IconButton(
                        onClick = { snackBarData.dismiss() },
                        content = {
                            Icon(Icons.Filled.Close, contentDescription = contentDescription)
                        },
                    )
                }
            }
        } else {
            null
        }
    Snackbar(
        modifier = modifier.padding(12.sdp),
        action = actionComposable,
        dismissAction = dismissActionComposable,
        actionOnNewLine = actionOnNewLine,
        shape = MaterialTheme.shapes.small,
        containerColor = containerColor,
        contentColor = contentColor,
        actionContentColor = actionContentColor,
        dismissActionContentColor = dismissActionContentColor,
        content = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.sdp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Filled.Error,
                    tint = contentColor,
                    contentDescription = "Error"
                )
                Text(snackBarData.visuals.message)
            }
        },
    )
}

@Composable
fun ErrorSnackBarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    actionOnNewLine: Boolean = true,
) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        ErrorSnackBar(snackBarData = data,actionOnNewLine = actionOnNewLine)
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@Composable
private fun PreviewErrorSnackBar() {
    AppTheme {
        val visuals: SnackbarVisuals = object : SnackbarVisuals {
            override val message: String
                get() = "Something went wrong Something went wrong Something went wrong \n Something went wrong Something went wrong"
            override val actionLabel: String?
                get() = "Retry"
            override val withDismissAction: Boolean
                get() = true
            override val duration: SnackbarDuration
                get() = SnackbarDuration.Short
        }
        val snackBarData = object : SnackbarData {
            override val visuals: SnackbarVisuals
                get() = visuals

            override fun performAction() {}

            override fun dismiss() {}

        }
        ErrorSnackBar(snackBarData = snackBarData)
    }
}
