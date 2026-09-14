pluginManagement {
    // The convention plugins (app.jvm.library, app.android.library, app.android.library.compose,
    // app.android.application) — see build-logic/.
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
// `projects.core.model` instead of `project(":core:model")` in every build script.
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "AndroidStarterKit"

include(
    ":app",
    ":core:common",
    ":core:model",
    ":core:network",
    ":core:datastore",
    ":core:designsystem",
    ":core:ui",
    ":core:testing",
)

// Feature verticals go here, one line per feature. Only include the layers the feature needs —
// see CLAUDE.md §3.4. Example:
// include(":domain:auth", ":data:auth", ":presentation:login")
// include(":domain:common", ":data:common")
// include(":presentation:shell")
