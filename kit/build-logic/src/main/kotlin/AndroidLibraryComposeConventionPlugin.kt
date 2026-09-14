import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * `app.android.library.compose` — `app.android.library` plus Compose: every `presentation:*`,
 * plus `core:ui` and `core:designsystem`.
 *
 * Also wires the JVM (Robolectric) test setup every presentation module needs, so a screen test
 * works the moment the module exists — the three flags below each fail unhelpfully on their own.
 */
class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("app.android.library")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.configure<LibraryExtension> {
            buildFeatures.compose = true
            testOptions.unitTests {
                // ViewModels (and Lottie's parser) log through android.util.Log; the stubbed
                // android.jar throws on every call unless allowed to return a default.
                isReturnDefaultValues = true
                // Screen tests assert on the strings the user reads, not the stub's empty ones.
                isIncludeAndroidResources = true
            }
        }

        dependencies {
            add("implementation", platform(catalog.findLibrary("compose-bom").get()))
            add("implementation", catalog.findLibrary("compose-ui").get())
            add("implementation", catalog.findLibrary("compose-material3").get())
            add("implementation", catalog.findLibrary("compose-ui-tooling-preview").get())
            add("debugImplementation", catalog.findLibrary("compose-ui-tooling").get())
            add("implementation", catalog.findLibrary("androidx-activity-compose").get())
            add("implementation", catalog.findLibrary("androidx-lifecycle-viewmodel-compose").get())
            add("implementation", catalog.findLibrary("androidx-lifecycle-runtime-compose").get())
            add("implementation", catalog.findLibrary("koin-compose").get())

            // Compose tests run on the JVM under Robolectric so `./gradlew test` (and CI) covers
            // them. The SDK it emulates is pinned once, in :core:testing's robolectric.properties.
            add("testImplementation", project(":core:testing"))
            add("testImplementation", catalog.findLibrary("robolectric").get())
            add("testImplementation", catalog.findLibrary("compose-ui-test-junit4").get())
            // debugImplementation, NOT testImplementation: unit tests run against the debug
            // variant's merged manifest, and this declares the activity the Compose rule launches.
            add("debugImplementation", catalog.findLibrary("compose-ui-test-manifest").get())
        }
    }
}
