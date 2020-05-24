plugins {
    kotlin("jvm") version embeddedKotlinVersion
    id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
    `java-gradle-plugin`
}

repositories {
    jcenter()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    api("gradle.plugin.net.minecrell:licenser:0.4.1")
    api("net.ltgt.gradle:gradle-errorprone-plugin:1.1.1")
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
