package com.starter.app.core.ui.component.textinputs

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.text.BasicSecureTextField
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldLabelScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * The app's text fields, on Compose's state-based text API: the text lives in a [TextFieldState],
 * not in a `value` + `onValueChange` pair. The value-based overloads and `VisualTransformation`
 * are deprecated from Compose 1.13; their replacements are [InputTransformation] (filter what is
 * typed), [OutputTransformation] (change how it is shown) and, for passwords,
 * [BasicSecureTextField] with a [TextObfuscationMode].
 *
 * Built on the Basic fields plus Material's own `decorator`s rather than on `OutlinedTextField` /
 * `TextField`, for the reason the kit always had: those hard-code a 280x56dp floor, and
 * `NumberPicker` is not a form field. Every default here is the value the Material field would
 * have used, so a caller that passes none renders identically.
 *
 * A ViewModel that keeps its text as `String` (MVI state) bridges in with
 * [rememberForwardingTextFieldState].
 */

/**
 * `singleLine` / `minLines` / `maxLines` as the state API's [TextFieldLineLimits]. The public
 * wrappers keep the three familiar knobs; this is the one place they are translated.
 */
fun lineLimitsOf(singleLine: Boolean, minLines: Int = 1, maxLines: Int = Int.MAX_VALUE): TextFieldLineLimits =
    if (singleLine) TextFieldLineLimits.SingleLine else TextFieldLineLimits.MultiLine(minLines, maxLines)

/** Material's decorators take a label scoped to `TextFieldLabelScope`; the kit's slots are plain. */
private fun (@Composable () -> Unit)?.asLabelSlot(): (@Composable TextFieldLabelScope.() -> Unit)? =
    this?.let { content -> { content() } }

/**
 * The text field itself, shared by the outlined and filled bases: a plain [BasicTextField], or a
 * [BasicSecureTextField] when [obfuscation] is given (a password — always one line, never
 * transformed for display, kept out of the clipboard and autocorrect).
 */
@Composable
private fun TextInputField(
    state: TextFieldState,
    modifier: Modifier,
    enabled: Boolean,
    readOnly: Boolean,
    textStyle: TextStyle,
    fieldColors: TextFieldColors,
    isError: Boolean,
    inputTransformation: InputTransformation?,
    outputTransformation: OutputTransformation?,
    obfuscation: TextObfuscationMode?,
    keyboardOptions: KeyboardOptions,
    onKeyboardAction: KeyboardActionHandler?,
    lineLimits: TextFieldLineLimits,
    interactionSource: MutableInteractionSource,
    decorator: TextFieldDecorator,
) {
    val focused by interactionSource.collectIsFocusedAsState()
    val textColor = textStyle.color.takeOrElse {
        when {
            !enabled -> fieldColors.disabledTextColor
            isError -> fieldColors.errorTextColor
            focused -> fieldColors.focusedTextColor
            else -> fieldColors.unfocusedTextColor
        }
    }
    val mergedStyle = textStyle.merge(TextStyle(color = textColor))
    val cursor = SolidColor(if (isError) fieldColors.errorCursorColor else fieldColors.cursorColor)

    CompositionLocalProvider(LocalTextSelectionColors provides fieldColors.textSelectionColors) {
        if (obfuscation != null) {
            BasicSecureTextField(
                state = state,
                modifier = modifier,
                enabled = enabled,
                readOnly = readOnly,
                inputTransformation = inputTransformation,
                textStyle = mergedStyle,
                keyboardOptions = keyboardOptions,
                onKeyboardAction = onKeyboardAction,
                interactionSource = interactionSource,
                cursorBrush = cursor,
                decorator = decorator,
                textObfuscationMode = obfuscation,
            )
        } else {
            BasicTextField(
                state = state,
                modifier = modifier,
                enabled = enabled,
                readOnly = readOnly,
                inputTransformation = inputTransformation,
                textStyle = mergedStyle,
                keyboardOptions = keyboardOptions,
                onKeyboardAction = onKeyboardAction,
                lineLimits = lineLimits,
                interactionSource = interactionSource,
                cursorBrush = cursor,
                outputTransformation = outputTransformation,
                decorator = decorator,
            )
        }
    }
}

