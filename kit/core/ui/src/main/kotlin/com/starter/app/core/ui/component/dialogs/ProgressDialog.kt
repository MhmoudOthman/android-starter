@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.starter.app.core.ui.component.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.DialogProperties
import com.starter.app.core.designsystem.theme.AppTheme
import com.starter.app.core.designsystem.dimen.sdp
import kotlin.math.roundToInt


@Composable
fun DownloadDialog(
    message: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    titleCentered: Boolean = false,
    isProgressing: Boolean = true,
    progress: Float = 0f,
    showPercentage: Boolean = true,
    positiveText: String? = null,
    onPositiveClick: (() -> Unit)? = null,
    iconSize: Dp = 20.sdp,
    negativeText: String? = null,
    onNegativeClick: (() -> Unit)? = null,
    onDismissRequest: () -> Unit = {},
    dismissOnBackPress: Boolean = false,
    dismissOnClickOutside: Boolean = false,
    ) = ProgressDialog(
    message = message,
    modifier = modifier,
    title = title,
    titleCentered = titleCentered,
    isProgressing = isProgressing,
    progress = progress,
    showPercentage = showPercentage,
    progressColor = MaterialTheme.colorScheme.tertiaryContainer,
    trackColor = MaterialTheme.colorScheme.onTertiaryContainer,
    progressContentDescription = "Download Progress",
    positiveText = positiveText,
    onPositiveClick = onPositiveClick,
    icon = { iconModifier ->
        Icon(
            Icons.Rounded.CloudDownload,
            contentDescription = null,
            modifier = iconModifier
        )
    },
    iconSize = iconSize,
    negativeText = negativeText,
    onNegativeClick = onNegativeClick,
    onDismissRequest = onDismissRequest,
    dismissOnBackPress = dismissOnBackPress,
    dismissOnClickOutside = dismissOnClickOutside,
    titlePanelColor = MaterialTheme.colorScheme.tertiaryContainer,
    onTitlePanelColor = MaterialTheme.colorScheme.onTertiaryContainer,
    positiveBtnColor = MaterialTheme.colorScheme.tertiaryContainer,
    positiveOnBtnColor = MaterialTheme.colorScheme.onTertiaryContainer,
    negativeBtnColor = MaterialTheme.colorScheme.primary,
    negativeOnBtnColor = MaterialTheme.colorScheme.onPrimary,
)

@Composable
fun ProgressDialogContent(
    modifier: Modifier = Modifier,
    message: String,
    title: String? = null,
    titleCentered: Boolean = false,
    isProgressing: Boolean = true,
    progress: Float = 0f,
    showPercentage: Boolean = true,
    progressColor: Color = WavyProgressIndicatorDefaults.indicatorColor,
    trackColor: Color = WavyProgressIndicatorDefaults.trackColor,
    progressContentDescription: String? = null,
    positiveText: String? = null,
    onPositiveClick: (() -> Unit)? = null,
    icon: (@Composable (Modifier) -> Unit)? = null,
    iconSize: Dp = 20.sdp,
    negativeText: String? = null,
    onNegativeClick: (() -> Unit)? = null,
    neutralText: String? = null,
    onNeutralClick: (() -> Unit)? = null,
    titlePanelColor: Color = Color.Unspecified,
    onTitlePanelColor: Color = MaterialTheme.colorScheme.onSurface,
    positiveBtnColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    positiveOnBtnColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    negativeBtnColor: Color = MaterialTheme.colorScheme.primary,
    negativeOnBtnColor: Color = MaterialTheme.colorScheme.onPrimary,
    neutralBtnColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
    neutralOnBtnColor: Color = MaterialTheme.colorScheme.onTertiaryContainer,
) {
    DialogScaffold(
        modifier = modifier,
        message = message,
        title = title,
        titleCentered = titleCentered,
        icon = icon,
        iconSize = iconSize,
        titlePanelColor = titlePanelColor,
        onTitlePanelColor = onTitlePanelColor,
    ) {
        if (isProgressing || progress > 0f) {
            // Label the indicator for screen readers. The determinate variant also announces its
            // percentage on its own (the progress lambda drives progressSemantics), but the
            // indeterminate one has no value to read, so a description is the only cue there.
            val indicatorModifier = Modifier
                .fillMaxWidth()
                .then(
                    if (progressContentDescription != null) {
                        Modifier.semantics { contentDescription = progressContentDescription }
                    } else {
                        Modifier
                    }
                )
            // progress = 0 معناها لسه الشغل ما بدأش، فبنعرض المؤشر بشكله الـ indeterminate
            // (بيلف/بيتموّج لوحده) لغاية ما الـ progress يبدأ يكبر، وساعتها بنحوّل للـ determinate.
            if (progress <= 0f) {
                LinearWavyProgressIndicator(
                    modifier = indicatorModifier,
                    color = progressColor,
                    trackColor = trackColor,
                )
            } else {
                val clampedProgress = progress.coerceIn(0f, 1f)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.sdp),
                ) {
                    LinearWavyProgressIndicator(
                        progress = { clampedProgress },
                        modifier = indicatorModifier.weight(1f),
                        color = progressColor,
                        trackColor = trackColor,
                    )
                    if (showPercentage) {
                        // النسبة جنب المؤشر؛ الـ Row بيتقلب لوحده في الـ RTL.
                        Text(
                            text = "${(clampedProgress * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = progressColor,
                        )
                    }
                }
            }
        } else {
            DialogButtonsRow(
                positiveText = positiveText,
                onPositiveClick = onPositiveClick,
                negativeText = negativeText,
                onNegativeClick = onNegativeClick,
                neutralText = neutralText,
                onNeutralClick = onNeutralClick,
                positiveBtnColor = positiveBtnColor,
                positiveOnBtnColor = positiveOnBtnColor,
                negativeBtnColor = negativeBtnColor,
                negativeOnBtnColor = negativeOnBtnColor,
                neutralBtnColor = neutralBtnColor,
                neutralOnBtnColor = neutralOnBtnColor,
            )
        }
    }
}


