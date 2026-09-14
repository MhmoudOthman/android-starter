package com.starter.app.core.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_TYPE_NORMAL
import androidx.compose.ui.tooling.preview.Preview
import com.starter.app.core.designsystem.theme.AppTheme

import com.starter.app.core.designsystem.dimen.sdp

@Composable
fun AppCheckBox(
    modifier: Modifier = Modifier,
    label: String,
    colors: CheckboxColors = CheckboxDefaults.colors(),
    checkedState: Boolean,
    onStateChange: (Boolean) -> Unit
) {
    Row(
        modifier = modifier
            .height(40.sdp)
            .toggleable(
                value = checkedState,
                onValueChange = { onStateChange(it) },
                role = Role.Checkbox,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Checkbox(
            colors = colors,
            checked = checkedState,
            onCheckedChange = null
        )
        Text(
            text = label,
            color = colors.checkedBoxColor,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 10.sdp),
        )
    }
}

@Composable
fun AppCheckBox(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    @StringRes label: Int,
    colors: CheckboxColors = CheckboxDefaults.colors(),
    checkedState: Boolean,
    onStateChange: (Boolean) -> Unit
) {
    Row(
        modifier = modifier
            .height(35.sdp)
//            .toggleable(
//                value = checkedState,
//                onValueChange = { onStateChange(it) },
//                role = Role.Checkbox,
//            )
        ,
        verticalAlignment = verticalAlignment,
        horizontalArrangement = horizontalArrangement
    ) {
        Checkbox(
            colors = colors,
            checked = checkedState,
            onCheckedChange = { onStateChange(it) }
        )
        Text(
            text = stringResource(label),
            color = colors.checkedBoxColor,
            style = MaterialTheme.typography.bodyLarge,
//            modifier = Modifier.padding(start = 3.sdp),
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@Composable
private fun PreviewCheckBox() {
    AppTheme {
        AppCheckBox(
            label = "Label",
            colors = CheckboxDefaults.colors().copy(
                checkedCheckmarkColor = MaterialTheme.colorScheme.onSecondaryContainer,
                uncheckedCheckmarkColor = MaterialTheme.colorScheme.onSecondaryContainer,
                checkedBoxColor = MaterialTheme.colorScheme.secondaryContainer,
                checkedBorderColor = MaterialTheme.colorScheme.secondaryContainer,
                uncheckedBorderColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            checkedState = false,
            onStateChange = {}
        )
    }
}
