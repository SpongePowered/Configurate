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

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ScopedConfigurationNode;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;

import java.lang.reflect.ParameterizedType;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

class MapSerializer implements TypeSerializer<Map<?, ?>> {
    static TypeToken<Map<?, ?>> TYPE = new TypeToken<Map<?, ?>>() {};

    @Override
    public <Node extends ScopedConfigurationNode<Node>> Map<?, ?> deserialize(@NonNull TypeToken<?> type, @NonNull Node node) throws ObjectMappingException {
        Map<Object, Object> ret = new LinkedHashMap<>();
        if (node.isMap()) {
            if (!(type.getType() instanceof ParameterizedType)) {
                throw new ObjectMappingException("Raw types are not supported for collections");
            }
            TypeToken<?> key = type.resolveType(Map.class.getTypeParameters()[0]);
            TypeToken<?> value = type.resolveType(Map.class.getTypeParameters()[1]);
            @Nullable TypeSerializer<?> keySerial = node.getOptions().getSerializers().get(key);
            @Nullable TypeSerializer<?> valueSerial = node.getOptions().getSerializers().get(value);

            if (keySerial == null) {
                throw new ObjectMappingException("No type serializer available for type " + key);
            }

            if (valueSerial == null) {
                throw new ObjectMappingException("No type serializer available for type " + value);
            }

            final BasicConfigurationNode keyNode = BasicConfigurationNode.root(node.getOptions());

            for (Map.Entry<Object, Node> ent : node.getChildrenMap().entrySet()) {
                @Nullable Object keyValue = keySerial.deserialize(key, keyNode.setValue(ent.getKey()));
                @Nullable Object valueValue = valueSerial.deserialize(value, ent.getValue());
                if (keyValue == null || valueValue == null) {
                    continue;
                }

                ret.put(keyValue, valueValue);
            }
        }
        return ret;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T extends ScopedConfigurationNode<T>> void serialize(@NonNull TypeToken<?> type, @Nullable Map<?, ?> obj, @NonNull T node) throws ObjectMappingException {
        if (!(type.getType() instanceof ParameterizedType)) {
            throw new ObjectMappingException("Raw types are not supported for collections");
        }
        TypeToken<?> key = type.resolveType(Map.class.getTypeParameters()[0]);
        TypeToken<?> value = type.resolveType(Map.class.getTypeParameters()[1]);
        TypeSerializer keySerial = node.getOptions().getSerializers().get(key);
        TypeSerializer valueSerial = node.getOptions().getSerializers().get(value);

        if (keySerial == null) {
            throw new ObjectMappingException("No type serializer available for type " + key);
        }

        if (valueSerial == null) {
            throw new ObjectMappingException("No type serializer available for type " + value);
        }

        if (obj == null || obj.isEmpty()) {
            node.setValue(ImmutableMap.of());
        } else {
            final Set<Object> unvisitedKeys = new HashSet<>(node.getChildrenMap().keySet());
            BasicConfigurationNode keyNode = BasicConfigurationNode.root(node.getOptions());
            for (Map.Entry<?, ?> ent : obj.entrySet()) {
                keySerial.serialize(key, ent.getKey(), keyNode);
                Object keyObj = requireNonNull(keyNode.getValue(), "Key must not be null!");
                valueSerial.serialize(value, ent.getValue(), node.getNode(keyObj));
                unvisitedKeys.remove(keyObj);
            }

            for (Object unusedChild : unvisitedKeys) {
                node.removeChild(unusedChild);
            }
        }
    }
}
