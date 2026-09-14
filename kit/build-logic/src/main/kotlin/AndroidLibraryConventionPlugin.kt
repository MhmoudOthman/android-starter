import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * `app.android.library` — an Android library without Compose: every `data:*`, plus `core:network`
 * and `core:datastore`. Each module still sets its own `android.namespace`.
 *
 * Mirrors APPLICATION_ID / VERSION_CODE / VERSION_NAME into the module's BuildConfig (with the
 * build-type suffix on the name) so code reads `BuildConfig.VERSION_NAME` identically anywhere, and
 * declares the `staging` build type — every library has to, or :app's staging variant would fall
 * back to the library's *release* variant and resolve :core:network to production.
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")

        extensions.configure<LibraryExtension> {
            compileSdk = COMPILE_SDK
            defaultConfig {
                minSdk = MIN_SDK
                buildConfigField("String", "APPLICATION_ID", appId.quoted())
                buildConfigField("int", "VERSION_CODE", appVersionCode.toString())
            }
            compileOptions {
                sourceCompatibility = JAVA_VERSION
                targetCompatibility = JAVA_VERSION
            }
            buildFeatures.buildConfig = true
            buildTypes {
                getByName("debug") {
                    buildConfigField("String", "VERSION_NAME", (appVersionName + DEBUG_SUFFIX).quoted())
                }
                getByName("release") {
                    buildConfigField("String", "VERSION_NAME", (appVersionName + RELEASE_SUFFIX).quoted())
                }
                maybeCreate("staging").apply {
                    buildConfigField("String", "VERSION_NAME", (appVersionName + STAGING_SUFFIX).quoted())
                }
            }
        }
        configureTestTrustStore()

        dependencies {
            add("implementation", catalog.findLibrary("androidx-core-ktx").get())
            add("implementation", catalog.findLibrary("coroutines-android").get())
            add("implementation", catalog.findLibrary("koin-core").get())
            add("implementation", catalog.findLibrary("koin-android").get())
            add("testImplementation", catalog.findLibrary("junit").get())
        }
    }
}