/**
 * The app's outlined text field.
 *
 * @param obfuscation non-null makes this a password field (see `OutlinedTextInputPassword`):
 *   [lineLimits] and [outputTransformation] are then ignored — a secure field is one line and is
 *   never re-shaped for display.
 * @param contentPadding space between the input text and the icons around it (or the container
 *   edges when there are no icons). Note the decorator subtracts the icons' own touch-target
 *   padding from the horizontal values, so with icons present anything under ~12dp reads as 0.
 * @param minWidth minimum width the field settles at when nothing else constrains it.
 * @param minHeight as [minWidth], for the height.
 */
@Composable
fun BaseOutlinedTextInput(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    inputTransformation: InputTransformation? = null,
    outputTransformation: OutputTransformation? = null,
    obfuscation: TextObfuscationMode? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = MaterialTheme.shapes.medium,
    contentPadding: PaddingValues = OutlinedTextFieldDefaults.contentPaddingWithoutLabel(),
    minWidth: Dp = OutlinedTextFieldDefaults.MinWidth,
    minHeight: Dp = OutlinedTextFieldDefaults.MinHeight,
    focusedColor: Color? = null,
    unfocusedColor: Color? = null,
    disabledColor: Color? = null,
    errorColor: Color? = null,
    disabledOpacity: Float? = null,
    focusedBorderThickness: Dp = OutlinedTextFieldDefaults.FocusedBorderThickness,
    unfocusedBorderThickness: Dp = OutlinedTextFieldDefaults.UnfocusedBorderThickness
) {
    var colors = TextInputColors.default()
    if (focusedColor != null) colors = colors.copy(focusedColor = focusedColor)
    if (unfocusedColor != null) colors = colors.copy(unfocusedColor = unfocusedColor)
    if (disabledColor != null) colors = colors.copy(disabledColor = disabledColor)
    if (errorColor != null) colors = colors.copy(errorColor = errorColor)
    if (disabledOpacity != null) colors = colors.copy(disabledOpacity = disabledOpacity)
    val fieldColors = outlinedTextFieldColors(colors)

    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val effectiveLineLimits = if (obfuscation != null) TextFieldLineLimits.SingleLine else lineLimits
    val effectiveOutput = if (obfuscation != null) null else outputTransformation
    // The field's "invalid input" announcement for screen readers. Our own string rather than
    // Compose's private `default_error_message`, which lint rightly refuses (PrivateResource).
    val errorMessage = stringResource(com.starter.app.core.designsystem.R.string.invalid_input)

    TextInputField(
        state = state,
        modifier = modifier
            .labelCutoutPadding(hasLabel = label != null)
            .semantics { if (isError) error(errorMessage) }
            .defaultMinSize(minWidth = minWidth, minHeight = minHeight),
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        fieldColors = fieldColors,
        isError = isError,
        inputTransformation = inputTransformation,
        outputTransformation = effectiveOutput,
        obfuscation = obfuscation,
        keyboardOptions = keyboardOptions,
        onKeyboardAction = onKeyboardAction,
        lineLimits = effectiveLineLimits,
        interactionSource = interactionSource,
        decorator = OutlinedTextFieldDefaults.decorator(
            state = state,
            enabled = enabled,
            lineLimits = effectiveLineLimits,
            outputTransformation = effectiveOutput,
            interactionSource = interactionSource,
            label = label.asLabelSlot(),
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            prefix = prefix,
            suffix = suffix,
            supportingText = supportingText,
            isError = isError,
            colors = fieldColors,
            contentPadding = contentPadding,
            container = {
                OutlinedTextFieldDefaults.Container(
                    enabled = enabled,
                    isError = isError,
                    interactionSource = interactionSource,
                    colors = fieldColors,
                    shape = shape,
                    focusedBorderThickness = focusedBorderThickness,
                    unfocusedBorderThickness = unfocusedBorderThickness,
                )
            },
        ),
    )
}

