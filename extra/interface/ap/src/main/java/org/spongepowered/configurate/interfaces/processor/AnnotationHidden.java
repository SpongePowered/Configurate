package org.spongepowered.configurate.interfaces.processor;

import com.squareup.javapoet.FieldSpec;
import javax.lang.model.element.ExecutableElement;
import org.spongepowered.configurate.interfaces.meta.Hidden;

final class AnnotationHidden {

    private AnnotationHidden() {}

    static void process(final ExecutableElement element, final FieldSpec.Builder fieldSpec) {
        if (Utils.hasAnnotation(element, Hidden.class)) {
            fieldSpec.addAnnotation(Hidden.class);
        }
    }

}
