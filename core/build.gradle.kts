import de.thetaphi.forbiddenapis.gradle.CheckForbiddenApis

plugins {
    jacoco
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

tasks {
    test {
        finalizedBy(jacocoTestReport)
    }
    jacocoTestReport {
        dependsOn(test)
    }
}

sourceSets {
    main {
        multirelease {
            alternateVersions(
                // 9, // VarHandles // TODO: temporarily disabled, cannot write final fields
                10, // immutable collections
                16 // FieldDiscoverer for records
            )
            // moduleName("org.spongepowered.configurate") // TODO: blocked by geantyref release
        }
    }

    test {
        multirelease {
            alternateVersions(16)
            configureVariants {
                tasks.named(variant().getTaskName("forbiddenApis", null), CheckForbiddenApis::class) {
                    isEnabled = false // fails to load records classes
                    targetCompatibility = "15" // todo: update when J16 signatures added
                }
            }
        }
    }
}
