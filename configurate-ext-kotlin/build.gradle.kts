plugins {
    kotlin("jvm") version "1.3.71"
}

dependencies {
    api(project(":configurate-core"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
}
