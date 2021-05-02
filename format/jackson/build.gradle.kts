plugins {
    id("org.spongepowered.configurate.build.component")
}

description = "JSON format loader for Configurate, implemented using Jackson"

dependencies {
    api(projects.core)
    api("com.fasterxml.jackson.core:jackson-core:2.+") {
        attributes {
            // Require that we're an actual release...
            // Too bad Gradle doesn't properly expose this attribute
            attribute(Attribute.of("org.gradle.status", String::class.java), "release")
        }
    }
    testImplementation("com.google.guava:guava:latest.release")
}

indra {
    configurePublications {
        // We only publish resolved versions, so the warning about using attributes is irrelevant.
        suppressAllPomMetadataWarnings()
    }
}
