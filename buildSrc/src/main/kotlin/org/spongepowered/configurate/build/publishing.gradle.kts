package org.spongepowered.configurate.build

import java.io.IOException
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission
import java.util.Base64
import java.util.Locale

plugins {
    id("net.kyori.indra.publishing")
}

val archiveName = "configurate-${name.toLowerCase(Locale.ROOT)}"
// convention.getPlugin(BasePluginConvention::class).archivesBaseName = archiveName

tasks.withType(Jar::class).configureEach {
    indraGit.applyVcsInformationToManifest(manifest)
}

if (project.hasProperty("spongeKeyStore")) {
    // We have to replace the default artifact which is a bit ugly
    // https://github.com/gradle/gradle/pull/13650 should make it easier
    fun forRelevantOutgoings(action: ConfigurationPublications.() -> Unit) {
        configurations[JavaPlugin.API_ELEMENTS_CONFIGURATION_NAME].outgoing.action()
        configurations[JavaPlugin.RUNTIME_ELEMENTS_CONFIGURATION_NAME].outgoing.action()
    }
    val keyStoreProp = project.property("spongeKeyStore") as String
    val fileTemp = File(keyStoreProp)
    val keyStoreFile = if (fileTemp.exists()) {
        fileTemp
    } else {
        // Write keystore to a temporary file
        val dest = layout.projectDirectory.file(".gradle/signing-key").asFile
        dest.parentFile.mkdirs()
        dest.createNewFile()
        try {
            Files.setPosixFilePermissions(dest.toPath(), setOf(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE))
        } catch (_: IOException) {
            // oh well
        }

        dest.writeBytes(Base64.getDecoder().decode(keyStoreProp))
        // Delete the temporary file when the runtime exits
        dest.deleteOnExit()
        dest
    }

    tasks.matching { it.name == "jar" && it is Jar }.whenTaskAdded {
        val jarTask = this as Jar
        jarTask.archiveClassifier.set("unsigned")
        val sign = tasks.register("signJar", SignJarTask::class) {
            dependsOn(jarTask)
            from(zipTree(jarTask.outputs.files.singleFile))
            manifest = jarTask.manifest
            archiveClassifier.set("")
            keyStore.set(keyStoreFile)
            alias.set(project.property("spongeKeyStoreAlias") as String)
            storePassword.set(project.property("spongeKeyStorePassword") as String)
        }

        forRelevantOutgoings {
            artifact(sign)
        }

        tasks.assemble {
            dependsOn(sign)
        }
    }

    afterEvaluate {
        // Remove the unsigned artifact from publications
        forRelevantOutgoings {
            artifacts.removeIf { it.classifier == "unsigned" }
        }
    }
}

indra {
    github("SpongePowered", "Configurate") {
        publishing(true) // GH Packages
        ci(true) // GH Actions
    }
    apache2License()

    if (
        project.hasProperty("spongeSnapshotRepo") &&
        project.hasProperty("spongeReleaseRepo")
    ) {
        publishSnapshotsTo("sponge", project.property("spongeSnapshotRepo") as String)
        publishReleasesTo("sponge", project.property("spongeReleaseRepo") as String)
    }

    configurePublications {
        artifactId = archiveName

        pom {
            inceptionYear.set("2014")

            developers {
                developer {
                    name.set("zml")
                    email.set("zml@spongepowered.org")
                }
            }
        }

        // Don't publish version ranges
        versionMapping {
            usage(Usage.JAVA_API) { fromResolutionResult() }
            usage(Usage.JAVA_RUNTIME) { fromResolutionResult() }
        }
    }
}

// Signing, using specified private key file
signing {
    val spongeSigningKey = project.findProperty("spongeSigningKey") as String?
    val spongeSigningPassword = project.findProperty("spongeSigningPassword") as String?
    if (spongeSigningKey != null && spongeSigningPassword != null) {
        val keyFile = file(spongeSigningKey)
        if (keyFile.exists()) {
            useInMemoryPgpKeys(file(spongeSigningKey).readText(Charsets.UTF_8), spongeSigningPassword)
        } else {
            useInMemoryPgpKeys(spongeSigningKey, spongeSigningPassword)
        }
    } else {
        signatories = PgpSignatoryProvider() // don't use gpg agent
    }
}

tasks.withType<Sign>().configureEach {
    onlyIf {
        !project.hasProperty("skipSigning")
    }
}
