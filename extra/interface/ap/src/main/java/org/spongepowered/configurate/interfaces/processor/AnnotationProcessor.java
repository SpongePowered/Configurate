/*
 * Configurate
 * Copyright (C) zml and Configurate contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spongepowered.configurate.interfaces.processor;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

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
    void process(
            TypeElement targetInterface,
            ExecutableElement element,
            TypeMirror nodeType,
            FieldSpecBuilderTracker fieldSpec
    ) throws IllegalStateException;

}
