package com.starter.app.core.ui.component

import androidx.annotation.RawRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import com.starter.app.core.designsystem.previews.PreviewLightDarkDeviceSystemUi
import com.starter.app.core.designsystem.theme.AppTheme
import com.starter.app.core.designsystem.R as DesignSystemR

/**
 * The two illustrations a [LoadingAnimation] can play. Both are stock artwork, and neither ships in
 * a colour either theme has — see [LoadingArtColors] for how they are re-painted at playback.
 *
 * The resource id is deliberately `internal`: a caller names the art, not the asset, so swapping
 * the JSON behind one of these is a change in this module alone.
 */
enum class LoadingArt(@RawRes internal val animationRes: Int) {
    /**
     * A delivery car with parcels, on a road. The asset is drawn in **two** colours, so it reads
     * [LoadingArtColors.ink] and [LoadingArtColors.light] and ignores the other ten roles.
     */
    CAR(DesignSystemR.raw.loading_car),

    /** A driver on a bike carrying a parcel, over grass. Reads every role in [LoadingArtColors]. */
    BIKE(DesignSystemR.raw.loading_bike),
}

/**
 * One colour per part the two assets draw. Everything the driver sees while a screen is blocked
 * comes from here, so a loading overlay can follow the surface it is drawn on instead of whatever
 * palette the artwork was exported with.
 *
 * **The roles are finer than the artwork's own colour count.** `loading_bike.json` is drawn in
 * seven colours but twelve *parts* — the cast shadow is the same `#004F4F` as every outline, the
 * road the same `#019D91` as the rider's shading, the parcel the same pink as the rider's hands —
 * and a palette keyed on colour welds each of those pairs together for good. Splitting them is what
 * makes it possible to lift the parcel out of the illustration, or push the road back, without
 * dragging its twin along. The split is the artist's own: every fill in that asset carries a `sid`
 * slot id, and these twelve are exactly those twelve ids (named in each role below).
 *
 * Roles are named after what they draw, not after a theme colour — [LoadingArtDefaults.colors] is
 * what maps them onto the theme, and passing this class directly is what overrides that.
 *
 * **An alpha here is discarded.** Lottie sets a paint's colour and then overwrites its alpha from
 * the shape's own opacity animation, which is why the defaults composite over the surface behind
 * the animation rather than handing a translucent colour over — the same trick `SuccessDialog`'s
 * halo uses, for the same reason.
 */
@Immutable
data class LoadingArtColors(
    /** Outlines, wheels, the rider's silhouette — the artwork's darkest tone. `driver-mint-dark`. */
    val ink: Color,
    /** The shadow the bike casts on the ground. Authored in [ink]'s colour, separate here. `shadow`. */
    val shadow: Color,
    /** The shading down the rider and the bike's darker panels. `driver-mint-mid`. */
    val trim: Color,
    /** The lighter pass over [trim]. `driver-mint-light`. */
    val trimLight: Color,
    /** The ground the bike rides over. `land`. */
    val road: Color,
    /** The grass thrown up along the road. `grass`. */
    val turf: Color,
    /** The bike's main panel. `driver-yellow-dark`. */
    val body: Color,
    /** The highlight along [body]. `driver-yellow-light`. */
    val bodyLight: Color,
    /** The rider's hands and face — the same pink as [parcel] in the artwork. `driver-skin`. */
    val rider: Color,
    /** The parcel being carried, the one part of the illustration worth picking out. `package`. */
    val parcel: Color,
    /** Glass, the car's sparkle fills, the rider's white highlights. `driver-white`. */
    val light: Color,
    /** The dust trailing behind the bike. `dust`. */
    val dust: Color,
)

/** Where the default palette lives, so [LoadingArtColors] stays a plain value type. */
object LoadingArtDefaults {

