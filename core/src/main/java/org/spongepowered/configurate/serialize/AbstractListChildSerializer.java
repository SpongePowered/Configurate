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

import net.kyori.coffee.function.Consumer1E;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * A serializer for nodes that are 'list-like' (i.e may be stored in nodes where {@link ConfigurationNode#isList()} is
 * {@literal true}.
 *
 * @param <T> the type of collection to serialize
 */
abstract class AbstractListChildSerializer<T> implements TypeSerializer<T> {

    @Override
    public T deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
        final Type entryType = elementType(type);
        final @Nullable TypeSerializer<?> entrySerial = node.options().serializers().get(entryType);
        if (entrySerial == null) {
            throw new SerializationException(node, entryType, "No applicable type serializer for type");
        }

        if (node.isList()) {
            final List<? extends ConfigurationNode> values = node.childrenList();
            final T ret = createNew(values.size(), entryType);
            for (int i = 0; i < values.size(); ++i) {
                try {
                    deserializeSingle(i, ret, entrySerial.deserialize(entryType, values.get(i)));
                } catch (final SerializationException ex) {
                    ex.initPath(values.get(i)::path);
                    throw ex;
                }
            }
            return ret;
        } else {
            final @Nullable Object unwrappedVal = node.raw();
            if (unwrappedVal != null) {
                final T ret = createNew(1, entryType);
                deserializeSingle(0, ret, entrySerial.deserialize(entryType, node));
                return ret;
            }
        }
        return createNew(0, entryType);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void serialize(final Type type, final @Nullable T obj, final ConfigurationNode node) throws SerializationException {
        final Type entryType = elementType(type);
        final @Nullable TypeSerializer entrySerial = node.options().serializers().get(entryType);
        if (entrySerial == null) {
            throw new SerializationException(node, entryType, "No applicable type serializer for type");
        }

        node.raw(Collections.emptyList());
        if (obj != null) {
            forEachElement(obj, el -> {
                final ConfigurationNode child = node.appendListNode();
                try {
                    entrySerial.serialize(entryType, el, child);
                } catch (final SerializationException ex) {
                    ex.initPath(child::path);
                    throw ex;
                }
            });
        }
    }

    @Override
    public @Nullable T emptyValue(final Type specificType, final ConfigurationOptions options) {
        try {
            return this.createNew(0, elementType(specificType));
        } catch (final SerializationException ex) {
            return null;
        }
    }

    /**
     * Given the type of container, provide the expected type of an element. If
     * the element type is not available, an exception must be thrown.
     *
     * @param containerType the type of container with type parameters resolved
     *                      to the extent possible.
     * @return the element type
     * @throws SerializationException if the element type could not be detected
     */
    abstract Type elementType(Type containerType) throws SerializationException;

    /**
     * Create a new instance of the collection. The returned instance must be
     * mutable, but may have a fixed length.
     *
     * @param length the necessary collection length
     * @param elementType the type of element contained within the collection,
     *                    as provided by {@link #elementType(Type)}
     * @return a newly created collection
     * @throws SerializationException when an error occurs during the creation
     *                                of the collection
     */
    abstract T createNew(int length, Type elementType) throws SerializationException;

    /**
     * Perform the provided action on each element of the provided collection.
     *
     * <p>This is equivalent to a foreach loop on the collection
     *
     * @param collection the collection to act on
     * @param action the action to perform
     * @throws SerializationException when thrown by the underlying action
     */
    abstract void forEachElement(T collection, Consumer1E<Object, SerializationException> action) throws SerializationException;

    /**
     * Place a single deserialized value into the collection being deserialized.
     *
     * @param index location to set value at
     * @param collection collection to modify
     * @param deserialized value to add
     * @throws SerializationException if object could not be coerced to an
     *         appropriate type.
     */
    abstract void deserializeSingle(int index, T collection, @Nullable Object deserialized) throws SerializationException;

}
