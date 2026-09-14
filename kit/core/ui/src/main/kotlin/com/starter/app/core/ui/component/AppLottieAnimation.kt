package com.starter.app.core.ui.component

import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.LottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty

/**
 * [AppLottieAnimation]'s `iterations` for an animation that plays until it is taken off screen —
 * a loading indicator, as opposed to the one-shot confirmations the default is written for.
 *
 * Re-exported as a plain `Int` rather than letting callers reach for `LottieConstants`, which is
 * what keeps this file the only one in the module that imports anything Lottie-typed.
 */
const val LOTTIE_LOOP_FOREVER: Int = LottieConstants.IterateForever

/**
 * Which of a shape's two colors a [LottieTint] replaces. A shape authored with both needs one
 * entry each — tinting only the fill leaves the outline whatever the asset shipped with, which on
 * a stock animation is usually a colour from someone else's palette.
 */
enum class LottieTintTarget { FILL, STROKE }

/**
 * A colour override applied to part of an animation at playback time, so an asset can follow the
 * app's `colorScheme` without the JSON being edited.
 *
 * [keyPath] addresses the shape the way Lottie does — by the names the animation was authored
 * with, outermost first — and `"**"` matches everything below that point. `listOf("circle-base",
 * "**")` is "every shape in the `circle-base` layer".
 *
 * **Name the layer, not the shape.** Names come from whatever tool exported the asset and are not
 * guaranteed to be ASCII or even correctly encoded — `done_animation`'s strokes are all called
 * `TraÃƒÂ§ado 1`, a double-encoded `Traçado 1`, which no literal in Kotlin source will match. Layer
 * and group names in that file happen to be clean; ending the path in `"**"` is what keeps a tint
 * from depending on the ones that are not.
 *
 * Note an override is a constant: applying one to a property the asset *animates* replaces the
 * animation with the fixed value.
 */
data class LottieTint(
    val keyPath: List<String>,
    val color: Color,
    val target: LottieTintTarget = LottieTintTarget.FILL,
)

/**
 * The one place Lottie's API is touched. Callers name a raw resource and, optionally, [tints] built
 * from types declared here — nothing Lottie-typed reaches a caller's signature, which is what keeps
 * `lottie-compose` an `implementation` dependency of `:core:ui` and off every other module's
 * classpath.
 *
 * [iterations] defaults to a single pass: these are one-shot confirmations, not decoration, and a
 * looping checkmark keeps announcing something the driver already acknowledged.
 *
 * `enableMergePaths` mirrors v1's `lottie_enableMergePathsForKitKatAndAbove` — the shipped
 * `done_animation` is authored with merge paths, and rendering it without them fills shapes that
 * are meant to be cut out.
 */
@Composable
fun AppLottieAnimation(
    @RawRes animationRes: Int,
    modifier: Modifier = Modifier,
    iterations: Int = 1,
    speed: Float = 1f,
    tints: List<LottieTint> = emptyList(),
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animationRes))
    LottieAnimation(
        composition = composition,
        modifier = modifier,
        iterations = iterations,
        speed = speed,
        enableMergePaths = true,
        dynamicProperties = rememberTintProperties(tints),
    )
}

/**
 * Translates [LottieTint]s into what the library wants. Built through the `remember…` helpers
 * rather than the constructors because [LottieDynamicProperties]'s is `internal`.
 *
 * Returns `null` for an empty list — that is the library's "leave the asset alone", and is not the
 * same as an empty [LottieDynamicProperties].
 */
@Composable
private fun rememberTintProperties(tints: List<LottieTint>): LottieDynamicProperties? {
    if (tints.isEmpty()) return null
    // A composable call per element: `map` is inline, so these are ordinary calls in this scope.
    val properties = tints.map { tint ->
        rememberLottieDynamicProperty(
            property = when (tint.target) {
                LottieTintTarget.FILL -> LottieProperty.COLOR
                LottieTintTarget.STROKE -> LottieProperty.STROKE_COLOR
            },
            value = tint.color.toArgb(),
            keyPath = tint.keyPath.toTypedArray(),
        )
    }
    return rememberLottieDynamicProperties(*properties.toTypedArray())
}
