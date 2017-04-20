Changelog
========

Next Release
------------
- Update Maven and various Maven plugins
- Update snakeyaml to 1.18 from 1.16 in YAML module
- Update jackson to 2.8.8 from 2.6.3 in JSON module
- Update typesafe config to 1.3.1 from 1.3.0 in HOCON module
- Update gson to 2.8.0 from 2.2.4 in GSON module
- Move usage of removed Guava method

3.2
---
- Allow Gson module to save empty files
- Resolve configuration variables for hocon
- Fix various issues on Windows
- Correct file permissions
- Improve error message when unable to find an appropriate TypeSerializer or when using raw types
- Allow stripping header from files entirely

3.1.1
-----
- Correctly use UTF-8 when loading from a URL
- Make setValue(TypeToken, T) be a default method in ConfigurationNode
- Correct TypeSerializer handling of empty maps and lists

3.1
---
- gson: Correct handling of long/double numbers
- core: Correctly silence NoSuchFileException when loading
- core: Allow specifying the default options to be used in a loader
- all: Refactor AbstractConfigurationLoader's constructor to take a Builder, making future extensibility easier
- core: Add function-requesting default methods for non-primitive type methods (using a Supplier)

3.0
---
- Update to require java8
- Change map factory system to allow constructing a map with any key or value type
- Switch from Guava's CharSource/Sink to Callable factories.
- Improve HOCON's preservation of configuration element order (only when writing configurations)

2.1
---
- gson: Fix GsonConfigurationLoader's handling of empty files
- core: Add a new field to ConfigurationOptions that allows default values used to be set to the config if no value is present.

2.0
---
- [BREAKING] core: Refactor TypeSerializer to use registry, be more specific about how registries are handled
- Fix atomic output stream's temporary file locations in some cases
- Fix transferring of comments to nested values
- Fix gson loader reading all numbers as doubles
- Add support for working with typed values using a TypeSerializer
- Add a type serializer for Patterns
- Allow configuration nodes to specify a limited list of acceptable types

1.2.2
-----
- core: properly implement equals() and hashCode() for configuration nodes
- core: correctly pass key type to key serializer in map type serializer

1.2.1
-----
- core: Properly remove values removed from maps while reserializing data
- gson: Downgrade gson dependency to 2.2.4 for expanded compatibility
- hocon: Properly load empty maps and lists

1.2
---
- core: add url and uri serializers
- core: add uuid serializers
- dependency version bumps (minor versions)


1.1.1
----
- core: Fix jdk6&windows compatibility

1.1
---
- core: Fix jdk6 compatibility
- core: Add equals, hashCode, and toString to node objects
- json: Bump jackson dependency to 2.5.2
- json: Make pretty printing more flexible with new methods on Builder

1.0.1
-----
- Handle objects with null fields being serialized
- Handle concurrent removal of nodes while saving in HoconConfigurationLoader
- Fix node path comparator to correctly handle paths with wildcards in a few cases

1.0
---
- Bug fixes
- Add merging of config values
- Add getKey, getParent, and getPath
- Add merged and versioned transformers
- Refactor ObjectMapper to be fancier and work better
- Add gson-backed JSON configuration loader
- Implement header loading
- Add support for atomic writing
- Allow choosing between serveal map implementations for nodes with map children
- Polishing

0.2
---
- Implemented object mapper system
- Added configuration migration support
- Added configurability to loaders

0.1
---
- Initial release. Supports YAML, JSON, and HOCON
