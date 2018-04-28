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

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

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
     * @return An object
     * @throws ObjectMappingException If the presented data is invalid
     */
    @Nullable
    T deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws ObjectMappingException;

    /**
     * Serialize an object to the given configuration node.
     *
     * @param type The type of the input object
     * @param obj The object to be serialized
     * @param value The node to write to
     * @throws ObjectMappingException If the object cannot be serialized
     */
    void serialize(@NonNull TypeToken<?> type, @Nullable T obj, @NonNull ConfigurationNode value) throws ObjectMappingException;

}
