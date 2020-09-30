import org.spongepowered.configurate.build.core

plugins {
    id("org.spongepowered.configurate-component")
}

dependencies {
    api(core())
    api("com.fasterxml.jackson.core:jackson-core:2.11.1")
    testImplementation("com.google.guava:guava:29.0-jre")
}
