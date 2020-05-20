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

import com.google.common.reflect.TypeToken;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.configurate.util.EnumLookup;

import java.util.Optional;
import java.util.function.Predicate;

final class EnumValueSerializer extends ScalarSerializer<Enum<?>> {

    EnumValueSerializer() {
        super(new TypeToken<Enum<?>>() {});
    }

    @Override
    public Enum<?> deserialize(TypeToken<?> type, Object obj) throws ObjectMappingException {
        final String enumConstant = obj.toString();
        @SuppressWarnings("unchecked")
        Optional<? extends Enum<?>> ret = EnumLookup.lookupEnum(type.getRawType().asSubclass(Enum.class),
                enumConstant);
        if (!ret.isPresent()) {
            throw new ObjectMappingException("Invalid enum constant provided: " +
                    "Expected a value of enum " + type + ", got " + enumConstant);
        }
        return ret.get();
    }

    @Override
    public Object serialize(Enum<?> item, Predicate<Class<?>> typeSupported) {
        return item.name();
    }
}
