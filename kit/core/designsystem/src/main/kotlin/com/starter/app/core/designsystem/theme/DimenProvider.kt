package com.starter.app.core.designsystem.theme

import androidx.annotation.DimenRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun sdp(@DimenRes res: Int): Dp {
    return dimensionResource(id = res)
}

@Composable
fun ssp(@DimenRes res: Int): TextUnit {
    return dimensionResource(id = res).value.sp
}
