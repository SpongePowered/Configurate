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
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

class ListSerializer implements TypeSerializer<List<?>> {
    @Override
    public List<?> deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode<?> value) throws ObjectMappingException {
        if (!(type.getType() instanceof ParameterizedType)) {
            throw new ObjectMappingException("Raw types are not supported for collections");
        }
        TypeToken<?> entryType = type.resolveType(List.class.getTypeParameters()[0]);
        TypeSerializer<?> entrySerial = value.getOptions().getSerializers().get(entryType);
        if (entrySerial == null) {
            throw new ObjectMappingException("No applicable type serializer for type " + entryType);
        }

        if (value.hasListChildren()) {
            List<? extends ConfigurationNode<?>> values = value.getChildrenList();
            List<Object> ret = new ArrayList<>(values.size());
            for (ConfigurationNode<?> ent : values) {
                ret.add(entrySerial.deserialize(entryType, ent));
            }
            return ret;
        } else {
            Object unwrappedVal = value.getValue();
            if (unwrappedVal != null) {
                return Lists.newArrayList(entrySerial.deserialize(entryType, value));
            }
        }
        return new ArrayList<>();
    }

    @Override
    public void serialize(@NonNull TypeToken<?> type, @Nullable List<?> obj, @NonNull ConfigurationNode<?> value) throws ObjectMappingException {
        if (!(type.getType() instanceof ParameterizedType)) {
            throw new ObjectMappingException("Raw types are not supported for collections");
        }
        TypeToken<?> entryType = type.resolveType(List.class.getTypeParameters()[0]);
        TypeSerializer entrySerial = value.getOptions().getSerializers().get(entryType);
        if (entrySerial == null) {
            throw new ObjectMappingException("No applicable type serializer for type " + entryType);
        }
        value.setValue(ImmutableList.of());
        for (Object ent : obj) {
            entrySerial.serialize(entryType, ent, value.getAppendedNode());
        }
    }
}
