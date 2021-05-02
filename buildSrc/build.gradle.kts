plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencyLocking {
    lockAllConfigurations()
}

dependencies {
    constraints {
        implementation("com.github.siom79.japicmp:japicmp") {
            version { require("0.15.+") }
            because("Use a newer version than the japicmp gradle plugin provides")
        }
    }
    val indraVersion = "2.+"

    implementation(gradleApi())
    api("net.kyori:indra-common:$indraVersion")
    api("net.kyori:indra-publishing-sonatype:$indraVersion")
    api("net.kyori:indra-git:$indraVersion")
    api("gradle.plugin.org.cadixdev.gradle:licenser:0.6.+")
    api("net.ltgt.gradle:gradle-errorprone-plugin:2.+")
    api("net.ltgt.gradle:gradle-nullaway-plugin:1.+")
    api("me.champeau.gradle:japicmp-gradle-plugin:0.2.+")
    api("de.thetaphi:forbiddenapis:3.+")
    implementation("com.google.guava:guava:+")
}
