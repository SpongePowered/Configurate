import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.3.71"
}


dependencies {
    // Configurate
    implementation(project(":configurate-xml"))
    implementation(project(":configurate-yaml"))
    implementation(project(":configurate-gson"))
    implementation(project(":configurate-hocon"))
    implementation(project(":configurate-ext-kotlin"))

    // Libraries
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.github.ajalt:clikt:2.6.0")
    implementation("com.github.ajalt:mordant:1.2.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs = listOf("-Xnew-inference")
}

tasks.jar {
    manifest {
        attributes(
                mapOf("Specification-Version" to project.version,
                "Implementation-Version" to project.version)
        )
    }
}

application {
    mainClassName = "org.spongepowered.configurate.tool.ToolKt"
}
