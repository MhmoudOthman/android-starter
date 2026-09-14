package com.starter.app.core.designsystem.previews

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Configuration.UI_MODE_TYPE_NORMAL
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview


@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Preview(
    name = "Light",
    uiMode = UI_MODE_TYPE_NORMAL,
    showSystemUi = false,
    showBackground = true,
    device = Devices.PIXEL_3,
    backgroundColor = 0xFFFDF8F8,
)
@Preview(
    name = "Dark",
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL,
    showSystemUi = false,
    showBackground = true,
    device = Devices.PIXEL_3,
    backgroundColor = 0xFF141313,
)
annotation class PreviewLightDarkDevice()



@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Preview(
    name = "Light",
    uiMode = UI_MODE_TYPE_NORMAL,
    showSystemUi = true,
    showBackground = true,
    device = Devices.PIXEL_3,
    backgroundColor = 0xFFFDF8F8,
)
@Preview(
    name = "Dark",
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL,
    showSystemUi = true,
    showBackground = true,
    device = Devices.PIXEL_3,
    backgroundColor = 0xFF141313,
)
annotation class PreviewLightDarkDeviceSystemUi()