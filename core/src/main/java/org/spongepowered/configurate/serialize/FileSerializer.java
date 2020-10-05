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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Path;

/**
 * Provided for legacy applications that still need to interact with
 * <em>legacy</em> applications.
 */
final class FileSerializer implements TypeSerializer<File> {

    static final FileSerializer INSTANCE = new FileSerializer();
    static final Class<File> TYPE = File.class;

    private FileSerializer() {
    }

    @Override
    public File deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
        return requireNonNull(node.get(Path.class), "node did not contain a valid path").toFile();
    }

    @Override
    public void serialize(final Type type, final @Nullable File obj, final ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.set(null);
        } else {
            node.set(Path.class, obj.toPath());
        }
    }

}
