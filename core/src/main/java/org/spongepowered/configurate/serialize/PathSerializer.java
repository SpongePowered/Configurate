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
import org.spongepowered.configurate.ConfigurationNode;

import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

final class PathSerializer implements TypeSerializer<Path> {

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    static final PathSerializer INSTANCE = new PathSerializer();
    static final Class<Path> TYPE = Path.class;

    @Override
    public Path deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
        if (node.isList()) {
            final @Nullable List<String> elements = node.getList(String.class);
            if (elements == null || elements.isEmpty()) {
                return Paths.get(".");
            } else if (elements.size() == 1) {
                return Paths.get(elements.get(0));
            }

            return Paths.get(elements.get(0), elements.subList(1, elements.size()).toArray(EMPTY_STRING_ARRAY));
        } else if (node.isMap()) {
            throw new SerializationException("Paths must be a list of strings, or a single string");
        }
        final @Nullable Object value = node.rawScalar();
        if (value == null) {
            throw new SerializationException("must have scalar value");
        }

        if (value instanceof URI) {
            return Paths.get((URI) value);
        } else {
            return Paths.get(value.toString());
        }
    }

    @Override
    public void serialize(final Type type, final @Nullable Path obj, final ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.set(null);
            return;
        }

        if (node.isList()) {
            node.set(null);
            for (Path element : obj) {
                node.appendListNode().set(element.toString());
            }
        } else if (!obj.getFileSystem().equals(FileSystems.getDefault())) { // try to do something for non-default filesystems
            node.set(URI.class, obj.toUri());
        } else {
            node.raw(obj.toString());
        }
    }

}
