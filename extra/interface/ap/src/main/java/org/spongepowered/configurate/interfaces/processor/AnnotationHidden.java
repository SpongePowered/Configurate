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

import static org.spongepowered.configurate.interfaces.processor.Utils.hasAnnotation;

import org.spongepowered.configurate.interfaces.meta.Hidden;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

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
            final TypeElement targetInterface,
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
