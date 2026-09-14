plugins {
    id("app.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.starter.app.core.network"

    // The server is a build-type field, never a value in code: a release binary carries no string
    // that points at staging. The cleartext exemption for an HTTP staging host lives in
    // app/src/debug/res/xml/network_security_config.xml (and app/src/staging/...), never in main.
    buildTypes {
        getByName("debug") {
            buildConfigField("String", "BASE_URL", "\"https://staging.example.com/api/\"")
        }
        getByName("release") {
            buildConfigField("String", "BASE_URL", "\"https://api.example.com/api/\"")
        }
        getByName("staging") {
            buildConfigField("String", "BASE_URL", "\"https://staging.example.com/api/\"")
        }
    }
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.common)

    // api: a feature's XxxAPIImpl (in :data:<feature>) takes the HttpClient.
    api(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.kotlinx.serialization.json)
}
