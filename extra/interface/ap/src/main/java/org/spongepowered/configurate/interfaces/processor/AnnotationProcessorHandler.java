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

import com.squareup.javapoet.FieldSpec;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

final class AnnotationProcessorHandler {

    private static final List<AnnotationProcessor> HANDLERS = new ArrayList<>();

    static {
        HANDLERS.add(AnnotationDefaults.INSTANCE);
        HANDLERS.add(AnnotationHidden.INSTANCE);
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
