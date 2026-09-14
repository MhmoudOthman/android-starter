@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.starter.app.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.starter.app.core.designsystem.previews.PreviewLightDarkDeviceSystemUi
import com.starter.app.core.designsystem.theme.AppTheme
import com.starter.app.core.ui.component.cards.AppElevatedCard
import com.starter.app.core.designsystem.dimen.sdp

/**
 * A non-dismissible full-screen loading dialog showing a looping illustration and an optional label.
 * Tint the card and text via [color]; the convenience wrappers below pick theme colors.
 *
 * **The animation is painted from [color] and [contentColor], not from its own palette.** Both
 * assets ship in colours of their own — the car in a flat `#005C99` blue, the bike in amber, pink
 * and mint — and this card is `secondary`, `tertiary` or `error` depending on which wrapper the
 * caller reached for, so nothing fixed reads correctly on all three. [LoadingArtDefaults.colors]
 * ramps the card's own content colour down to its container instead; pass [artColors] to override
 * the whole palette, or [art] to swap the illustration.
 */
@Composable
fun LoadingOverlay(
    modifier: Modifier = Modifier,
    text: String? = null,
    color: Color = MaterialTheme.colorScheme.secondary,
    contentColor: Color = contentColorFor(color),
    art: LoadingArt = LoadingArt.CAR,
    artColors: LoadingArtColors = LoadingArtDefaults.colors(
        content = contentColor,
        container = color,
    ),
) {
    Dialog(
        onDismissRequest = {}, // تمنع القفل
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false // fullscreen
        )
    ) {

        AppElevatedCard(containerColor = color, contentColor = contentColor) {
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(4.sdp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LoadingAnimation(
                    art = art,
                    colors = artColors,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .progressSemantics()
                        .heightIn(max = 48.sdp)
                        .aspectRatio(1f),
                )
                text?.let {
                    Text(
                        modifier = Modifier.padding(end = 8.sdp),
                        text = it,
//                        color = color,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun SecondaryLoadingOverlay(text: String? = null, art: LoadingArt = LoadingArt.CAR) =
    LoadingOverlay(text = text, color = MaterialTheme.colorScheme.secondary, art = art)

@Composable
fun TertiaryLoadingOverlay(text: String? = null, art: LoadingArt = LoadingArt.CAR) =
    LoadingOverlay(text = text, color = MaterialTheme.colorScheme.tertiaryContainer, art = art)

@Composable
fun RedLoadingOverlay(text: String? = null, art: LoadingArt = LoadingArt.CAR) =
    LoadingOverlay(text = text, color = MaterialTheme.colorScheme.error, art = art)

@PreviewLightDarkDeviceSystemUi
@Composable
private fun PreviewLoadingOverlay() {
    AppTheme {
        LoadingOverlay(text = "Loading")
    }
}

/**
 * The animation on its own, on each of the three cards it can land on. A [Dialog] draws in its own
 * window, which Layoutlib cannot render, so the overlay previews above come up blank in the IDE —
 * this is where the tints are actually eyeballed.
 */
@PreviewLightDarkDeviceSystemUi
@Composable
private fun PreviewLoadingArt() {
    AppTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(4.sdp)) {
            listOf(
                MaterialTheme.colorScheme.secondary,
                MaterialTheme.colorScheme.tertiary,
                MaterialTheme.colorScheme.error,
            ).forEach { container ->
                LoadingArt.entries.forEach { art ->
                    AppElevatedCard(
                        containerColor = container,
                        contentColor = contentColorFor(container),
                    ) {
                        LoadingAnimation(
                            art = art,
                            colors = LoadingArtDefaults.colors(
                                content = contentColorFor(container),
                                container = container,
                            ),
                            modifier = Modifier
                                .heightIn(max = 48.sdp)
                                .aspectRatio(1f),
                        )
                    }
                }
            }
        }
    }
}
