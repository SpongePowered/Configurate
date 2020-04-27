import org.spongepowered.configurate.build.Versions

plugins {
  id("org.spongepowered.configurate-component")
}

dependencies {
  api(project(":configurate-core"))
  api("org.yaml:snakeyaml:${Versions.SNAKEYAML}")
}
