import org.spongepowered.configurate.build.core
import org.spongepowered.configurate.build.format

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
        api(core())
        api(project(":ext-kotlin"))
        api(project(":tool"))
        api(format("gson"))
        api(format("hocon"))
        api(format("jackson"))
        api(format("xml"))
        api(format("yaml"))
    }
}
