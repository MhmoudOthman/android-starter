package com.starter.app.core.ui.component.textinputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.starter.app.core.designsystem.previews.PreviewLightDarkDevice
import com.starter.app.core.designsystem.theme.AppTheme

/**
 * The app's outlined form field: `String` label, `ImageVector` icons, the text in a
 * [TextFieldState]. For a ViewModel that keeps the text as `String`, create the state with
 * [rememberForwardingTextFieldState].
 *
 * @param inputTransformation filters or changes what is typed (a max length, digits only).
 * @param onKeyboardAction runs on the IME action; leave it `null` and the action does its default
 *   (Next moves focus, Done closes the keyboard).
 */
@Composable
fun OutlinedTextInput(
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
    textStyle: TextStyle = LocalTextStyle.current,
    inputTransformation: InputTransformation? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
) {
    BaseOutlinedTextInput(
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
        textStyle = textStyle,
        inputTransformation = inputTransformation,
        keyboardOptions = keyboardOptions,
        onKeyboardAction = onKeyboardAction,
    )
}

/**
 * [OutlinedTextInput] for a password: a secure field (one line, kept out of the clipboard and
 * autocorrect) with a show/hide toggle in the trailing slot, which is why this variant takes no
 * `trailIcon`. The toggle appears once something is typed.
 */
@Composable
fun OutlinedTextInputPassword(
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
    textStyle: TextStyle = LocalTextStyle.current,
    inputTransformation: InputTransformation? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    BaseOutlinedTextInput(
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
        textStyle = textStyle,
        inputTransformation = inputTransformation,
        keyboardOptions = keyboardOptions.asPassword(),
        onKeyboardAction = onKeyboardAction,
    )
}

/** Shown: plain text. Hidden: masked, with the last typed character briefly visible. */
internal fun obfuscationFor(visible: Boolean): TextObfuscationMode =
    if (visible) TextObfuscationMode.Visible else TextObfuscationMode.RevealLastTyped

/** A password keyboard, with no suggestions or autocorrect learning the password. */
internal fun KeyboardOptions.asPassword(): KeyboardOptions =
    copy(keyboardType = KeyboardType.Password, autoCorrectEnabled = false)

/** The show/hide toggle, or nothing while the field is empty. */
internal fun passwordToggleSlot(
    state: TextFieldState,
    visible: Boolean,
    onToggle: () -> Unit,
): @Composable (() -> Unit)? =
    if (state.text.isEmpty()) {
        null
    } else {
        iconSlot(
            icon = if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
            contentDescription = if (visible) "Hide password" else "Show password",
            onClick = onToggle,
        )
    }

@PreviewLightDarkDevice
@Composable
fun PreviewOutlinedTextInput() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextInput(
                state = rememberTextFieldState(),
                label = "Label",
                supportText = "Supporting text",
                leadIcon = Icons.Rounded.Password,
                trailIcon = Icons.Rounded.Visibility,
                prefixText = "Prefix",
                suffixText = "Suffix"
            )
            OutlinedTextInput(
                state = rememberTextFieldState("Error"),
                isError = true,
                label = "Label",
                supportText = "Error message",
                leadIcon = Icons.Rounded.Password,
                trailIcon = Icons.Rounded.Visibility,
                prefixText = "Prefix",
                suffixText = "Suffix"
            )
            OutlinedTextInput(
                state = rememberTextFieldState("Disabled"),
                enabled = false,
                label = "Label",
                supportText = "Supporting text",
                leadIcon = Icons.Rounded.Password,
                trailIcon = Icons.Rounded.Visibility,
                prefixText = "Prefix",
                suffixText = "Suffix"
            )
            OutlinedTextInput(
                state = rememberTextFieldState("Read-only"),
                readOnly = true,
                label = "Label",
                supportText = "Supporting text",
                leadIcon = Icons.Rounded.Password,
                trailIcon = Icons.Rounded.Visibility,
                prefixText = "Prefix",
                suffixText = "Suffix"
            )
            OutlinedTextInputPassword(
                state = rememberTextFieldState("secret"),
                label = "Password",
                supportText = "Supporting text",
                leadIcon = Icons.Rounded.Password,
            )
        }
    }
}
