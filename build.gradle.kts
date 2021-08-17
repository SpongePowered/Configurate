import org.spongepowered.configurate.build.applyCommonAttributes

plugins {
  kotlin("jvm") version "1.3.71" apply false
  id("io.freefair.aggregate-javadoc-jar") version "5.0.0-rc6"
  id("org.ajoberstar.grgit") version "4.0.2"
  id("org.ajoberstar.git-publish") version "3.0.0-rc.1"
  id("io.codearte.nexus-staging")
}

group = "org.spongepowered"
version = "3.7.3"

nexusStaging {
    val spongeOssrhUsername: String? by project
    val spongeOssrhPassword: String? by project
    username = spongeOssrhUsername
    password = spongeOssrhPassword
}

tasks.aggregateJavadoc.configure {
  applyCommonAttributes()
  title = "Configurate $version (all modules)"

  val excludedProjects = listOf("examples").map {
    project(":configurate-$it").tasks.named("javadoc", Javadoc::class).get().classpath
  }
  classpath = classpath.minus(files(excludedProjects))
}

gitPublish {
  branch.set("gh-pages")
  contents {
    from("src/site") {
      val versions = (listOf(project.version as String) + (grgit.tag.list().map { it.name }.reversed() - (project.version as String))).filter {
        repoDir.get().dir(it).getAsFile().exists() || it == project.version
      }
      expand("project" to project, "versions" to versions)
    }
    from(tasks.aggregateJavadoc) {
      into("$version/apidocs")
    }
  }

  preserve {
    include(".gitattributes")
    include("**/") // include everything in directories
    exclude("/*.html")
  }

  commitMessage.set("Publish javadocs (via gradle-git-publish)")
}

