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
        api(project(":configurate-xml"))
        api(project(":configurate-gson"))
        api(project(":configurate-yaml"))
        api(project(":configurate-hocon"))
        api(project(":configurate-jackson"))
        api(project(":configurate-tool"))
    }
}
