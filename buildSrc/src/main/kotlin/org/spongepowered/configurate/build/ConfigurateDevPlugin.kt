package org.spongepowered.configurate.build

import net.ltgt.gradle.errorprone.errorprone
import net.minecrell.gradle.licenser.LicenseExtension
import net.minecrell.gradle.licenser.Licenser
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.plugins.quality.CheckstylePlugin
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
internal val targetVersion = JavaVersion.VERSION_1_8

class ConfigurateDevPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            plugins.apply {
                apply(Licenser::class.java)
                apply(JavaLibraryPlugin::class.java)
                apply(ConfiguratePublishingPlugin::class.java)
                apply(CheckstylePlugin::class.java)
                apply("net.ltgt.errorprone")
            }

            tasks.withType(JavaCompile::class.java).configureEach {
                with(it.options) {
                    val version = JavaVersion.toVersion(it.toolChain.version)
                    compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-path", "-Xlint:-serial", "-parameters"))
                    if (version.isJava9Compatible) {
                        compilerArgs.addAll(listOf("--release", "8"))
                    } else {
                        errorprone.isEnabled.set(false)
                    }
                    isDeprecation = true
                    encoding = "UTF-8"
                }
            }

            tasks.withType(AbstractArchiveTask::class.java).configureEach {
                it.isPreserveFileTimestamps = false
                it.isReproducibleFileOrder = true
            }

            extensions.configure(JavaPluginExtension::class.java) {
                it.withJavadocJar()
                it.withSourcesJar()
                it.sourceCompatibility = targetVersion
                it.targetCompatibility = targetVersion
            }

            tasks.withType(Javadoc::class.java).configureEach {
                it.applyCommonAttributes()
            }

            extensions.configure(LicenseExtension::class.java) {
                with(it) {
                    header = rootProject.file("LICENSE_HEADER")
                    include("**/*.java")
                    include("**/*.kt")
                    newLine = false
                }
            }

            repositories.addAll(listOf(repositories.mavenLocal(), repositories.mavenCentral(), repositories.jcenter()))
            dependencies.apply {
                // error-prone compiler
                add(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME, "com.google.errorprone:error_prone_annotations:${Versions.ERROR_PRONE}")
                add("errorprone", "com.google.errorprone:error_prone_core:${Versions.ERROR_PRONE}")

                // Testing
                add(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, "org.junit.jupiter:junit-jupiter-api:5.7.0")
                add(JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME, "org.junit.jupiter:junit-jupiter-engine:5.7.0")
            }

            tasks.withType(Test::class.java).configureEach {
                it.useJUnitPlatform()
            }

            // Checkstyle (based on Sponge config)
            extensions.configure(CheckstyleExtension::class.java) {
                it.toolVersion = "8.32"
                it.configDirectory.set(rootProject.projectDir.resolve("etc/checkstyle"))
                it.configProperties = mapOf(
                    "basedir" to project.projectDir,
                    "severity" to "error"
                )
            }

            extensions.configure(ConfiguratePublishingExtension::class.java) {
                it.publish {
                    from(components.getByName("java"))
                }
            }
        }
    }
}
