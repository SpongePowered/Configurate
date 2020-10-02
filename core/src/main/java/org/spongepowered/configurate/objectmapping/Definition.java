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
package org.spongepowered.configurate.objectmapping;

import com.google.auto.value.AutoValue;

import java.lang.annotation.Annotation;

/**
 * Object mapping rule definition.
 *
 * @param <A> annotation type
 * @param <T> field type
 * @param <F> factory type
 */
@AutoValue
abstract class Definition<A extends Annotation, T, F> {

    static <A extends Annotation, T, F> Definition<A, T, F> of(final Class<A> annotation, final Class<T> type,
            final F factory) {
        return new AutoValue_Definition<>(annotation, type, factory);
    }

    /**
     * Marker annotation type.
     *
     * @return annotation type
     */
    abstract Class<A> annotation();

    /**
     * Applicable value type.
     *
     * @return value type
     */
    abstract Class<T> type();

    /**
     * A factory that can create new instances of the rule.
     *
     * @return factory instance
     */
    abstract F factory();

}
