package org.spongepowered.configurate.build

import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * Create a dependency on a specific component in this project
 */
fun DependencyHandler.configurate(component: String): Dependency {
    return project(mapOf("path" to ":configurate-$component"))
}

fun DependencyHandler.core(): Dependency {
    return configurate("core")
}
