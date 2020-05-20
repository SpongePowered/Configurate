import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.spongepowered.configurate.build.core

plugins {
    kotlin("jvm")
    id("org.spongepowered.configurate-component")
    id("org.jlleitschuh.gradle.ktlint")
}

tasks.withType(KotlinCompile::class).configureEach {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs += listOf("-Xopt-in=kotlin.RequiresOptIn")
}

dependencies {
    api(core())
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5")
}
