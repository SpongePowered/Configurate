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

import static java.util.Objects.requireNonNull;

import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

final class MapSerializer implements TypeSerializer.Annotated<Map<?, ?>> {

    static final TypeToken<Map<?, ?>> TYPE = new TypeToken<Map<?, ?>>() {};

    MapSerializer() {
    }

    @Override
    public Map<?, ?> deserialize(final AnnotatedType type, final ConfigurationNode node) throws SerializationException {
        final Map<Object, Object> ret = new LinkedHashMap<>();
        if (node.isMap()) {
            if (!(type instanceof AnnotatedParameterizedType)) {
                throw new SerializationException(type, "Raw types are not supported for collections");
            }
            final AnnotatedParameterizedType param = (AnnotatedParameterizedType) type;
            final AnnotatedType[] typeArgs = param.getAnnotatedActualTypeArguments();
            if (typeArgs.length != 2) {
                throw new SerializationException(type, "Map expected two type arguments!");
            }
            final AnnotatedType key = typeArgs[0];
            final AnnotatedType value = typeArgs[1];
            final @Nullable TypeSerializer<?> keySerial = node.options().serializers().get(key);
            final @Nullable TypeSerializer<?> valueSerial = node.options().serializers().get(value);

            if (keySerial == null) {
                throw new SerializationException(type, "No type serializer available for key type " + key);
            }

            if (valueSerial == null) {
                throw new SerializationException(type, "No type serializer available for value type " + value);
            }

            final BasicConfigurationNode keyNode = BasicConfigurationNode.root(node.options());

            for (final Map.Entry<Object, ? extends ConfigurationNode> ent : node.childrenMap().entrySet()) {
                ret.put(requireNonNull(keySerial.deserialize(key, keyNode.set(ent.getKey())), "key"),
                    requireNonNull(valueSerial.deserialize(value, ent.getValue()), "value"));
            }
        }
        return ret;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void serialize(final AnnotatedType type, final @Nullable Map<?, ?> obj, final ConfigurationNode node) throws SerializationException {
        if (!(type instanceof AnnotatedParameterizedType)) {
            throw new SerializationException(type, "Raw types are not supported for collections");
        }
        final AnnotatedParameterizedType param = (AnnotatedParameterizedType) type;
        final AnnotatedType[] typeArgs = param.getAnnotatedActualTypeArguments();
        if (typeArgs.length != 2) {
            throw new SerializationException(type, "Map expected two type arguments!");
        }
        final AnnotatedType key = typeArgs[0];
        final AnnotatedType value = typeArgs[1];
        final @Nullable TypeSerializer keySerial = node.options().serializers().get(key);
        final @Nullable TypeSerializer valueSerial = node.options().serializers().get(value);

        if (keySerial == null) {
            throw new SerializationException(type, "No type serializer available for key type " + key);
        }

        if (valueSerial == null) {
            throw new SerializationException(type, "No type serializer available for value type " + value);
        }

        if (obj == null || obj.isEmpty()) {
            node.set(Collections.emptyMap());
        } else {
            final Set<Object> unvisitedKeys;
            if (node.empty()) {
                node.raw(Collections.emptyMap());
                unvisitedKeys = Collections.emptySet();
            } else {
                unvisitedKeys = new HashSet<>(node.childrenMap().keySet());
            }
            final BasicConfigurationNode keyNode = BasicConfigurationNode.root(node.options());
            for (final Map.Entry<?, ?> ent : obj.entrySet()) {
                keySerial.serialize(key, ent.getKey(), keyNode);
                final Object keyObj = requireNonNull(keyNode.raw(), "Key must not be null!");
                final ConfigurationNode child = node.node(keyObj);
                try {
                    valueSerial.serialize(value, ent.getValue(), child);
                } catch (final SerializationException ex) {
                    ex.initPath(child::path);
                } finally {
                    unvisitedKeys.remove(keyObj);
                }
            }

            for (final Object unusedChild : unvisitedKeys) {
                node.removeChild(unusedChild);
            }
        }
    }

    @Override
    public Map<?, ?> emptyValue(final AnnotatedType specificType, final ConfigurationOptions options) {
        return new LinkedHashMap<>();
    }

}
