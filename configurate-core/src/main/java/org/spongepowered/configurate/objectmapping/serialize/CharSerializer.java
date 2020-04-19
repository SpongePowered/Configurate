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
import org.spongepowered.configurate.objectmapping.ObjectMappingException;

import java.util.function.Predicate;

final class CharSerializer extends ScalarSerializer<Character> {
    CharSerializer() {
        super(Character.class);
    }

    @Override
    public Character deserialize(TypeToken<?> type, Object val) throws ObjectMappingException {
        if (val instanceof String) {
            String strVal = ((String) val);
            if (strVal.length() == 1) {
                return strVal.charAt(0);
            }
            throw new ObjectMappingException("Only single character expected, but received " + strVal);
        } else if (val instanceof Number) {
            return (char) ((Number) val).shortValue();
        }
        throw new CoercionFailedException(val, "char");
    }

    @Override
    public Object serialize(@NonNull Character item, Predicate<Class<?>> typeSupported) {
        if (typeSupported.test(char.class)) {
            return item;
        } else {
            return item.toString();
        }
    }
}
