package test;

import org.spongepowered.configurate.interfaces.processor.util.AnnotationOthersAnnotations;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Matches;
import org.spongepowered.configurate.objectmapping.meta.Required;

@ConfigSerializable
public interface OtherAnnotations {
    @Comment(value = "Hello!", override = true)
    @Matches(value = "abc", failureMessage = "ohno!")
    @Required
    String hello();

    @Comment("Hi!")
    String hi();

    @AnnotationOthersAnnotations.AnnotationNoField
    String noField();

    @AnnotationOthersAnnotations.AnnotationNoTarget
    String noTarget();

    @AnnotationOthersAnnotations.AnnotationOtherRetention
    String otherRetention();

    @AnnotationOthersAnnotations.AnnotationNoRetention
    String noRetention();
}