    /**
     * The theme-following palette: one ramp, from the content colour down to the surface behind it.
     *
     * A loading overlay draws on a card whose container is `secondary`, `tertiary` or `error`
     * depending on the caller, so a fixed mapping onto named roles would clash on at least one of
     * the three. Tinting every part with [content] at its own weight lands correctly on all of them
     * and in both themes.
     *
     * The weights are a **depth** order rather than the artwork's own luminance: [ink] and [body]
     * carry the shape, [parcel] is what the app is about, and [road], [light] and [dust] fall back
     * towards the card so they read as ground rather than as more bike.
     *
     * Every part is also a parameter, so a caller can move one without restating the other eleven —
     * `LoadingArtDefaults.colors(parcel = MaterialTheme.colorScheme.error)` keeps the ramp and puts
     * the box in red. Building a [LoadingArtColors] directly ignores the ramp entirely.
     */
    @Composable
    fun colors(
        content: Color = LocalContentColor.current,
        container: Color = MaterialTheme.colorScheme.surface,
        ink: Color = content.washOver(container, fraction = 1.00f),
        body: Color = content.washOver(container, fraction = 0.85f),
        parcel: Color = content.washOver(container, fraction = 0.80f),
        rider: Color = content.washOver(container, fraction = 0.65f),
        trim: Color = content.washOver(container, fraction = 0.55f),
        bodyLight: Color = content.washOver(container, fraction = 0.45f),
        trimLight: Color = content.washOver(container, fraction = 0.40f),
        shadow: Color = content.washOver(container, fraction = 0.35f),
        turf: Color = content.washOver(container, fraction = 0.30f),
        road: Color = content.washOver(container, fraction = 0.22f),
        light: Color = content.washOver(container, fraction = 0.16f),
        dust: Color = content.washOver(container, fraction = 0.10f),
    ): LoadingArtColors = LoadingArtColors(
        ink = ink,
        shadow = shadow,
        trim = trim,
        trimLight = trimLight,
        road = road,
        turf = turf,
        body = body,
        bodyLight = bodyLight,
        rider = rider,
        parcel = parcel,
        light = light,
        dust = dust,
    )

    @Composable
    fun colorBikeTertiary(): LoadingArtColors = LoadingArtColors(
        ink = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            .compositeOver(MaterialTheme.colorScheme.tertiary),
        shadow = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.35f)
            .compositeOver(MaterialTheme.colorScheme.primary),
        trim = MaterialTheme.colorScheme.tertiary,
        trimLight = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.3f)
            .compositeOver(MaterialTheme.colorScheme.tertiary),
        road = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            .compositeOver(MaterialTheme.colorScheme.surface),
        turf = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.3f)
            .compositeOver(MaterialTheme.colorScheme.tertiary),
        body = MaterialTheme.colorScheme.tertiaryContainer,
        bodyLight = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            .compositeOver(MaterialTheme.colorScheme.onTertiaryContainer),
        rider = Color(0xFFF6CAC5),
        parcel = MaterialTheme.colorScheme.primary,
        light = Color.DarkGray.copy(alpha = 0.5f)
            .compositeOver(Color.White),
        dust = Color.LightGray.copy(alpha = 0.12f)
            .compositeOver(Color.Transparent),
    )

    @Composable
    fun colorBikeSecondary(): LoadingArtColors = LoadingArtColors(
        ink = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)
            .compositeOver(MaterialTheme.colorScheme.onSecondary),
        shadow = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.35f)
            .compositeOver(MaterialTheme.colorScheme.primary),
        trim = MaterialTheme.colorScheme.secondary,
        trimLight = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.3f)
            .compositeOver(MaterialTheme.colorScheme.secondary),
        road = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            .compositeOver(MaterialTheme.colorScheme.surface),
        turf = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.3f)
            .compositeOver(MaterialTheme.colorScheme.secondary),
        body = MaterialTheme.colorScheme.secondaryContainer,
        bodyLight = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            .compositeOver(MaterialTheme.colorScheme.onSecondaryContainer),
        rider = Color(0xFFF6CAC5),
        parcel = MaterialTheme.colorScheme.primary,
        light = Color.DarkGray.copy(alpha = 0.5f)
            .compositeOver(Color.White),
        dust = Color.LightGray.copy(alpha = 0.12f)
            .compositeOver(Color.Transparent),
    )

