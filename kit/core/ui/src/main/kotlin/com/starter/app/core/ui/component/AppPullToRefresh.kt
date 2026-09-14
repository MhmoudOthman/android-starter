@file:OptIn(ExperimentalMaterial3Api::class)

package com.starter.app.core.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.util.lerp
import com.starter.app.core.designsystem.dimen.sdp
import com.starter.app.core.designsystem.R as DesignSystemR

/**
 * Pull-to-refresh that drags the *content* down with the finger rather than sliding something over
 * a screen that stays put.
 *
 * The gesture shows no spinner — only an arrow saying a refresh is armed. The actual load belongs
 * to the caller: it reacts to [onRefresh] by raising its own loading state, which puts the usual
 * blocking [LoadingOverlay] on screen. So there is exactly one loading visual in the app, and it
 * appears *after* the release, never during the drag.
 *
 * Because of that split this composable has no `isRefreshing` — the content always springs back as
 * soon as the finger lifts, and the overlay takes it from there.
 *
 * The [content] must own a scrollable that fills the box — a `LazyColumn` with `fillMaxSize()` on
 * the orders list, a `Column` with `verticalScroll` on order details — **even when it is empty**:
 * the gesture rides that child's nested-scroll connection, so the scrollable still has to be there
 * for the pull to work on a list with no rows or a screen whose load failed. That last case is the
 * one worth protecting, since it is exactly where a driver would pull to retry.
 */
@Composable
fun AppPullToRefreshBox(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    state: PullToRefreshState = rememberPullToRefreshState(),
    color: Color = MaterialTheme.colorScheme.tertiary,
    contentColor: Color = contentColorFor(color),
    content: @Composable BoxScope.() -> Unit,
) {
    val thresholdPx = with(LocalDensity.current) {
        PullToRefreshDefaults.PositionalThreshold.toPx()
    }

    PullToRefreshBox(
        isRefreshing = false,
        onRefresh = onRefresh,
        modifier = modifier,
        state = state,
        indicator = {
            PullToRefreshHint(
                state = state,
                color = color,
                contentColor = contentColor,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // The content rides the drag, so the gesture reads as pulling the screen itself
                // down and the hint is revealed in the gap it leaves behind. Read inside
                // graphicsLayer so the offset lands at draw time — no recomposition per frame.
                .graphicsLayer { translationY = state.pullOffset(thresholdPx) },
            content = content,
        )
    }
}

/**
 * The pill shown while dragging: an arrow plus a line of text. It fades and scales in as the gap
 * opens and stays centred in it; the moment the pull passes the release threshold the arrow flips
 * to point up, the label switches to [releaseLabel], and a haptic ticks. Past that point, letting
 * go refreshes.
 */
@Composable
fun PullToRefreshHint(
    state: PullToRefreshState,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.tertiary,
    contentColor: Color = contentColorFor(color),
    pullLabel: String = stringResource(DesignSystemR.string.pull_to_refresh),
    releaseLabel: String = stringResource(DesignSystemR.string.release_to_refresh),
) {
    val thresholdPx = with(LocalDensity.current) {
        PullToRefreshDefaults.PositionalThreshold.toPx()
    }
    // derivedStateOf so crossing the threshold recomposes once, instead of every frame of the drag.
    val armed by remember(state) { derivedStateOf { state.distanceFraction >= 1f } }
    val rotation by animateFloatAsState(if (armed) 180f else 0f, label = "PullArrowRotation")
    var hintHeight by remember { mutableIntStateOf(0) }

    val haptics = LocalHapticFeedback.current
    LaunchedEffect(armed) {
        if (armed) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Surface(
        shape = CircleShape,
        color = color,
        contentColor = contentColor,
        modifier = modifier
            .onSizeChanged { hintHeight = it.height }
            .graphicsLayer {
                // Sit in the middle of the gap the content just opened up.
                translationY = (state.pullOffset(thresholdPx) - hintHeight) / 2f
                val appear = state.distanceFraction.coerceIn(0f, 1f)
                alpha = appear
                val scale = lerp(MIN_HINT_SCALE, 1f, appear)
                scaleX = scale
                scaleY = scale
            },
    ) {
        Row(
            // The two labels are different lengths, so let the pill resize into the new one
            // instead of snapping wider at the threshold.
            modifier = Modifier
                .animateContentSize()
                .padding(horizontal = 10.sdp, vertical = 5.sdp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.sdp),
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowDownward,
                contentDescription = null,
                modifier = Modifier
                    .size(14.sdp)
                    .graphicsLayer { rotationZ = rotation },
            )
            Text(
                text = if (armed) releaseLabel else pullLabel,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
            )
        }
    }
}

/** How far the content has travelled, in px, for a given refresh [thresholdPx]. */
private fun PullToRefreshState.pullOffset(thresholdPx: Float): Float =
    distanceFraction.coerceIn(0f, MAX_PULL_FRACTION) * thresholdPx

/**
 * How far past the refresh threshold the content may travel. The gesture is already half-damped by
 * the pull modifier; this only stops a hard fling from shoving the content most of the way off
 * screen.
 */
private const val MAX_PULL_FRACTION = 1.5f

/** The hint starts small and grows into place rather than popping in at full size. */
private const val MIN_HINT_SCALE = 0.6f
