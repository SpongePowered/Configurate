import org.spongepowered.configurate.build.core

plugins {
    id("org.spongepowered.configurate.build.component")
}

description = "Guice support for Configurate's object mapper"

dependencies {
    implementation(core())
    // We want to remain compatible with Guice 4 since it's the last upstream version compatible with Minecraft's Guava
    implementation("com.google.inject:guice:4.1.0")
    // Run tests against Guice 5.0.1 since Sponge uses an adapted fork of it
    testRuntimeOnly("com.google.inject:guice:5.0.1")
}
