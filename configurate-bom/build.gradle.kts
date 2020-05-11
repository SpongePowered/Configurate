plugins {
    `java-platform`
    id("org.spongepowered.configurate-publishing")
}

configurate {
    publish {
        from(components["javaPlatform"])
    }
}

dependencies {
    constraints {
        api(project(":configurate-core"))
        api(project(":configurate-ext-kotlin"))
        api(project(":configurate-gson"))
        api(project(":configurate-hocon"))
        api(project(":configurate-json"))
        api(project(":configurate-tool"))
        api(project(":configurate-xml"))
        api(project(":configurate-yaml"))
    }
}
