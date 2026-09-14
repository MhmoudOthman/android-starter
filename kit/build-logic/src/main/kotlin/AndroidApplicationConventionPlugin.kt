import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * `app.android.application` — the one `:app` module. Identity, version, SDK levels, Compose and
 * the three build types come from here; signing and R8 stay in app/build.gradle.kts because they
 * are the part worth reading in one place.
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.configure<ApplicationExtension> {
            namespace = appId
            compileSdk = COMPILE_SDK
            defaultConfig {
                applicationId = appId
                minSdk = MIN_SDK
                targetSdk = TARGET_SDK
                versionCode = appVersionCode
                versionName = appVersionName
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            compileOptions {
                sourceCompatibility = JAVA_VERSION
                targetCompatibility = JAVA_VERSION
            }
            buildFeatures {
                compose = true
                buildConfig = true
            }
            buildTypes {
                getByName("debug") { versionNameSuffix = DEBUG_SUFFIX }
                getByName("release") { versionNameSuffix = RELEASE_SUFFIX }
                // A release build pointed at the staging server; app/build.gradle.kts finishes it
                // (signing, R8). Declared here so the suffix and the name are in one place.
                maybeCreate("staging").apply {
                    versionNameSuffix = STAGING_SUFFIX
                    isDebuggable = false
                }
            }
            testOptions.unitTests {
                isIncludeAndroidResources = true
                isReturnDefaultValues = true
            }
        }
        configureTestTrustStore()

        dependencies {
            add("implementation", catalog.findLibrary("androidx-core-ktx").get())
            add("implementation", catalog.findLibrary("androidx-activity-compose").get())
            add("implementation", catalog.findLibrary("androidx-lifecycle-runtime-compose").get())
            add("implementation", platform(catalog.findLibrary("compose-bom").get()))
            add("implementation", catalog.findLibrary("compose-ui").get())
            add("implementation", catalog.findLibrary("compose-material3").get())
            add("implementation", catalog.findLibrary("compose-ui-tooling-preview").get())
            add("debugImplementation", catalog.findLibrary("compose-ui-tooling").get())
            add("implementation", catalog.findLibrary("koin-android").get())
            add("implementation", catalog.findLibrary("koin-compose").get())

            add("testImplementation", catalog.findLibrary("junit").get())
            add("testImplementation", project(":core:testing"))
            add("testImplementation", catalog.findLibrary("robolectric").get())
            add("testImplementation", catalog.findLibrary("compose-ui-test-junit4").get())
            add("debugImplementation", catalog.findLibrary("compose-ui-test-manifest").get())
        }
    }
}
