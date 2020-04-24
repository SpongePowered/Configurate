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
import org.spongepowered.configurate.ScopedConfigurationNode;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;

/**
 * A TypeSerializer to directly access a {@link ConfigurationNode}. This allows bypassing the ObjectMapper in common cases.
 *
 * This serializer uses copies -- so changing the contents of the mapped node is not reflected in the source, and vice versa.
 */
class ConfigurationNodeSerializer implements TypeSerializer<ConfigurationNode> {
    static final TypeToken<ConfigurationNode> TYPE = TypeToken.of(ConfigurationNode.class);

    @Nullable
    @Override
    public <Node extends ScopedConfigurationNode<Node>> ConfigurationNode deserialize(@NonNull TypeToken<?> type, @NonNull Node node) throws ObjectMappingException {
        return node.copy();
    }

    @Override
    public <Node extends ScopedConfigurationNode<Node>> void serialize(@NonNull TypeToken<?> type, @Nullable ConfigurationNode obj, @NonNull Node node) throws ObjectMappingException {
        node.setValue(obj);
    }
}
