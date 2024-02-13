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
package org.spongepowered.configurate.interfaces;

import static io.leangen.geantyref.GenericTypeReflector.erase;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;
import java.util.StringJoiner;

final class InterfaceTypeSerializer implements TypeSerializer<Object> {

    public static final InterfaceTypeSerializer INSTANCE = new InterfaceTypeSerializer();

    private final Properties interfaceMappings = new Properties();

    public static boolean applicable(final AnnotatedType type) {
        return type.isAnnotationPresent(ConfigSerializable.class) && erase(type.getType()).isInterface();
    }

    private InterfaceTypeSerializer() {
        final @Nullable URL mappingsUrl = getClass().getClassLoader().getResource(Constants.MAPPING_FILE);
        if (mappingsUrl == null) {
            return;
        }

        try (InputStream stream = mappingsUrl.openStream()) {
            this.interfaceMappings.load(stream);
        } catch (final IOException exception) {
            throw new RuntimeException("Could not load interface mappings!", exception);
        }
    }

    @Override
    public Object deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
        final String canonicalName = erase(type).getTypeName();
        final @Nullable String typeImpl = this.interfaceMappings.getProperty(canonicalName);
        if (typeImpl == null) {
            throw new SerializationException(String.format(
                Locale.ROOT,
                "No mapping found for type %s. Available mappings: %s",
                canonicalName, availableMappings()
            ));
        }

        final Class<?> implClass;
        try {
            implClass = Class.forName(typeImpl, true, erase(type).getClassLoader());
        } catch (final ClassNotFoundException exception) {
            throw new SerializationException(String.format(
                Locale.ROOT,
                "Could not find implementation class %s for type %s!",
                typeImpl, canonicalName
            ));
        }

        final @Nullable TypeSerializer<?> serializer = node.options().serializers().get(implClass);
        if (serializer == null) {
            throw new SerializationException("No serializer found for implementation class " + implClass);
        }
        return serializer.deserialize(implClass, node);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void serialize(
        final Type type,
        final @Nullable Object obj,
        final ConfigurationNode node
    ) throws SerializationException {
        // don't determine serialize from type, because that might be incorrect for subsections
        if (obj == null) {
            node.set(null);
            return;
        }

        final @Nullable TypeSerializer serializer = node.options().serializers().get(obj.getClass());
        if (serializer == null) {
            throw new SerializationException("No serializer found for implementation class " + obj.getClass());
        }
        serializer.serialize(obj.getClass(), obj, node);
    }

    private String availableMappings() {
        final StringJoiner joiner = new StringJoiner(", ");
        this.interfaceMappings.keySet().forEach((key) -> joiner.add((CharSequence) key));
        return joiner.toString();
    }

}
