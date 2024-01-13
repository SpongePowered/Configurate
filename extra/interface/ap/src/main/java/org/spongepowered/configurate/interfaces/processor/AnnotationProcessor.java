package org.spongepowered.configurate.interfaces.processor;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import java.lang.annotation.Annotation;
import java.util.Set;

interface AnnotationProcessor {

    /**
     * A set of annotations this processor will process.
     *
     * @return a set of annotations this processor will process.
     */
    Set<Class<? extends Annotation>> processes();

    /**
     * Process a method.
     * There is no guarantee that one of the {@link #processes()} annotations is present on this element.
     *
     * @param element the method that is being processed
     * @param nodeType the type of the field that is being generated
     * @param fieldSpec the builder of the field that is being generated
     * @throws IllegalStateException when something goes wrong
     */
    void process(ExecutableElement element, TypeMirror nodeType, FieldSpecBuilderTracker fieldSpec) throws IllegalStateException;

}
