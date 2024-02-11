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

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.TypeElement;

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
