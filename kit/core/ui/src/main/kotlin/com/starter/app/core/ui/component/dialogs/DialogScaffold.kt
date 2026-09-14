package com.starter.app.core.ui.component.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.starter.app.core.ui.component.buttons.ElevatedButtonBase
import com.starter.app.core.ui.component.buttons.OutlinedButtonBase
import com.starter.app.core.ui.component.buttons.TextButtonBase
import com.starter.app.core.designsystem.dimen.sdp

/**
 * Shared dialog chrome: the [Surface] (background, shape, elevation), the optional full-bleed
 * [DialogTitlePanel] header, and the padded content column that starts with [message]. Callers
 * supply whatever comes after the message (buttons, a progress indicator, …) via [content], and
 * whatever comes *before* it (an animation, an illustration) via [header] — [header] sits inside
 * the padded column rather than in the title panel, which is full-bleed and sized for an icon.
 *
 * [messageHasMarkup] runs [message] through an HTML parser so a string can carry `<b>`. It is
 * opt-in rather than always-on because most messages here are **relayed from the backend**, and
 * putting a server string through a parser that eats `<`, `>` and `&` mangles it silently. Only
 * pass it for a message the app authored itself. Note the tags have to reach this composable
 * literally: `stringResource` drops real styling spans, so write them escaped in the resource
 * (`&lt;b&gt;`), not as live XML markup.
 */
@Composable
internal fun DialogScaffold(
    message: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    titleCentered: Boolean = false,
    icon: (@Composable (Modifier) -> Unit)? = null,
    iconSize: Dp = 20.sdp,
    titlePanelColor: Color = Color.Unspecified,
    onTitlePanelColor: Color = MaterialTheme.colorScheme.onSurface,
    shape: Shape =MaterialTheme.shapes.extraLarge,
    messageTextAlign: TextAlign? = null,
    messageFontSize: TextUnit = TextUnit.Unspecified,
    messageHasMarkup: Boolean = false,
    header: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    // Wrap content in a Surface to provide background, shape, and elevation,
    // as BasicAlertDialog does not provide these by default.
    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.sdp,
        modifier = modifier,
    ) {
        Column {
            DialogTitlePanel(
                title = title,
                titleCentered = titleCentered,
                icon = icon,
                iconSize = iconSize,
                titlePanelColor = titlePanelColor,
                onTitlePanelColor = onTitlePanelColor,
            )

            Column(
                modifier = Modifier.padding(16.sdp , vertical = 8.sdp),
                verticalArrangement = Arrangement.spacedBy(8.sdp),
            ) {
                header?.invoke(this)

                Text(
                    text = if (messageHasMarkup) {
                        AnnotatedString.fromHtml(message)
                    } else {
                        AnnotatedString(message)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = messageTextAlign,
                    fontSize = messageFontSize,
                    // Only widened when the caller asked for an alignment: a Text that wraps its
                    // content has nothing to align within, so the parameter would do nothing.
                    modifier = if (messageTextAlign != null) Modifier.fillMaxWidth() else Modifier,
                )

                content()
            }
        }
    }
}

/**
 * Full-bleed header row with an optional [icon] and [title]. Renders nothing when both are absent.
 */
@Composable
internal fun DialogTitlePanel(
    title: String?,
    titleCentered: Boolean,
    icon: (@Composable (Modifier) -> Unit)?,
    iconSize: Dp,
    titlePanelColor: Color,
    onTitlePanelColor: Color,
) {
    if (title.isNullOrBlank() && icon == null) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(titlePanelColor)
            .padding(horizontal = 12.sdp, vertical = 6.sdp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(
            8.sdp,
            if (titleCentered) Alignment.CenterHorizontally else Alignment.Start,
        ),
    ) {
        // Provide onTitlePanelColor as the content color so the icon (which tints from
        // LocalContentColor) and the title always share the same color.
        CompositionLocalProvider(LocalContentColor provides onTitlePanelColor) {
            // Enforce a fixed icon size regardless of what the caller passes, by handing the
            // sized Modifier down to the icon slot.
            icon?.invoke(Modifier.size(iconSize))
            title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
    }
}

/**
 * The neutral / negative / positive action row. Each button is only shown when both its text and
 * click handler are provided; otherwise a weighted [Spacer] holds its place so the remaining
 * buttons keep their layout.
 */
@Composable
internal fun DialogButtonsRow(
    btnShape: Shape = MaterialTheme.shapes.medium,
    positiveText: String?,
    onPositiveClick: (() -> Unit)?,
    negativeText: String?,
    onNegativeClick: (() -> Unit)?,
    neutralText: String?,
    onNeutralClick: (() -> Unit)?,
    positiveBtnColor: Color,
    positiveOnBtnColor: Color,
    negativeBtnColor: Color,
    negativeOnBtnColor: Color,
    neutralBtnColor: Color,
    neutralOnBtnColor: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.sdp),
    ) {
        if (neutralText != null && onNeutralClick != null) {
            OutlinedButtonBase(
                text = neutralText,
                modifier = Modifier.weight(1f),
                shape = btnShape,
                loadingIndicatorColor = neutralOnBtnColor,
                contentColor = neutralBtnColor,
                onClick = onNeutralClick,
            )
        }
        if (negativeText != null && onNegativeClick != null) {
            TextButtonBase(
                text = negativeText,
                modifier = Modifier.weight(1f),
                shape = btnShape,
                loadingIndicatorColor = negativeOnBtnColor,
                contentColor = negativeBtnColor,
                onClick = onNegativeClick,
            )
        }
        if (positiveText != null && onPositiveClick != null) {
            ElevatedButtonBase(
                text = positiveText,
                modifier = Modifier.weight(1f),
                shape = btnShape,
                containerColor = positiveBtnColor,
                contentColor = positiveOnBtnColor,
                onClick = onPositiveClick,
            )
        }
    }
}
