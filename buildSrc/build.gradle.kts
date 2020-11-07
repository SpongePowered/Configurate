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

dependencyLocking {
    lockAllConfigurations()
}

dependencies {
    val indraVersion = "1.+"

    implementation(gradleApi())
    api("net.kyori:indra-common:$indraVersion")
    api("net.kyori:indra-publishing-sonatype:$indraVersion")
    api("gradle.plugin.org.cadixdev.gradle:licenser:0.5.+")
    api("net.ltgt.gradle:gradle-errorprone-plugin:1.+")
    api("net.ltgt.gradle:gradle-nullaway-plugin:1.+")
    api("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.22.+")
    api("org.ajoberstar.grgit:grgit-gradle:4.+")
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
