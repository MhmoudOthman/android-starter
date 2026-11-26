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
import androidx.compose.ui.unit.dp
import com.starter.app.core.designsystem.previews.PreviewLightDarkDevice
import com.starter.app.core.designsystem.theme.AppTheme

/**
 * The filled text field with its **top two corners rounded and its indicator line intact** — the
 * classic form field. [OutlinedTextInput]'s filled twin, sharing its plain-value API: `String`
 * label, `ImageVector` icons.
 *
 * This is the shape to reach for by default. [FilledTextInputFullRounded] is the other half of the
 * pair, and the choice between them is not cosmetic — see its file for what a rounded bottom corner
 * does to the line.
 *
 * @param cornerSize radius of the top two corners; the bottom two stay square, so the indicator
 *   runs the full width with square ends however wide this gets. Defaults to the theme's medium
 *   corner — the same one [OutlinedTextInput] uses — where Material's own filled default is the
 *   much smaller 4dp `TextFieldDefaults.shape`.
 * @param containerColor the field's background. Left unspecified, Material's own filled container
 *   color applies.
 */
@Composable
fun FilledTextInputTopRounded(
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
    textStyle: TextStyle = LocalTextStyle.current,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    inputTransformation: InputTransformation? = null,
    onKeyboardAction: KeyboardActionHandler? = null,
) {
    BaseFilledTextInputTopRounded(
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
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        inputTransformation = inputTransformation,
        onKeyboardAction = onKeyboardAction,
    )
}

@Composable
fun FilledTextInputTopRoundedPassword(
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
    textStyle: TextStyle = LocalTextStyle.current,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    inputTransformation: InputTransformation? = null,
    onKeyboardAction: KeyboardActionHandler? = null,
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    BaseFilledTextInputTopRounded(
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
        textStyle = textStyle,
        keyboardOptions = keyboardOptions.asPassword(),
        inputTransformation = inputTransformation,
        onKeyboardAction = onKeyboardAction,
    )
}

@PreviewLightDarkDevice
@Composable
fun PreviewFilledTextInputTopRounded() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FilledTextInputTopRounded(
                state = rememberTextFieldState(""),
                label = "Label",
                supportText = "Supporting text",
                leadIcon = Icons.Rounded.Password,
                trailIcon = Icons.Rounded.Visibility,
                prefixText = "Prefix",
                suffixText = "Suffix"
            )
            FilledTextInputTopRounded(
                isError = true,
                state = rememberTextFieldState("Error"),
                label = "Label",
                supportText = "Error message",
                leadIcon = Icons.Rounded.Password,
                trailIcon = Icons.Rounded.Visibility,
                prefixText = "Prefix",
                suffixText = "Suffix"
            )
            FilledTextInputTopRounded(
                enabled = false,
                state = rememberTextFieldState("Disabled"),
                label = "Label",
                supportText = "Supporting text",
                leadIcon = Icons.Rounded.Password,
                trailIcon = Icons.Rounded.Visibility,
                prefixText = "Prefix",
                suffixText = "Suffix"
            )
            FilledTextInputTopRounded(
                readOnly = true,
                state = rememberTextFieldState("Read-only"),
                label = "Label",
                supportText = "Supporting text",
                leadIcon = Icons.Rounded.Password,
                trailIcon = Icons.Rounded.Visibility,
                prefixText = "Prefix",
                suffixText = "Suffix"
            )
            FilledTextInputTopRounded(
                state = rememberTextFieldState("No label"),
                placeholder = "Placeholder",
                supportText = "Supporting text",
            )
            // The point of the knob: the line stays full-width no matter how wide the corner gets.
            FilledTextInputTopRounded(
                state = rememberTextFieldState("Wide corner"),
                label = "Label",
                cornerSize = CornerSize(24.dp),
            )
            FilledTextInputTopRoundedPassword(
                state = rememberTextFieldState("secret"),
                label = "Password",
                supportText = "Supporting text",
                leadIcon = Icons.Rounded.Password,
            )
        }
    }
}
