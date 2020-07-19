import org.spongepowered.configurate.build.core

plugins {
    id("org.spongepowered.configurate-component")
    antlr
}

configurations.compile {
    exclude("org.antlr", "antlr4")
}

dependencies {
    antlr("org.antlr:antlr4:4.8-1")
    implementation("org.antlr:antlr4-runtime:4.8-1")

    api(core())
}
