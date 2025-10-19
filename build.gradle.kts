plugins {
    // no root-specific plugins
}

tasks.wrapper {
    gradleVersion = providers.gradleProperty("gradleVersion").get()
}