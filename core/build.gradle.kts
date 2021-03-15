import de.thetaphi.forbiddenapis.gradle.CheckForbiddenApis
import kotlin.math.max

plugins {
    id("org.spongepowered.configurate.build.component")
}
description = """
    A simple configuration library for Java applications that can handle a variety of formats and
    provides a node-based data structure able to handle a wide variety of configuration schemas
""".trimIndent().replace('\n', ' ')

dependencies {
    api("io.leangen.geantyref:geantyref:1.+")
    compileOnlyApi("org.checkerframework:checker-qual:3.+")
    compileOnly("com.google.auto.value:auto-value-annotations:1.+")
    annotationProcessor("com.google.auto.value:auto-value:1.+")
    testImplementation("com.google.guava:guava:latest.release")
}

tasks.jar {
    manifest.attributes["Automatic-Module-Name"] = "${project.group}.configurate"
}

// Build multirelease, theoretically modular jar
// Based on guidance at https://blog.gradle.org/mrjars
// and an example at https://github.com/melix/mrjar-gradle
java {
    modularity.inferModulePath.set(true)
}

fun SourceSet.versionName(version: Int) = this.getTaskName(null, "java$version")

val alternateVersions = arrayOf(
    9,
    10
)
alternateVersions.sort()

alternateVersions.forEachIndexed { index, version ->
    val base = sourceSets.main.get()
    sourceSets.register(base.versionName(version)) {
        java.setSrcDirs(base.java.srcDirs.map { it.resolveSibling(it.name + version) })

        val parent = if (index == 0) {
            // inherit from base directly
            base
        } else {
            // inherit from previous version
            sourceSets[base.versionName(alternateVersions[index - 1])]
        }

        project.dependencies.add(implementationConfigurationName, parent.output)
        this.compileClasspath += parent.compileClasspath
        this.runtimeClasspath += parent.runtimeClasspath

        // Configure versions
        tasks.named(compileJavaTaskName, JavaCompile::class) {
            options.release.set(version)
            javaCompiler.set(
                javaToolchains.compilerFor {
                    languageVersion.set(indra.javaVersions.actualVersion.map { JavaLanguageVersion.of(max(it, version)) })
                }
            )

            options.compilerArgumentProviders += CommandLineArgumentProvider {
                if (!modularity.inferModulePath.getOrElse(false)) {
                    return@CommandLineArgumentProvider listOf()
                }

                val sourceSets = sequenceOf(base) + alternateVersions.asSequence().take(index).map { sourceSets[base.versionName(it)] }
                // --patch-module
                listOf(
                    "--patch-module",
                    "org.spongepowered.configurate=" + sourceSets
                        .flatMap { it.output.asSequence() }
                        .map { it.absolutePath }
                        .joinToString(File.pathSeparator)
                )
            }
        }

        val output = this.output
        tasks.jar {
            into("META-INF/versions/$version") {
                from(output)
            }
        }
    }
}

if (!alternateVersions.isEmpty()) {
    tasks.jar {
        manifest.attributes("Multi-Release" to true)
    }

    // Set up test task to use multi-release jar
    tasks.withType(Test::class).configureEach {
        dependsOn(tasks.jar)
        classpath = files(tasks.jar.flatMap { it.archiveFile }, classpath) - sourceSets.main.get().output
    }
}

// TODO: Standardized multi-release test sources handling

// Set up Java 15 tests for record support
val java15Test by sourceSets.registering {
    val testDir = file("src/test/java15")
    java.srcDir(testDir)

    tasks.named<JavaCompile>(compileJavaTaskName).configure {
        javaCompiler.set(javaToolchains.compilerFor { languageVersion.set(JavaLanguageVersion.of(15)) })
        options.release.set(15)
        options.compilerArgs.addAll(listOf("--enable-preview", "-Xlint:-preview")) // For records
    }

    dependencies.add(implementationConfigurationName, sourceSets.main.map { it.output })

    configurations.named(compileClasspathConfigurationName).configure { extendsFrom(configurations.testCompileClasspath.get()) }
    configurations.named(runtimeClasspathConfigurationName).configure { extendsFrom(configurations.testRuntimeClasspath.get()) }
}

// If our primary JDK is Java 15, then let's add the Java 15 classes to the main test task
tasks.test {
    if (indra.javaVersions.actualVersion.get() == 15) {
        testClassesDirs += java15Test.get().output.classesDirs
        classpath += java15Test.get().runtimeClasspath
        dependsOn(tasks.named(java15Test.get().compileJavaTaskName))
        jvmArgs("--enable-preview") // For records
    }
}

tasks.named("forbiddenApisJava15Test", CheckForbiddenApis::class) {
    targetCompatibility = "15"
}

// But always add to the java 15-specific test task
tasks.matching { it.name == "testJava15" }.configureEach {
    require(this is Test) { "Unexpected task type!" }
    testClassesDirs += java15Test.get().output.classesDirs
    classpath += java15Test.get().runtimeClasspath
    dependsOn(tasks.named(java15Test.get().compileJavaTaskName))
    jvmArgs("--enable-preview") // For records
}
