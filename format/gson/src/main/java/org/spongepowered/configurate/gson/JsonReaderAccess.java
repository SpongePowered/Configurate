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
package org.spongepowered.configurate.gson;

import com.google.gson.stream.JsonReader;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

final class JsonReaderAccess {

    static final int VALUE_UNKNOWN = -1;
    private static final @Nullable MethodHandle JSON_READER_POS;
    private static final @Nullable MethodHandle JSON_READER_LINE_NUMBER;
    private static final @Nullable MethodHandle JSON_READER_LINE_START;

    static {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        @Nullable MethodHandle pos = null;
        @Nullable MethodHandle lineNumber = null;
        @Nullable MethodHandle lineStart = null;
        try {
            pos = getter(lookup, JsonReader.class, "pos", int.class);
            lineNumber = getter(lookup, JsonReader.class, "lineNumber", int.class);
            lineStart = getter(lookup, JsonReader.class, "lineStart", int.class);
        } catch (NoSuchFieldException | IllegalAccessException ignore) {
            // ignore, we won't have this info available
            // wouldn't it be nice if gson added actual api?
        }

        JSON_READER_POS = pos;
        JSON_READER_LINE_NUMBER = lineNumber;
        JSON_READER_LINE_START = lineStart;
    }

    private static @Nullable MethodHandle getter(final MethodHandles.Lookup lookup, final Class<?> clazz, final String name, final Class<?> type)
            throws IllegalAccessException, NoSuchFieldException {
        final Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        if (!field.getType().equals(type)) {
            return null;
        }
        return lookup.unreflectGetter(field);
    }

    private JsonReaderAccess() {
    }

    static int lineNumber(final JsonReader reader) {
        if (JSON_READER_LINE_NUMBER == null) {
            return VALUE_UNKNOWN;
        }

        try {
            return (int) JSON_READER_LINE_NUMBER.invoke(reader) + 1;
        } catch (final Error err) {
            throw err;
        } catch (final Throwable throwable) {
            return VALUE_UNKNOWN;
        }
    }

    static int column(final JsonReader reader) {
        if (JSON_READER_POS == null || JSON_READER_LINE_START == null) {
            return VALUE_UNKNOWN;
        }

        try {
            final int pos = (int) JSON_READER_POS.invoke(reader);
            final int lineStart = (int) JSON_READER_LINE_START.invoke(reader);
            return pos - lineStart + 1;
        } catch (final Error err) {
            throw err;
        } catch (final Throwable thr) {
            return VALUE_UNKNOWN;
        }
    }

}
