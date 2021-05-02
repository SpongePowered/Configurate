plugins {
    id("org.spongepowered.configurate.build.component")
    application
}

dependencies {
    implementation(projects.core)
    implementation(projects.format.hocon)
    implementation(projects.format.yaml)
}

application {
    mainClass.set("org.spongepowered.configurate.examples.ValueReferences")
}

tasks.withType<AbstractPublishToMaven>().configureEach {
    onlyIf { false } // don't publish
}

tasks.withType<Javadoc>().configureEach {
    (options as? StandardJavadocDocletOptions)?.apply {
        addBooleanOption("Xdoclint:-missing", true)
    }
}
