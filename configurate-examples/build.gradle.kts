import org.spongepowered.configurate.build.configurate
import org.spongepowered.configurate.build.core

plugins {
    id("org.spongepowered.configurate-component")
    `application`
}

dependencies {
    implementation(core())
    implementation(configurate("hocon"))
    implementation(configurate("yaml"))
}

application {
    mainClassName = "org.spongepowered.configurate.examples.ValueReferences"
}

tasks.withType(AbstractPublishToMaven::class.java).configureEach {
    onlyIf { false } // don't publish
}

