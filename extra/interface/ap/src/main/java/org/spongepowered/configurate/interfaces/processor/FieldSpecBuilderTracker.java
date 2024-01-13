package org.spongepowered.configurate.interfaces.processor;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import javax.lang.model.element.TypeElement;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

final class FieldSpecBuilderTracker {

    private final Set<ClassName> processed = new HashSet<>();
    private final FieldSpec.Builder builder;

    FieldSpecBuilderTracker(final FieldSpec.Builder builder) {
        this.builder = builder;
    }

    void addAnnotation(final AnnotationSpec annotation) {
        this.processed.add(((ClassName) annotation.type));
        this.builder.addAnnotation(annotation);
    }

    void addAnnotation(final AnnotationSpec.Builder annotationBuilder) {
        addAnnotation(annotationBuilder.build());
    }

    void initializer(final String format, final Object... args) {
        this.builder.initializer(format, args);
    }

    boolean isProcessed(final Class<? extends Annotation> annotation) {
        return this.processed.contains(ClassName.get(annotation));
    }

    boolean isProcessed(final TypeElement annotationType) {
        return this.processed.contains(ClassName.get(annotationType));
    }

    void processed(final Collection<Class<? extends Annotation>> annotations) {
        for (Class<? extends Annotation> annotation : annotations) {
            this.processed.add(ClassName.get(annotation));
        }
    }

}
