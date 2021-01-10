import org.spongepowered.configurate.build.core
import org.spongepowered.configurate.build.format

plugins {
    `java-platform`
    id("org.spongepowered.configurate.build.publishing")
}

description = "Dependency alignment for all Configurate modules"

indra {
    configurePublications {
        from(components["javaPlatform"])
    }
}

dependencies {
    constraints {
        api(core())
        api(project(":extra:configurate-extra-kotlin"))
        api(project(":extra:configurate-extra-guice"))
        api(project(":extra:configurate-extra-dfu2"))
        api(project(":extra:configurate-extra-dfu3"))
        api(project(":extra:configurate-extra-dfu4"))
        api(project(":configurate-tool"))
        api(format("gson"))
        api(format("hocon"))
        api(format("jackson"))
        api(format("xml"))
        api(format("yaml"))
    }
}
