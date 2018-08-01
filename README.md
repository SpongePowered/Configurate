# Configurate
Configurate is a simple configuration library released under the [Apache 2.0](LICENSE) that provides a node-tree representation of configurations in a variety of formats.

*Build Status*: [![Travis CI](https://travis-ci.org/SpongePowered/configurate.svg)](https://travis-ci.org/SpongePowered/configurate)

*Javadocs*: http://configurate.aoeu.xyz/apidocs

Want to talk to us about Configurate? Come to the `#dev-irc` channel in our [Discord](https://discord.gg/PtaGRAs) or the `#spongedev` channel on `irc.esper.net` where people familiar with the project will hang around.

## Prerequisites

- [Java](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) 8

## Clone
The following steps will ensure your project is cloned properly.

1. `git clone https://github.com/SpongePowered/configurate.git`
2. `cd configurate`

## Building
**Note:** If you do not have [Gradle](https://www.gradle.org/) installed then use ./gradlew for Unix systems or Git Bash and gradlew.bat for Windows systems in place of any 'gradle' command.

In order to build Configurate you simply need to run the `gradle build` command. You can find the compiled JAR files in `./build/libs`  (found in each subproject) labeled similarly to 'configurate-subproject-x.x-SNAPSHOT.jar'.

## Usage

**Gradle**:
```groovy
repositories {
    mavenCentral()
}

dependencies {
    compile 'ninja.leaping.configurate:configurate-hocon:3.6'
}
```

**Maven**:
```xml
<dependency>
    <groupId>ninja.leaping.configurate</groupId>
    <artifactId>configurate-hocon</artifactId>
    <version>3.6</version> <!-- Update this with the most recent version -->
</dependency>
``` 

For other build systems, take a look at the [full list on the Configurate site](http://configurate.aoeu.xyz/configurate-hocon/dependency-info.html)

This dependency statement is for the hocon format implementation. Other formats managed in this repository use the same group id and versioning.

Now, to load:
```java
ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setPath(file).build(); // Create the loader
CommentedConfigurationNode node = loader.load(); // Load the configuration into memory

node.getNode("some", "value").getValue(); // Get the value
```
More detailed explanations of all the methods available in ConfigurationNode are available in the javadocs.

## Contributing
We love PRs! However, when contributing, here are some things to keep in mind:

- Take a look at open issues first before you get too far in -- someone might already be working on what you were planning on doing
- In general, we follow the Oracle style guidelines for code style
- Please, please, please test PRs. It makes the process a lot easier for everybody :)

