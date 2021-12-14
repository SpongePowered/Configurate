import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
    id("org.spongepowered.configurate.build.component")
    id("io.gitlab.arturbosch.detekt")
}

description = "CLI tool to inspect Configurate's view of files"

dependencies {
    // Configurate
    implementation(projects.format.xml)
    implementation(projects.format.yaml)
    implementation(projects.format.gson)
    implementation(projects.format.hocon)
    implementation(projects.extra.extraKotlin)

    // Libraries
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.github.ajalt:clikt:2.8.0")
    implementation("org.fusesource.jansi:jansi:2.4.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs = listOf("-Xnew-inference")
}

tasks.jar {
    manifest {
        attributes(
            mapOf(
                "Specification-Version" to project.version,
                "Implementation-Version" to project.version
            )
        )
    }
}

tasks.withType(Detekt::class).configureEach {
    config.setFrom(rootProject.file(".detekt/detekt.yml"))
}

application {
    mainClass.set("org.spongepowered.configurate.tool.ToolKt")
}
