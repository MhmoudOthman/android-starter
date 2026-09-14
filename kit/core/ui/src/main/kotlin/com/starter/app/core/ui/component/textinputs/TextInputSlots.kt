package com.starter.app.core.ui.component.textinputs

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.starter.app.core.ui.component.buttons.IconButtonBase

/**
 * The `String`/[ImageVector] → slot adapters shared by [OutlinedTextInput],
 * [FilledTextInputTopRounded] and [FilledTextInputFullRounded].
 * The bases below take composable slots, the public wrappers take plain values; this is the one
 * translation between them, kept in a single place so the two shapes cannot drift apart on how a
 * label or an icon is built.
 */

/** Wraps [text] in a [Text] slot, or returns null when [text] is null. */
fun textSlot(
    text: String?,
    fontWeight: FontWeight = FontWeight.Normal,
): @Composable (() -> Unit)? =
    text?.let { { Text(text = it, fontWeight = fontWeight) } }

/** Wraps [icon] in an [Icon] slot, or returns null when [icon] is null. */
internal fun iconSlot(
    icon: ImageVector?,
    contentDescription: String?,
    onClick: () -> Unit = {},
): @Composable (() -> Unit)? =
    icon?.let { { IconButtonBase(
        onClick = onClick,
        icon = { modifier: Modifier ->
        Icon(
            modifier = modifier,
            imageVector = it,
            contentDescription = contentDescription
        )
    }
    ) }}
