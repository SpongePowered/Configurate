import org.spongepowered.configurate.build.core

plugins {
    id("org.spongepowered.configurate.build.component")
    groovy
}

tasks.withType(GroovyCompile::class).configureEach {
    // Toolchains need this... for some reason
    options.release.set(indra.javaVersions.target)
}

tasks.processResources {
    inputs.property("version", project.version)
    expand("version" to project.version)
}

dependencies {
    api(core())

    implementation("org.codehaus.groovy:groovy:3.+:indy")
    implementation("org.codehaus.groovy:groovy-nio:3.+:indy")
    testImplementation("org.codehaus.groovy:groovy-test:3.+:indy")
}
