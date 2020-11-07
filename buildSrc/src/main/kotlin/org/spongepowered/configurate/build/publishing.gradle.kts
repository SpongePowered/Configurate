package org.spongepowered.configurate.build

import net.kyori.indra.grgit
import java.util.Locale

plugins {
    id("net.kyori.indra.publishing.sonatype")
    id("org.ajoberstar.grgit")
}

val archiveName = "configurate-${name.toLowerCase(Locale.ROOT)}"
convention.getPlugin(BasePluginConvention::class).archivesBaseName = archiveName

tasks.withType(Jar::class).configureEach {
    val git = grgit(project)
    if (git != null) {
        git.head()?.also { manifest.attributes["Git-Commit"] = it.id }
        git.branch.current()?.also { manifest.attributes["Git-Branch"] = it.name }
    }
}

indra {
    github("SpongePowered", "Configurate") {
        publishing = true // GH packages
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
        pom {
            artifactId = archiveName

            description.set(
                """
                A simple configuration library for Java applications that can handle a variety of formats and
                provides a node-based data structure able to handle a wide variety of configuration schemas
                """.trimIndent().replace('\n', ' ')
            )

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
