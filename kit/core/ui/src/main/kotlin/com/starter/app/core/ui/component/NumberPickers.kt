package com.starter.app.core.ui.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.starter.app.core.designsystem.previews.PreviewLightDarkDevice
import com.starter.app.core.designsystem.theme.AppTheme
import com.starter.app.core.ui.component.buttons.FilledIconButtonBase
import com.starter.app.core.ui.component.textinputs.BaseOutlinedTextInput
import com.starter.app.core.designsystem.dimen.sdp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun NumberPicker(
    modifier: Modifier = Modifier,
    maxValue: Int = Int.MAX_VALUE,
    minValue: Int = Int.MIN_VALUE,
    value: Int = minValue,
    onValueChange: (Int) -> Unit,
    enabled: Boolean = true,
    editable: Boolean = true,
    shape: Shape = CircleShape,
    color: Color = MaterialTheme.colorScheme.tertiary,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    repeatDelayMillis: Long = 500L,
    repeatIntervalMillis: Long = 80L,
) {
    // The press comes from each button's own interaction stream. A `pointerInput` layered over the
    // button competes with its internal `clickable` for the same events and loses — that is what
    // kept hold-to-repeat dead. This is the same press the ripple is drawn from, so it cannot
    // disagree with what the driver sees.
    val incInteraction = remember { MutableInteractionSource() }
    val decInteraction = remember { MutableInteractionSource() }
    val incPressed by incInteraction.collectIsPressedAsState()
    val decPressed by decInteraction.collectIsPressedAsState()

    // True once a hold has actually stepped the value. The button's own `onClick` still fires when
    // the finger comes up, and without this a hold would land one past where the driver let go.
    var repeated by remember { mutableStateOf(false) }

    // The repeat loops outlive the recompositions their own steps cause, and the effects are keyed
    // on the press flag alone — so capturing [value] and [onValueChange] directly would freeze them
    // at the moment the finger went down, and every iteration would re-send the *same* number.
    // Holding would then be indistinguishable from a single tap. These read the current ones.
    val currentValue by rememberUpdatedState(value)
    val currentOnValueChange by rememberUpdatedState(onValueChange)

    // Auto-increment effect. `repeated` is cleared as the press begins, never as it ends — `onClick`
    // fires after the release and has to still see whether this press did any stepping.
    LaunchedEffect(incPressed) {
        if (!incPressed) return@LaunchedEffect
        repeated = false
        delay(repeatDelayMillis.milliseconds)
        while (incPressed && currentValue < maxValue) {
            repeated = true
            currentOnValueChange((currentValue + 1).coerceIn(minValue, maxValue))
            delay(repeatIntervalMillis.milliseconds)
        }
    }

    // Auto-decrement effect
    LaunchedEffect(decPressed) {
        if (!decPressed) return@LaunchedEffect
        repeated = false
        delay(repeatDelayMillis.milliseconds)
        while (decPressed && currentValue > minValue) {
            repeated = true
            currentOnValueChange((currentValue - 1).coerceIn(minValue, maxValue))
            delay(repeatIntervalMillis.milliseconds)
        }
    }


    // The two buttons are the whole width of this control, and M3 pads every icon button out to a
    // 48dp touch target — nine dead pixels a side plus a 48dp floor on the height. Dropping the
    // enforcement is what actually makes the picker compact; the buttons keep their own 30.sdp
    // minimum, which is what the rest of the app's icon buttons are sized at anyway.
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 1.dp) {
        BaseOutlinedTextInput(
            modifier = modifier,
            value = value.toString(),
            enabled = enabled,
            unfocusedColor = color,
            focusedColor = color,
            focusedBorderThickness = OutlinedTextFieldDefaults.UnfocusedBorderThickness,
            unfocusedBorderThickness = OutlinedTextFieldDefaults.UnfocusedBorderThickness,
            supportingText = supportingText,
            isError = isError,
            readOnly = !editable,
            // Not just "a number is one line": Material only centers the text vertically on a
            // single-line field, and hangs it from the top padding otherwise. The height here is
            // set by the two buttons — which *are* centered — so without this the number sits high.
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.NumberSigned),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            shape = shape,
            // The default 16dp all round plus a 280x56dp minimum is a form field's spacing, not a
            // stepper's — the number would float in the middle of a box far wider than it needs.
            contentPadding = PaddingValues(horizontal = 6.sdp, vertical = 2.sdp),
            minWidth = 80.sdp,
            minHeight = 36.sdp,
            onValueChange = { input ->
                if (input.isEmpty()) {
                    onValueChange(minValue.coerceAtLeast(0))
                    return@BaseOutlinedTextInput
                }

                input.toIntOrNull()?.let { newValue ->
                    onValueChange(newValue.coerceIn(minValue, maxValue))
                }
            },
            leadingIcon = {
                FilledIconButtonBase(
                    // Skipped when the hold already stepped the value — the release would otherwise
                    // add one more on top of where the driver stopped.
                    onClick = {
                        if (!repeated && value < maxValue) {
                            onValueChange((value + 1).coerceIn(minValue, maxValue))
                        }
                    },
                    enabled = enabled && value < maxValue,
                    icon = {
                        Icon(
                            modifier = it,
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Increment",
                        )
                    },
                    interactionSource = incInteraction,
                    modifier = Modifier.padding(all = 3.sdp),
                )
            },
            trailingIcon = {
                FilledIconButtonBase(
                    onClick = {
                        if (!repeated && value > minValue) {
                            onValueChange((value - 1).coerceIn(minValue, maxValue))
                        }
                    },
                    enabled = enabled && value > minValue,
                    icon = {
                        Icon(
                            modifier = it,
                            imageVector = Icons.Rounded.Remove,
                            contentDescription = "Decrement",
                        )
                    },
                    interactionSource = decInteraction,
                    modifier = Modifier.padding(all = 3.sdp),
                )
            },
        )
    }
}





@PreviewLightDarkDevice
@Composable
private fun PreviewNumberPicker() {
    AppTheme {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            NumberPicker(
                modifier = Modifier.wrapContentSize(),
                value = 0,
                onValueChange = {},
            )
            NumberPicker(
                modifier = Modifier.wrapContentSize(),
                value = 0,
                onValueChange = {},
                enabled = false
            )
            NumberPicker(
                modifier = Modifier.wrapContentSize(),
                value = 0,
                onValueChange = {},
                isError = true
            )

        }


    }
}