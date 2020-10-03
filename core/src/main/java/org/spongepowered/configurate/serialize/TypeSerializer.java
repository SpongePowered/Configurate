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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.configurate.util.CheckedFunction;

import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Represents an object which can serialize and deserialize objects of a
 * given type.
 *
 * @param <T> The type
 */
public interface TypeSerializer<T> {

    /**
     * Given the provided functions, create a new serializer for a scalar value.
     *
     * <p>The returned serializer must fulfill all the requirements of a {@link ScalarSerializer}
     *
     * @param type The type of value returned by the serializer
     * @param serializer The serialization function, implementing {@link ScalarSerializer#serialize(Object, Predicate)}
     * @param deserializer The deserialization function, implementing {@link ScalarSerializer#deserialize(Type, Object)}
     * @param <T> The type of value to deserialize
     * @return A new and unregistered type serializer
     */
    static <T> ScalarSerializer<T> of(Type type, BiFunction<T, Predicate<Class<?>>, Object> serializer,
                                      CheckedFunction<Object, T, ObjectMappingException> deserializer) {
        return new FunctionScalarSerializer<>(type, deserializer, serializer);
    }

    /**
     * Given the provided functions, create a new serializer for a scalar value.
     *
     * <p>The returned serializer must fulfill all the requirements of
     * a {@link ScalarSerializer}
     *
     * @param type The type of value. Must not be a parameterized type
     * @param serializer The serialization function, implementing {@link ScalarSerializer#serialize(Object, Predicate)}
     * @param deserializer The deserialization function, implementing {@link ScalarSerializer#deserialize(Type, Object)}
     * @param <T> The type of value to deserialize
     * @see #of(Type, BiFunction, CheckedFunction) for the version of this
     *      function that takes a parameterized type
     * @return A new and unregistered type serializer
     */
    static <T> ScalarSerializer<T> of(Class<T> type,
            BiFunction<T, Predicate<Class<?>>, Object> serializer, CheckedFunction<Object, T, ObjectMappingException> deserializer) {
        if (type.getTypeParameters().length > 0) {
            throw new IllegalArgumentException("Parameterized types must be specified using TypeTokens, not raw classes");
        }

        return new FunctionScalarSerializer<T>(type, deserializer, serializer);
    }

    /**
     * Deserialize an object (of the correct type) from the given configuration
     * node.
     *
     * @param type The type of return value required
     * @param node The node containing serialized data
     * @return An object
     * @throws ObjectMappingException If the presented data is invalid
     */
    T deserialize(Type type, ConfigurationNode node) throws ObjectMappingException;

    /**
     * Serialize an object to the given configuration node.
     *
     * @param type The type of the input object
     * @param obj The object to be serialized
     * @param node The node to write to
     * @throws ObjectMappingException If the object cannot be serialized
     */
    void serialize(Type type, @Nullable T obj, ConfigurationNode node) throws ObjectMappingException;

    /**
     * Create an empty value of the appropriate type.
     *
     * <p>This method is for the most part designed to create empty collection
     * types, though it may be useful for scalars in limited cases.</p>
     *
     * @param specificType specific subtype to create an empty value of
     * @param options options used from the loading node
     * @return new empty value
     */
    default @Nullable T emptyValue(final Type specificType, ConfigurationOptions options) {
        return null;
    }

}
