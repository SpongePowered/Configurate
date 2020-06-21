plugins {
    id("org.spongepowered.configurate-component")
}

val exposedCompileOnly by configurations.registering
configurations {
    apiElements.configure {
        extendsFrom(exposedCompileOnly.get())
    }
    compileOnly.configure {
        extendsFrom(exposedCompileOnly.get())
    }
}

dependencies {
    api("io.leangen.geantyref:geantyref:1.3.11")
    "exposedCompileOnly"("org.checkerframework:checker-qual:3.5.0")
    compileOnly("com.google.auto.value:auto-value-annotations:1.7.4")
    annotationProcessor("com.google.auto.value:auto-value:1.7.4")
    testImplementation("com.google.guava:guava:29.0-jre")
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
