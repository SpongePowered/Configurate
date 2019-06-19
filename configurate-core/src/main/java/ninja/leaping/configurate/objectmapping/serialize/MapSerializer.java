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
package ninja.leaping.configurate.objectmapping.serialize;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.ParameterizedType;
import java.util.LinkedHashMap;
import java.util.Map;

class MapSerializer implements TypeSerializer<Map<?, ?>> {
    @Override
    public Map<?, ?> deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode node) throws ObjectMappingException {
        Map<Object, Object> ret = new LinkedHashMap<>();
        if (node.hasMapChildren()) {
            if (!(type.getType() instanceof ParameterizedType)) {
                throw new ObjectMappingException("Raw types are not supported for collections");
            }
            TypeToken<?> key = type.resolveType(Map.class.getTypeParameters()[0]);
            TypeToken<?> value = type.resolveType(Map.class.getTypeParameters()[1]);
            TypeSerializer<?> keySerial = node.getOptions().getSerializers().get(key);
            TypeSerializer<?> valueSerial = node.getOptions().getSerializers().get(value);

            if (keySerial == null) {
                throw new ObjectMappingException("No type serializer available for type " + key);
            }

            if (valueSerial == null) {
                throw new ObjectMappingException("No type serializer available for type " + value);
            }

            for (Map.Entry<Object, ? extends ConfigurationNode> ent : node.getChildrenMap().entrySet()) {
                Object keyValue = keySerial.deserialize(key, SimpleConfigurationNode.root().setValue(ent.getKey()));
                Object valueValue = valueSerial.deserialize(value, ent.getValue());
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
    public void serialize(@NonNull TypeToken<?> type, @Nullable Map<?, ?> obj, @NonNull ConfigurationNode node) throws ObjectMappingException {
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

        node.setValue(ImmutableMap.of());
        if (obj != null) {
            for (Map.Entry<?, ?> ent : obj.entrySet()) {
                SimpleConfigurationNode keyNode = SimpleConfigurationNode.root();
                keySerial.serialize(key, ent.getKey(), keyNode);
                valueSerial.serialize(value, ent.getValue(), node.getNode(keyNode.getValue()));
            }
        }
    }
}
