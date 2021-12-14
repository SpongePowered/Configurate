plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    constraints {
        implementation("com.github.siom79.japicmp:japicmp") {
            version { require("0.15.+") }
            because("Use a newer version than the japicmp gradle plugin provides")
        }
    }
    val indraVersion = "2.0.6"

    implementation(gradleApi())
    api("net.kyori:indra-common:$indraVersion")
    api("net.kyori:indra-publishing-sonatype:$indraVersion")
    api("net.kyori:indra-git:$indraVersion")
    api("gradle.plugin.org.cadixdev.gradle:licenser:0.6.1")
    api("net.ltgt.gradle:gradle-errorprone-plugin:2.0.2")
    api("net.ltgt.gradle:gradle-nullaway-plugin:1.2.0")
    api("me.champeau.gradle:japicmp-gradle-plugin:0.3.0")
    api("de.thetaphi:forbiddenapis:3.2")
    implementation("com.google.guava:guava:31.0.1-jre")
}
