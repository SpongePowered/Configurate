plugins {
    kotlin("jvm") version embeddedKotlinVersion
    id("org.jlleitschuh.gradle.ktlint") version "9.4.0"
    `java-gradle-plugin`
}

repositories {
    jcenter()
    gradlePluginPortal()
}

ktlint {
    version.set("0.39.0")
}

dependencies {
    implementation(gradleApi())
    api("gradle.plugin.org.cadixdev.gradle:licenser:0.5.0")
    api("net.ltgt.gradle:gradle-errorprone-plugin:1.2.1")
    api("de.marcphilipp.gradle:nexus-publish-plugin:0.4.0")
    api("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.22.0")
}

gradlePlugin {
    plugins {
        create("configurate-component") {
            id = "org.spongepowered.configurate-component"
            implementationClass = "org.spongepowered.configurate.build.ConfigurateDevPlugin"
        }
        create("configurate-publishing") {
            id = "org.spongepowered.configurate-publishing"
            implementationClass = "org.spongepowered.configurate.build.ConfiguratePublishingPlugin"
        }
    }
}
