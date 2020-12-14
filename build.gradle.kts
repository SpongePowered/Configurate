import org.spongepowered.configurate.build.applyCommonAttributes

plugins {
    kotlin("jvm") version "1.4.20" apply false
    id("org.jetbrains.dokka") version "1.4.20" apply false
    id("io.gitlab.arturbosch.detekt") version "1.14.2" apply false
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
    id("io.freefair.aggregate-javadoc-jar") version "5.3.0"
    id("org.ajoberstar.grgit")
    id("org.ajoberstar.git-publish") version "3.0.0"
    id("com.github.ben-manes.versions") version "0.36.0"
    id("io.codearte.nexus-staging")
    `java-base`
}

buildscript {
    configurations.classpath {
        resolutionStrategy {
            activateDependencyLocking()
        }
    }
}

group = "org.spongepowered"
version = "4.1.0-SNAPSHOT"

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}

allprojects {
    repositories {
        jcenter()
    }

    ktlint {
        version.set("0.40.0")
    }
}

nexusStaging {
    val sonatypeUsername: String? by project
    val sonatypePassword: String? by project
    username = sonatypeUsername
    password = sonatypePassword
}

tasks.aggregateJavadoc.configure {
    applyCommonAttributes()
    title = "Configurate $version (all modules)"

    val excludedProjects = listOf("examples").map {
        project(":$it").tasks.named("javadoc", Javadoc::class).get().classpath
    }
    classpath = classpath.minus(files(excludedProjects))
    (options as? StandardJavadocDocletOptions)?.apply {
        addBooleanOption("Xdoclint:-missing", true)
        links("https://docs.oracle.com/javase/8/docs/api/")
    }
}

gitPublish {
    branch.set("gh-pages")
    contents {
        from("src/site") {
            val versions = {
                (listOf(project.version as String) + grgit.tag.list().map { it.name }.reversed())
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
