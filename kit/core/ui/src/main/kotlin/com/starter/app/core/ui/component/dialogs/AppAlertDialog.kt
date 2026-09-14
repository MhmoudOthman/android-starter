@file:OptIn(ExperimentalMaterial3Api::class)

package com.starter.app.core.ui.component.dialogs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.DialogProperties
import com.starter.app.core.designsystem.previews.PreviewLightDarkDeviceSystemUi
import com.starter.app.core.designsystem.theme.AppTheme
import com.starter.app.core.designsystem.dimen.sdp


@Composable
fun ErrorDialog(
    modifier: Modifier = Modifier,
    title: String? = null,
    titleCentered: Boolean = false,
    message: String,
    positiveText: String? = null,
    onPositiveClick: (() -> Unit)? = null,
    onDismissRequest: () -> Unit,
    negativeText: String? = null,
    onNegativeClick: (() -> Unit)? = null,
    neutralText: String? = null,
    onNeutralClick: (() -> Unit)? = null,
    dismissOnBackPress: Boolean = false,
    dismissOnClickOutside: Boolean = false,
    messageHasMarkup: Boolean = false,
) = AppAlertDialog(
    modifier = modifier,
    title = title,
    titleCentered = titleCentered,
    message = message,
    messageHasMarkup = messageHasMarkup,
    positiveText = positiveText,
    onPositiveClick = onPositiveClick,
    onDismissRequest = onDismissRequest,
    negativeText = negativeText,
    onNegativeClick = onNegativeClick,
    neutralText = neutralText,
    onNeutralClick = onNeutralClick,
    dismissOnBackPress = dismissOnBackPress,
    dismissOnClickOutside = dismissOnClickOutside,
    icon = { iconModifier ->
        Icon(Icons.Rounded.Error, contentDescription = null, modifier = iconModifier)
    },
    titlePanelColor = MaterialTheme.colorScheme.error,
    onTitlePanelColor = MaterialTheme.colorScheme.onError,
    positiveBtnColor = MaterialTheme.colorScheme.error,
    positiveOnBtnColor = MaterialTheme.colorScheme.onError,
    neutralBtnColor = MaterialTheme.colorScheme.error,
    neutralOnBtnColor = MaterialTheme.colorScheme.onError,

    )


/**
 * `materialAlertDialogTheme` + `AlertDialog.TitlePanel` — dialog with a full-bleed primary title
 * header, a secondary "confirm" button and an optional outlined "neutral" button. The stock M3
 * [AlertDialog] can't render a full-bleed header, so this is built on [BasicAlertDialog].
 */
@Composable
fun AppAlertDialog(
    modifier: Modifier = Modifier,
    message: String,
    title: String? = null,
    titleCentered: Boolean = false,
    positiveText: String? = null,
    onPositiveClick: (() -> Unit)? = null,
    onDismissRequest: () -> Unit,
    icon: (@Composable (Modifier) -> Unit)? = null,
    iconSize: Dp = 20.sdp,
    negativeText: String? = null,
    onNegativeClick: (() -> Unit)? = null,
    neutralText: String? = null,
    onNeutralClick: (() -> Unit)? = null,
    dismissOnBackPress: Boolean = false,
    dismissOnClickOutside: Boolean = false,
    titlePanelColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    onTitlePanelColor: Color = contentColorFor(titlePanelColor),
    positiveBtnColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    positiveOnBtnColor: Color = contentColorFor(positiveBtnColor),
    negativeBtnColor: Color = MaterialTheme.colorScheme.error,
    negativeOnBtnColor: Color = contentColorFor(negativeBtnColor),
    neutralBtnColor: Color = MaterialTheme.colorScheme.primary,
    neutralOnBtnColor: Color = contentColorFor(neutralBtnColor),
    messageHasMarkup: Boolean = false,
) {
    // Keep usePlatformDefaultWidth = true (the default): forcing it to false makes the dialog
    // unconstrained in width, which triggers an IndexOutOfBoundsException in Layoutlib and leaves
    // the @Preview blank. The full-bleed header comes from the Row filling the Surface, not from
    // the window width, so this has no effect on the visual result.
    val properties = DialogProperties(
        usePlatformDefaultWidth = true,
        dismissOnBackPress = dismissOnBackPress,
        dismissOnClickOutside = dismissOnClickOutside
    )
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        properties = properties,
        modifier = modifier,
    ) {
        AppAlertDialogContent(
            title = title,
            titleCentered = titleCentered,
            message = message,
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
            messageHasMarkup = messageHasMarkup,
        )
    }
}

/**
 * Stateless dialog body extracted from [AppAlertDialog] so it can be rendered directly in a
 * `@Preview`. Compose dialogs draw in a separate window that Layoutlib can't render, so previewing
 * the content on its own (not wrapped in [BasicAlertDialog]) is the only way to see it in the IDE.
 */
@Composable
private fun AppAlertDialogContent(
    modifier: Modifier = Modifier,
    message: String,
    title: String? = null,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    btnShape: Shape = MaterialTheme.shapes.medium,
    titleCentered: Boolean = false,
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
    messageHasMarkup: Boolean = false,
) {
    DialogScaffold(
        modifier = modifier,
        message = message,
        title = title,
        shape = shape,
        titleCentered = titleCentered,
        icon = icon,
        iconSize = iconSize,
        titlePanelColor = titlePanelColor,
        onTitlePanelColor = onTitlePanelColor,
        messageHasMarkup = messageHasMarkup,
    ) {
        DialogButtonsRow(
            btnShape = btnShape,
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


@PreviewLightDarkDeviceSystemUi
@Composable
private fun PreviewAppAlertDialog() {
    AppTheme {
        AppAlertDialog(
            title = "Confirm",
            message = "Are you sure you want to continue?",
            positiveText = "Yes",
            negativeText = "No",
            neutralText = "Later",
            onPositiveClick = {},
            onNegativeClick = {},
            onNeutralClick = {},
            onDismissRequest = {},
            icon = { iconModifier ->
                Icon(Icons.Rounded.Error, contentDescription = null, modifier = iconModifier)
            },
        )
    }
}


@PreviewLightDarkDeviceSystemUi
@Composable
private fun PreviewErrorDialog() {
    AppTheme {
        ErrorDialog(
            title = "Confirm",
            message = "Are you sure you want to continue?",
            positiveText = "Retry",
//            negativeText = "Cancel",
            neutralText = "Later",
            onPositiveClick = {},
            onNegativeClick = {},
            onNeutralClick = {},
            onDismissRequest = {},
        )
    }
}
