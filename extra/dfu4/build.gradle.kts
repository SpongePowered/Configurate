plugins {
    id("org.spongepowered.configurate.build.component")
}

description = "Integration between Configurate and Mojang's DataFixerUpper v4 library"

dependencies {
    api(projects.core)
    api("com.mojang:datafixerupper:4.0.26")
    testImplementation(projects.format.gson)
    testImplementation("com.flowpowered:flow-math:1.0.3")
}

tasks.withType(Javadoc::class) {
    val options = this.options
    if (options is StandardJavadocDocletOptions) {
        options.links(
            "https://kvverti.github.io/Documented-DataFixerUpper/snapshot/"
        )
    }
}
