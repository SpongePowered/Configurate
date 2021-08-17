import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.spongepowered.configurate-component")
}

tasks.withType(KotlinCompile::class).configureEach {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs += listOf("-Xopt-in=kotlin.RequiresOptIn")
}

tasks.jar {
    manifest.attributes(
            "Automatic-Module-Name" to "org.spongepowered.configurate.extra.kotlin"
    )
}

dependencies {
    api(project(":configurate-core"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5")
}
