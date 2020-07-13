plugins {
    kotlin("jvm") version embeddedKotlinVersion
    id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
    `java-gradle-plugin`
}

repositories {
    jcenter()
    gradlePluginPortal()
}

ktlint {
    version.set("0.37.2")
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
