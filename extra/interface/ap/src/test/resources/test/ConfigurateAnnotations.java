package test;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Matches;
import org.spongepowered.configurate.objectmapping.meta.Required;

@ConfigSerializable
public interface ConfigurateAnnotations {
    @Comment(value = "Hello!", override = true)
    @Matches(value = "abc", failureMessage = "ohno!")
    @Required
    String hello();

    @Comment("Hi!")
    String hi();
}
