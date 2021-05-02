plugins {
    id("org.spongepowered.configurate.build.component")
}

description = "Integration between Configurate and Mojang's DataFixerUpper v2 library"

dependencies {
    api(projects.core)
    api("com.mojang:datafixerupper:2.0.24")
    testImplementation(projects.format.gson)
}

tasks.withType(Javadoc::class) {
    val options = this.options
    if (options is StandardJavadocDocletOptions) {
        options.links(
            "https://kvverti.github.io/Documented-DataFixerUpper/snapshot/"
        )
    }
}
