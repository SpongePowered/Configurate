# Configurate
Configurate is a simple configuration library released under the [Apache 2.0](LICENSE) that provides a node-tree representation of configurations in a variety of formats.

*Build Status*: [![Travis CI](https://travis-ci.org/SpongePowered/configurate.svg)](https://travis-ci.org/SpongePowered/configurate)

*Javadocs*: http://configurate.aoeu.xyz/apidocs

Want to talk to us about Configurate? Come to the `#dev-irc` channel in our [Discord](https://discord.gg/PtaGRAs) or the `#spongedev` channel on `irc.esper.net` where people familiar with the project will hang around.


## Building
We use Gradle, so this part is pretty easy. 

Configurate requires JDK 8 to build and run.

From the project's directory (the root of this repository), run `gradle clean build` to build Configurate. Its artifacts will be located at `./build/libs/`.

## Usage

**Gradle**:
```groovy
repositories {
    mavenCentral()
}

dependencies {
    compile 'ninja.leaping.configurate:configurate-hocon:3.3'
}
```

**Maven**:
```xml
<dependency>
    <groupId>ninja.leaping.configurate</groupId>
    <artifactId>configurate-hocon</artifactId>
    <version>3.3</version> <!-- Update this with the most recent version -->
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

