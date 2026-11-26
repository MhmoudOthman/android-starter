package com.starter.app.core.ui.component.textinputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.starter.app.core.designsystem.previews.PreviewLightDarkDevice
import com.starter.app.core.designsystem.theme.AppTheme

/**
 * The filled text field rounded on **all four corners, with no indicator line** — the container is
 * the whole field. Same plain-value API as [FilledTextInputTopRounded]; only the shape differs.
 *
 * The missing line is not an omission. The indicator is drawn as a full-width strip along the
 * bottom edge intersected with the container's outline, so a rounded bottom corner eats its ends
 * and leaves a short line with curved tips — the wider the [cornerSize], the less survives.
 * Rounding all four corners means dropping the line, and dropping it is what makes the rounding
 * read as deliberate rather than clipped.
 *
 * The cost is that focus has no shape to announce itself with: it reads only through the label,
 * cursor and icon colors. Where that is too quiet, pass a [containerColor] that stands out from the
 * surface behind it, or use [FilledTextInputTopRounded] and keep the line.
 *
 * @param cornerSize radius of all four corners. Accepts a percentage (`CornerSize(50)`) for a pill.
 * @param containerColor the field's background — the only thing drawing this field. Left
 *   unspecified, Material's own filled container color applies.
 * @param shadowElevation shadow cast by the field, drawn on the container alone so the supporting
 *   text stays outside it. Keep [containerColor] opaque or the shadow shows through, and expect it
 *   to all but disappear in a dark theme — a lighter container is the reliable way to lift a field
 *   in both. [FilledTextInputTopRounded] has no equivalent: a shadow under its flat bottom edge
 *   fights the indicator line.
 */
@Composable
fun FilledTextInputFullRounded(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    supportText: String? = null,
    prefixText: String? = null,
    suffixText: String? = null,
    leadIcon: ImageVector? = null,
    trailIcon: ImageVector? = null,
    leadIconOnClick: (() -> Unit)? = null,
    trailIconOnClick: (() -> Unit)? = null,
    leadIconDescription: String? = null,
    trailIconDescription: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    cornerSize: CornerSize = MaterialTheme.shapes.medium.topStart,
    containerColor: Color = Color.Unspecified,
    shadowElevation: Dp = 0.dp,
    textStyle: TextStyle = LocalTextStyle.current,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    inputTransformation: InputTransformation? = null,
    onKeyboardAction: KeyboardActionHandler? = null,
) {
    BaseFilledTextInputFullRounded(
        state = state,
        modifier = modifier,
        label = textSlot(label),
        supportingText = textSlot(supportText, FontWeight.SemiBold),
        placeholder = textSlot(placeholder),
        leadingIcon = iconSlot(leadIcon, leadIconDescription, leadIconOnClick),
        trailingIcon = iconSlot(trailIcon, trailIconDescription, trailIconOnClick),
        prefix = textSlot(prefixText),
        suffix = textSlot(suffixText),
        enabled = enabled,
        readOnly = readOnly,
        isError = isError,
        lineLimits = lineLimitsOf(singleLine, minLines, maxLines),
        cornerSize = cornerSize,
        containerColor = containerColor,
        shadowElevation = shadowElevation,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        inputTransformation = inputTransformation,
        onKeyboardAction = onKeyboardAction,
    )
}

@Composable
fun FilledTextInputFullRoundedPassword(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    supportText: String? = null,
    prefixText: String? = null,
    suffixText: String? = null,
    leadIcon: ImageVector? = null,
    leadIconOnClick: (() -> Unit)? = null,
    leadIconDescription: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    cornerSize: CornerSize = MaterialTheme.shapes.medium.topStart,
    containerColor: Color = Color.Unspecified,
    shadowElevation: Dp = 0.dp,
    textStyle: TextStyle = LocalTextStyle.current,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    inputTransformation: InputTransformation? = null,
    onKeyboardAction: KeyboardActionHandler? = null,
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    BaseFilledTextInputFullRounded(
        state = state,
        modifier = modifier,
        label = textSlot(label),
        supportingText = textSlot(supportText, FontWeight.SemiBold),
        placeholder = textSlot(placeholder),
        leadingIcon = iconSlot(leadIcon, leadIconDescription, leadIconOnClick),
        trailingIcon = passwordToggleSlot(state, passwordVisible) { passwordVisible = !passwordVisible },
        obfuscation = obfuscationFor(passwordVisible),
        prefix = textSlot(prefixText),
        suffix = textSlot(suffixText),
        enabled = enabled,
        readOnly = readOnly,
        isError = isError,
        cornerSize = cornerSize,
        containerColor = containerColor,
        shadowElevation = shadowElevation,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions.asPassword(),
        inputTransformation = inputTransformation,
        onKeyboardAction = onKeyboardAction,
    )
}

@PreviewLightDarkDevice
@Composable
fun PreviewFilledTextInputFullRounded() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FilledTextInputFullRounded(
                state = rememberTextFieldState(""),
                label = "Label",
                supportText = "Supporting text",
                leadIcon = Icons.Rounded.Password,
                trailIcon = Icons.Rounded.Visibility,
                prefixText = "Prefix",
                suffixText = "Suffix"
            )
            FilledTextInputFullRounded(
                isError = true,
                state = rememberTextFieldState("Error"),
                label = "Label",
                supportText = "Error message",
                leadIcon = Icons.Rounded.Password,
                trailIcon = Icons.Rounded.Visibility,
                prefixText = "Prefix",
                suffixText = "Suffix"
            )
            FilledTextInputFullRounded(
                enabled = false,
                state = rememberTextFieldState("Disabled"),
                label = "Label",
                supportText = "Supporting text",
                leadIcon = Icons.Rounded.Password,
                trailIcon = Icons.Rounded.Visibility,
                prefixText = "Prefix",
                suffixText = "Suffix"
            )
            FilledTextInputFullRounded(
                readOnly = true,
                state = rememberTextFieldState("Read-only"),
                label = "Label",
                supportText = "Supporting text",
                leadIcon = Icons.Rounded.Password,
                trailIcon = Icons.Rounded.Visibility,
                prefixText = "Prefix",
                suffixText = "Suffix"
            )
            // Where this shape earns its keep: a pill with no label and nothing but the container.
            FilledTextInputFullRounded(
                state = rememberTextFieldState("Search"),
                placeholder = "Placeholder",
                leadIcon = Icons.Rounded.Search,
                cornerSize = CornerSize(50),
                singleLine = true,
            )
            // The elevated case carries supporting text on purpose: the shadow has to stop at the
            // container and leave that line alone.
            FilledTextInputFullRounded(
                state = rememberTextFieldState("Elevated"),
                placeholder = "Placeholder",
                supportText = "The shadow stops at the container",
                leadIcon = Icons.Rounded.Search,
                cornerSize = CornerSize(50),
                shadowElevation = 6.dp,
                singleLine = true,
            )
            FilledTextInputFullRoundedPassword(
                state = rememberTextFieldState("secret"),
                label = "Password",
                supportText = "Supporting text",
                leadIcon = Icons.Rounded.Password,
            )
        }
    }
}
