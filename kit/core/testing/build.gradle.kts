plugins {
    id("app.jvm.library")
}

dependencies {
    // api, not implementation: this module exists to be consumed from a `testImplementation`, and
    // every consumer needs JUnit's annotations and runTest on its own compile classpath anyway.
    api(libs.junit)
    api(libs.coroutines.test)
}
