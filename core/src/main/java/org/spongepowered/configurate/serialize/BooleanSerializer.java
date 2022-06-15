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

import java.lang.reflect.Type;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * Attempts to convert a given value to a {@link Boolean}.
 *
 * <ul>
 *     <li>If <code>value</code> is a {@link Number}, returns true if value is
 *     not 0</li>
 *     <li>If {@code value.toString()} is true, t, yes, y, or 1,
 *     returns true</li>
 *     <li>If {@code value.toString()} is false, f, no, n, or 0,
 *     returns false</li>
 *     <li>Otherwise throws a {@link CoercionFailedException}</li>
 * </ul>
 */
final class BooleanSerializer extends ScalarSerializer<Boolean> {

    BooleanSerializer() {
        super(Boolean.class);
    }

    @Override
    public Boolean deserialize(final Type type, final Object value) throws SerializationException {
        if (value instanceof Number) {
            return !value.equals(0);
        }

        final String potential = value.toString().toLowerCase(Locale.ROOT);
        if (potential.equals("true")
                || potential.equals("t")
                || potential.equals("yes")
                || potential.equals("y")
                || potential.equals("1")) {
            return true;
        } else if (potential.equals("false")
                || potential.equals("f")
                || potential.equals("no")
                || potential.equals("n")
                || potential.equals("0")) {
            return false;
        }

        throw new CoercionFailedException(type, value, "boolean");
    }

    @Override
    public Object serialize(final Boolean item, final Predicate<Class<?>> typeSupported) {
        if (typeSupported.test(Integer.class)) {
            return item ? 1 : 0;
        } else {
            return item.toString();
        }
    }

}
