package org.spongepowered.configurate.build

import org.gradle.api.GradleException
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
    return project(mapOf("path" to ":format:$component"))
}

fun DependencyHandler.core(): Dependency {
    return project(mapOf("path" to ":core"))
}

fun RepositoryHandler.mojang() {
    maven(url = "https://libraries.minecraft.net") {
        name = "mojang"
    }
}

fun Javadoc.applyCommonAttributes() {
    val version = JavaVersion.toVersion(toolChain.version)
    if (version == JavaVersion.VERSION_12) {
        throw GradleException(
            "Javadoc cannot be generated on JDK 12 -- " +
                "see https://bugs.openjdk.java.net/browse/JDK-8222091"
        )
    }
    val options = this.options
    options.encoding = "UTF-8"
    if (options is StandardJavadocDocletOptions) {
        options.links(
            "https://lightbend.github.io/config/latest/api/",
            "https://fasterxml.github.io/jackson-core/javadoc/2.10/",
            "https://checkerframework.org/api/",
            "https://www.javadoc.io/doc/io.leangen.geantyref/geantyref/1.3.11/"
        )
        options.source = "1.8"
        if (version.isJava9Compatible) {
            options.addBooleanOption("html5", true)
        }
        options.linkSource()
    }
}
