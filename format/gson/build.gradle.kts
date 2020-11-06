import org.spongepowered.configurate.build.core

plugins {
    id("org.spongepowered.configurate.build.component")
}

dependencies {
    api(core())
    // version must be kept in sync with MC's version
    implementation("com.google.code.gson:gson:2.8.0")
    testImplementation("com.google.guava:guava:latest.release")
}
