package org.spongepowered.configurate.build

import org.gradle.api.GradleException
import org.gradle.api.JavaVersion
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions

enum class Versions(val version: String) {
    GUAVA("25.1-jre"),
    HOCON("1.4.0"),
    SNAKEYAML("1.26"),
    JACKSON("2.10.3"),
    ERROR_PRONE("2.3.4");

    override fun toString(): String {
        return version
    }
}

/**
 * Create a dependency on a specific component in this project
 */
fun DependencyHandler.format(component: String): Dependency {
    return project(mapOf("path" to ":format:$component"))
}

fun DependencyHandler.core(): Dependency {
    return project(mapOf("path" to ":core"))
}

fun Javadoc.applyCommonAttributes() {
    val version = JavaVersion.toVersion(toolChain.version)
    if (version == JavaVersion.VERSION_12) {
        throw GradleException("Javadoc cannot be generated on JDK 12 -- " +
                "see https://bugs.openjdk.java.net/browse/JDK-8222091")
    }
    val options = this.options
    options.encoding = "UTF-8"
    if (options is StandardJavadocDocletOptions) {
        options.links(
                "https://guava.dev/releases/${Versions.GUAVA}/api/docs/",
                "https://lightbend.github.io/config/latest/api/",
                "https://fasterxml.github.io/jackson-core/javadoc/2.10/",
                "https://checkerframework.org/api/",
                "https://docs.oracle.com/javase/8/docs/api"
        )
        options.source = targetVersion.toString()
        if (version.isJava9Compatible) {
            options.addBooleanOption("html5", true)
        }
        options.linkSource()
    }
}
