import net.minecrell.gradle.licenser.LicenseExtension

plugins {
  id("net.minecrell.licenser") version "0.4.1" apply false
  kotlin("jvm") version "1.3.71" apply false
}


subprojects {
  apply(plugin="java-library")
  apply(plugin = "maven-publish")
  apply(plugin = "signing")
  apply(plugin = "net.minecrell.licenser")

  group = "org.spongepowered"
  version = "4.0.0-SNAPSHOT"


  tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-path", "-Xlint:-serial", "-parameters"))
    options.isDeprecation = true
    options.encoding = "UTF-8"
  }

  extensions.configure(JavaPluginExtension::class) {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  val javadoc by tasks.getting(Javadoc::class) {
    val opts = this.options
    if (opts is StandardJavadocDocletOptions) {
      opts.links(
              "https://guava.dev/releases/25.1-jre/api/docs/"
      )
      opts.addBooleanOption("html5")
    }
  }

  extensions.configure(LicenseExtension::class) {
    header = rootProject.file("LICENSE_HEADER")
    include("**/*.java")
    include("**/*.kt")
    newLine = false
  }

  repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
  }

  dependencies {
    "testImplementation"("org.junit.jupiter:junit-jupiter-api:5.2.0")
    "testImplementation"("org.junit-pioneer:junit-pioneer:0.1.2")
    "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:5.2.0")
  }

  tasks.withType<Test> {
    useJUnitPlatform()
  }

  val publishing = extensions.getByType(PublishingExtension::class).apply{
    publications {
       register("maven", MavenPublication::class) {
        from(components["java"])

        pom {
          description.set("A simple configuration library for Java applications that can handle a variety of formats and " +
                  "provides a node-based data structure able to handle a wide variety of configuration schemas")
          name.set(project.name)
          url.set("https://github.com/SpongePowered/configurate/")

          inceptionYear.set("2014")

          developers {
            developer {
              name.set("zml")
              email.set("zml@aoeu.xyz")
            }
          }

          issueManagement {
            system.set("GitHub Issues")
            url.set("https://github.com/SpongePowered/configurate/issues")
          }

          licenses {
            license {
              name.set("Apache License, Version 2.0")
              url.set("https://opensource.org/licenses/Apache-2.0")
            }
          }

          scm {
            connection.set("scm:git@github.com:SpongePowered/configurate.git")
            developerConnection.set("scm:git@github.com:SpongePowered/configurate.git")
            url.set("https://github.com/SpongePowered/configurate/")
          }
        }
      }
    }

    if (project.hasProperty("spongeRepo") && project.hasProperty("spongeUsername") && project.hasProperty("spongePassword")) {
        repositories {
          maven(url = project.property("spongeRepo")!!) {
            name = "spongeRepo"
            credentials {
              username = project.property("spongeUsername") as String?
              password = project.property("spongePassword") as String?
            }
          }
        }
      }
  }

  extensions.configure(SigningExtension::class) {
    useGpgCmd()
    sign(publishing.publications["maven"] as Publication)
  }

  tasks.withType<Sign> {
    onlyIf { val version = project.version.toString()
      !version.endsWith("-SNAPSHOT") }
  }
}
