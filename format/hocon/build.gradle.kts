import org.spongepowered.configurate.build.core

plugins {
    id("org.spongepowered.configurate.build.component")
}

dependencies {
    api(core())
    implementation("com.typesafe:config:1.+")
    testImplementation("com.google.guava:guava:latest.release")
}
