// Nothing is configured here. Every module applies one of the convention plugins from build-logic/
// (app.jvm.library, app.android.library, app.android.library.compose, app.android.application),
// which is where compileSdk, minSdk, JVM level, BuildConfig fields, the `staging` build type and the
// shared dependencies live — once. App identity and version are in gradle.properties (`app.*`);
// signing is in app/build.gradle.kts and reads local.properties.
//
// `apply false` keeps these plugins on the build classpath so the convention plugins (which only
// `compileOnly` them) can apply them by id.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.compose.compiler) apply false
}
