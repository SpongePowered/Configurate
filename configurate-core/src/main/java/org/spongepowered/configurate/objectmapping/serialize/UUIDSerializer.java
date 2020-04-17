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
import org.spongepowered.configurate.ScopedConfigurationNode;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;

import java.util.UUID;

class UUIDSerializer implements TypeSerializer<UUID> {
    @Override
    public <Node extends ScopedConfigurationNode<Node>> UUID deserialize(@NonNull TypeToken<?> type, @NonNull Node node) throws ObjectMappingException {
        try {
            return UUID.fromString(node.getString());
        } catch (IllegalArgumentException ex) {
            throw new ObjectMappingException("Value not a UUID", ex);
        }
    }

    @Override
    public <Node extends ScopedConfigurationNode<Node>> void serialize(@NonNull TypeToken<?> type, @Nullable UUID obj, @NonNull Node node) throws ObjectMappingException {
        node.setValue(obj.toString());
    }
}
