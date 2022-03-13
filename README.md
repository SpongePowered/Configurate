# [Configurate](https://configurate.aoeu.xyz/) 

![GitHub Workflow Status (branch)](https://img.shields.io/github/workflow/status/SpongePowered/Configurate/Build%20And%20Test/master)
[![MIT License](https://img.shields.io/badge/license-Apache%202.0-blue)](LICENSE) 
[![Maven Central](https://img.shields.io/maven-central/v/org.spongepowered/configurate-core?label=stable&color=%23f6cf17)][Maven Central]
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/org.spongepowered/configurate-core?label=dev&server=https%3A%2F%2Foss.sonatype.org)

Configurate is a simple configuration library for Java applications that provides a node-based representation of data, able to handle a wide variety of configuration formats.

Want to talk to us about Configurate? Join us in the `#dev` channel on our [Discord](https://discord.gg/PtaGRAs) or start a thread on our (new!) [Discussions](https://github.com/SpongePowered/Configurate/discussions) page.

The current supported formats are:

* [JSON](https://www.json.org/)
* [HOCON](https://github.com/lightbend/config/blob/master/HOCON.md)
* [YAML](http://yaml.org/)
* [XML](https://www.w3.org/XML/)

## Project Structure
The project is split into different modules.

#### Configurate core
`configurate-core` is the base of the library, containing the main APIs used to manipulate configurations. It is generic, and does not depend on any specific format of configuration.

#### Configurate loaders
Each distinct configuration format is implemented as a "configuration loader", in a separate module.

A number of loader implementations are provided as standard in this project, however it is possible to implement a custom loader for a new format separately.

The current supported loaders provided by the project are:

* `configurate-gson` - Implementation for the JSON format, using the [Gson](https://github.com/google/gson) library for parsing and generation
* `configurate-hocon` - Implementation for the HOCON format, using the [lightbend config](https://github.com/lightbend/config) library for parsing and generation
* `configurate-jackson` - Implementation for the JSON format, using the [Jackson](https://github.com/FasterXML/jackson-core) library for parsing and generation
* `configurate-xml` - Implementation for the XML format, using the [JAXP](https://docs.oracle.com/javase/tutorial/jaxp/index.html) library for parsing and generation
* `configurate-yaml` - Implementation for the YAML format, using the [SnakeYAML](https://bitbucket.org/asomov/snakeyaml) library for parsing and generation

#### extras

Some features that don't need tight integration with Configurate itself are provided as separate modules. These are:

* `configurate-extra-dfu[2-4]` - Integration between Mojang's [DataFixerUpper](https://github.com/Mojang/DataFixerUpper) and Configurate's systems.
* `configurate-extra-kotlin` - Extensions to allow Configurate types to use [Kotlin] features, and support for Kotlin data classes in the object
 mapper.
* `configurate-extra-guice` - Allows using a [Guice](https://github.com/google/guice) `Injector` to create new object instances in the object mapper.


## Usage

* To use Configurate, your project must be configured to use Java 8 or higher.
* Releases are on [Maven Central] and snapshot artifacts are hosted on Sonatype OSS. Builds are also included on SpongePowered's Maven Repository
, available at https://repo.spongepowered.org/maven/.

If your project uses Maven or Gradle, just add the following to your build scripts.

#### Gradle

```groovy
repositories {
    mavenCentral()
}

dependencies {
    // Modify this line to target the loader you wish to use.
    compile 'org.spongepowered:configurate-hocon:4.0.0'
}
```

#### Maven

```xml
<dependencies>
    <dependency>
        <groupId>org.spongepowered</groupId>
        <!-- Modify this line to target the loader you wish to use. -->
        <artifactId>configurate-hocon</artifactId>
        <version>4.0.0</version>
    </dependency>
</dependencies>
```

More detailed usage instructions can be found in the [Configurate wiki](https://github.com/SpongePowered/Configurate/wiki).

## Contributing

#### Clone
The following steps will ensure your project is cloned properly.

1. `git clone https://github.com/SpongePowered/Configurate.git`
2. `cd Configurate`

#### Building
**Note:** If you do not have [Gradle](https://www.gradle.org/) 7.0+ installed then use ./gradlew for Unix systems or Git Bash and gradlew.bat for
 Windows systems in place of any 'gradle' command.

In order to build Configurate you simply need to run the `gradle build` command. You can find the compiled JAR files in `./build/libs`  (found in
 each subproject) labeled similarly to '<subproject>-x.x.x-SNAPSHOT.jar'.
 
 While the entire project can run on Java 8, the project requires Java 11 to build, and for multi-release JAR contents and some tests requires at least Java 16. Gradle will automatically download necessary JDK versions -- see [their documentation](https://docs.gradle.org/current/userguide/toolchains.html) for details on this feature, and how to point it at existing local installations.

#### Pull Requests
We love PRs! However, when contributing, here are some things to keep in mind:

- Take a look at open issues first before you get too far in -- someone might already be working on what you were planning on doing
- In general, we follow the [Sponge style guidelines](https://docs.spongepowered.org/stable/en/contributing/implementation/codestyle.html) for code
 style -- see the [Contributing Guidelines](CONTRIBUTING.md) for details.
- Please, please, please test PRs. It makes the process a lot easier for everybody :)


[Kotlin]: https://kotlinlang.org
[Maven Central]: https://search.maven.org/search?q=g:org.spongepowered%20AND%20a:configurate*
