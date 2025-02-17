import javax.xml.parsers.DocumentBuilderFactory

plugins {
    id("net.minecraftforge.gitpatcher") version "0.10.+"
}

patches {
    submodule = "snakeyaml-upstream"
    target = file("snakeyaml")
    patches = file("snakeyaml-patches")
}

subprojects {
    apply(plugin = "java-library")

    group = "configurate.thirdparty"
    version = "version-from-submodule"

    tasks.withType(JavaCompile::class) {
        options.encoding = "UTF-8"
    }

    extensions.configure(JavaPluginExtension::class) {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(11))
        }
    }
}

project(":snakeyaml") {
    val mavenPom = project.file("../snakeyaml-upstream/pom.xml")
    if (mavenPom.exists()) {
        val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document = documentBuilder.parse(mavenPom)
        val mavenProject = document.getElementsByTagName("project").item(0)

        fun org.w3c.dom.NodeList.element(tagName: String): org.w3c.dom.Element? {
            for (i in 0..this.length) {
                val element = this.item(i)
                if (element is org.w3c.dom.Element && element.tagName == tagName) {
                    return element
                }
            }
            return null
        }

        dependencies {
            val dependencies = mavenProject.childNodes.element("dependencies")!!.childNodes
            for (i in 0..dependencies.length) {
                val dep = dependencies.item(i)
                if (dep is org.w3c.dom.Element && dep.tagName == "dependency") {
                    val children = dep.childNodes
                    val group = children.element("groupId")?.textContent
                    val artifact = children.element("artifactId")?.textContent
                    val version = children.element("version")?.textContent
                    val configuration = when (children.element("scope")?.textContent) {
                        "test" -> "testImplementation"
                        "compile" -> "implementation"
                        "runtime" -> "runtime"
                        "provided" -> "compileOnly"
                        else -> null
                    }
                    if (configuration != null) {
                        configuration(group!!, artifact!!, version)
                    }
                }
            }
        }
    }

    val applyPatches = rootProject.tasks.named("applyPatches")
    tasks.withType(JavaCompile::class) {
        options.release.set(7)
        // dependsOn(applyPatches)
    }
    tasks.withType(ProcessResources::class) {
        // dependsOn(applyPatches)
    }

    tasks.named("test", Test::class) {
        environment(
                "EnvironmentKey1" to "EnvironmentValue1",
                "environmentEmpty" to ""
        )
        filter {
            // needs classpath provided via expansions, doesn't seem to be easily doable with the maven-style property names
            excludeTest("org.yaml.snakeyaml.issues.issue318.ContextClassLoaderTest", null)
        }
    }
}