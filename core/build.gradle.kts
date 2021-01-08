import net.kyori.indra.versionNumber
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
    if (max(indra.javaVersions.minimumToolchain.get(), versionNumber(JavaVersion.current())) == 15) {
        testClassesDirs += java15Test.get().output.classesDirs
        classpath += java15Test.get().runtimeClasspath
        dependsOn(tasks.named(java15Test.get().compileJavaTaskName))
        jvmArgs("--enable-preview") // For records
    }
}

// But always add to the java 15-specific test task
tasks.matching { it.name == "testJava15" }.configureEach {
    require(this is Test) { "Unexpected task type!" }
    testClassesDirs += java15Test.get().output.classesDirs
    classpath += java15Test.get().runtimeClasspath
    dependsOn(tasks.named(java15Test.get().compileJavaTaskName))
    jvmArgs("--enable-preview") // For records
}
