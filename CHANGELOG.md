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