/**
 * [BasicAlertDialog] wrapper around [ProgressDialogContent], mirroring [AppAlertDialog]. Use this
 * to actually show the dialog; [ProgressDialogContent] stays public only so it can be rendered on
 * its own in a `@Preview`. Defaults mirror [ProgressDialogContent], so passing nothing here looks
 * identical to calling the content directly.
 */
@Composable
fun ProgressDialog(
    message: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    titleCentered: Boolean = false,
    isProgressing: Boolean = true,
    progress: Float = 0f,
    showPercentage: Boolean = true,
    progressColor: Color = WavyProgressIndicatorDefaults.indicatorColor,
    trackColor: Color = WavyProgressIndicatorDefaults.trackColor,
    progressContentDescription: String? = null,
    positiveText: String? = null,
    onPositiveClick: (() -> Unit)? = null,
    icon: (@Composable (Modifier) -> Unit)? = null,
    iconSize: Dp = 20.sdp,
    negativeText: String? = null,
    onNegativeClick: (() -> Unit)? = null,
    neutralText: String? = null,
    onNeutralClick: (() -> Unit)? = null,
    onDismissRequest: () -> Unit = {},
    dismissOnBackPress: Boolean = false,
    dismissOnClickOutside: Boolean = false,
    titlePanelColor: Color = Color.Unspecified,
    onTitlePanelColor: Color = MaterialTheme.colorScheme.onSurface,
    positiveBtnColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    positiveOnBtnColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    negativeBtnColor: Color = MaterialTheme.colorScheme.primary,
    negativeOnBtnColor: Color = MaterialTheme.colorScheme.onPrimary,
    neutralBtnColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
    neutralOnBtnColor: Color = MaterialTheme.colorScheme.onTertiaryContainer,
) {
    // See AppAlertDialog for why usePlatformDefaultWidth stays true.
    val properties = DialogProperties(
        usePlatformDefaultWidth = true,
        dismissOnBackPress = dismissOnBackPress,
        dismissOnClickOutside = dismissOnClickOutside,
    )
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        properties = properties,
        modifier = modifier,
    ) {
        ProgressDialogContent(
            message = message,
            title = title,
            titleCentered = titleCentered,
            isProgressing = isProgressing,
            progress = progress,
            showPercentage = showPercentage,
            progressColor = progressColor,
            trackColor = trackColor,
            progressContentDescription = progressContentDescription,
            positiveText = positiveText,
            onPositiveClick = onPositiveClick,
            icon = icon,
            iconSize = iconSize,
            negativeText = negativeText,
            onNegativeClick = onNegativeClick,
            neutralText = neutralText,
            onNeutralClick = onNeutralClick,
            titlePanelColor = titlePanelColor,
            onTitlePanelColor = onTitlePanelColor,
            positiveBtnColor = positiveBtnColor,
            positiveOnBtnColor = positiveOnBtnColor,
            negativeBtnColor = negativeBtnColor,
            negativeOnBtnColor = negativeOnBtnColor,
            neutralBtnColor = neutralBtnColor,
            neutralOnBtnColor = neutralOnBtnColor,
        )
    }
}


// progress = 0f: the indeterminate state that keeps waving until progress starts growing.
@PreviewLightDark
@Composable
fun PreviewDownloadIndeterminate() {
    AppTheme {
        ProgressDialogContent(
            title = "Downloading",
            message = "Preparing the update…",
            progress = 0f,
        )
    }
}

// progress > 0f: the determinate state, filling to the given percentage.
@PreviewLightDark
@Composable
fun PreviewDownloadDeterminate() {
    AppTheme {
        ProgressDialogContent(
            title = "Downloading",
            message = "Downloading the update…",
            progress = 0.6f,
        )
    }
}

// isProgressing = false: the action-buttons state shown once progress is done.
@PreviewLightDark
@Composable
fun PreviewDownloadButtons() {
    AppTheme {
        ProgressDialogContent(
            title = "Confirm",
            message = "Are you sure you want to continue?",
            isProgressing = false,
            positiveText = "Yes",
            onPositiveClick = {},
            neutralText = "Later",
            onNeutralClick = {},
        )
    }
}
