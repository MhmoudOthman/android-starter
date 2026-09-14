package com.starter.app.core.ui.component.dialogs

import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.model.KeyPath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import com.starter.app.core.designsystem.R as DesignSystemR

/**
 * A tint whose keypath matches nothing is applied to nothing, and Lottie says so in no way at all —
 * no exception, no log, just the asset playing in its authored colours. That is the one failure
 * this component has, and the only place it can be caught is against the real JSON.
 *
 * Runs on the JVM under Robolectric because parsing a raw resource needs a `Context`.
 */
@RunWith(RobolectricTestRunner::class)
class SuccessAnimationKeyPathTest {

    private val composition: LottieComposition
        get() = checkNotNull(
            LottieCompositionFactory
                .fromRawResSync(RuntimeEnvironment.getApplication(), DesignSystemR.raw.done_animation)
                .value,
        ) { "done_animation.json failed to parse" }

    private fun resolve(keyPath: List<String>): List<KeyPath> {
        val drawable = LottieDrawable()
        drawable.setComposition(composition)
        return drawable.resolveKeyPath(KeyPath(*keyPath.toTypedArray()))
    }

    @Test
    fun `the check keypath resolves against the shipped asset`() {
        assertTrue(resolve(checkKeyPath()).isNotEmpty())
    }

    @Test
    fun `the disc keypath resolves against the shipped asset`() {
        assertTrue(resolve(discKeyPath()).isNotEmpty())
    }

    @Test
    fun `the halo keypath resolves against the shipped asset`() {
        assertTrue(resolve(haloKeyPath()).isNotEmpty())
    }

    /**
     * The three are tinted separately, so a path reaching more than its own layer would paint the
     * disc in the tick's colour, or the halo in the disc's. The disc and the halo make this worth
     * pinning: they are the same shape, and each holds a group called `Ellipse 1` containing a fill
     * called `Fill 1` — only the layer above them tells the two apart.
     */
    @Test
    fun `the three keypaths do not overlap`() {
        val check = resolve(checkKeyPath()).toSet()
        val disc = resolve(discKeyPath()).toSet()
        val halo = resolve(haloKeyPath()).toSet()

        assertEquals(emptySet<KeyPath>(), check intersect disc)
        assertEquals(emptySet<KeyPath>(), check intersect halo)
        assertEquals(emptySet<KeyPath>(), disc intersect halo)
    }

    /**
     * What a keypath that has stopped matching looks like: an empty list, and an animation that
     * plays in its authored colours as though nothing had been asked of it. This is the whole
     * reason the three tests above exist — the failure has no other symptom.
     */
    @Test
    fun `a keypath naming a layer the asset does not have resolves to nothing`() {
        assertEquals(emptyList<KeyPath>(), resolve(listOf("**", "circle-base", "**")))
    }
}