@Composable
fun colorBikeRed(): LoadingArtColors = LoadingArtColors(
    ink = MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
        .compositeOver(MaterialTheme.colorScheme.onError),
    shadow = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.35f)
        .compositeOver(MaterialTheme.colorScheme.primary),
    trim = MaterialTheme.colorScheme.error,
    trimLight = MaterialTheme.colorScheme.onError.copy(alpha = 0.3f)
        .compositeOver(MaterialTheme.colorScheme.error),
    road = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        .compositeOver(MaterialTheme.colorScheme.surface),
    turf = MaterialTheme.colorScheme.onError.copy(alpha = 0.3f)
        .compositeOver(MaterialTheme.colorScheme.error),
    body = MaterialTheme.colorScheme.errorContainer,
    bodyLight = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        .compositeOver(MaterialTheme.colorScheme.onErrorContainer),
    rider = Color(0xFFF6CAC5),
    parcel = MaterialTheme.colorScheme.primary,
    light = Color.DarkGray.copy(alpha = 0.5f)
        .compositeOver(Color.White),
    dust = Color.LightGray.copy(alpha = 0.12f)
        .compositeOver(Color.Transparent),
)
}

/** How much of this colour survives once it is laid over [background]. See [LoadingArtColors]. */
private fun Color.washOver(background: Color, fraction: Float): Color =
    copy(alpha = fraction).compositeOver(background)

/**
 * Plays one of the [LoadingArt]s, looping, in the app's own colours.
 *
 * [iterations][LOTTIE_LOOP_FOREVER] is the difference from every other animation in the module:
 * these run for as long as the work does, where the confirmations play once and stop.
 */
@Composable
fun LoadingAnimation(
    art: LoadingArt,
    modifier: Modifier = Modifier,
    speed: Float = 1f,
    colors: LoadingArtColors = LoadingArtDefaults.colors(),
) {
    AppLottieAnimation(
        animationRes = art.animationRes,
        modifier = modifier,
        iterations = LOTTIE_LOOP_FOREVER,
        speed = speed,
        tints = art.tints(colors),
    )
}

/**
 * The names the paintable shapes carry inside the two JSONs.
 *
 * **They were put there.** Both assets arrived with every fill called `Fill` or `Relleno 1` and
 * every stroke called `Trazo 1` — one name across dozens of shapes, which no keypath can tell
 * apart. Each fill and stroke was renamed in the JSON after the part it draws, so one keypath
 * reaches every shape in that part however the layers above it are nested; the car alone has 47 of
 * them across fifteen layers, and naming the layers instead would have meant a keypath per layer
 * and a colour per layer with it.
 *
 * The bike's names come from the `sid` slot ids its own fills carry, which is why its twelve parts
 * split finer than its seven colours. The ids are no use at runtime — lottie-android 6.7.1 ships no
 * slot support (there is no `Slot` class in the artifact), so the `slots` block in that file is
 * inert and the inline colours are what render. They are left in place as the record of where these
 * names came from, and `LoadingArtColors` names each one against its role.
 *
 * **None of these may equal a *layer* name in either asset.** The bike's layers are `dust`, `grass`,
 * `driver`, `shadow`, `package` and `land`, which is why five of the parts below are `dust-trail`,
 * `turf`, `shadow-cast`, `parcel` and `road` instead. A keypath that matches a layer as well as a
 * shape paints across parts that were split on purpose.
 *
 * **Carry these names over when re-exporting either asset.** A keypath that has stopped matching is
 * applied to nothing and Lottie reports it in no way at all; `LoadingAnimationKeyPathTest` is the
 * only thing that catches it.
 */
internal const val INK_NODE = "ink"
internal const val INK_LINE_NODE = "ink-line"
internal const val SHADOW_NODE = "shadow-cast"
internal const val TRIM_NODE = "trim"
internal const val TRIM_LIGHT_NODE = "trim-light"
internal const val ROAD_NODE = "road"
internal const val TURF_NODE = "turf"
internal const val BODY_NODE = "body"
internal const val BODY_LIGHT_NODE = "body-light"
internal const val RIDER_NODE = "rider"
internal const val PARCEL_NODE = "parcel"
internal const val LIGHT_NODE = "light"
internal const val DUST_NODE = "dust-trail"

/**
 * Addresses a paint node by name at any depth. The leading `"**"` is what makes the path
 * independent of the nesting above it — `loading_car.json` puts two of its shapes inside a precomp
 * (`destellos`) and the rest at the top level, and one path has to reach both. The trailing `"**"`
 * costs nothing and survives an asset that arrives with the shapes one group deeper.
 */
