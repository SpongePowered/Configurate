package org.spongepowered.configurate.build

import de.thetaphi.forbiddenapis.gradle.CheckForbiddenApis
import net.ltgt.gradle.errorprone.errorprone
import org.cadixdev.gradle.licenser.tasks.LicenseCheck
import org.eclipse.jgit.lib.Repository
import org.gradle.api.internal.artifacts.configurations.ResolutionStrategyInternal
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.Pmd
import org.gradle.api.tasks.compile.JavaCompile
import kotlin.math.min

plugins {
    id("org.spongepowered.configurate.build.publishing")
    id("net.kyori.indra")
    id("net.kyori.indra.checkstyle")
    id("net.kyori.indra.license-header")
    id("net.ltgt.errorprone")
    // id("net.ltgt.nullaway")
    id("me.champeau.gradle.japicmp")
    id("de.thetaphi.forbiddenapis")
    pmd
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
            .filter { it.isCanBeResolved && (it.resolutionStrategy as ResolutionStrategyInternal).isDependencyLockingEnabled }
            .forEach { it.resolve() }
    }
}

/**
 * Fix metadata on Maven artifacts to properly handle rc/beta/alpha versions.
 *
 * These should not be detected as releases -- but are, sadly.
 */
@CacheableRule
open class CorrectlyClassifyMilestonesRule : ComponentMetadataRule {
    companion object {
        val notActuallyReleaseStatus = Regex("rc|beta|alpha|(-M\\d+$)")
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
        all(CorrectlyClassifyMilestonesRule::class.java)
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
    compileOnlyApi("com.google.errorprone:error_prone_annotations:$errorProneVersion")
    errorprone("com.google.errorprone:error_prone_core:$errorProneVersion")
    // errorprone("com.uber.nullaway:nullaway:0.8.0")

    // Testing
    val junitVersion: String by project
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion") {
        attributes {
            attribute(Attribute.of("org.gradle.status", String::class.java), "release")
        }
    }
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion") {
        attributes {
            attribute(Attribute.of("org.gradle.status", String::class.java), "release")
        }
    }
}

configurations {
    runtimeClasspath { shouldResolveConsistentlyWith(compileClasspath.get()) }
}

// Apply a license header
license {
    header(rootProject.file("LICENSE_HEADER"))
    exclude("generated/**")
}

// Set up automatic module name
tasks.jar {
    manifest.attributes["Automatic-Module-Name"] = "${project.group}.configurate.${project.name.replace('-', '.')}"
}

// Configure target versions
indra {
    javaVersions().testWith(8, 11, 16)
}

// Don't compile AP-generated sources from within IDE
// IntelliJ puts its output *within* the Gradle source root.....................

sourceSets.configureEach {
    tasks.named(compileJavaTaskName, JavaCompile::class) {
        exclude("generated/**")
    }
}
tasks.withType(Checkstyle::class) {
    exclude("generated/**")
}
tasks.withType(Pmd::class) {
    exclude("generated/**")
}

// Forbidden API validation
forbiddenApis {
    bundledSignatures = setOf("jdk-unsafe", "jdk-deprecated")
    failOnMissingClasses = false
}

tasks.withType(CheckForbiddenApis::class).configureEach {
    targetCompatibility = min(indra.javaVersions().actualVersion().get(), 15).toString() // todo: forbidden apis needs J16 sigs
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

indra.checkstyle().set(checkstyleVersion)

// Allow checkstyle only to be resolved from mavenLocal if set to a snapshot
if (checkstyleVersion.endsWith("-SNAPSHOT")) {
    repositories.mavenLocal {
        content {
            includeGroup("com.puppycrawl.tools")
        }
    }
}

val objects = project.objects
tasks.withType(Checkstyle::class) {
    classpath = objects.fileCollection()
}

pmd {
    isConsoleOutput = true
    // incrementalAnalysis.set(true)
    ruleSetConfig = resources.text.fromFile(rootProject.file(".pmd/rules.xml"))
    ruleSets.clear() // Remove default rule sets
    toolVersion = "6.29.0"
}

// API diff viewer

val apiDiffPrevious by configurations.registering {
    isCanBeConsumed = false
    isCanBeResolved = true
    resolutionStrategy.deactivateDependencyLocking() // We dynamically calculate our version, no need for this
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class, Usage.JAVA_API))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class, Category.LIBRARY))
        attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, indra.javaVersions().target().get())
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
    }

    defaultDependencies {
        // create based on git's previous tag
        val lastTag = indraGit.tags().lastOrNull()
        if (lastTag != null) {
            add(project.dependencies.create("$group:configurate-${project.name}:${Repository.shortenRefName(lastTag.name)}"))
        }
    }
}

val apiDiffPreviousArchive by configurations.registering {
    isCanBeResolved = true
    isTransitive = false
    resolutionStrategy.deactivateDependencyLocking()
    extendsFrom(apiDiffPrevious.get())
    isVisible = false
}

val apiDiff by tasks.registering(me.champeau.gradle.japicmp.JapicmpTask::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Generate an API diff between the current source and the last tagged version."
    // Old artifact: output of old japicmp
    oldClasspath = apiDiffPrevious.get()
    oldArchives = apiDiffPreviousArchive.get()
    // New: Current compile classpath and api output
    newClasspath = configurations.compileClasspath.get()
    newArchives = configurations.apiElements.get().outgoing.artifacts.files
    isIgnoreMissingClasses = true // TODO: Doesn't seem to respect the classpath parameters

    isOnlyModified = true
    htmlOutputFile = layout.buildDirectory.file("reports/api-diff-long.html").get().asFile

    richReport {
        title = "${rootProject.name} API difference report for ${project.name}"
        reportName = "api-diff.html"
    }
}
