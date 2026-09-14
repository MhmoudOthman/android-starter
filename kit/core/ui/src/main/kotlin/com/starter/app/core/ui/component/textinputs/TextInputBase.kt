package com.starter.app.core.ui.component.textinputs

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starter.app.core.designsystem.dimen.sdp


/**
 * The app's outlined text field. Built on [BasicTextField] + [OutlinedTextFieldDefaults.DecorationBox]
 * rather than on `OutlinedTextField` itself, because the `String` overload of the latter hard-codes
 * its spacing: there is no way through it to change the 16dp of [contentPadding] or the 280x56dp
 * floor. That is a form field's spacing, and `NumberPicker` is not a form field. Every default here
 * is the value `OutlinedTextField` would have used, so a caller that passes none renders identically.
 *
 * @param contentPadding space between the input text and the icons around it (or the container
 *   edges when there are no icons). Note the decoration box subtracts the icons' own touch-target
 *   padding from the horizontal values, so with icons present anything under ~12dp reads as 0.
 * @param minWidth minimum width the field settles at when nothing else constrains it.
 * @param minHeight as [minWidth], for the height.
 */
@Composable
fun BaseOutlinedTextInput(
    value: String,
    onValueChange: (String) -> Unit,
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
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
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
    val validatedMaxLines = if (singleLine) 1 else maxLines
    val validatedMinLines = if (singleLine) 1 else minLines

    var colors = TextInputColors.default()
    if (focusedColor != null) colors = colors.copy(focusedColor = focusedColor)
    if (unfocusedColor != null) colors = colors.copy(unfocusedColor = unfocusedColor)
    if (disabledColor != null) colors = colors.copy(disabledColor = disabledColor)
    if (errorColor != null) colors = colors.copy(errorColor = errorColor)
    if (disabledOpacity != null) colors = colors.copy(disabledOpacity = disabledOpacity)
    val fieldColors = outlinedTextFieldColors(colors)

    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val textColor = textStyle.color.takeOrElse {
        when {
            !enabled -> fieldColors.disabledTextColor
            isError -> fieldColors.errorTextColor
            focused -> fieldColors.focusedTextColor
            else -> fieldColors.unfocusedTextColor
        }
    }
    // Compose's own localized "Invalid input" string — the same one `OutlinedTextField` announces,
    // reached directly because the accessor that wraps it is internal to Material 3.
    val errorMessage = stringResource(androidx.compose.ui.R.string.default_error_message)

    CompositionLocalProvider(LocalTextSelectionColors provides fieldColors.textSelectionColors) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .labelCutoutPadding(hasLabel = label != null)
                .semantics { if (isError) error(errorMessage) }
                .defaultMinSize(minWidth = minWidth, minHeight = minHeight),
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle.merge(TextStyle(color = textColor)),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = validatedMaxLines,
            minLines = validatedMinLines,
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            cursorBrush = SolidColor(
                if (isError) fieldColors.errorCursorColor else fieldColors.cursorColor
            ),
            decorationBox = { innerTextField ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = value,
                    innerTextField = innerTextField,
                    enabled = enabled,
                    singleLine = singleLine,
                    visualTransformation = visualTransformation,
                    interactionSource = interactionSource,
                    isError = isError,
                    label = label,
                    placeholder = placeholder,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    prefix = prefix,
                    suffix = suffix,
                    supportingText = supportingText,
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
                            unfocusedBorderThickness = unfocusedBorderThickness

                        )
                    },
                )
            },
        )
    }
}

/**
 * The app's filled text field — the same field as [BaseOutlinedTextInput] with a tonal container
 * and a bottom indicator line instead of a border, built on [BasicTextField] +
 * [TextFieldDefaults.DecorationBox] for exactly the same reason: the `String` overload of
 * `TextField` hard-codes its 16dp [contentPadding] and its 280x56dp floor. Every default here is
 * the value `TextField` would have used, so a caller that passes none renders identically.
 *
 * Two things differ from the outlined base, both because the filled label sits *inside* the
 * container rather than straddling the top edge: there is no cutout padding to reserve
 * (`labelCutoutPadding` is outlined-only), and the default [contentPadding] switches on whether a
 * [label] is present — a labelled filled field packs the label and the input into the same height,
 * so it needs the tighter vertical padding or the two collide.
 *
 * @param contentPadding space between the input text and the icons around it (or the container
 *   edges when there are no icons). Note the decoration box subtracts the icons' own touch-target
 *   padding from the horizontal values, so with icons present anything under ~12dp reads as 0.
 * @param minWidth minimum width the field settles at when nothing else constrains it.
 * @param minHeight as [minWidth], for the height.
 * @param shape defaults to [TextFieldDefaults.shape] — top corners only — rather than to
 *   `shapes.medium` like the outlined field, because the indicator line is clipped to this shape
 *   and rounded bottom corners cut its ends off.
 * @param containerColor the field's background in every state. Left unspecified, Material's own
 *   filled container color applies.
 * @param shadowElevation shadow cast by the field. It rides on the **container's** own modifier,
 *   not on the text field's: the decoration box draws the supporting text below the container, so a
 *   shadow applied further out would trace the field and its supporting line together. Note the
 *   shadow shows through a translucent [containerColor], and that Material's own answer to
 *   elevation is a tonal container rather than a shadow — in a dark theme the shadow is barely
 *   visible while a lighter container still reads. Only [BaseFilledTextInputFullRounded] exposes
 *   this: on the top-rounded shape the shadow runs under the flat bottom edge and past the ends of
 *   the indicator line.
 */
