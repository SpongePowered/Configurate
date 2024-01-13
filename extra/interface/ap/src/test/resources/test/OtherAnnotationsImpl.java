package test;

import java.lang.Override;
import java.lang.String;

import org.spongepowered.configurate.interfaces.processor.util.AnnotationOthersAnnotations;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Matches;
import org.spongepowered.configurate.objectmapping.meta.Required;

/**
 * Automatically generated implementation of the config */
@ConfigSerializable
final class OtherAnnotationsImpl implements OtherAnnotations {
    @Comment(value = "Hello!", override = true)
    @Matches(value = "abc", failureMessage = "ohno!")
    @Required
    private String hello;

    @Comment("Hi!")
    private String hi;

    private String noField;

    @AnnotationOthersAnnotations.AnnotationNoTarget
    private String noTarget;

    private String otherRetention;

    private String noRetention;

    @Override
    public String hello() {
        return hello;
    }

    @Override
    public String hi() {
        return hi;
    }

    @Override
    public String noField() {
        return noField;
    }

    @Override
    public String noTarget() {
        return noTarget;
    }

    @Override
    public String otherRetention() {
        return otherRetention;
    }

    @Override
    public String noRetention() {
        return noRetention;
    }
}
