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
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link TypeSpec.Builder} does not keep track of duplicates, resulting in failures to compile.
 * This will only allow a single definition of a given method/field
 */
final class TypeSpecBuilderTracker {

    private final Map<String, FieldSpec.Builder> fieldSpecs = new LinkedHashMap<>();
    private final Map<String, MethodSpec.Builder> methodSpecs = new LinkedHashMap<>();
    private final Map<String, TypeSpec> typeSpecs = new LinkedHashMap<>();

    void add(final String fieldIdentifier, final FieldSpec.Builder builder, final boolean override) {
        final FieldSpec.Builder existing = override ? this.fieldSpecs.remove(fieldIdentifier)
                : this.fieldSpecs.get(fieldIdentifier);
        if (existing != null) {
            final FieldSpec existingBuild = existing.build();
            final FieldSpec builderBuild = builder.build();
            // copy initializer of the builder to the existing one if the existing one doesn't have an initializer
            if (existingBuild.initializer.isEmpty() && !builderBuild.initializer.isEmpty()) {
                existing.initializer(builderBuild.initializer);
            }
            existing.addAnnotations(pickNewAnnotations(existingBuild.annotations, builderBuild.annotations));
            if (override) {
                this.fieldSpecs.put(fieldIdentifier, existing);
            }
            return;
        }
        this.fieldSpecs.put(fieldIdentifier, builder);
    }

    void add(final String methodIdentifier, final MethodSpec.Builder builder) {
        final MethodSpec.Builder existing = this.methodSpecs.get(methodIdentifier);
        if (existing != null) {
            existing.addAnnotations(pickNewAnnotations(existing.build().annotations, builder.build().annotations));
            return;
        }
        this.methodSpecs.put(methodIdentifier, builder);
    }

    void add(final String typeIdentifier, final TypeSpec.Builder builder) {
        if (this.typeSpecs.putIfAbsent(typeIdentifier, builder.build()) != null) {
            throw new IllegalStateException(
                "Cannot have multiple nested types with the same name! Name: " + typeIdentifier);
        }
    }

    void writeTo(final TypeSpec.Builder builder) {
        for (FieldSpec.Builder field : this.fieldSpecs.values()) {
            builder.addField(field.build());
        }
        for (MethodSpec.Builder method : this.methodSpecs.values()) {
            builder.addMethod(method.build());
        }
        this.typeSpecs.values().forEach(builder::addType);
    }

    private List<AnnotationSpec> pickNewAnnotations(
        final List<AnnotationSpec> existing,
        final List<AnnotationSpec> newOne
    ) {
        final List<AnnotationSpec> result = new ArrayList<>();
        // only add annotations if they don't already exist
        outer: for (AnnotationSpec spec : newOne) {
            for (AnnotationSpec existingSpec : existing) {
                if (existingSpec.type.equals(spec.type)) {
                    break outer;
                }
            }
            result.add(spec);
        }
        return result;
    }

}
