import org.spongepowered.configurate.build.core

plugins {
    id("org.spongepowered.configurate.build.component")
}

description = "JSON format loader for Configurate, implemented using Jackson"

dependencies {
    api(core())
    api("com.fasterxml.jackson.core:jackson-core:2.+") {
        attributes {
            // Require that we're an actual release...
            // Too bad Gradle doesn't properly expose this attribute
            attribute(Attribute.of("org.gradle.status", String::class.java), "release")
        }
    }
    testImplementation("com.google.guava:guava:latest.release")
}
