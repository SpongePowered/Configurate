plugins {
    kotlin("jvm") version embeddedKotlinVersion
    `java-gradle-plugin`
}

repositories {
    jcenter()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    api("gradle.plugin.net.minecrell:licenser:0.4.1")
    api("de.marcphilipp.gradle:nexus-publish-plugin:0.4.0")
    api("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.21.2")
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
