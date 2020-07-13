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
import org.spongepowered.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.configurate.util.CheckedConsumer;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * A serializer for nodes that are 'list-like' (i.e may be stored in nodes where {@link ConfigurationNode#isList()} is
 * {@literal true}.
 *
 * @param <T> The type of collection to serialize
 */
abstract class AbstractListChildSerializer<T> implements TypeSerializer<T> {

    @Override
    public T deserialize(final Type type, final ConfigurationNode node) throws ObjectMappingException {
        final Type entryType = getElementType(type);
        final @Nullable TypeSerializer<?> entrySerial = node.getOptions().getSerializers().get(entryType);
        if (entrySerial == null) {
            throw new ObjectMappingException("No applicable type serializer for type " + entryType);
        }

        if (node.isList()) {
            final List<? extends ConfigurationNode> values = node.getChildrenList();
            final T ret = createNew(values.size(), entryType);
            for (int i = 0; i < values.size(); ++i) {
                deserializeSingle(i, ret, entrySerial.deserialize(entryType, values.get(i)));
            }
            return ret;
        } else {
            final @Nullable Object unwrappedVal = node.getValue();
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
    public void serialize(final Type type, final @Nullable T obj, final ConfigurationNode node) throws ObjectMappingException {
        final Type entryType = getElementType(type);
        final @Nullable TypeSerializer entrySerial = node.getOptions().getSerializers().get(entryType);
        if (entrySerial == null) {
            throw new ObjectMappingException("No applicable type serializer for type " + entryType);
        }

        node.setValue(Collections.emptyList());
        if (obj != null) {
            forEachElement(obj, el -> entrySerial.serialize(entryType, el, node.appendListNode()));
        }
    }

    /**
     * Given the type of container, provide the expected type of an element. If
     * the element type is not available, an exception must be thrown.
     *
     * @param containerType The type of container with type parameters resolved
     *                      to the extent possible.
     * @return The element type
     * @throws ObjectMappingException If the element type could not be detected
     */
    abstract Type getElementType(Type containerType) throws ObjectMappingException;

    /**
     * Create a new instance of the collection. The returned instance must be
     * mutable, but may have a fixed length.
     *
     * @param length The necessary collection length
     * @param elementType The type of element contained within the collection,
     *                    as provided by {@link #getElementType(Type)}
     * @return A newly created collection
     * @throws ObjectMappingException When an error occurs during the creation
     *                                of the collection
     */
    abstract T createNew(int length, Type elementType) throws ObjectMappingException;

    /**
     * Perform the provided action on each element of the provided collection.
     *
     * <p>This is equivalent to a foreach loop on the collection
     *
     * @param collection The collection to act on
     * @param action The action to perform
     * @throws ObjectMappingException When thrown by the underlying action
     */
    abstract void forEachElement(T collection, CheckedConsumer<Object, ObjectMappingException> action) throws ObjectMappingException;

    /**
     * Place a single deserialized value into the collection being deserialized.
     *
     * @param index Location to set value at
     * @param collection Collection to modify
     * @param deserialized value to add
     * @throws ObjectMappingException if object could not be coerced to an
     *         appropriate type.
     */
    abstract void deserializeSingle(int index, T collection, @Nullable Object deserialized) throws ObjectMappingException;

}
