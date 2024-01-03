package org.spongepowered.configurate.interfaces.processor;

import java.lang.annotation.Annotation;
import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

final class Utils {

    private Utils() {
    }

    public static boolean hasAnnotation(final AnnotatedConstruct element, final Class<? extends Annotation> annotation) {
        //noinspection ConstantValue not everything is nonnull by default
        return element.getAnnotation(annotation) != null;
    }

    public static boolean isNestedConfig(final TypeElement type) {
        if (!type.getNestingKind().isNested()) {
            return false;
        }

        Element current = type;
        while (current.getKind() == ElementKind.INTERFACE && hasAnnotation(current, ConfigSerializable.class)) {
            current = current.getEnclosingElement();
        }
        return current.getKind() == ElementKind.PACKAGE;
    }

}
