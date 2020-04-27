package org.spongepowered.configurate.build

import net.minecrell.gradle.licenser.LicenseExtension
import net.minecrell.gradle.licenser.Licenser
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPluginExtension
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
            }

            tasks.withType(JavaCompile::class.java) {
                with(it.options) {
                    compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-path", "-Xlint:-serial", "-parameters"))
                    isDeprecation = true
                    encoding = "UTF-8"
                }
            }

            tasks.withType(AbstractArchiveTask::class.java) {
                it.isPreserveFileTimestamps = false
                it.isReproducibleFileOrder = true
            }

            extensions.configure(JavaPluginExtension::class.java) {
                it.withJavadocJar()
                it.withSourcesJar()
                it.sourceCompatibility = targetVersion
                it.targetCompatibility = targetVersion
            }

            tasks.withType(Javadoc::class.java) {
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
                add("testImplementation", "org.junit.jupiter:junit-jupiter-api:5.2.0")
                add("testImplementation", "org.junit-pioneer:junit-pioneer:0.1.2")
                add("testRuntimeOnly", "org.junit.jupiter:junit-jupiter-engine:5.2.0")
            }

            tasks.withType(Test::class.java) {
                it.useJUnitPlatform()
            }

            extensions.configure(ConfiguratePublishingExtension::class.java) {
                it.publish {
                    from(components.getByName("java"))
                }
            }
        }
    }
}
