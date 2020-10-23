import org.spongepowered.configurate.build.core

plugins {
    id("org.spongepowered.configurate.build.component")
}

dependencies {
    api(core())
    api("com.fasterxml.jackson.core:jackson-core:2.11.3")
    testImplementation("com.google.guava:guava:30.0-jre")
}
