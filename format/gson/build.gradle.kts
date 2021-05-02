plugins {
    id("org.spongepowered.configurate.build.component")
}

description = "JSON loader for Configurate, implemented using Gson"

dependencies {
    api(projects.core)
    // version must be kept in sync with MC's version
    implementation("com.google.code.gson:gson:2.8.0")
    testImplementation("com.google.guava:guava:latest.release")
}
