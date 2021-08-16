import net.ltgt.gradle.errorprone.errorprone
import org.spongepowered.configurate.build.useAutoValue

plugins {
    id("org.spongepowered.configurate.build.component")
    id("com.github.johnrengelman.shadow") version "6.1.0"
    groovy // For writing tests
}

description = "YAML format loader for Configurate"

tasks.withType(GroovyCompile::class).configureEach {
    // Toolchains need this... for some reason
    options.release.set(indra.javaVersions().target())
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}

val shaded by configurations.registering {
    resolutionStrategy.deactivateDependencyLocking()
}

configurations {
    compileClasspath { extendsFrom(shaded.get()) }
    runtimeClasspath { extendsFrom(shaded.get()) }
    testImplementation { extendsFrom(shaded.get()) }
}

useAutoValue()
dependencies {
    api(projects.core)
    // When updating snakeyaml, check ConfigurateScanner for changes against upstream
    "shaded"("configurate.thirdparty:snakeyaml:version-from-submodule")

    testImplementation("org.codehaus.groovy:groovy:3.+:indy")
    testImplementation("org.codehaus.groovy:groovy-nio:3.+:indy")
    testImplementation("org.codehaus.groovy:groovy-test-junit5:3.+:indy")
    testImplementation("org.codehaus.groovy:groovy-templates:3.+:indy")
}
tasks {
    shadowJar {
        configurations = listOf(shaded.get())
        minimize()
        relocate("org.yaml.snakeyaml", "${project.group}.configurate.yaml.internal.snakeyaml")
    }

    assemble {
        dependsOn(shadowJar)
    }

    pmdMain {
        isEnabled = false
    }
}

if (project.hasProperty("spongeKeyStore")) {
    // Just update the input of the sign jar task
    tasks.shadowJar {
        archiveClassifier.set("unsigned-all")
    }

    tasks.named("sign", org.spongepowered.configurate.build.SignJarTask::class) {
        dependsOn(tasks.shadowJar)
        // TODO: this is super ugly, need to give jar signing a bit of a redesign
        (this.rootSpec as org.gradle.api.internal.file.copy.DefaultCopySpec).sourcePaths.clear()
        from(zipTree(tasks.shadowJar.get().outputs.files.singleFile))
    }
} else {
    // Replace the default artifact
    // We have to replace the default artifact which is a bit ugly
    // https://github.com/gradle/gradle/pull/13650 should make it easier
    fun forRelevantOutgoings(action: ConfigurationPublications.() -> Unit) {
        configurations[JavaPlugin.API_ELEMENTS_CONFIGURATION_NAME].outgoing.action()
        configurations[JavaPlugin.RUNTIME_ELEMENTS_CONFIGURATION_NAME].outgoing.action()
    }

    forRelevantOutgoings {
        artifact(tasks.shadowJar)
    }

    tasks.shadowJar {
        archiveClassifier.set("")
    }
    tasks.jar {
        archiveClassifier.set("thin")
    }

    afterEvaluate {
        forRelevantOutgoings {
            artifacts.removeIf { it.classifier == "thin" }
        }
    }
}
