import org.spongepowered.configurate.build.configurate
import org.spongepowered.configurate.build.core

plugins {
    id("org.spongepowered.configurate-component")
}

dependencies {
    implementation(core())
    implementation(configurate("hocon"))
    implementation(configurate("yaml"))
}

tasks.withType(AbstractPublishToMaven::class.java).configureEach {
    onlyIf { false } // don't publish
}

