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
package org.spongepowered.configurate.jackson;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

final class JacksonCompat {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final MethodHandle PARSER_TOKEN_LOCATION = JacksonCompat.tryNames(
            LOOKUP,
            JsonParser.class,
            MethodType.methodType(JsonLocation.class),
            "currentTokenLocation", "getTokenLocation"
    );
    private static final MethodHandle PARSER_CURRENT_LOCATION = JacksonCompat.tryNames(
            LOOKUP,
            JsonParser.class,
            MethodType.methodType(JsonLocation.class),
            "currentLocation", "getCurrentLocation"
    );
    private static final MethodHandle PARSER_CURRENT_NAME = JacksonCompat.tryNames(
            LOOKUP,
            JsonParser.class,
            MethodType.methodType(String.class),
            "currentName", "getCurrentName"
    );

    private JacksonCompat() {
    }

    /**
     * try various names for a method.
     *
     * <p>Throws an exception if no such method was found.</p>
     *
     * @param lookup the lookup to resolve
     * @param type the method type
     * @param names names to attempt
     * @return a method handle
     */
    private static MethodHandle tryNames(final MethodHandles.Lookup lookup, final Class<?> owner, final MethodType type, final String... names) {
        for (int i = 0; i < names.length; i++) {
            try {
                return lookup.findVirtual(owner, names[i], type);
            } catch (final NoSuchMethodException | IllegalAccessException ex) {
                if (i == names.length - 1) {
                    throw new RuntimeException(ex);
                }

                // fall-through to continue
            }
        }
        throw new RuntimeException("No names provided");
    }

    static JsonLocation currentTokenLocation(final JsonParser parser) {
        try {
            return (JsonLocation) PARSER_TOKEN_LOCATION.invokeExact(parser);
        } catch (final Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    static JsonLocation currentLocation(final JsonParser parser) {
        try {
            return (JsonLocation) PARSER_CURRENT_LOCATION.invokeExact(parser);
        } catch (final Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    static String currentName(final JsonParser parser) {
        try {
            return (String) PARSER_CURRENT_NAME.invokeExact(parser);
        } catch (final Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

}
