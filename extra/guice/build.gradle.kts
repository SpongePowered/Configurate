import org.spongepowered.configurate.build.core

plugins {
    id("org.spongepowered.configurate-component")
}

dependencies {
    implementation(core())
    implementation("com.google.inject:guice:4.2.3")
}
