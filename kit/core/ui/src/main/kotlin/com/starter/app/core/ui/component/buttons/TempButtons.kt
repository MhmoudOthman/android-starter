package com.starter.app.core.ui.component.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_TYPE_NORMAL
import androidx.compose.ui.tooling.preview.Preview
import com.starter.app.core.designsystem.theme.AppTheme
import com.starter.app.core.designsystem.theme.LocalExtendedColorScheme
import com.starter.app.core.designsystem.dimen.sdp


/** `Widget.Button.OutlinedButton` — outlined, primary content. */
@Composable
fun OutlinedAppButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(35.sdp),
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
    ) {
        icon?.let {
            it()
            Spacer(modifier = Modifier.width(8.sdp))
        }
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}

/** `Widget.Button.TextButton` — borderless text button. */
@Composable
fun TextAppButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.height(35.sdp),
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
    ) {
        icon?.let {
            it()
            Spacer(modifier = Modifier.width(8.sdp))
        }
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}

/** `Widget.Button.ElevatedButton` / `ThemeOverlay.Button.ElevatedButton` — secondary container. */
@Composable
fun ElevatedAppButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier.height(35.sdp),
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    ) {
        icon?.let {
            it()
            Spacer(modifier = Modifier.width(8.sdp))
        }
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}

/** `Widget.Button.FilledIcon` / `ThemeOverlay.Button.FilledIcon` — round tertiary icon button. */
@Composable
fun FilledIconAppButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    FilledIconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = CircleShape,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary,
        ),
    ) {
        content()
    }
}

/**
 * `Widget.SegmentButton` / `ThemeOverlay.SegmentButton` — single-choice segmented control.
 * The selected segment uses the extended [LocalExtendedColorScheme] `selectedContainer` color,
 * with tertiary outline/content, matching the Views overlay.
 */
//@Composable
//fun AppSegmentedButtons(
//    options: List<String>,
//    selectedIndex: Int,
//    onSelect: (Int) -> Unit,
//    modifier: Modifier = Modifier,
//    enabled: Boolean = true,
//) {
//    SingleChoiceSegmentedButtonRow(modifier = modifier) {
//        options.forEachIndexed { index, label ->
//            SegmentedButton(
//                selected = index == selectedIndex,
//                onClick = { onSelect(index) },
//                enabled = enabled,
//                shape = SegmentedButtonDefaults.itemShape(index, options.size),
//                colors = SegmentedButtonDefaults.colors(
//                    activeContainerColor = LocalExtendedColorScheme.current.selectedContainer,
//                    activeContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
//                    activeBorderColor = MaterialTheme.colorScheme.tertiary,
//                    inactiveContentColor = MaterialTheme.colorScheme.tertiary,
//                    inactiveBorderColor = MaterialTheme.colorScheme.tertiary,
//                ),
//            ) {
//                Text(text = label, fontWeight = FontWeight.Bold)
//            }
//        }
//    }
//}


@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@Composable
private fun PreviewOtherButtons() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.sdp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.sdp)
        ) {
            OutlinedAppButton("Outlined") {}
            TextAppButton("Text") {}
            ElevatedAppButton("Elevated") {}
            FilledIconAppButton(
                content = { Icon(Icons.Rounded.Search, contentDescription = "Search") },
                onClick = {},
            )
//            AppSegmentedButtons(
//                options = listOf("One", "Two", "Three"),
//                selectedIndex = 0,
//                onSelect = {},
//            )
        }
    }
}
