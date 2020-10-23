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

import net.kyori.coffee.function.Predicate1;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.Type;

final class CharSerializer extends ScalarSerializer<Character> {

    CharSerializer() {
        super(Character.class);
    }

    @Override
    public Character deserialize(final Type type, final Object val) throws SerializationException {
        if (val instanceof String) {
            final String strVal = (String) val;
            if (strVal.length() == 1) {
                return strVal.charAt(0);
            }
            throw new SerializationException(type, "Only single character expected, but received " + strVal);
        } else if (val instanceof Number) {
            return (char) ((Number) val).shortValue();
        }
        throw new CoercionFailedException(type, val, "char");
    }

    @Override
    public Object serialize(final @NonNull Character item, final Predicate1<Class<?>> typeSupported) {
        if (typeSupported.test(char.class)) {
            return item;
        } else {
            return item.toString();
        }
    }

}
