// The convention plugins are an *included build*: they compile once and every module applies them
// by id (`app.jvm.library`, `app.android.library`, `app.android.library.compose`,
// `app.android.application`). This replaces both the repeated android { } blocks in every module and
// the old root `subprojects { }` cross-project configuration, which project isolation forbids.
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    // The same catalog the main build uses, so plugin code reads libs.findLibrary("compose-ui").
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
rootProject.name = "build-logic"
