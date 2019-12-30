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
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.configurate.util.ThrowingConsumer;

import java.util.List;

abstract class AbstractListChildSerializer<T> implements TypeSerializer<T> {
    @Nullable
    @Override
    public final T deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode<?> value) throws ObjectMappingException {
        TypeToken<?> entryType = getElementType(type);
        TypeSerializer<?> entrySerial = value.getOptions().getSerializers().get(entryType);
        if (entrySerial == null) {
            throw new ObjectMappingException("No applicable type serializer for type " + entryType);
        }

        if (value.hasListChildren()) {
            List<? extends ConfigurationNode<?>> values = value.getChildrenList();
            T ret = createNew(values.size(), entryType);
            for (int i = 0; i < values.size(); ++i) {
                deserializeSingle(i, ret, entrySerial.deserialize(entryType, values.get(i)));
            }
            return ret;
        } else {
            Object unwrappedVal = value.getValue();
            if (unwrappedVal != null) {
                T ret = createNew(1, entryType);
                deserializeSingle(0, ret, entrySerial.deserialize(entryType, value));
                return ret;
            }
        }
        return createNew(0, entryType);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public final void serialize(@NonNull TypeToken<?> type, @Nullable T obj, @NonNull ConfigurationNode<?> value) throws ObjectMappingException {
        TypeToken<?> entryType = getElementType(type);
        TypeSerializer entrySerial = value.getOptions().getSerializers().get(entryType);
        if (entrySerial == null) {
            throw new ObjectMappingException("No applicable type serializer for type " + entryType);
        }

        value.setValue(ImmutableList.of());
        if (obj != null) {
            forEachElement(obj, el -> {
                entrySerial.serialize(entryType, el, value.appendListNode());
            });
        }
    }

    abstract TypeToken<?> getElementType(TypeToken<?> containerType) throws ObjectMappingException;
    abstract T createNew(int length, TypeToken<?> elementType) throws ObjectMappingException;
    abstract void forEachElement(T collection, ThrowingConsumer<Object, ObjectMappingException> action) throws ObjectMappingException;
    abstract void deserializeSingle(int index, T collection, Object deserialized) throws ObjectMappingException;
}
