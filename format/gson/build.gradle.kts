import org.spongepowered.configurate.build.core

plugins {
    id("org.spongepowered.configurate-component")
}

dependencies {
    api(core())
    implementation("com.google.code.gson:gson:2.8.0")
    testImplementation("com.google.guava:guava:29.0-jre")
}
