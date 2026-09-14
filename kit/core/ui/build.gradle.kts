plugins {
    id("app.android.library.compose")
}

android {
    namespace = "com.starter.app.core.ui"
}

dependencies {
    api(projects.core.model)
    api(projects.core.designsystem)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    api(libs.androidx.lifecycle.viewmodel.savedstate)

    implementation(libs.play.services.location)

    // Animations inside the shared dialogs. implementation, not api: a dialog takes a raw-res id,
    // so no Lottie type reaches a caller's signature.
    implementation(libs.lottie.compose)
}
