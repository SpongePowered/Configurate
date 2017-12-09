/*
 * This file is part of Configurate, licensed under the Apache-2.0 License.
 *
 * Copyright (C) zml
 * Copyright (C) IchorPowered
 * Copyright (C) Contributors
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

package ninja.leaping.configurate.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility class to cache more flexible enum lookup. While normally case and punctuation have to match exactly, this
 * method performs lookup that:
 *<ul>
 *     <li>is case-insensitive</li>
 *     <li>ignores underscores</li>
 *     <li>Caches mappings</li>
 *</ul>
 *
 * If the enum has two fields that are equal except for case and underscores, an exact match will return the
 * appropriate value, and any fuzzy matches will map to the first value in the enum that is applicable.
 */
public class EnumLookup {
    private static final LoadingCache<Class<? extends Enum<?>>, Map<String, Enum<?>>> enumFieldCache = CacheBuilder
            .newBuilder()
            .weakKeys()
            .maximumSize(512)
            .build(new CacheLoader<Class<? extends Enum<?>>, Map<String, Enum<?>>>() {
                @Override
                public Map<String, Enum<?>> load(Class<? extends Enum<?>> key) throws Exception {
                    Map<String, Enum<?>> ret = new HashMap<>();
                    for (Enum<?> field : key.getEnumConstants()) {
                        ret.put(field.name(), field);
                        ret.putIfAbsent(processKey(field.name()), field);
                    }
                    return ImmutableMap.copyOf(ret);
                }
            });

    private EnumLookup() {
        // wheeeeeeee
    }

    private static String processKey(String key) {
        checkNotNull(key, "key");
        return "🌸" + key.toLowerCase().replace("_", ""); // stick a flower at the front so processed keys are
        // different from literal keys
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> Optional<T> lookupEnum(Class<T> clazz, String key) {
        checkNotNull(clazz, "clazz");
        try {
            Map<String, Enum<?>> vals = enumFieldCache.get(clazz);
            Enum<?> possibleRet = vals.get(key);
            if (possibleRet != null) {
                return Optional.of((T) possibleRet);
            }
            return Optional.ofNullable((T) vals.get(processKey(key)));
        } catch (ExecutionException e) {
            return Optional.empty();
        }

    }
}
