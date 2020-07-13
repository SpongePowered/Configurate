import org.spongepowered.configurate.build.core

plugins {
    id("org.spongepowered.configurate-component")
}

repositories {
    maven(url = "https://libraries.minecraft.net") {
        name = "mojang"
    }
}

dependencies {
    api(core())
    api("com.mojang:datafixerupper:2.0.24")
    testImplementation(project(":format:gson"))
}

tasks.withType(Javadoc::class) {
    val options = this.options
    if (options is StandardJavadocDocletOptions) {
        options.links(
            "https://kvverti.github.io/Documented-DataFixerUpper/snapshot/"
        )
    }
}
