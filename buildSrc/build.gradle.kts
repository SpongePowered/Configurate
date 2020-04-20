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
    api("org.checkerframework:checkerframework-gradle-plugin:0.4.14")
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
