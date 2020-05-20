import org.spongepowered.configurate.build.Versions
import org.spongepowered.configurate.build.core

plugins {
  id("org.spongepowered.configurate-component")
}

dependencies {
  api(core())
  api("com.fasterxml.jackson.core:jackson-core:${Versions.JACKSON}")
}
