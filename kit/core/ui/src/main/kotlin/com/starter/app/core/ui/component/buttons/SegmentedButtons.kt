package com.starter.app.core.ui.component.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_TYPE_NORMAL
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.starter.app.core.designsystem.theme.AppTheme
import com.starter.app.core.designsystem.R as DesignSystemR
import com.starter.app.core.designsystem.theme.LocalExtendedColorScheme
import com.starter.app.core.designsystem.dimen.sdp

@Composable
fun AppSegmentedButtons(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    activeContainerColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
    activeContentColor: Color = MaterialTheme.colorScheme.onTertiaryContainer,
    activeBorderColor: Color = MaterialTheme.colorScheme.tertiary,
    inactiveContentColor: Color = MaterialTheme.colorScheme.tertiary,
    inactiveBorderColor: Color = MaterialTheme.colorScheme.tertiary,
) {
    val colors = SegmentedButtonDefaults.colors(
        activeContainerColor = activeContainerColor,
        activeContentColor = activeContentColor,
        activeBorderColor = activeBorderColor,
        inactiveContentColor = inactiveContentColor,
        inactiveBorderColor = inactiveBorderColor,
    )
    // Rebuilt, not just recomposed, when the direction or the labels change.
    //
    // `SegmentedButtonContent` caches its measure policy in a bare `remember`, and that policy holds
    // the offset it uses to centre a label with no icon next to it — measured once, then animated to
    // any later value. It also places with `place()`, which is absolute rather than direction-aware.
    // Switching language recomposes this row without ever removing it from the composition, so the
    // cache survives holding an offset measured for the previous labels in the previous direction,
    // and the row renders visibly wrong until something disposes it. Keying rebuilds the nodes,
    // which is what leaving the screen and coming back used to do by hand.
    key(LocalLayoutDirection.current, options) {
        SingleChoiceSegmentedButtonRow(modifier = modifier) {
            options.forEachIndexed { index, label ->
                SegmentedButton(
                    selected = index == selectedIndex,
                    onClick = { onSelect(index) },
                    enabled = enabled,
                    shape = SegmentedButtonDefaults.itemShape(
                        index,
                        options.size,
                        MaterialTheme.shapes.small,
                    ),
                    colors = colors,
                    icon = {}
                ) {
                    Text(text = label, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@Composable
private fun PreviewAppSegmentedButtons() {
    AppTheme {
        AppSegmentedButtons(
            options = listOf(stringResource(DesignSystemR.string.active), stringResource(DesignSystemR.string.history)),
            selectedIndex = 0,
            onSelect = {},
        )
    }
}