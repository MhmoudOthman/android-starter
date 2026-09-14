package com.starter.app.core.ui.component.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.Dp
import com.starter.app.core.designsystem.previews.PreviewLightDarkDevice
import com.starter.app.core.designsystem.theme.AppTheme
import com.starter.app.core.designsystem.dimen.sdp

@Composable
fun AppCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showBorder: Boolean = false,
    shape: Shape = MaterialTheme.shapes.medium,
    containerColor: Color? = null,
    contentColor: Color? = null,
    disabledContainerColor: Color? = null,
    strokeColor: Color? = null,
    strokeWidth: Dp? = null,
    disabledOpacity: Float = 0.38f,
    elevation: CardElevation = CardDefaults.cardElevation(),
    content: @Composable ColumnScope.() -> Unit,
) {
    val defaultContainerColor = containerColor ?: MaterialTheme.colorScheme.surfaceContainerHighest
    val defaultContentColor = contentColor ?: contentColorFor(defaultContainerColor)
    val defaultDisabledContainerColor = disabledContainerColor?.copy(alpha = disabledOpacity)
        ?.compositeOver(disabledContainerColor)
        ?: MaterialTheme.colorScheme.surface.copy(alpha = disabledOpacity)
            .compositeOver(defaultContainerColor)

    val cardBorder = if (showBorder) {
        val defaultStrokeColor = strokeColor ?: MaterialTheme.colorScheme.outline
        val defaultStrokeWidth = strokeWidth ?: 1.sdp
        val color =
            if (enabled) {
                defaultStrokeColor
            } else {
                defaultStrokeColor
                    .copy(alpha = disabledOpacity)
                    .compositeOver(defaultContainerColor)
            }
        remember(color) { BorderStroke(defaultStrokeWidth, color) }
    } else {
        null
    }

    Card(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = CardColors(
            containerColor = defaultContainerColor,
            contentColor = defaultContentColor,
            disabledContainerColor = defaultDisabledContainerColor,
            disabledContentColor = defaultContentColor.copy(alpha = disabledOpacity),
        ),
        border = cardBorder,
        elevation = elevation,
        content = content,
    )
}

@Composable
fun AppElevatedCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color? = null,
    contentColor: Color? = null,
    disabledOpacity: Float = 0.38f,
    elevation: Dp= 6.sdp,
    shape: Shape = MaterialTheme.shapes.medium,
    content: @Composable ColumnScope.() -> Unit,
) {
    val defaultCardElevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation)
    val defaultContainerColor = containerColor ?: MaterialTheme.colorScheme.surfaceContainerLow
    val defaultContentColor = contentColor ?: contentColorFor(defaultContainerColor)
    val disabledContainerColor = MaterialTheme.colorScheme.surface

    AppCard(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        containerColor = defaultContainerColor,
        contentColor = defaultContentColor,
        disabledContainerColor = disabledContainerColor,
        disabledOpacity = disabledOpacity,
        elevation = defaultCardElevation,
        shape = shape,
        content = content,
    )
}

/** `materialCardViewOutlinedStyle` / `Widget.OutlinedCardView` — large shape, 1sdp outline, transparent fill. */
@Composable
fun AppOutlinedCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color? = null,
    contentColor: Color? = null,
    strokeColor: Color? = null,
    strokeWidth: Dp? = null,
    disabledOpacity: Float = 0.38f,
    shape: Shape = MaterialTheme.shapes.medium,
    content: @Composable ColumnScope.() -> Unit,
) {
    val defaultContainerColor = containerColor ?: MaterialTheme.colorScheme.surface
    val defaultContentColor = contentColor ?: contentColorFor(defaultContainerColor)

    AppCard(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        showBorder = true,
        strokeColor = strokeColor,
        strokeWidth = strokeWidth,
        containerColor = defaultContainerColor,
        contentColor = defaultContentColor,
        disabledOpacity = disabledOpacity,
        shape = shape,
        content = content,
    )
}


@PreviewLightDarkDevice
@Composable
private fun PreviewCards() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.sdp),
            verticalArrangement = Arrangement.spacedBy(8.sdp),
        ) {
            AppCard({},modifier = Modifier.fillMaxWidth()) {
                Text("App card", modifier = Modifier.padding(16.sdp))
            }
            AppCard({},modifier = Modifier.fillMaxWidth(), enabled = false) {
                Text("App card", modifier = Modifier.padding(16.sdp))
            }


            AppElevatedCard({},modifier = Modifier.fillMaxWidth()) {
                Text("Elevated card", modifier = Modifier.padding(16.sdp))
            }
            AppElevatedCard({},modifier = Modifier.fillMaxWidth(), enabled = false) {
                Text("Elevated card", modifier = Modifier.padding(16.sdp))
            }


            AppOutlinedCard({},modifier = Modifier.fillMaxWidth()) {
                Text("Outlined card", modifier = Modifier.padding(16.sdp))
            }

            AppOutlinedCard({},modifier = Modifier.fillMaxWidth(), enabled = false) {
                Text("Outlined card", modifier = Modifier.padding(16.sdp))
            }
        }
    }
}
