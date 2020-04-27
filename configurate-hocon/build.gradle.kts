import org.spongepowered.configurate.build.Versions

plugins {
  id("org.spongepowered.configurate-component")
}

dependencies {
  api(project(":configurate-core"))
  api("com.typesafe:config:${Versions.HOCON}")
}
