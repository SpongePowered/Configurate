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
package org.spongepowered.configurate.util;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Utility class to cache more flexible enum lookup.
 *
 * <p>While normally case and punctuation have to match exactly, this method
 * performs lookup that:</p>
 *
 * <ul>
 *     <li>is case-insensitive</li>
 *     <li>ignores underscores</li>
 *     <li>caches mappings</li>
 * </ul>
 *
 * <p>If the enum has two fields that are equal except for case and underscores,
 * an exact match will return the appropriate value, and any fuzzy matches will
 * map to the first value in the enum that is applicable.</p>
 */
public final class EnumLookup {

    private EnumLookup() {

    }

    private static final Map<Class<? extends Enum<?>>, Map<String, Enum<?>>> ENUM_FIELD_CACHE = new WeakHashMap<>();

    private static String processKey(final String key) {
        // stick a flower at the front so processed keys are different from literal keys
        return "🌸" + key.toLowerCase().replace("_", "");
    }

    /**
     * Perform a fuzzy lookup of {@code key} in enum {@code clazz}.
     *
     * @param clazz class to find key in
     * @param key key to find
     * @param <T> type of value
     * @return optionally the enum
     */
    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> @Nullable T lookupEnum(final Class<T> clazz, final String key) {
        final Map<String, Enum<?>> vals = ENUM_FIELD_CACHE.computeIfAbsent(requireNonNull(clazz), c2 -> {
            final Map<String, Enum<?>> ret = new HashMap<>();
            for (Enum<?> field : c2.getEnumConstants()) {
                ret.put(field.name(), field);
                ret.putIfAbsent(processKey(field.name()), field);
            }
            return ImmutableMap.copyOf(ret);
        });


        final Enum<?> possibleRet = vals.get(requireNonNull(key, "key"));
        if (possibleRet != null) {
            return (T) possibleRet;
        }
        return (T) vals.get(processKey(key));
    }

}
