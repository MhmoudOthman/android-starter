package com.starter.app.core.designsystem.dimen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

/*
 * Scalable dimensions, reading the same Intuit sdp/ssp tables the app has always used - but
 * resolving each resource id **once per process** instead of once per read.
 *
 * This exists because `ir.kaaveh.sdpcompose.sdp` does not look its dimensions up by id. Every read
 * builds the resource *name* with a StringBuilder ("_8sdp") and hands it to
 * `Resources.getIdentifier`, a string-keyed scan of the resource table that Android's own docs
 * discourage. Nothing caches it, so the cost lands on every recomposition of every caller - and
 * with ~270 call sites, an `OrderCard` alone pays it 26 times per card, on the scrolling path of
 * the busiest screen in the app.
 *
 * The ids are what get cached, deliberately, and nothing else. A resource id is fixed for the life
 * of the process, so caching it is always correct; the *value* behind it is not - it changes with
 * density and screen size - so the `dimensionResource` read stays on every call, where the
 * configuration can still reach it. That read is a lookup by integer id, which is the cheap half.
 *
 * The tables run `_1sdp`.._600sdp` and `_minus1sdp`.._minus60sdp`. Anything outside that (0
 * included - there is no `_0sdp`) falls through to a plain dp, which is what the underlying
 * library does too.
 *
 * Note the fallback on a *missing* resource is also plain dp, and that is the failure mode
 * `app/src/main/res/raw/keep.xml` exists to prevent: strip the dimens and every size in the app
 * silently becomes unscaled dp. See the shrinking section in CLAUDE.md.
 */

private const val MAX_SDP = 600
private const val MIN_SDP = -60
private const val UNRESOLVED = -1

/** Resource ids are stable for the life of the process; an int write is atomic, so no lock. */
private val positiveIds = IntArray(MAX_SDP + 1) { UNRESOLVED }
private val negativeIds = IntArray(-MIN_SDP + 1) { UNRESOLVED }

val Int.sdp: Dp
    @Composable
    @ReadOnlyComposable
    get() {
        val id = sdpResourceId(this)
        return if (id != 0) dimensionResource(id) else this.dp
    }

val Int.ssp: TextUnit
    @Composable
    @ReadOnlyComposable
    get() = with(LocalDensity.current) { this@ssp.sdp.toSp() }

// `DiscouragedApi` is lint objecting to getIdentifier, and it is right to in general — the call is
// a string-keyed scan of the resource table. It is deliberate here because there is no id to use:
// the sdp tables are ~760 generated dimens and naming every one of them as a constant is the only
// alternative. Caching the result per id is what makes it affordable; see the note at the top.
@Suppress("DiscouragedApi")
@Composable
@ReadOnlyComposable
private fun sdpResourceId(value: Int): Int {
    val cache: IntArray
    val index: Int
    val name: String

    when (value) {
        in 1..MAX_SDP -> {
            cache = positiveIds
            index = value
            name = "_${value}sdp"
        }

        in MIN_SDP..-1 -> {
            cache = negativeIds
            index = -value
            name = "_minus${-value}sdp"
        }

        else -> return 0
    }

    val cached = cache[index]
    if (cached != UNRESOLVED) return cached

    // Safe in a @ReadOnlyComposable: no groups are emitted, so reaching this conditionally cannot
    // desynchronise the slot table.
    val context = LocalContext.current
    val resolved = context.resources.getIdentifier(name, "dimen", context.packageName)
    cache[index] = resolved
    return resolved
}
