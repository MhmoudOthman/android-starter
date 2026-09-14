import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

/** The `gradle/libs.versions.toml` catalog, for plugin code that adds dependencies by alias. */
internal val Project.catalog: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

/** One place for the JVM level every module compiles to. */
internal val JAVA_VERSION: JavaVersion = JavaVersion.VERSION_11
internal const val COMPILE_SDK = 37
internal const val MIN_SDK = 26
internal const val TARGET_SDK = 37

/**
 * App identity and version, read from `gradle.properties` (`app.applicationId`, `app.versionName`,
 * `app.versionCode`). Properties rather than `rootProject.extra`: a plugin may not reach into
 * another project's state under project isolation, and a property is still a reviewable diff.
 */
internal val Project.appId: String get() = providers.gradleProperty("app.applicationId").get()
internal val Project.appVersionName: String get() = providers.gradleProperty("app.versionName").get()
internal val Project.appVersionCode: Int get() = providers.gradleProperty("app.versionCode").get().toInt()

/** Build-type suffixes. Keep every suffix BEHIND the first '-' — version comparisons split on it. */
internal const val DEBUG_SUFFIX = "-Dev"
internal const val RELEASE_SUFFIX = "-Release"
internal const val STAGING_SUFFIX = "-Staging"

/** Quotes a Kotlin string for `buildConfigField("String", ...)`. */
internal fun String.quoted(): String = "\"$this\""

/**
 * Robolectric downloads the platform jar it emulates on first run, from a forked test JVM that
 * knows nothing about Gradle's trust store. Behind a corporate proxy that fetch dies on an
 * SSLHandshakeException. Windows-only: `Windows-ROOT` is where such a proxy's certificate lives
 * there, and it is not a store type Linux (CI) has.
 */
internal fun Project.configureTestTrustStore() {
    tasks.withType<Test>().configureEach {
        if (System.getProperty("os.name").startsWith("Windows")) {
            systemProperty("javax.net.ssl.trustStoreType", "Windows-ROOT")
        }
    }
}
