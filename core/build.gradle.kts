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
