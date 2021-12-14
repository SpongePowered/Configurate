plugins {
    id("org.spongepowered.configurate.build.component")
}

description = "XML format loader for Configurate"

dependencies {
    val guavaVersion: String by project

    api(projects.core)
    testImplementation("com.google.guava:guava:$guavaVersion")
}
