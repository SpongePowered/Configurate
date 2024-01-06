package test;

import java.lang.Override;
import java.lang.String;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Matches;
import org.spongepowered.configurate.objectmapping.meta.Required;

/**
 * Automatically generated implementation of the config */
@ConfigSerializable
final class ConfigurateAnnotationsImpl implements ConfigurateAnnotations {
    @Comment(override = true, value = "Hello!")
    @Matches(failureMessage = "ohno!", value = "abc")
    @Required
    private String hello;

    @Comment("Hi!")
    private String hi;

    @Override
    public String hello() {
        return hello;
    }

    @Override
    public String hi() {
        return hi;
    }
}