/**
 * The app's filled text field — the same field as [BaseOutlinedTextInput] with a tonal container
 * and a bottom indicator line instead of a border.
 *
 * Two things differ from the outlined base, both because the filled label sits *inside* the
 * container rather than straddling the top edge: there is no cutout padding to reserve
 * (`labelCutoutPadding` is outlined-only), and the default [contentPadding] switches on whether a
 * [label] is present — a labelled filled field packs the label and the input into the same height,
 * so it needs the tighter vertical padding or the two collide.
 *
 * @param obfuscation non-null makes this a password field; see [BaseOutlinedTextInput].
 * @param shape defaults to [TextFieldDefaults.shape] — top corners only — rather than to
 *   `shapes.medium` like the outlined field, because the indicator line is clipped to this shape
 *   and rounded bottom corners cut its ends off.
 * @param containerColor the field's background in every state. Left unspecified, Material's own
 *   filled container color applies.
 * @param shadowElevation shadow cast by the field. It rides on the **container's** own modifier,
 *   not on the text field's: the decorator draws the supporting text below the container, so a
 *   shadow applied further out would trace the field and its supporting line together. Only
 *   [BaseFilledTextInputFullRounded] exposes this.
 */
@Composable
fun BaseFilledTextInput(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    inputTransformation: InputTransformation? = null,
    outputTransformation: OutputTransformation? = null,
    obfuscation: TextObfuscationMode? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = TextFieldDefaults.shape,
    contentPadding: PaddingValues = if (label == null) {
        TextFieldDefaults.contentPaddingWithoutLabel()
    } else {
        TextFieldDefaults.contentPaddingWithLabel()
    },
    minWidth: Dp = TextFieldDefaults.MinWidth,
    minHeight: Dp = TextFieldDefaults.MinHeight,
    focusedColor: Color? = null,
    unfocusedColor: Color? = null,
    disabledColor: Color? = null,
    errorColor: Color? = null,
    disabledOpacity: Float? = null,
    containerColor: Color = Color.Unspecified,
    shadowElevation: Dp = 0.dp,
    focusedIndicatorThickness: Dp = TextFieldDefaults.FocusedIndicatorThickness,
    unfocusedIndicatorThickness: Dp = TextFieldDefaults.UnfocusedIndicatorThickness
) {
    var colors = TextInputColors.default()
    if (focusedColor != null) colors = colors.copy(focusedColor = focusedColor)
    if (unfocusedColor != null) colors = colors.copy(unfocusedColor = unfocusedColor)
    if (disabledColor != null) colors = colors.copy(disabledColor = disabledColor)
    if (errorColor != null) colors = colors.copy(errorColor = errorColor)
    if (disabledOpacity != null) colors = colors.copy(disabledOpacity = disabledOpacity)
    val fieldColors = filledTextFieldColors(colors, containerColor)

    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val effectiveLineLimits = if (obfuscation != null) TextFieldLineLimits.SingleLine else lineLimits
    val effectiveOutput = if (obfuscation != null) null else outputTransformation
    val errorMessage = stringResource(com.starter.app.core.designsystem.R.string.invalid_input)

    TextInputField(
        state = state,
        modifier = modifier
            .semantics(mergeDescendants = true) {}
            .semantics { if (isError) error(errorMessage) }
            .defaultMinSize(minWidth = minWidth, minHeight = minHeight),
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        fieldColors = fieldColors,
        isError = isError,
        inputTransformation = inputTransformation,
        outputTransformation = effectiveOutput,
        obfuscation = obfuscation,
        keyboardOptions = keyboardOptions,
        onKeyboardAction = onKeyboardAction,
        lineLimits = effectiveLineLimits,
        interactionSource = interactionSource,
        decorator = TextFieldDefaults.decorator(
            state = state,
            enabled = enabled,
            lineLimits = effectiveLineLimits,
            outputTransformation = effectiveOutput,
            interactionSource = interactionSource,
            label = label.asLabelSlot(),
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            prefix = prefix,
            suffix = suffix,
            supportingText = supportingText,
            isError = isError,
            colors = fieldColors,
            contentPadding = contentPadding,
            container = {
                TextFieldDefaults.Container(
                    enabled = enabled,
                    isError = isError,
                    interactionSource = interactionSource,
                    // A zero elevation returns the modifier untouched, so the shadow costs
                    // nothing on the fields that do not ask for one.
                    modifier = Modifier.shadow(shadowElevation, shape),
                    colors = fieldColors,
                    shape = shape,
                    focusedIndicatorLineThickness = focusedIndicatorThickness,
                    unfocusedIndicatorLineThickness = unfocusedIndicatorThickness,
                )
            },
        ),
    )
}

