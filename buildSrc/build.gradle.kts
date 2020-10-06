plugins {
    `kotlin-dsl`
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
}

repositories {
    jcenter()
    gradlePluginPortal()
}

ktlint {
    version.set("0.39.0")
}

dependencies {
    val indraVersion = "1.0.1"

    implementation(gradleApi())
    api("net.kyori:indra-common:$indraVersion")
    api("net.kyori:indra-publishing-sonatype:$indraVersion")
    api("gradle.plugin.org.cadixdev.gradle:licenser:0.5.0")
    api("net.ltgt.gradle:gradle-errorprone-plugin:1.2.1")
    api("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.22.0")
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

ktlint {
    filter {
        // Don't validate generated code
        exclude("**/kotlin/dsl/**")
    }
}
