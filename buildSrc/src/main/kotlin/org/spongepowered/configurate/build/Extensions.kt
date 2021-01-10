package org.spongepowered.configurate.build

import org.gradle.api.JavaVersion
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.kotlin.dsl.maven

/**
 * Create a dependency on a specific component in this project
 */
fun DependencyHandler.format(component: String): Dependency {
    return project(mapOf("path" to ":format:configurate-$component"))
}

fun DependencyHandler.core(): Dependency {
    return project(mapOf("path" to ":configurate-core"))
}

fun RepositoryHandler.mojang() {
    maven(url = "https://libraries.minecraft.net") {
        name = "mojang"
    }
}

fun Javadoc.applyCommonAttributes() {
    val options = this.options
    options.encoding = "UTF-8"
    if (options is StandardJavadocDocletOptions) {
        options.links(
            "https://lightbend.github.io/config/latest/api/",
            "https://fasterxml.github.io/jackson-core/javadoc/2.10/",
            "https://checkerframework.org/api/",
            "https://www.javadoc.io/doc/io.leangen.geantyref/geantyref/1.3.11/"
        )

        if (JavaVersion.current() > JavaVersion.VERSION_1_8 && JavaVersion.current() < JavaVersion.VERSION_12) {
            options.addBooleanOption("-no-module-directories", true)
        }

        options.addBooleanOption("html5", true)
        options.addStringOption("-release", "8")
        options.linkSource()
    }
}
