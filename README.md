# Configurate
Configurate is a simple configuration library released under the [Apache 2.0](LICENSE) that provides a node-tree representation of configurations in a variety of formats.

*Build Status*: [![Travis CI](https://travis-ci.org/zml2008/configurate.svg)](https://travis-ci.org/zml2008/configurate)
*Javadocs*: http://zml2008.github.io/configurate/apidocs


## Building
We use Maven, so this part is pretty easy. 

Configurate requires JDK 8 to build and run.

Make sure Maven is installed and from the project's directory (the root of this repository), run `mvn clean install` to build Configuate and install its artifacts to the local Maven repository.

## Usage
```xml
<dependency>
    <groupId>ninja.leaping.configurate</groupId>
    <artifactId>configurate-hocon</artifactId>
    <version>3.2</version> <!-- Update this with the most recent version -->
</dependency>
```
This dependency statement is for the hocon format implementation. Other formats managed in this repository use the same group id and versioning.
The only dependency Configurate has is on Guava (check the pom for info on which version is currently being used)

Now, to load:
```java
ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setPath(file).build(); // Create the loader
CommentedConfigurationNode node = loader.load(); // Load the configuration into memory

node.getNode("some", "value").getValue(); // Get the value
```
More detailed explanations of all the methods available in ConfigurationNode are available in the javadocs.

## Contributing
We love PRs! However, when contributing, here are some things to keep in mind:

- In general, we follow the Oracle style guidelines for code style
- Please, please, please test PRs. It makes the process a lot easier for everybody

