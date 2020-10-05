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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;

import java.lang.reflect.Type;

/**
 * A TypeSerializer to directly access a {@link ConfigurationNode}. This allows
 * bypassing the ObjectMapper in common cases.
 *
 * <p>This serializer uses copied nodes -- so changing the contents of the
 * mapped node is not reflected in the source, and vice versa.
 */
class ConfigurationNodeSerializer implements TypeSerializer<ConfigurationNode> {

    static final Class<ConfigurationNode> TYPE = ConfigurationNode.class;

    @Override
    public ConfigurationNode deserialize(final Type type, final ConfigurationNode node) {
        return node.copy();
    }

    @Override
    public void serialize(final Type type, final @Nullable ConfigurationNode obj, final ConfigurationNode node) throws SerializationException {
        node.set(obj);
    }

    @Override
    public @Nullable ConfigurationNode emptyValue(final Type specificType, final ConfigurationOptions options) {
        return BasicConfigurationNode.root(options);
    }

}
