package org.spongepowered.configurate.build

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension
import java.net.URI

typealias PublicationConfigureCb = MavenPublication.() -> Unit

open class ConfiguratePublishingExtension {
    internal val configureFunctions = mutableListOf<PublicationConfigureCb>()

    fun publish(cb: PublicationConfigureCb) {
        configureFunctions.add(cb)
    }
}

class ConfiguratePublishingPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            plugins.apply {
                apply("maven-publish")
                apply("signing")
            }

            group = rootProject.group
            version = rootProject.version

            val configurateExtension = extensions.create("configurate", ConfiguratePublishingExtension::class.java)

            val publishing = extensions.getByType(PublishingExtension::class.java).apply {
                publications { container ->
                    container.register("maven", MavenPublication::class.java) { pub ->
                        with(pub) {
                            pom.apply {
                                description.set("A simple configuration library for Java applications that can handle a variety of formats and " +
                                        "provides a node-based data structure able to handle a wide variety of configuration schemas")
                                name.set(project.name)
                                url.set("https://github.com/SpongePowered/configurate/")

                                inceptionYear.set("2014")

                                developers {
                                    it.developer { d ->
                                        d.name.set("zml")
                                        d.email.set("zml@aoeu.xyz")
                                    }
                                }

                                issueManagement {
                                    it.system.set("GitHub Issues")
                                    it.url.set("https://github.com/SpongePowered/configurate/issues")
                                }

                                licenses { s ->
                                    s.license {
                                        it.name.set("Apache License, Version 2.0")
                                        url.set("https://opensource.org/licenses/Apache-2.0")
                                    }
                                }

                                scm {
                                    it.connection.set("scm:git@github.com:SpongePowered/configurate.git")
                                    it.developerConnection.set("scm:git@github.com:SpongePowered/configurate.git")
                                    it.url.set("https://github.com/SpongePowered/configurate/")
                                }
                            }

                        }
                    }
                }

                if (project.hasProperty("spongeRepo") && project.hasProperty("spongeUsername") && project.hasProperty("spongePassword")) {
                    repositories {
                        it.maven { repo ->
                            repo.url = URI(project.property("spongeRepo")!! as String)
                            repo.name = "spongeRepo"
                            repo.credentials { cred ->
                                cred.username = project.property("spongeUsername") as String?
                                cred.password = project.property("spongePassword") as String?
                            }
                        }
                    }
                }
            }
            val mavenPublication = publishing.publications.getByName("maven") as MavenPublication

            afterEvaluate {
                for (cb in configurateExtension.configureFunctions) {
                    mavenPublication.cb()
                }
            }

            extensions.configure(SigningExtension::class.java) {
                it.useGpgCmd()
                it.sign(mavenPublication)
            }

            tasks.withType(Sign::class.java) {
                it.onlyIf {
                    val version = project.version.toString()
                    val forceSign = findProperty("forceSign") as Boolean? ?: false
                    !project.hasProperty("skipSign") && (!version.endsWith("-SNAPSHOT") || forceSign)
                }
            }
        }
    }
}