@Composable
fun BaseFilledTextInput(
    value: String,
    onValueChange: (String) -> Unit,
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
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
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
    val validatedMaxLines = if (singleLine) 1 else maxLines
    val validatedMinLines = if (singleLine) 1 else minLines

    var colors = TextInputColors.default()
    if (focusedColor != null) colors = colors.copy(focusedColor = focusedColor)
    if (unfocusedColor != null) colors = colors.copy(unfocusedColor = unfocusedColor)
    if (disabledColor != null) colors = colors.copy(disabledColor = disabledColor)
    if (errorColor != null) colors = colors.copy(errorColor = errorColor)
    if (disabledOpacity != null) colors = colors.copy(disabledOpacity = disabledOpacity)
    val fieldColors = filledTextFieldColors(colors, containerColor)

    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val textColor = textStyle.color.takeOrElse {
        when {
            !enabled -> fieldColors.disabledTextColor
            isError -> fieldColors.errorTextColor
            focused -> fieldColors.focusedTextColor
            else -> fieldColors.unfocusedTextColor
        }
    }
    // Compose's own localized "Invalid input" string — the same one `TextField` announces,
    // reached directly because the accessor that wraps it is internal to Material 3.
    val errorMessage = stringResource(androidx.compose.ui.R.string.default_error_message)

    CompositionLocalProvider(LocalTextSelectionColors provides fieldColors.textSelectionColors) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .semantics(mergeDescendants = true) {}
                .semantics { if (isError) error(errorMessage) }
                .defaultMinSize(minWidth = minWidth, minHeight = minHeight),
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle.merge(TextStyle(color = textColor)),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = validatedMaxLines,
            minLines = validatedMinLines,
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            cursorBrush = SolidColor(
                if (isError) fieldColors.errorCursorColor else fieldColors.cursorColor
            ),
            decorationBox = { innerTextField ->
                TextFieldDefaults.DecorationBox(
                    value = value,
                    innerTextField = innerTextField,
                    enabled = enabled,
                    singleLine = singleLine,
                    visualTransformation = visualTransformation,
                    interactionSource = interactionSource,
                    isError = isError,
                    label = label,
                    placeholder = placeholder,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    prefix = prefix,
                    suffix = suffix,
                    supportingText = supportingText,
                    shape = shape,
                    colors = fieldColors,
                    contentPadding = contentPadding,
                    container = {
                        TextFieldDefaults.Container(
                            enabled = enabled,
                            isError = isError,
                            interactionSource = interactionSource,
                            // A zero elevation returns the modifier untouched, so the shadow
                            // costs nothing on the fields that do not ask for one.
                            modifier = Modifier.shadow(shadowElevation, shape),
                            colors = fieldColors,
                            shape = shape,
                            focusedIndicatorLineThickness = focusedIndicatorThickness,
                            unfocusedIndicatorLineThickness = unfocusedIndicatorThickness
                        )
                    },
                )
            },
        )
    }
}

/**
 * [BaseFilledTextInput] rounded on **all four corners, with no indicator line** — the container is
 * the whole field.
 *
 * The two go together and neither is a free choice. The indicator is drawn as a full-width strip
 * along the bottom edge intersected with the container's own outline, so a rounded bottom corner
 * eats the strip's ends and leaves a short line with curved tips; the wider the [cornerSize], the
 * less line survives. Rounding all four corners therefore means dropping the line, and dropping the
 * line is what makes the rounding look deliberate rather than clipped.
 *
 * The cost is that focus has no shape to announce itself with: it reads only through the label,
 * cursor and icon colors. Where that is too quiet, pass a [containerColor] that stands out from the
 * surface behind it, or use [BaseFilledTextInputTopRounded] and keep the line.
 *
 * @param cornerSize radius of all four corners. Defaults to the theme's medium corner — the same
 *   one [BaseOutlinedTextInput] uses — and accepts a percentage (`CornerSize(50)`) for a pill.
 * @param shadowElevation shadow cast by the field, drawn on the container alone. This is the one
 *   shape it belongs on: with no indicator line and every corner rounded, the container *is* the
 *   field, so a shadow traces it exactly. See [BaseFilledTextInput] for the caveats.
 */
@Composable
fun BaseFilledTextInputFullRounded(
    value: String,
    onValueChange: (String) -> Unit,
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
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
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
    value = value,
    onValueChange = onValueChange,
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
    visualTransformation = visualTransformation,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    singleLine = singleLine,
    maxLines = maxLines,
    minLines = minLines,
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
 * classic filled field, at whatever radius the design calls for.
 *
 * This is the variant that leaves the line alone: the bottom edge stays square, so the strip runs
 * the full width with square ends however wide [cornerSize] gets. For the fully rounded look, see
 * [BaseFilledTextInputFullRounded], which drops the line rather than let it be clipped.
 *
 * @param cornerSize radius of the top two corners; the bottom two stay square. Defaults to the
 *   theme's medium corner — note Material's own filled default is the smaller 4dp
 *   [TextFieldDefaults.shape].
 */
@Composable
fun BaseFilledTextInputTopRounded(
    value: String,
    onValueChange: (String) -> Unit,
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
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
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
    value = value,
    onValueChange = onValueChange,
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
    visualTransformation = visualTransformation,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    singleLine = singleLine,
    maxLines = maxLines,
    minLines = minLines,
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
 * [OutlinedTextFieldDefaults.DecorationBox] — which is what buys the `contentPadding` knob — means
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
