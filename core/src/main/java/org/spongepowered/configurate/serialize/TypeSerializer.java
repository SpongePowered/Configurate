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
package org.spongepowered.configurate.serialize;

import io.leangen.geantyref.GenericTypeReflector;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.util.CheckedFunction;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Represents an object which can serialize and deserialize objects of a
 * given type.
 *
 * <p>The type serializer interface has methods both for working with
 * annotated types, and discarding annotated type information. If annotation
 * information is desired, the {@link Annotated} interface overrides the
 * standard TypeSerializer interface to prefer annotated type information.</p>
 *
 * @param <T> the type
 * @since 4.0.0
 */
public interface TypeSerializer<T> {

    /**
     * Given the provided functions, create a new serializer for a scalar value.
     *
     * <p>The returned serializer must fulfill all the requirements of a {@link ScalarSerializer}
     *
     * @param type the type of value returned by the serializer
     * @param serializer the serialization function, implementing {@link ScalarSerializer#serialize(Object, Predicate)}
     * @param deserializer the deserialization function, implementing {@link ScalarSerializer#deserialize(Type, Object)}
     * @param <T> the type of value to deserialize
     * @return a new and unregistered type serializer
     * @since 4.0.0
     */
    static <T> ScalarSerializer<T> of(final Type type, final BiFunction<T, Predicate<Class<?>>, Object> serializer,
                                      final CheckedFunction<Object, T, SerializationException> deserializer) {
        return new FunctionScalarSerializer<>(type, deserializer, serializer);
    }

    /**
     * Given the provided functions, create a new serializer for a scalar value.
     *
     * <p>The returned serializer must fulfill all the requirements of
     * a {@link ScalarSerializer}
     *
     * @param type the type of value. Must not be a parameterized type
     * @param serializer the serialization function, implementing {@link ScalarSerializer#serialize(Object, Predicate)}
     * @param deserializer the deserialization function, implementing {@link ScalarSerializer#deserialize(Type, Object)}
     * @param <T> the type of value to deserialize
     * @return a new and unregistered type serializer
     * @see #of(Type, BiFunction, CheckedFunction) for the version of this
     *      function that takes a parameterized type
     * @since 4.0.0
     */
    static <T> ScalarSerializer<T> of(final Class<T> type,
            final BiFunction<T, Predicate<Class<?>>, Object> serializer, final CheckedFunction<Object, T, SerializationException> deserializer) {
        if (type.getTypeParameters().length > 0) {
            throw new IllegalArgumentException("Parameterized types must be specified using TypeTokens, not raw classes");
        }

        return new FunctionScalarSerializer<T>(type, deserializer, serializer);
    }

    /**
     * Deserialize an object (of the correct type) from the given
     * configuration node.
     *
     * @param type the annotated type of return value required
     * @param node the node containing serialized data
     * @return an object
     * @throws SerializationException if the presented data is invalid
     * @since 4.2.0
     */
    default T deserialize(final AnnotatedType type, final ConfigurationNode node) throws SerializationException {
        return this.deserialize(type.getType(), node);
    }

    /**
     * Deserialize an object (of the correct type) from the given configuration
     * node.
     *
     * @param type the type of return value required
     * @param node the node containing serialized data
     * @return an object
     * @throws SerializationException if the presented data is invalid
     * @since 4.0.0
     */
    T deserialize(Type type, ConfigurationNode node) throws SerializationException;

    /**
     * Serialize an object to the given configuration node.
     *
     * @param type the annotated type of the input object
     * @param obj the object to be serialized
     * @param node the node to write to
     * @throws SerializationException if the object cannot be serialized
     * @since 4.2.0
     */
    default void serialize(final AnnotatedType type, @Nullable final T obj, final ConfigurationNode node) throws SerializationException {
        this.serialize(type.getType(), obj, node);
    }

    /**
     * Serialize an object to the given configuration node.
     *
     * @param type the type of the input object
     * @param obj the object to be serialized
     * @param node the node to write to
     * @throws SerializationException if the object cannot be serialized
     * @since 4.0.0
     */
    void serialize(Type type, @Nullable T obj, ConfigurationNode node) throws SerializationException;

    /**
     * Create an empty value of the appropriate type.
     *
     * <p>This method is for the most part designed to create empty collection
     * types, though it may be useful for scalars in limited cases.</p>
     *
     * @param specificType specific subtype to create an empty value of
     * @param options options used from the loading node
     * @return new empty value
     * @since 4.0.0
     */
    default @Nullable T emptyValue(final Type specificType, final ConfigurationOptions options) {
        return null;
    }

    /**
     * Create an empty value of the appropriate type.
     *
     * <p>This method is for the most part designed to create empty
     * collection types, though it may be useful for scalars
     * in limited cases.</p>
     *
     * @param specificType specific annotated subtype to create an empty
     *     value of
     * @param options options used from the loading node
     * @return new empty value
     * @since 4.2.0
     */
    default @Nullable T emptyValue(final AnnotatedType specificType, final ConfigurationOptions options) {
        return this.emptyValue(specificType.getType(), options);
    }

    /**
     * A type serializer that prefers type use annotation metadata to
     * deserialize the type.
     *
     *
     * @param <V> the value type
     * @since 4.2.0
     */
    interface Annotated<V> extends TypeSerializer<V> {

        @Override
        V deserialize(AnnotatedType type, ConfigurationNode node) throws SerializationException;

        @Override
        default V deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
            return this.deserialize(GenericTypeReflector.annotate(type), node);
        }

        @Override
        void serialize(AnnotatedType type, @Nullable V obj, ConfigurationNode node) throws SerializationException;

        @Override
        default void serialize(final Type type, @Nullable final V obj, final ConfigurationNode node) throws SerializationException {
            this.serialize(GenericTypeReflector.annotate(type), obj, node);
        }

        @Override
        default @Nullable V emptyValue(final AnnotatedType specificType, final ConfigurationOptions options) {
            return null;
        }

        @Override
        default @Nullable V emptyValue(final Type specificType, final ConfigurationOptions options) {
            return this.emptyValue(GenericTypeReflector.annotate(specificType), options);
        }

    }

}