/**
 * [BaseFilledTextInput] rounded on **all four corners, with no indicator line** — the container is
 * the whole field. A rounded bottom corner would eat the ends of the indicator line, so rounding
 * all four means dropping it; focus then reads only through the label, cursor and icon colors.
 *
 * @param cornerSize radius of all four corners. Accepts a percentage (`CornerSize(50)`) for a pill.
 * @param shadowElevation shadow cast by the field, drawn on the container alone. This is the one
 *   shape it belongs on: with no line and every corner rounded, the container *is* the field.
 */
@Composable
fun BaseFilledTextInputFullRounded(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    inputTransformation: InputTransformation? = null,
    outputTransformation: OutputTransformation? = null,
    obfuscation: TextObfuscationMode? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
    interactionSource: MutableInteractionSource? = null,
    cornerSize: CornerSize = MaterialTheme.shapes.medium.topStart,
    contentPadding: PaddingValues = if (label == null) {
        TextFieldDefaults.contentPaddingWithoutLabel()
    } else {
        TextFieldDefaults.contentPaddingWithLabel()
    },
    minWidth: Dp = TextFieldDefaults.MinWidth,
    minHeight: Dp = TextFieldDefaults.MinHeight,
    focusedColor: Color? = null,
    unfocusedColor: Color? = null,
    disabledColor: Color? = null,
    errorColor: Color? = null,
    disabledOpacity: Float? = null,
    containerColor: Color = Color.Unspecified,
    shadowElevation: Dp = 0.dp,
) = BaseFilledTextInput(
    state = state,
    modifier = modifier,
    enabled = enabled,
    readOnly = readOnly,
    textStyle = textStyle,
    label = label,
    placeholder = placeholder,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    prefix = prefix,
    suffix = suffix,
    supportingText = supportingText,
    isError = isError,
    inputTransformation = inputTransformation,
    outputTransformation = outputTransformation,
    obfuscation = obfuscation,
    keyboardOptions = keyboardOptions,
    onKeyboardAction = onKeyboardAction,
    lineLimits = lineLimits,
    interactionSource = interactionSource,
    shape = RoundedCornerShape(cornerSize),
    contentPadding = contentPadding,
    minWidth = minWidth,
    minHeight = minHeight,
    focusedColor = focusedColor,
    unfocusedColor = unfocusedColor,
    disabledColor = disabledColor,
    errorColor = errorColor,
    disabledOpacity = disabledOpacity,
    containerColor = containerColor,
    shadowElevation = shadowElevation,
    focusedIndicatorThickness = 0.dp,
    unfocusedIndicatorThickness = 0.dp,
)

/**
 * [BaseFilledTextInput] rounded on the **top two corners only**, keeping the indicator line — the
 * classic filled field, at whatever radius the design calls for. The bottom edge stays square, so
 * the line runs the full width with square ends however wide [cornerSize] gets.
 */
