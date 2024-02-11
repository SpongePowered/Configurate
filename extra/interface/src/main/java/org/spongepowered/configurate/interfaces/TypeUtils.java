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

import static io.leangen.geantyref.GenericTypeReflector.box;

import java.lang.reflect.Type;

final class TypeUtils {

    private TypeUtils() {}

    static boolean isNumeric(final Type type) {
        final Type boxed = box(type);
        return Byte.class.equals(boxed) || Character.class.equals(boxed) || Short.class.equals(boxed)
            || Integer.class.equals(boxed) || Long.class.equals(boxed);
    }

    static boolean isDecimal(final Type type) {
        final Type boxed = box(type);
        return Float.class.equals(boxed) || Double.class.equals(boxed);
    }

    static boolean isBoolean(final Type type) {
        return Boolean.class.equals(box(type));
    }

}
