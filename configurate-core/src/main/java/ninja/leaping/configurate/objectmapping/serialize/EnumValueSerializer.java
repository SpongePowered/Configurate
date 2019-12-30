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
package ninja.leaping.configurate.objectmapping.serialize;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.util.EnumLookup;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

class EnumValueSerializer implements TypeSerializer<Enum<?>> {
    @Override
    public Enum<?> deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        String enumConstant = value.getString();
        if (enumConstant == null) {
            throw new ObjectMappingException("No value present in node " + value);
        }

        @SuppressWarnings("unchecked")
        Optional<? extends Enum<?>> ret = EnumLookup.lookupEnum(type.getRawType().asSubclass(Enum.class),
                enumConstant);
        if (!ret.isPresent()) {
            throw new ObjectMappingException("Invalid enum constant provided for " + value.getKey() + ": " +
                    "Expected a value of enum " + type + ", got " + enumConstant);
        }
        return ret.get();
    }

    @Override
    public void serialize(@NonNull TypeToken<?> type, @Nullable Enum<?> obj, @NonNull ConfigurationNode value) throws ObjectMappingException {
        value.setValue(obj == null ? null : obj.name());
    }
}