@Composable
fun BaseFilledTextInputTopRounded(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    inputTransformation: InputTransformation? = null,
    outputTransformation: OutputTransformation? = null,
    obfuscation: TextObfuscationMode? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
    interactionSource: MutableInteractionSource? = null,
    cornerSize: CornerSize = MaterialTheme.shapes.medium.topStart,
    contentPadding: PaddingValues = if (label == null) {
        TextFieldDefaults.contentPaddingWithoutLabel()
    } else {
        TextFieldDefaults.contentPaddingWithLabel()
    },
    minWidth: Dp = TextFieldDefaults.MinWidth,
    minHeight: Dp = TextFieldDefaults.MinHeight,
    focusedColor: Color? = null,
    unfocusedColor: Color? = null,
    disabledColor: Color? = null,
    errorColor: Color? = null,
    disabledOpacity: Float? = null,
    containerColor: Color = Color.Unspecified,
    focusedIndicatorThickness: Dp = TextFieldDefaults.FocusedIndicatorThickness,
    unfocusedIndicatorThickness: Dp = TextFieldDefaults.UnfocusedIndicatorThickness
) = BaseFilledTextInput(
    state = state,
    modifier = modifier,
    enabled = enabled,
    readOnly = readOnly,
    textStyle = textStyle,
    label = label,
    placeholder = placeholder,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    prefix = prefix,
    suffix = suffix,
    supportingText = supportingText,
    isError = isError,
    inputTransformation = inputTransformation,
    outputTransformation = outputTransformation,
    obfuscation = obfuscation,
    keyboardOptions = keyboardOptions,
    onKeyboardAction = onKeyboardAction,
    lineLimits = lineLimits,
    interactionSource = interactionSource,
    shape = RoundedCornerShape(
        topStart = cornerSize,
        topEnd = cornerSize,
        bottomEnd = ZeroCornerSize,
        bottomStart = ZeroCornerSize,
    ),
    contentPadding = contentPadding,
    minWidth = minWidth,
    minHeight = minHeight,
    focusedColor = focusedColor,
    unfocusedColor = unfocusedColor,
    disabledColor = disabledColor,
    errorColor = errorColor,
    disabledOpacity = disabledOpacity,
    containerColor = containerColor,
    focusedIndicatorThickness = focusedIndicatorThickness,
    unfocusedIndicatorThickness = unfocusedIndicatorThickness,
)

/**
 * The label straddles the top border, so the field reserves half a label line above itself or the
 * cutout is clipped. `OutlinedTextField` does this internally; going through
 * Material's `decorator` — which is what buys the `contentPadding` knob — means
 * doing it here instead.
 */
@Composable
private fun Modifier.labelCutoutPadding(hasLabel: Boolean): Modifier {
    if (!hasLabel) return this
    val lineHeight = MaterialTheme.typography.bodySmall.lineHeight
    val halfLabelHeight = with(LocalDensity.current) {
        (if (lineHeight.isSp) lineHeight else 16.sp).toDp() / 2
    }
    // Merged at the start of the chain so the padding counts as part of the text field.
    return this
        .semantics(mergeDescendants = true) {}
        .padding(top = halfLabelHeight)
}

@Composable
fun outlinedTextFieldColors(colors: TextInputColors = TextInputColors.default()): TextFieldColors =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.focusedColor,
        unfocusedBorderColor = colors.unfocusedColor,
        errorBorderColor = colors.errorColor,

        disabledBorderColor = colors.disabledColor.copy(alpha = colors.disabledOpacity),
        focusedLabelColor = colors.focusedColor,
        unfocusedLabelColor = colors.unfocusedColor,
        errorLabelColor = colors.errorColor,
        disabledLabelColor = colors.disabledColor.copy(alpha = colors.disabledOpacity),

        focusedLeadingIconColor = colors.focusedColor,
        unfocusedLeadingIconColor = colors.unfocusedColor,
        errorLeadingIconColor = colors.errorColor,
        disabledLeadingIconColor = colors.disabledColor.copy(alpha = colors.disabledOpacity),

        focusedTrailingIconColor = colors.focusedColor,
        unfocusedTrailingIconColor = colors.unfocusedColor,
        errorTrailingIconColor = colors.errorColor,
        disabledTrailingIconColor = colors.disabledColor.copy(alpha = colors.disabledOpacity),

        focusedPrefixColor = colors.focusedColor,
        unfocusedPrefixColor = colors.unfocusedColor,
        errorPrefixColor = colors.errorColor,
        disabledPrefixColor = colors.disabledColor.copy(alpha = colors.disabledOpacity),

        focusedSuffixColor = colors.focusedColor,
        unfocusedSuffixColor = colors.unfocusedColor,
        errorSuffixColor = colors.errorColor,
        disabledSuffixColor = colors.disabledColor.copy(alpha = colors.disabledOpacity),

        cursorColor = colors.focusedColor,

    )

