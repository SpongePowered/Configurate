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

import java.net.URI;
import java.net.URISyntaxException;

class URISerializer implements TypeSerializer<URI> {
    @Override
    public <Node extends ScopedConfigurationNode<Node>> URI deserialize(@NonNull TypeToken<?> type, @NonNull Node value) throws ObjectMappingException {
        String plainUri = value.getString();
        if (plainUri == null) {
            throw new ObjectMappingException("No value present in node " + value);
        }

        URI uri;
        try {
            uri = new URI(plainUri);
        } catch (URISyntaxException e) {
             throw new ObjectMappingException("Invalid URI string provided for " + value.getKey() + ": got " + plainUri);
        }

        return uri;
    }

    @Override
    public <Node extends ScopedConfigurationNode<Node>> void serialize(@NonNull TypeToken<?> type, @Nullable URI obj, @NonNull Node value) throws ObjectMappingException {
        value.setValue(obj == null ? null : obj.toString());
    }
}
