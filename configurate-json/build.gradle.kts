import org.spongepowered.configurate.build.Versions

plugins {
  id("org.spongepowered.configurate-component")
}

dependencies {
  api(project(":configurate-core"))
  api("com.fasterxml.jackson.core:jackson-core:${Versions.JACKSON}")
}

tasks.jar {
  manifest.attributes(
          "Automatic-Module-Name" to "org.spongepowered.configurate.jackson"
  )
}
