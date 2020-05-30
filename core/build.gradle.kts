import org.spongepowered.configurate.build.Versions

plugins {
    id("org.spongepowered.configurate-component")
}

java {
    registerFeature("guiceSupport") {
        usingSourceSet(sourceSets["main"])
    }
}

dependencies {
    implementation("com.google.auto.value:auto-value-annotations:1.7.2")
    annotationProcessor("com.google.auto.value:auto-value:1.7.2")
    api("com.google.guava:guava:${Versions.GUAVA}")
    "guiceSupportImplementation"("com.google.inject:guice:4.2.3")
    api("org.checkerframework:checker-qual:3.3.0")
}
