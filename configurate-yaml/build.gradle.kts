plugins {
  id("org.spongepowered.configurate-component")
}

dependencies {
  api(project(":configurate-core"))
  api("org.yaml:snakeyaml:1.25")
}
