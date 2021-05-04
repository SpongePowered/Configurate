import org.eclipse.jgit.lib.Repository.shortenRefName
import org.spongepowered.configurate.build.applyCommonAttributes

plugins {
    kotlin("jvm") version "1.4.20" apply false
    id("org.jetbrains.dokka") version "1.4.20" apply false
    id("io.gitlab.arturbosch.detekt") version "1.16.0" apply false
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
    id("io.freefair.aggregate-javadoc-jar") version "5.3.3.3"
    id("net.kyori.indra.publishing.sonatype")
    id("net.kyori.indra.git")
    id("org.ajoberstar.git-publish") version "3.0.0"
    id("com.github.ben-manes.versions") version "0.38.0"
    `java-base`
}

group = "org.spongepowered"
version = "4.2.0-SNAPSHOT"

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}

allprojects {
    ktlint {
        version.set("0.41.0")
    }
}

tasks.aggregateJavadoc.configure {
    val gradleJdk = JavaVersion.current()
    // at least java 11, but not 12 (java 12 is broken for some reason :( )
    if (gradleJdk < JavaVersion.VERSION_11 || gradleJdk == JavaVersion.VERSION_12) {
        javadocTool.set(javaToolchains.javadocToolFor { this.languageVersion.set(JavaLanguageVersion.of(11)) })
    }

    applyCommonAttributes()
    title = "Configurate $version (all modules)"

    val excludedProjects = listOf("examples").map {
        project(":$it").tasks.named("javadoc", Javadoc::class).get().classpath
    }
    exclude {
        excludedProjects.find { coll -> coll.contains(it.file) } != null
    }

    (options as? StandardJavadocDocletOptions)?.apply {
        addBooleanOption("Xdoclint:-missing", true)
        links("https://docs.oracle.com/javase/8/docs/api/")
        if (JavaVersion.current() > JavaVersion.VERSION_1_8 && JavaVersion.current() < JavaVersion.VERSION_12) {
            addBooleanOption("-no-module-directories", true)
        }
    }
}

gitPublish {
    val repo = indraGit.git()
    if (repo != null) {
        repo.remoteList().call().find { config -> config.name == "origin" }?.apply {
            repoUri.set((this.pushURIs.firstOrNull() ?: this.urIs.first()).toPrivateString())
            referenceRepoUri.set(this.urIs.first().toPrivateString())
        }
    }

    branch.set("gh-pages")
    contents {
        from("src/site") {
            val versions = {
                (listOf(project.version as String) + indraGit.tags().map { shortenRefName(it.name) }.reversed())
                    .distinct()
                    .filter { repoDir.get().dir(it).getAsFile().exists() || it == project.version }
            }
            expand("project" to project, "versions" to versions)
        }
        from(tasks.aggregateJavadoc) {
            into("$version/apidocs")
        }
    }

    preserve {
        include(".gitattributes")
        include("**/") // include everything in directories
        exclude("/*.html")
    }
}

tasks.dependencyUpdates {
    gradleReleaseChannel = "current"
    revision = "release"
}