/**
 * [outlinedTextFieldColors]'s counterpart for the filled field: the same [TextInputColors] mapped
 * onto the **indicator** line rather than the border, plus the container.
 *
 * [containerColor] is left out of [TextInputColors] on purpose — the outlined field's container is
 * transparent, so the two shapes have nothing to share there. An unspecified value keeps Material's
 * own filled container color, and a specified one applies to every state: only the content colors
 * fade by [TextInputColors.disabledOpacity] when disabled.
 */
@Composable
fun filledTextFieldColors(
    colors: TextInputColors = TextInputColors.default(),
    containerColor: Color = Color.Unspecified,
): TextFieldColors =
    TextFieldDefaults.colors(
        focusedIndicatorColor = colors.focusedColor,
        unfocusedIndicatorColor = colors.unfocusedColor,
        errorIndicatorColor = colors.errorColor,
        disabledIndicatorColor = colors.disabledColor.copy(alpha = colors.disabledOpacity),

        focusedContainerColor = containerColor,
        unfocusedContainerColor = containerColor,
        errorContainerColor = containerColor,
        disabledContainerColor = containerColor,

        focusedLabelColor = colors.focusedColor,
        unfocusedLabelColor = colors.unfocusedColor,
        errorLabelColor = colors.errorColor,
        disabledLabelColor = colors.disabledColor.copy(alpha = colors.disabledOpacity),

        focusedLeadingIconColor = colors.focusedColor,
        unfocusedLeadingIconColor = colors.unfocusedColor,
        errorLeadingIconColor = colors.errorColor,
        disabledLeadingIconColor = colors.disabledColor.copy(alpha = colors.disabledOpacity),

        focusedTrailingIconColor = colors.focusedColor,
        unfocusedTrailingIconColor = colors.unfocusedColor,
        errorTrailingIconColor = colors.errorColor,
        disabledTrailingIconColor = colors.disabledColor.copy(alpha = colors.disabledOpacity),

        focusedPrefixColor = colors.focusedColor,
        unfocusedPrefixColor = colors.unfocusedColor,
        errorPrefixColor = colors.errorColor,
        disabledPrefixColor = colors.disabledColor.copy(alpha = colors.disabledOpacity),

        focusedSuffixColor = colors.focusedColor,
        unfocusedSuffixColor = colors.unfocusedColor,
        errorSuffixColor = colors.errorColor,
        disabledSuffixColor = colors.disabledColor.copy(alpha = colors.disabledOpacity),

        cursorColor = colors.focusedColor,

    )



data class TextInputColors(
    val focusedColor: Color,
    val unfocusedColor: Color,
    val disabledColor: Color,
    val errorColor: Color,
    val disabledOpacity: Float,
) {
    companion object {
        @Composable
        fun default(
            focusedColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
            unfocusedColor: Color = MaterialTheme.colorScheme.onSurface,
            disabledColor: Color = MaterialTheme.colorScheme.onSurface,
            errorColor: Color = MaterialTheme.colorScheme.error,
            disabledOpacity: Float = 0.38f,
        ): TextInputColors = TextInputColors(
            focusedColor = focusedColor,
            unfocusedColor = unfocusedColor,
            disabledColor = disabledColor,
            errorColor = errorColor,
            disabledOpacity = disabledOpacity,
        )
    }
}
