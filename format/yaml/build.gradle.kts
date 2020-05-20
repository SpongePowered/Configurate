import org.spongepowered.configurate.build.Versions
import org.spongepowered.configurate.build.core

plugins {
  id("org.spongepowered.configurate-component")
}

dependencies {
  api(core())
  api("org.yaml:snakeyaml:${Versions.SNAKEYAML}")
}
