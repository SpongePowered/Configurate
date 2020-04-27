import org.spongepowered.configurate.build.Versions

plugins {
  id("org.spongepowered.configurate-component")
}

dependencies {
  api("com.google.guava:guava:${Versions.GUAVA}")
  implementation("com.google.inject:guice:4.1.0")
  testImplementation("com.google.inject:guice:4.1.0")
  api("org.checkerframework:checker-qual:2.4.0")
}
