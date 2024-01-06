package org.spongepowered.configurate.interfaces.processor;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Matches;
import org.spongepowered.configurate.objectmapping.meta.Required;

final class AnnotationConfigurate {

    private static final List<Class<? extends Annotation>> annotations = Arrays.asList(Comment.class, Matches.class, Required.class);

    private AnnotationConfigurate() {}

    static void process(final ExecutableElement element, final FieldSpec.Builder fieldSpec) {
        for (final Class<? extends Annotation> annotation : annotations) {
            final @Nullable Annotation current = element.getAnnotation(annotation);
            //noinspection ConstantValue
            if (current != null) {
                fieldSpec.addAnnotation(AnnotationSpec.get(current));
            }
        }
    }

}
