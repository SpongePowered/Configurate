import org.spongepowered.configurate.build.core
import org.spongepowered.configurate.build.format

plugins {
    id("org.spongepowered.configurate.build.component")
    application
}

dependencies {
    implementation(core())
    implementation(format("hocon"))
    implementation(format("yaml"))
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
