package org.spongepowered.configurate.interfaces.processor;

import static org.spongepowered.configurate.interfaces.processor.Utils.annotation;

import com.google.auto.common.MoreElements;

import com.squareup.javapoet.AnnotationSpec;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import java.util.Arrays;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

final class AnnotationOthers implements AnnotationProcessor {

    static final AnnotationOthers INSTANCE = new AnnotationOthers();

    private AnnotationOthers() {}

    @Override
    public Set<Class<? extends Annotation>> processes() {
        return new HashSet<>();
    }

    @Override
    public void process(final ExecutableElement element, final TypeMirror nodeType, final FieldSpecBuilderTracker fieldSpec) {
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            //noinspection UnstableApiUsage
            final TypeElement annotationType = MoreElements.asType(annotationMirror.getAnnotationType().asElement());

            // only handle not yet processed annotations
            if (fieldSpec.isProcessed(annotationType)) {
                continue;
            }

            final @Nullable Target target = annotation(annotationType, Target.class);
            final boolean isCompatible = target == null || Arrays.stream(target.value()).anyMatch(elementType -> ElementType.FIELD == elementType);
            // an annotation is only compatible if it supports fields, if it has no target it supports everything
            if (!isCompatible) {
                continue;
            }

            final @Nullable Retention retention = annotation(annotationType, Retention.class);
            final boolean hasRuntimeRetention = retention != null && RetentionPolicy.RUNTIME == retention.value();
            // not needed to add an annotation if it has no runtime retention
            if (!hasRuntimeRetention) {
                continue;
            }

            fieldSpec.addAnnotation(AnnotationSpec.get(annotationMirror));
        }
    }

}
