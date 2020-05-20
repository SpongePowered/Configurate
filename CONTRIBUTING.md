# Contribution Guidelines

We always appreciate high-quality submissions to Configurate. 

- Any minor (typo, formatting) changes are best made as issues. Often it is faster to just change the files ourselves rather than process a pull
 request.
- Any design changes or larger additions should be presented as an issue first
- No pull request will be accepted that breaks tests or does not pass checkstyle.

Feel free to be in touch on Discord or on GitHub issues with any proposals.

## Code Style

When in doubt, we follow the [Sponge code style guidelines](https://docs.spongepowered.org/stable/en/contributing/implementation/codestyle.html
). This code style is for the most part enforced by automated style checks.

When breaking lines to meet the maximums (80 columns for Javadoc, 150 columns for code), breaks should not leave a single word dangling, so any
 documentation line should have at least two words. Code lines should be broken in a way that helps understanding. As long as every line is under
  the limit, there is no need to get closer to the limit.

We differ from the Sponge project as a whole in the following areas:

- We avoid using the `Optional` class, instead preferring thorough nullability annotations. Most modern IDEs can verify those annotations
- Only `checker-qual` nullability annotations should be used.
- Lambda methods may use `$` to indicate an unused parameter
- Local variables and method parameters should be marked `final` if they are unchanged.
- Usage of Guava should be minimized. If an equivalent exists in the JDK, it should be used.

## Licensing

Configurate is released under the terms of the Apache 2.0 license. By submitting any contribution to the project, you agree to release that
 contribution under the license, though you are not required to assign copyright.
