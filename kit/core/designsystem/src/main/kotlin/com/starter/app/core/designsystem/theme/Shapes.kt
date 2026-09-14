package com.starter.app.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import com.starter.app.core.designsystem.dimen.sdp

//@Composable
//fun appShapes() = Shapes(
//    extraSmall =RoundedCornerShape(sdp(R.dimen._3sdp)),
//    small = RoundedCornerShape(sdp(R.dimen._6sdp)),
//    medium = RoundedCornerShape(sdp(R.dimen._8sdp)),
//    large = RoundedCornerShape(sdp(R.dimen._10sdp)),
//    extraLarge = RoundedCornerShape(sdp(R.dimen._12sdp))
//
//)

@Composable
fun appShapes() = Shapes(
    extraSmall =RoundedCornerShape(3.sdp),
    small = RoundedCornerShape(6.sdp),
    medium = RoundedCornerShape(8.sdp),
    large = RoundedCornerShape(10.sdp),
    extraLarge = RoundedCornerShape(12.sdp)

)