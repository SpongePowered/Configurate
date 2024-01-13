package org.spongepowered.configurate.interfaces.processor;

import com.squareup.javapoet.FieldSpec;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import java.util.ArrayList;
import java.util.List;

final class AnnotationProcessorHandler {

    private static final List<AnnotationProcessor> HANDLERS = new ArrayList<>();

    static {
        HANDLERS.add(AnnotationDefaults.INSTANCE);
        // always add others as last because it adds all annotations that have not been processed
        HANDLERS.add(AnnotationOthers.INSTANCE);
    }

    private AnnotationProcessorHandler() {}

    static void handle(final ExecutableElement element, final TypeMirror nodeType, final FieldSpec.Builder fieldSpec) {
        final FieldSpecBuilderTracker fieldTracker = new FieldSpecBuilderTracker(fieldSpec);

        for (AnnotationProcessor handler : HANDLERS) {
            handler.process(element, nodeType, fieldTracker);
            fieldTracker.processed(handler.processes());
        }
    }

}
