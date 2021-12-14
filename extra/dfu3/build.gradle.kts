plugins {
    id("org.spongepowered.configurate.build.component")
}

description = "Integration between Configurate and Mojang's DataFixerUpper v3 library"

dependencies {
    api(projects.core)
    api("com.mojang:datafixerupper:3.0.25")
    testImplementation(projects.format.gson)
    testImplementation("org.spongepowered:math:2.0.1")
}

tasks.withType(Javadoc::class) {
    val options = this.options
    if (options is StandardJavadocDocletOptions) {
        options.links(
            "https://kvverti.github.io/Documented-DataFixerUpper/snapshot/"
        )
    }
}
