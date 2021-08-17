import org.spongepowered.configurate.build.Versions

plugins {
  id("org.spongepowered.configurate-component")
}

dependencies {
  api("com.google.guava:guava:${Versions.GUAVA}")
  implementation("com.google.inject:guice:4.2.3")
  testImplementation("com.google.inject:guice:4.2.3")
  api("org.checkerframework:checker-qual:2.4.0")
}

tasks.jar {
  manifest.attributes(
      "Automatic-Module-Name" to "org.spongepowered.configurate"
  )
}
