import org.spongepowered.configurate.build.core

plugins {
    id("org.spongepowered.configurate-component")
}

dependencies {
    api(core())
    implementation("com.typesafe:config:1.4.0")
    testImplementation("com.google.guava:guava:29.0-jre")
}
