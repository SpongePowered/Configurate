package org.spongepowered.configurate.build

import net.kyori.indra.version
import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("org.spongepowered.configurate.build.publishing")
    id("net.kyori.indra")
    id("net.kyori.indra.checkstyle")
    id("net.kyori.indra.license-header")
    id("net.ltgt.errorprone")
}

repositories {
    mavenCentral()
    jcenter()
}

tasks.withType(JavaCompile::class).configureEach {
    if (!version(toolChain).isJava9Compatible) {
        options.errorprone.isEnabled.set(false)
    }
    options.compilerArgs.add("-Xlint:-processing")
}

tasks.withType(Javadoc::class).configureEach {
    val options = this.options
    if (options is StandardJavadocDocletOptions) {
        options.links(
                "https://lightbend.github.io/config/latest/api/",
                "https://fasterxml.github.io/jackson-core/javadoc/2.10/",
                "https://checkerframework.org/api/"
        )
        options.linkSource()
    }
    //applyCommonAttributes()
}

dependencies {
    // error-prone compiler
    val errorProneVersion: String by project
    compileOnly("com.google.errorprone:error_prone_annotations:$errorProneVersion")
    errorprone("com.google.errorprone:error_prone_core:$errorProneVersion")

    // Testing
    val junitVersion: String by project
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

license {
    header = rootProject.file("LICENSE_HEADER")
}

// Checkstyle (based on Sponge config)
// We have the checkstyle version exposed as a property for use in checkstyle's own CI
// do not modify the checkstyle configuration without an understanding of how they test against Configurate
val checkstyleVersion: String by project
indra.checkstyle.set(checkstyleVersion)

// Allow checkstyle only to be resolved from mavenLocal if set to a snapshot
if (checkstyleVersion.endsWith("-SNAPSHOT")) {
    repositories.mavenLocal {
        content {
            includeGroup("com.puppycrawl.tools")
        }
    }
}

// Create task for executing all checkstyle tasks
tasks.register("checkstyleAll") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    dependsOn(tasks.withType(Checkstyle::class))
}
