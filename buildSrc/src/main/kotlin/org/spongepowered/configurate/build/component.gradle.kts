package org.spongepowered.configurate.build

import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("org.spongepowered.configurate.build.publishing")
    id("net.kyori.indra")
    id("net.kyori.indra.checkstyle")
    id("net.kyori.indra.license-header")
    id("net.ltgt.errorprone")
    // id("net.ltgt.nullaway")
    pmd
}

repositories {
    mavenCentral()
    jcenter()
}

// Dependency locking

dependencyLocking {
    lockAllConfigurations()
    lockFile.set(rootProject.layout.projectDirectory.file("gradle/dependencies/${project.path.replace(':', '-').substring(1)}.lock"))
}

tasks.register("resolveAllForLocking") {
    description = "Update all dependency locks. Must be run with the `--write-locks` flag."

    doFirst {
        require(gradle.startParameter.isWriteDependencyLocks)
    }
    doLast {
        configurations
            .filter { it.isCanBeResolved }
            .forEach { it.resolve() }
    }
}

/**
 * Fix metadata on Maven artifacts to properly handle rc/beta/alpha versions.
 *
 * These should not be detected as releases -- but are, sadly.
 */
@CacheableRule
open class RcAsIntegrationRule : ComponentMetadataRule {
    companion object {
        val notActuallyReleaseStatus = Regex("rc|beta|alpha")
    }

    override fun execute(context: ComponentMetadataContext) {
        with(context.details) {
            val version = id.version

            if (status == "release" && notActuallyReleaseStatus.containsMatchIn(version)) {
                status = "milestone"
            }
        }
    }
}

dependencies {
    components {
        all(RcAsIntegrationRule::class.java)
    }
}

tasks.withType(JavaCompile::class).configureEach {
    options.errorprone {
        isEnabled.set(javaCompiler.map { it.metadata.languageVersion.asInt() >= 9 }.orElse(JavaVersion.current().isJava9Compatible))
        /* if (!name.toLowerCase().contains("test")) {
            nullaway {
                severity.set(CheckSeverity.ERROR)
                annotatedPackages.add("org.spongepowered.configurate")
                excludedFieldAnnotations.add("org.checkerframework.checker.nullness.qual.MonotonicNonNull")
                treatGeneratedAsUnannotated.set(true)
            }
        }*/
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
    // applyCommonAttributes()
}

dependencies {
    // error-prone compiler
    val errorProneVersion: String by project
    compileOnly("com.google.errorprone:error_prone_annotations:$errorProneVersion")
    errorprone("com.google.errorprone:error_prone_core:$errorProneVersion")
    // errorprone("com.uber.nullaway:nullaway:0.8.0")

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
dependencies {
    checkstyle("com.puppycrawl.tools:checkstyle:$checkstyleVersion")
    checkstyle("ca.stellardrift:stylecheck:0.1")
}

dependencyLocking {
    ignoredDependencies.add("com.puppycrawl.tools:*")
}

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

pmd {
    isConsoleOutput = true
    // incrementalAnalysis.set(true)
    ruleSetConfig = resources.text.fromFile(rootProject.file(".pmd/rules.xml"))
    ruleSets.clear() // Remove default rule sets
    toolVersion = "6.29.0"
}

// Copy-paste detector

sourceSets.configureEach set@{
    val outputDir = project.layout.buildDirectory.dir("reports/cpd")
    val outputFile = outputDir.map { it.file("$name.txt") }
    val cpdClasspath = configurations["pmd"]

    // TODO: Break this out into a proper task
    // That'll let us declare inputs properly and declare per-subproject exclusions
    val cpdTask = tasks.register(getTaskName("cpd", null)) {
        onlyIf {
            this@set.allJava.files.firstOrNull { it.exists() } != null
        }

        doLast {
            outputDir.get().asFile.mkdirs()
            ant.withGroovyBuilder {
                "taskdef"(
                    "name" to "cpd",
                    "classname" to "net.sourceforge.pmd.cpd.CPDTask",
                    "classpath" to cpdClasspath.asPath
                )
                "cpd"(
                    "encoding" to "UTF-8",
                    "minimumtokencount" to 105,
                    "outputFile" to outputFile.get().asFile,
                    "skipLexicalErrors" to true
                ) {
                    this@set.allJava.addToAntBuilder(this, "fileset", FileCollection.AntType.FileSet)
                }
            }

            val text = project.resources.text.fromFile(outputFile).asString()
            if (!text.isEmpty()) {
                logger.error(text)
                throw GradleException("Found duplication in source set $name!")
            }
        }
    }

    tasks.check.configure {
        dependsOn(cpdTask)
    }
}
