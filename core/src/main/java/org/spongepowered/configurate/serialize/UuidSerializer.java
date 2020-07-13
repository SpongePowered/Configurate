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

import org.spongepowered.configurate.objectmapping.ObjectMappingException;

import java.lang.reflect.Type;
import java.util.UUID;
import java.util.function.Predicate;

final class UuidSerializer extends ScalarSerializer<UUID> {

    private static final char DASH = '-';

    UuidSerializer() {
        super(UUID.class);
    }

    @Override
    public UUID deserialize(final Type type, final Object obj) throws ObjectMappingException {
        if (obj instanceof long[]) {
            final long[] arr = (long[]) obj;
            if (arr.length == 2) { // big-endian, cuz we're java
                return new UUID(arr[0], arr[1]);
            }
        }
        String uuidStr = obj.toString();
        if (uuidStr.length() == 32) { // Mojang-style, without dashes
            uuidStr = new StringBuilder(36)
                    .append(uuidStr, 0, 8).append(DASH)
                    .append(uuidStr, 8, 12).append(DASH)
                    .append(uuidStr, 12, 16).append(DASH)
                    .append(uuidStr, 16, 20).append(DASH)
                    .append(uuidStr, 20, 32)
                    .toString();
        }
        try {
            return UUID.fromString(uuidStr);
        } catch (final IllegalArgumentException ex) {
            throw new CoercionFailedException(obj, "UUID");
        }
    }

    @Override
    public Object serialize(final UUID item, final Predicate<Class<?>> typeSupported) {
        return item.toString();
    }

}
