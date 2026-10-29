plugins {
    id("app.android.library.compose")
}

android {
    namespace = "com.starter.app.core.designsystem"
}

dependencies {
    api(projects.core.common)

    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // The Intuit resource tables only (the `_Nsdp` / `_Nssp` dimens); `dimen/Sdp.kt` reads them
    // with the id cached per process.
    api(libs.ssp)
    api(libs.sdp)
    api(libs.compose.material.icons.core)
    // R8 keeps the unused icons out of the APK; anything api'd here reaches every module.
    api(libs.compose.material.icons.extended)
    api(libs.icons.material.symbols.rounded)
    api(libs.icons.material.symbols.rounded.filled)
    implementation(libs.compose.ui.graphics)
}
