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
package org.spongepowered.configurate.objectmapping.serialize;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ScopedConfigurationNode;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.configurate.util.ThrowingConsumer;

import java.util.List;

abstract class AbstractListChildSerializer<@NonNull T> implements TypeSerializer<T> {
    @Nullable
    @Override
    public <Node extends ScopedConfigurationNode<Node>> T deserialize(@NonNull TypeToken<?> type, @NonNull Node node) throws ObjectMappingException {
        TypeToken<?> entryType = getElementType(type);
        TypeSerializer<?> entrySerial = node.getOptions().getSerializers().get(entryType);
        if (entrySerial == null) {
            throw new ObjectMappingException("No applicable type serializer for type " + entryType);
        }

        if (node.isList()) {
            List<Node> values = node.getChildrenList();
            T ret = createNew(values.size(), entryType);
            for (int i = 0; i < values.size(); ++i) {
                deserializeSingle(i, ret, entrySerial.deserialize(entryType, values.get(i)));
            }
            return ret;
        } else {
            Object unwrappedVal = node.getValue();
            if (unwrappedVal != null) {
                T ret = createNew(1, entryType);
                deserializeSingle(0, ret, entrySerial.deserialize(entryType, node));
                return ret;
            }
        }
        return createNew(0, entryType);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <Node extends ScopedConfigurationNode<Node>> void serialize(@NonNull TypeToken<?> type, @Nullable T obj, @NonNull Node node) throws ObjectMappingException {
        TypeToken<?> entryType = getElementType(type);
        TypeSerializer entrySerial = node.getOptions().getSerializers().get(entryType);
        if (entrySerial == null) {
            throw new ObjectMappingException("No applicable type serializer for type " + entryType);
        }

        node.setValue(ImmutableList.of());
        if (obj != null) {
            forEachElement(obj, el -> {
                entrySerial.serialize(entryType, el, node.appendListNode());
            });
        }
    }

    abstract TypeToken<?> getElementType(TypeToken<?> containerType) throws ObjectMappingException;
    abstract T createNew(int length, TypeToken<?> elementType) throws ObjectMappingException;
    abstract void forEachElement(T collection, ThrowingConsumer<Object, ObjectMappingException> action) throws ObjectMappingException;
    abstract void deserializeSingle(int index, T collection, Object deserialized) throws ObjectMappingException;
}
