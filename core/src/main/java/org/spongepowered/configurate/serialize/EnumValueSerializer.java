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

import static io.leangen.geantyref.GenericTypeReflector.erase;

import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.util.EnumLookup;

import java.lang.reflect.Type;
import java.util.function.Predicate;

final class EnumValueSerializer extends ScalarSerializer<Enum<?>> {

    EnumValueSerializer() {
        super(new TypeToken<Enum<?>>() {});
    }

    @Override
    public Enum<?> deserialize(final Type type, final Object obj) throws SerializationException {
        final String enumConstant = obj.toString();
        @SuppressWarnings("unchecked")
        final @Nullable Enum<?> ret = EnumLookup.lookupEnum(erase(type).asSubclass(Enum.class), enumConstant);
        if (ret == null) {
            throw new SerializationException(type, "Invalid enum constant provided, expected a value of enum, got " + enumConstant);
        }
        return ret;
    }

    @Override
    public Object serialize(final Enum<?> item, final Predicate<Class<?>> typeSupported) {
        return item.name();
    }

}
