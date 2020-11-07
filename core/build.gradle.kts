plugins {
    id("org.spongepowered.configurate.build.component")
}

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

// Set up Java 14 tests for record support

if (JavaVersion.current() >= JavaVersion.VERSION_14) {
    val java14Test by sourceSets.registering {
        val testDir = file("src/test/java14")
        java.srcDir(testDir)

        tasks.named<JavaCompile>(compileJavaTaskName).configure {
            options.release.set(JavaVersion.current().ordinal + 1)
            options.compilerArgs.addAll(listOf("--enable-preview", "-Xlint:-preview")) // For records
        }

        dependencies.add(implementationConfigurationName, sourceSets.main.map { it.output })

        configurations.named(compileClasspathConfigurationName).configure { extendsFrom(configurations.testCompileClasspath.get()) }
        configurations.named(runtimeClasspathConfigurationName).configure { extendsFrom(configurations.testRuntimeClasspath.get()) }
    }

    tasks.test {
        testClassesDirs += java14Test.get().output.classesDirs
        classpath += java14Test.get().runtimeClasspath
        dependsOn(tasks.named(java14Test.get().compileJavaTaskName))
        jvmArgs("--enable-preview") // For records
    }
}
