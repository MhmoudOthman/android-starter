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

/**
 * Wraps [icon] in a slot, or returns null when [icon] is null.
 *
 * A button only when there is something to do: with no [onClick] the icon is plain decoration.
 * Every icon used to be a button, a no-op one when nothing was passed — focusable, announced by
 * TalkBack as a button, and the place keyboard Next landed instead of the next field (on a sign-in
 * form, Next from the username focused its account icon).
 */
internal fun iconSlot(
    icon: ImageVector?,
    contentDescription: String?,
    onClick: (() -> Unit)? = null,
): @Composable (() -> Unit)? =
    icon?.let { vector ->
        if (onClick == null) {
            { Icon(imageVector = vector, contentDescription = contentDescription) }
        } else {
            {
                IconButtonBase(
                    onClick = onClick,
                    icon = { modifier: Modifier ->
                        Icon(
                            modifier = modifier,
                            imageVector = vector,
                            contentDescription = contentDescription,
                        )
                    },
                )
            }
        }
    }
