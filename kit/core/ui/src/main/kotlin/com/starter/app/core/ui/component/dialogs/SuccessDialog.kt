@file:OptIn(ExperimentalMaterial3Api::class)

package com.starter.app.core.ui.component.dialogs

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.window.DialogProperties
import com.starter.app.core.designsystem.previews.PreviewLightDarkDeviceSystemUi
import com.starter.app.core.designsystem.theme.AppTheme
import com.starter.app.core.ui.component.AppLottieAnimation
import com.starter.app.core.ui.component.LottieTint
import com.starter.app.core.ui.component.LottieTintTarget
import com.starter.app.core.designsystem.R as DesignSystemR

/**
 * The "it went through" dialog: v1's `done_animation` playing once above a centered message, and a
 * single acknowledging button. The sibling of [ErrorDialog] — same scaffold, same button row, and
 * the same rule that a driver is told what happened in the shape the outcome deserves.
 *
 * The animation stands in for [AppAlertDialog]'s title-panel icon rather than joining it: a
 * checkmark drawing itself and a static check beside the word "Success" say the same thing twice.
 * [title] is therefore optional and off by default, which is how v1 showed this dialog.
 *
 * Defaults to no dismiss route other than the button ([dismissOnBackPress] / [dismissOnClickOutside]
 * both `false`), matching [ErrorDialog]: what the caller does on acknowledgement — leaving the
 * screen, clearing a form — should not be reachable by tapping the scrim beside the dialog.
 */
@Composable
fun SuccessDialog(
    message: String,
    messageFontSize: TextUnit = TextUnit.Unspecified,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    positiveText: String? = null,
    onPositiveClick: (() -> Unit)? = null,
    dismissOnBackPress: Boolean = false,
    dismissOnClickOutside: Boolean = false,
) {
    // usePlatformDefaultWidth stays true for the reason AppAlertDialog documents: an unconstrained
    // width throws inside Layoutlib and leaves the @Preview blank.
    val properties = DialogProperties(
        usePlatformDefaultWidth = true,
        dismissOnBackPress = dismissOnBackPress,
        dismissOnClickOutside = dismissOnClickOutside,
    )
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        properties = properties,
        modifier = modifier,
    ) {
        SuccessDialogContent(
            message = message,
            messageFontSize=messageFontSize,
            title = title,
            positiveText = positiveText,
            onPositiveClick = onPositiveClick,
        )
    }
}

/**
 * Stateless body extracted for the same reason [AppAlertDialog]'s is: Compose dialogs draw in their
 * own window, which Layoutlib cannot render.
 */
@Composable
private fun SuccessDialogContent(
    message: String,
    messageFontSize: TextUnit = TextUnit.Unspecified,
    title: String? = null,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    btnShape: Shape = MaterialTheme.shapes.medium,
    positiveText: String? = null,
    onPositiveClick: (() -> Unit)? = null,
    titlePanelColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    onTitlePanelColor: Color = contentColorFor(titlePanelColor),
    positiveBtnColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    positiveOnBtnColor: Color = contentColorFor(positiveBtnColor),
    modifier: Modifier = Modifier,
) {
    DialogScaffold(
        modifier = modifier,
        message = message,
        messageFontSize = messageFontSize,
        title = title,
        titleCentered = true,
        shape = shape,
        titlePanelColor = titlePanelColor,
        onTitlePanelColor = onTitlePanelColor,
        messageTextAlign = TextAlign.Center,
        header = { SuccessAnimation() },
    ) {
        DialogButtonsRow(
            btnShape = btnShape,
            positiveText = positiveText,
            onPositiveClick = onPositiveClick,
            negativeText = null,
            onNegativeClick = null,
            neutralText = null,
            onNeutralClick = null,
            positiveBtnColor = positiveBtnColor,
            positiveOnBtnColor = positiveOnBtnColor,
            negativeBtnColor = Color.Unspecified,
            negativeOnBtnColor = Color.Unspecified,
            neutralBtnColor = Color.Unspecified,
            neutralOnBtnColor = Color.Unspecified,
        )
    }
}

