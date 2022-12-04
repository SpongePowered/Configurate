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

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Locale;

public final class UppercaseStringTypeSerializer implements TypeSerializer<@UpperCase String> {

    public static final UppercaseStringTypeSerializer INSTANCE = new UppercaseStringTypeSerializer();

    public static boolean applicable(final AnnotatedType type) {
        return (type.isAnnotationPresent(UpperCase.class) || type.isAnnotationPresent(UpperCase.Field.class)) && String.class.equals(type.getType());
    }

    private UppercaseStringTypeSerializer() {
    }

    @Override
    public @UpperCase String deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
        final String string = node.getString();
        return string.toUpperCase(Locale.ROOT);
    }

    @Override
    public void serialize(
        final Type type,
        final @UpperCase @Nullable String obj,
        final ConfigurationNode node
    ) throws SerializationException {
        if (obj == null) {
            node.set(null);
            return;
        }

        node.set(obj.toUpperCase(Locale.ROOT));
    }

}
