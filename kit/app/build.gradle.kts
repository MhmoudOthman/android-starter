import java.util.Properties

plugins {
    id("app.android.application")
    // Typed navigation routes are @Serializable.
    alias(libs.plugins.kotlin.serialization)
}

// ---------------------------------------------------------------------------------------------
// Signing — read from local.properties (gitignored), falling back to environment variables for
// CI. NOTHING here may be inlined: this file is committed, and a password written here is in the
// history for good.
//
//   signing.storeFile=D:\\path\\to\\release.jks
//   signing.storePassword=...
//   signing.keyAlias=release
//   signing.keyPassword=...
// ---------------------------------------------------------------------------------------------
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun signingCredential(property: String, environmentVariable: String): String? =
    localProperties.getProperty(property) ?: System.getenv(environmentVariable)

val signingStorePath = signingCredential("signing.storeFile", "SIGNING_STORE_FILE")

// Debug builds never touch the release config, so a clone without credentials still works. Asking
// for a release OR staging build without them is the case worth catching: left alone, AGP would
// hand back an unsigned APK that fails to install with nothing explaining why.
if (signingStorePath == null &&
    gradle.startParameter.taskNames.any {
        it.contains("release", ignoreCase = true) || it.contains("staging", ignoreCase = true)
    }
) {
    error(
        "Release signing is not configured. Add signing.storeFile, signing.storePassword, " +
            "signing.keyAlias and signing.keyPassword to local.properties."
    )
}

android {
    signingConfigs {
        create("release") {
            if (signingStorePath != null) {
                storeFile = file(signingStorePath)
                storePassword = signingCredential("signing.storePassword", "SIGNING_STORE_PASSWORD")
                keyAlias = signingCredential("signing.keyAlias", "SIGNING_KEY_ALIAS")
                keyPassword = signingCredential("signing.keyPassword", "SIGNING_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // The classic pair rather than AGP 9's `optimization { enable = true }`: that DSL is
            // the incubating "gradual R8" path and refuses to configure without an opt-in flag.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        // A release build in every respect except the server: R8 on, same keep rules, same key,
        // not debuggable. BASE_URL comes from :core:network's own staging build type and the
        // cleartext exemption from app/src/staging/res/xml. It exists so obfuscated code can be
        // exercised against a backend that is safe to mutate.
        getByName("staging") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

dependencies {
    // Core
    implementation(projects.core.network)
    implementation(projects.core.datastore)
    implementation(projects.core.ui)

    // Feature modules — one implementation(...) per module, in the same order as settings.gradle.kts.
    // implementation(projects.domain.auth)
    // implementation(projects.data.auth)
    // implementation(projects.presentation.login)

    // Navigation — only :app depends on it. Presentation modules take lambdas.
    implementation(libs.navigation.compose)

    // AppModulesTest: Module.verify() proves every Koin definition resolves, at test time.
    testImplementation(libs.koin.test)
    testImplementation(kotlin("reflect"))
    testImplementation(libs.coroutines.test)
    // DataStore is an implementation detail of :core:datastore; the test names it as an extraType.
    testImplementation(libs.datastore.preferences)
}
