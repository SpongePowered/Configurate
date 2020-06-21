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
package org.spongepowered.configurate.objectmapping.meta;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Perform a validation on data upon load.
 *
 * @param <V> value type
 */
public interface Constraint<V> {

    /**
     * Check if the provided deserialized value matches an expected condition.
     *
     * @param value Value to test
     * @throws ObjectMappingException if the value falls outside its constraint.
     */
    void validate(@Nullable V value) throws ObjectMappingException;

    /**
     * Provider for a specific constraint given a field type.
     *
     * @param <A> annotation type
     * @param <V> data type
     */
    interface Factory<A extends Annotation, V> {

        /**
         * Create a new specialized constraint.
         *
         * @param data annotation with metadata
         * @param type annotated type. is a subtype of {@code V}
         * @return new constraint
         */
        Constraint<V> make(A data, Type type);
    }

    /**
     * Require a value to be present for fields marked with the
     * annotation {@code T}.
     *
     * @param <T> marker annotation type
     * @return new constraint factory
     */
    static <T extends Annotation> Constraint.Factory<T, Object> required() {
        return (data, type) -> value -> {
            if (value == null) {
                throw new ObjectMappingException("A value is required for this field");
            }
        };
    }

    /**
     * Require values to match the {@link Matches#value() pattern} provided.
     *
     * <p>Upon failure, an error message will be taken from the annotation.</p>
     *
     * @return factory providing matching pattern test
     */
    static Constraint.Factory<Matches, String> pattern() {
        return (data, type) -> {
            final Pattern test = Pattern.compile(data.value());
            final MessageFormat format = new MessageFormat(data.failureMessage());
            return value -> {
                if (value != null) {
                    final Matcher match = test.matcher(value);
                    if (!match.matches()) {
                        throw new ObjectMappingException(format.format(new Object[]{value, data.value()}));
                    }
                }
            };
        };
    }

    /**
     * Require values to match the {@link Matches#value() pattern} provided.
     *
     * <p>Upon failure, an error message will be taken from {@code bundle} with
     * a key defined in the annotation.</p>
     *
     * @param bundle source for localized messages
     * @return factory providing matching pattern test
     */
    static Constraint.Factory<Matches, String> localizedPattern(final ResourceBundle bundle) {
        return (data, type) -> {
            final Pattern test = Pattern.compile(data.value());
            final MessageFormat format = new MessageFormat(Localization.key(bundle, data.failureMessage()), bundle.getLocale());
            return value -> {
                if (value != null) {
                    final Matcher match = test.matcher(value);
                    if (!match.matches()) {
                        throw new ObjectMappingException(format.format(new Object[]{value, data.value()}));
                    }
                }
            };
        };
    }

}
