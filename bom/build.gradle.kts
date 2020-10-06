import org.spongepowered.configurate.build.core
import org.spongepowered.configurate.build.format

plugins {
    `java-platform`
    id("org.spongepowered.configurate.build.publishing")
}

indra {
    configurePublications {
        from(components["javaPlatform"])
    }
}

dependencies {
    constraints {
        api(core())
        api(project(":extra:extra-kotlin"))
        api(project(":extra:extra-guice"))
        api(project(":extra:extra-dfu2"))
        api(project(":extra:extra-dfu3"))
        api(project(":extra:extra-dfu4"))
        api(project(":tool"))
        api(format("gson"))
        api(format("hocon"))
        api(format("jackson"))
        api(format("xml"))
        api(format("yaml"))
    }
}
