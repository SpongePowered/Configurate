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
package org.spongepowered.configurate.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class JsonElementSerializer implements TypeSerializer<JsonElement> {

    static final JsonElementSerializer INSTANCE = new JsonElementSerializer();

    private JsonElementSerializer() {
    }

    @Override
    public JsonElement deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
        if (type.equals(JsonArray.class) && !node.isList()) {
            throw new SerializationException(node, type, "Expected node to be a list, but it was not!");
        }

        if (type.equals(JsonObject.class) && !node.isMap()) {
            throw new SerializationException(node, type, "Expected node to be a map, but it was not!");
        }

        if (node.isList()) { // Become a JSON array
            final JsonArray ret = new JsonArray();
            for (final ConfigurationNode child : node.childrenList()) {
                ret.add(child.get(JsonElement.class));
            }
            return ret;
        } else if (node.isMap()) {
            final JsonObject ret = new JsonObject();
            for (final Map.Entry<Object, ? extends ConfigurationNode> entry : node.childrenMap().entrySet()) {
                ret.add(String.valueOf(entry.getKey()), entry.getValue().get(JsonElement.class));
            }
            return ret;
        }

        final @Nullable Object raw = node.rawScalar();
        if (raw == null) {
            return JsonNull.INSTANCE;
        } else if (raw instanceof Boolean) {
            return new JsonPrimitive((Boolean) raw);
        } else if (raw instanceof Number) {
            return new JsonPrimitive((Number) raw);
        } else if (raw instanceof Character) {
            return new JsonPrimitive((Character) raw);
        } else if (raw instanceof CharSequence) {
            return new JsonPrimitive(((CharSequence) raw).toString());
        }

        throw new SerializationException(node, type, "Unsure how to convert value of type " + node.raw() + " to a json element ");
    }

    @Override
    public void serialize(final Type type, final @Nullable JsonElement obj, final ConfigurationNode node) throws SerializationException {
        if (obj == null || obj instanceof JsonNull) {
            node.raw(null);
            return;
        }

        if (obj instanceof JsonArray) {
            serializeArray((JsonArray) obj, node);
        } else if (obj instanceof JsonObject) {
            serializeObject((JsonObject) obj, node);
        } else if (obj instanceof JsonPrimitive) {
            final JsonPrimitive primitive = (JsonPrimitive) obj;
            if (primitive.isBoolean()) {
                node.set(primitive.getAsBoolean());
            } else if (primitive.isNumber()) {
                node.set(primitive.getAsNumber());
            } else if (primitive.isString()) {
                node.set(primitive.getAsString());
            } else {
                throw new SerializationException(node, type, "Unknown type of primitive: " + primitive);
            }
        } else {
            throw new SerializationException(node, type, "Unknown JsonElement subtype: " + obj.getClass());
        }
    }

    private void serializeArray(final JsonArray array, final ConfigurationNode target) throws SerializationException {
        target.set(Collections.emptyList());
        // TODO: Attempt to preserve comments/attributes
        for (final JsonElement child : array) {
            target.appendListNode().set(JsonElement.class, child);
        }
    }

    private void serializeObject(final JsonObject object, final ConfigurationNode target) throws SerializationException {
        if (object.size() == 0) {
            target.set(Collections.emptyMap());
        } else {
            // Logic taken from MapSerializer to preserve comments and other metadata in source node
            final Set<Object> unvisitedKeys;
            if (target.empty()) {
                target.raw(Collections.emptyMap());
                unvisitedKeys = Collections.emptySet();
            } else {
                unvisitedKeys = new HashSet<>(target.childrenMap().keySet());
            }

            for (Map.Entry<String, JsonElement> ent : object.entrySet()) {
                final ConfigurationNode child = target.node(ent.getKey());
                try {
                    child.set(JsonElement.class, ent.getValue());
                } catch (final SerializationException ex) {
                    ex.initPath(child::path);
                } finally {
                    unvisitedKeys.remove(ent.getKey());
                }
            }

            for (Object unusedChild : unvisitedKeys) {
                target.removeChild(unusedChild);
            }
        }
    }

    @Override
    public @Nullable JsonElement emptyValue(final Type specificType, final ConfigurationOptions options) {
        if (specificType.equals(JsonObject.class)) {
            return new JsonObject();
        } else if (specificType.equals(JsonArray.class)) {
            return new JsonArray();
        }
        return null;
    }

}
