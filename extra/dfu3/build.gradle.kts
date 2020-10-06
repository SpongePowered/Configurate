import org.spongepowered.configurate.build.core
import org.spongepowered.configurate.build.mojang

plugins {
    id("org.spongepowered.configurate.build.component")
}

repositories {
    mojang()
}

dependencies {
    api(core())
    api("com.mojang:datafixerupper:3.0.25")
    testImplementation(project(":format:gson"))
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
