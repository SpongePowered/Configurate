import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("io.gitlab.arturbosch.detekt")
    id("org.spongepowered.configurate.build.component")
    id("org.jlleitschuh.gradle.ktlint")
}

description = "Kotlin API support for Configurate"

val examples by sourceSets.registering {
    compileClasspath += sourceSets.main.get().compileClasspath
    runtimeClasspath += sourceSets.main.get().runtimeClasspath
}

dependencies {
    "examplesImplementation"(sourceSets.main.map { it.output })
    "examplesImplementation"(projects.format.yaml)
}

kotlin {
    coreLibrariesVersion = "1.4.20"
    target {
        compilations.configureEach {
            kotlinOptions {
                jvmTarget = "1.8"
                languageVersion = "1.4"
                freeCompilerArgs += listOf("-Xopt-in=kotlin.RequiresOptIn", "-Xemit-jvm-type-annotations")
            }
        }
    }
}

tasks.javadocJar.configure {
    from(tasks.dokkaJavadoc.map { it.outputs })
}

tasks.withType(DokkaTask::class).configureEach {
    moduleName.set("configurate-${project.name}")
    dokkaSourceSets.configureEach {
        includes.from(file("src/main/packages.md"))
        samples.from(
            kotlin.sourceSets.named("examples").map {
                it.kotlin.sourceDirectories
            }
        )
        jdkVersion.set(8)
        reportUndocumented.set(true)
    }
}

tasks.withType(Detekt::class).configureEach {
    config.setFrom(rootProject.file(".detekt/detekt.yml"))
}

dependencies {
    api(projects.core)
    implementation(kotlin("reflect"))
    // This version has to be kept in sync with the Kotlin plugin version
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
}
