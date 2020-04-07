plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":configurate-core"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
}
