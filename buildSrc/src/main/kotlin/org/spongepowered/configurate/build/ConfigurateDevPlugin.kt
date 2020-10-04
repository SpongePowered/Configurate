package org.spongepowered.configurate.build

import net.ltgt.gradle.errorprone.errorprone
import org.cadixdev.gradle.licenser.LicenseExtension
import org.cadixdev.gradle.licenser.Licenser
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.plugins.quality.CheckstylePlugin
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.language.base.plugins.LifecycleBasePlugin

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
                    release.set(targetVersion.ordinal + 1)
                    if (!version.isJava9Compatible) {
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

            dependencies.apply {
                // error-prone compiler
                val errorProneVersion = properties["errorProneVersion"]
                add(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME, "com.google.errorprone:error_prone_annotations:$errorProneVersion")
                add("errorprone", "com.google.errorprone:error_prone_core:$errorProneVersion")

                // Testing
                val junitVersion = properties["junitVersion"]
                add(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, "org.junit.jupiter:junit-jupiter-api:$junitVersion")
                add(JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME, "org.junit.jupiter:junit-jupiter-engine:$junitVersion")
            }

            tasks.withType(Test::class.java).configureEach {
                it.useJUnitPlatform()
            }

            // Checkstyle (based on Sponge config)
            // We have the checkstyle version exposed as a property for use in checkstyle's own CI
            // do not modify the checkstyle configuration without an understanding of how they test against Configurate
            val checkstyleVersion = properties["checkstyleVersion"].toString()
            extensions.configure(CheckstyleExtension::class.java) {
                it.toolVersion = checkstyleVersion
                it.configDirectory.set(rootProject.projectDir.resolve("etc/checkstyle"))
                it.configProperties = mapOf(
                    "basedir" to project.projectDir,
                    "severity" to "error"
                )
            }

            // Allow checkstyle only to be resolved from mavenLocal if set to a snapshot
            if (checkstyleVersion.endsWith("-SNAPSHOT")) {
                repositories.mavenLocal {
                    it.content { desc ->
                        desc.includeGroup("com.puppycrawl.tools")
                    }
                }
            }

            // Create task for executing all checkstyle tasks
            tasks.register("checkstyleAll") {
                it.group = LifecycleBasePlugin.VERIFICATION_GROUP
                it.dependsOn(tasks.withType(Checkstyle::class.java))
            }

            // now set up other repositories
            repositories.apply {
                mavenCentral()
                jcenter()
            }

            extensions.configure(ConfiguratePublishingExtension::class.java) {
                it.publish {
                    from(components.getByName("java"))
                }
            }
        }
    }
}
