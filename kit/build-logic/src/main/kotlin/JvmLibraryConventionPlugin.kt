import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/**
 * `app.jvm.library` — a pure Kotlin/JVM module: every `domain:*`, plus `core:model`, `core:common`
 * and `core:testing`. No Android on the classpath, which is what keeps business rules testable
 * without Robolectric.
 */
class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("java-library")
        pluginManager.apply("org.jetbrains.kotlin.jvm")

        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JAVA_VERSION
            targetCompatibility = JAVA_VERSION
        }
        extensions.configure<KotlinJvmProjectExtension> {
            compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
        }
        configureTestTrustStore()

        dependencies {
            add("implementation", catalog.findLibrary("coroutines-core").get())
            add("implementation", catalog.findLibrary("koin-core").get())
            add("testImplementation", catalog.findLibrary("junit").get())
        }
    }
}
