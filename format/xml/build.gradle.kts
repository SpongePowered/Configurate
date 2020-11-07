import org.spongepowered.configurate.build.core

plugins {
    id("org.spongepowered.configurate.build.component")
}

description = "XML format loader for Configurate"

dependencies {
    api(core())
    testImplementation("com.google.guava:guava:latest.release")
}
