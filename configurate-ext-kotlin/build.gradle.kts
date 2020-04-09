
plugins {
    kotlin("jvm")
    id("org.spongepowered.configurate-component")
}

dependencies {
    api(project(":configurate-core"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
}
