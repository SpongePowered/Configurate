import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.spongepowered.configurate.build.core

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("org.spongepowered.configurate.build.component")
    id("org.jlleitschuh.gradle.ktlint")
}

tasks.withType(KotlinCompile::class).configureEach {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs += listOf("-Xopt-in=kotlin.RequiresOptIn", "-Xemit-jvm-type-annotations")
}

tasks.javadocJar.configure {
    from(tasks.dokkaJavadoc.map { it.outputs })
}

tasks.withType(DokkaTask::class).configureEach {
    moduleName.set("configurate-${project.name}")
    dokkaSourceSets.configureEach {
        includes.from(file("src/main/packages.md"))
        jdkVersion.set(8)
        reportUndocumented.set(true)
    }
}

dependencies {
    api(core())
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
}
