plugins {
  id("org.spongepowered.configurate-component")
}


java {
  registerFeature("guiceSupport") {
    usingSourceSet(sourceSets["main"])
  }
}

dependencies {
  api("com.google.guava:guava:25.1-jre")
  "guiceSupportImplementation"("com.google.inject:guice:4.2.3")
  api("org.checkerframework:checker-qual:2.4.0")
}
