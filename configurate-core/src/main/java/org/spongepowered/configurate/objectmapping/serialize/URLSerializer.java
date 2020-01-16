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

import java.net.MalformedURLException;
import java.net.URL;

class URLSerializer implements TypeSerializer<URL> {
    @Override
    public <Node extends ConfigurationNode<Node>> URL deserialize(@NonNull TypeToken<?> type, @NonNull Node value) throws ObjectMappingException {
        String plainUrl = value.getString();
        if (plainUrl == null) {
            throw new ObjectMappingException("No value present in node " + value);
        }

        URL url;
        try {
            url = new URL(plainUrl);
        } catch (MalformedURLException e) {
            throw new ObjectMappingException("Invalid URL string provided for " + value.getKey() + ": got " + plainUrl);
        }

        return url;
    }

    @Override
    public <T extends ConfigurationNode<T>> void serialize(@NonNull TypeToken<?> type, @Nullable URL obj, @NonNull T value) throws ObjectMappingException {
        value.setValue(obj.toString());
    }
}
