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

import org.spongepowered.configurate.CommentedConfigurationNodeIntermediary;
import org.spongepowered.configurate.ConfigurationNode;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ResourceBundle;

/**
 * Performs a transformation on a value annotated with a specific type.
 *
 * @since 4.0.0
 */
@FunctionalInterface
public interface Processor<V> {

    /**
     * Transform the output node on write.
     *
     * @param value source value
     * @param destination destination node
     * @since 4.0.0
     */
    void process(V value, ConfigurationNode destination);

    /**
     * Provider to, given an annotation instance and the type it's on,
     * create a {@link Processor}.
     *
     * @param <A> annotation type
     * @param <T> handled value type
     * @since 4.0.0
     */
    @FunctionalInterface
    interface Factory<A extends Annotation, T> {

        /**
         * Create a new processor given the annotation and data type.
         *
         * @param data annotation type on record field
         * @param value declared field type
         * @return new processor
         * @since 4.0.0
         */
        Processor<T> make(A data, Type value);

    }

    /**
     * Apply comments from {@link Comment} annotation on save.
     *
     * @return a new processor factory
     * @since 4.0.0
     */
    static Processor.Factory<Comment, Object> comments() {
        return (data, fieldType) -> (value, destination) -> {
            if (destination instanceof CommentedConfigurationNodeIntermediary<?>) {
                final CommentedConfigurationNodeIntermediary<?> commented = (CommentedConfigurationNodeIntermediary<?>) destination;
                if (data.override()) {
                    commented.comment(data.value());
                } else {
                    commented.commentIfAbsent(data.value());
                }
            }
        };
    }

    /**
     * Apply localized comments from {@link Comment} annotation on save.
     *
     * <p>The {@link Comment#value() comment's value} will be treated as a key
     * into {@code source}, resolved to the system default locale. Missing keys
     * will be written literally to node.</p>
     *
     * @param source source bundle for comments
     * @return a new processor factory
     * @since 4.0.0
     */
    static Processor.Factory<Comment, Object> localizedComments(final ResourceBundle source) {
        return (data, fieldType) -> {
            final String translated = Localization.key(source, data.value());
            return (value, destination) -> {
                if (destination instanceof CommentedConfigurationNodeIntermediary<?>) {
                    final CommentedConfigurationNodeIntermediary<?> commented = (CommentedConfigurationNodeIntermediary<?>) destination;
                    if (data.override()) {
                        commented.comment(translated);
                    } else {
                        commented.commentIfAbsent(translated);
                    }
                }
            };
        };
    }

}
