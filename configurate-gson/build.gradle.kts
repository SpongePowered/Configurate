plugins {
  id("org.spongepowered.configurate-component")
}

dependencies {
  api(project(":configurate-core"))
  implementation("com.google.code.gson:gson:2.8.0")
}
