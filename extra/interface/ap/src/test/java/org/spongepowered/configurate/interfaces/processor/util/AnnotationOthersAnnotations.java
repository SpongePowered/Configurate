package org.spongepowered.configurate.interfaces.processor.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class AnnotationOthersAnnotations {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface AnnotationNoField {}

    @Retention(RetentionPolicy.RUNTIME)
    public @interface AnnotationNoTarget {}

    @Target({ElementType.METHOD, ElementType.FIELD})
    @Retention(RetentionPolicy.CLASS)
    public @interface AnnotationOtherRetention {}

    @Target({ElementType.METHOD, ElementType.FIELD})
    public @interface AnnotationNoRetention {}

}
