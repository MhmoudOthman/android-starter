plugins {
    id("app.android.library")
}

android {
    namespace = "com.starter.app.core.datastore"
}

dependencies {
    implementation(projects.core.common)
    // api: SessionState is in SessionGuard's public signature.
    api(projects.core.model)

    implementation(libs.datastore.preferences)

    // No Robolectric: SessionGuard is plain Kotlin, and AppDataStoreImpl takes a DataStore rather
    // than a Context, so a real preferences store over a temp file runs on the JVM.
    testImplementation(libs.coroutines.test)
}
