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

import com.google.common.reflect.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;

/**
 * Represents an object which can serialize and deserialize objects of a given type.
 *
 * @param <T> The type
 */
public interface TypeSerializer<T> {

    /**
     * Deserialize an object (of the correct type) from the given configuration node.
     *
     * @param type The type of return value required
     * @param value The node containing serialized data
     * @param <Node> The type of node to deserialize from
     * @return An object
     * @throws ObjectMappingException If the presented data is invalid
     */
    @Nullable
    <Node extends ConfigurationNode<Node>> T deserialize(@NonNull TypeToken<?> type, @NonNull Node value) throws ObjectMappingException;

    /**
     * Serialize an object to the given configuration node.
     *
     * @param type The type of the input object
     * @param obj The object to be serialized
     * @param value The node to write to
     * @param <Node> The type of node to serialize to
     * @throws ObjectMappingException If the object cannot be serialized
     */
    <Node extends ConfigurationNode<Node>> void serialize(@NonNull TypeToken<?> type, @Nullable T obj, @NonNull Node value) throws ObjectMappingException;

}