/**
 * Half the dialog's width and square, which is the box v1 gave it (`layout_constraintWidth_percent`
 * 0.5 on a 1:1 ratio). Sized in fractions rather than `sdp` so it tracks the dialog's own width,
 * which the platform sets from the window.
 *
 * **The asset's own colours are overridden, not kept.** `done_animation` is stock artwork drawn in
 * a green of its own (`#2CDA94` for the disc over a `#C8F1E0` halo) — a palette from neither
 * theme, and one that ignores Dark entirely. [discColor] and [checkColor] are an M3 contrast pair
 * by definition, so the tick stays readable on the disc in both themes whatever the palette does.
 *
 * [haloColor] is the one that is computed rather than named: the halo is authored as a pale tint of
 * the disc, and no role means "a wash of the tertiary colour". Compositing [discColor] over the
 * dialog's own `surface` at [HALO_ALPHA] is what that wash actually is, and it lands correctly on a
 * dark surface too — where a fixed pale green would glow. It is composited rather than passed with
 * an alpha because a tint's alpha is discarded: Lottie sets the paint's colour and then overwrites
 * its alpha from the shape's opacity animation.
 */
@Composable
private fun ColumnScope.SuccessAnimation(
    discColor: Color = MaterialTheme.colorScheme.tertiary,
    checkColor: Color = MaterialTheme.colorScheme.onTertiary,
    haloColor: Color = discColor
        .copy(alpha = HALO_ALPHA)
        .compositeOver(MaterialTheme.colorScheme.surface),
) {
    // The check layer is a stroked path with a trim-path drawing it on — it has no fill, so a FILL
    // tint there would resolve and then apply to nothing. The halo carries both, and its 2px rim is
    // authored white for a white background, which is exactly the assumption being corrected here.
    val tints = listOf(
        LottieTint(discKeyPath(), discColor, LottieTintTarget.FILL),
        LottieTint(haloKeyPath(), haloColor, LottieTintTarget.FILL),
        LottieTint(haloKeyPath(), haloColor, LottieTintTarget.STROKE),
        LottieTint(checkKeyPath(), checkColor, LottieTintTarget.STROKE),
    )

    AppLottieAnimation(
        animationRes = DesignSystemR.raw.done_animation,
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .fillMaxWidth(fraction = 0.5f)
            .aspectRatio(1f),
        // 130 frames at 60fps — about 2.2s, where v1's asset ran 5.5s and needed the 2x push to
        // stop the driver waiting on a checkmark. 1.5x lands near 1.5s and still lets the tick draw.
        speed = 1.5f,
        tints = tints,
    )
}

/** How much of [SuccessAnimation]'s disc colour the halo keeps once composited over the surface. */
private const val HALO_ALPHA = 0.2f

/**
 * Layer names inside `done_animation.json`. The asset arrived with After Effects' defaults for two
 * of them (`Shape Layer 1` / `Shape Layer 2`, which say nothing about what they draw); they were
 * renamed in the JSON to match these. Re-exporting the animation without carrying the names over
 * fails `SuccessAnimationKeyPathTest` rather than silently dropping the tints.
 */
internal const val CHECK_LAYER = "check"
internal const val DISC_LAYER = "disc"
internal const val HALO_LAYER = "halo"

/**
 * **Every path opens with `"**"`, and that is deliberate even though this asset does not need it.**
 * These three are top-level layers here, so `listOf(CHECK_LAYER, "**")` would resolve too — but the
 * asset this dialog shipped with wrapped the same layers in a precomp named `success`, and against
 * that one an un-prefixed path matched nothing at all. Lottie reports that in no way: no exception,
 * no log, just the animation playing in its authored colours. A leading globstar costs nothing and
 * makes a path independent of whatever nesting the next exported asset arrives with.
 *
 * `SuccessAnimationKeyPathTest` resolves all three against the real JSON, which is the only place a
 * keypath that has stopped matching can be caught.
 */
internal fun checkKeyPath() = listOf("**", CHECK_LAYER, "**")

internal fun discKeyPath() = listOf("**", DISC_LAYER, "**")

internal fun haloKeyPath() = listOf("**", HALO_LAYER, "**")

@PreviewLightDarkDeviceSystemUi
@Composable
private fun PreviewSuccessDialogContent() {
    AppTheme {
        SuccessDialog(
            message = "Orders assigned successfully",
            positiveText = "OK",
            onPositiveClick = {},
            onDismissRequest = {}
        )
    }
}

@PreviewLightDarkDeviceSystemUi
@Composable
private fun PreviewSuccessDialogContentTitled() {
    AppTheme {
        SuccessDialog(
            title = "Success",
            message = "Orders assigned successfully",
            positiveText = "OK",
            onPositiveClick = {},
            onDismissRequest = {}
        )
    }
}
