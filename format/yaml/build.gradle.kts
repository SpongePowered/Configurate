import net.ltgt.gradle.errorprone.errorprone
plugins {
    id("org.spongepowered.configurate.build.component")
}

description = "YAML format loader for Configurate"

dependencies {
    api(projects.core)
    implementation("org.yaml:snakeyaml:1.28")
}

tasks.compileJava {
    options.errorprone.excludedPaths.set(".*org[\\\\/]spongepowered[\\\\/]configurate[\\\\/]yaml[\\\\/]ConfigurateScanner.*")
    // our vendored version of ScannerImpl has invalid JD, so we have to suppress some warnings
    options.compilerArgs.add("-Xdoclint:-html")
}