internal fun paintKeyPath(node: String) = listOf("**", node, "**")

/** Every node name [tints] paints for this art, which is what the keypath test walks. */
internal val LoadingArt.paintNodes: List<String>
    get() = when (this) {
        LoadingArt.CAR -> listOf(INK_NODE, INK_LINE_NODE, LIGHT_NODE)
        LoadingArt.BIKE -> listOf(
            INK_NODE, SHADOW_NODE, TRIM_NODE, TRIM_LIGHT_NODE, ROAD_NODE, TURF_NODE,
            BODY_NODE, BODY_LIGHT_NODE, RIDER_NODE, PARCEL_NODE, LIGHT_NODE, DUST_NODE,
        )
    }

/**
 * The car's outlines are drawn twice over: as fills on the bodywork and as strokes on the road
 * lines and the sparkles, so [LoadingArtColors.ink] arrives at two nodes. A `FILL` tint on a
 * stroked shape resolves and then applies to nothing, so the two targets are declared rather than
 * guessed at. Every shape in the bike is a fill.
 */
internal fun LoadingArt.tints(colors: LoadingArtColors): List<LottieTint> = when (this) {
    LoadingArt.CAR -> listOf(
        LottieTint(paintKeyPath(INK_NODE), colors.ink, LottieTintTarget.FILL),
        LottieTint(paintKeyPath(INK_LINE_NODE), colors.ink, LottieTintTarget.STROKE),
        LottieTint(paintKeyPath(LIGHT_NODE), colors.light, LottieTintTarget.FILL),
    )

    LoadingArt.BIKE -> listOf(
        LottieTint(paintKeyPath(INK_NODE), colors.ink, LottieTintTarget.FILL),
        LottieTint(paintKeyPath(SHADOW_NODE), colors.shadow, LottieTintTarget.FILL),
        LottieTint(paintKeyPath(TRIM_NODE), colors.trim, LottieTintTarget.FILL),
        LottieTint(paintKeyPath(TRIM_LIGHT_NODE), colors.trimLight, LottieTintTarget.FILL),
        LottieTint(paintKeyPath(ROAD_NODE), colors.road, LottieTintTarget.FILL),
        LottieTint(paintKeyPath(TURF_NODE), colors.turf, LottieTintTarget.FILL),
        LottieTint(paintKeyPath(BODY_NODE), colors.body, LottieTintTarget.FILL),
        LottieTint(paintKeyPath(BODY_LIGHT_NODE), colors.bodyLight, LottieTintTarget.FILL),
        LottieTint(paintKeyPath(RIDER_NODE), colors.rider, LottieTintTarget.FILL),
        LottieTint(paintKeyPath(PARCEL_NODE), colors.parcel, LottieTintTarget.FILL),
        LottieTint(paintKeyPath(LIGHT_NODE), colors.light, LottieTintTarget.FILL),
        LottieTint(paintKeyPath(DUST_NODE), colors.dust, LottieTintTarget.FILL),
    )
}

@PreviewLightDarkDeviceSystemUi
@Composable
private fun PreviewAnimation() {
    AppTheme {
        Column {
            // The bike, hand-painted part by part. The pairs the artwork draws in one colour are
            // what the extra roles buy: rider apart from parcel, road apart from trim, shadow apart
            // from ink, dust apart from light.
            LoadingAnimation(
                art = LoadingArt.BIKE,
                colors = LoadingArtDefaults.colorBikeRed(),
                modifier = Modifier
                    .progressSemantics()
                    .weight(1f)
                    .aspectRatio(1f),
            )

            // The same class against the car, which draws two of the twelve. The loud values on the
            // rest are the point: they resolve to nothing, so the car is unaffected by them.
            LoadingAnimation(
                art = LoadingArt.CAR,
                colors = LoadingArtDefaults.colors(
                    ink = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        .compositeOver(MaterialTheme.colorScheme.tertiary),
                    light = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                        .compositeOver(MaterialTheme.colorScheme.onTertiary),
                ),
                modifier = Modifier
                    .progressSemantics()
                    .weight(1f)
                    .aspectRatio(1f),
            )
        }
    }
}
