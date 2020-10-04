import net.ltgt.gradle.errorprone.errorprone
import org.spongepowered.configurate.build.core

plugins {
    id("org.spongepowered.configurate-component")
}

dependencies {
    api(core())
    implementation("org.yaml:snakeyaml:1.27")
}

tasks.compileJava {
    options.errorprone.excludedPaths.set(".*org[\\\\/]spongepowered[\\\\/]configurate[\\\\/]yaml[\\\\/]ConfigurateScanner.*")
}
