package com.starter.app.core.ui.component

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

/**
 * The sibling of `SuccessAnimationKeyPathTest`, and it exists for the same reason: a tint whose
 * keypath matches nothing is applied to nothing, and Lottie reports that in no way at all — no
 * exception, no log, just the asset playing in the colours it was exported with.
 *
 * It matters more here than there. Both loading assets were renamed by hand — every fill and stroke
 * in them arrived called `Fill` / `Relleno 1` / `Trazo 1`, one name across dozens of shapes in as
 * many as seven colours — so re-exporting either JSON from the design tool drops all of it at once.
 * See the node constants in `LoadingAnimation.kt`.
 *
 * Runs on the JVM under Robolectric because parsing a raw resource needs a `Context`.
 */
@RunWith(RobolectricTestRunner::class)
class LoadingAnimationKeyPathTest {

    private fun composition(art: LoadingArt): LottieComposition = checkNotNull(
        LottieCompositionFactory
            .fromRawResSync(RuntimeEnvironment.getApplication(), art.animationRes)
            .value,
    ) { "${art.name} animation failed to parse" }

    private fun resolve(art: LoadingArt, keyPath: List<String>): List<KeyPath> {
        val drawable = LottieDrawable()
        drawable.setComposition(composition(art))
        return drawable.resolveKeyPath(KeyPath(*keyPath.toTypedArray()))
    }

    @Test
    fun `every node the car is tinted with resolves against the shipped asset`() {
        LoadingArt.CAR.paintNodes.forEach { node ->
            assertTrue(
                "loading_car.json has no shape named '$node'",
                resolve(LoadingArt.CAR, paintKeyPath(node)).isNotEmpty(),
            )
        }
    }

    @Test
    fun `every node the bike is tinted with resolves against the shipped asset`() {
        LoadingArt.BIKE.paintNodes.forEach { node ->
            assertTrue(
                "loading_bike.json has no shape named '$node'",
                resolve(LoadingArt.BIKE, paintKeyPath(node)).isNotEmpty(),
            )
        }
    }

    /**
     * Each role is painted its own colour, so a path reaching past its own shapes would paint the
     * bike's parcel in the road's colour. The pair worth pinning is `body` and `body-light`: they
     * are the same panel drawn twice, and only the name tells them apart.
     */
    @Test
    fun `no two nodes of the same art overlap`() {
        LoadingArt.entries.forEach { art ->
            val resolved = art.paintNodes.associateWith { resolve(art, paintKeyPath(it)).toSet() }
            art.paintNodes.forEachIndexed { index, node ->
                art.paintNodes.drop(index + 1).forEach { other ->
                    assertEquals(
                        "'$node' and '$other' both match a shape in ${art.name}",
                        emptySet<KeyPath>(),
                        resolved.getValue(node) intersect resolved.getValue(other),
                    )
                }
            }
        }
    }

    /**
     * What a keypath that has stopped matching looks like: an empty list, and an animation playing
     * as though nothing had been asked of it. This is the whole reason the tests above exist — the
     * failure has no other symptom.
     */
    @Test
    fun `a keypath naming a shape neither asset has resolves to nothing`() {
        LoadingArt.entries.forEach { art ->
            assertEquals(emptyList<KeyPath>(), resolve(art, paintKeyPath("Relleno 1")))
        }
    }
}
