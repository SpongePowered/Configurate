package org.spongepowered.configurate.interfaces.processor;

import static org.spongepowered.configurate.interfaces.processor.Utils.hasAnnotation;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import org.spongepowered.configurate.interfaces.meta.Hidden;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

final class AnnotationHidden implements AnnotationProcessor {

    static final AnnotationHidden INSTANCE = new AnnotationHidden();

    private AnnotationHidden() {}

    @Override
    public Set<Class<? extends Annotation>> processes() {
        // the purpose of this class is to warn people, not to add the annotation.
        // AnnotationOthers can do that just fine
        return Collections.emptySet();
    }

    @Override
    public void process(
            final ExecutableElement element,
            final TypeMirror nodeType,
            final FieldSpecBuilderTracker fieldSpec
    ) throws IllegalStateException {
        if (!element.isDefault()) {
            return;
        }

        // throw exception to prevent unexpected behaviour during runtime
        if (hasAnnotation(element, Hidden.class) && AnnotationDefaults.hasNoAnnotationDefaults(element)) {
            throw new IllegalStateException(
                    "Due to limitations there is no support for methods that use the default value and Hidden. Method: " + element
            );
        }
    }

}
