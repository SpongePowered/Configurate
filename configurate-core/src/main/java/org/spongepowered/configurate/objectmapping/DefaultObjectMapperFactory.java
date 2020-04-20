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
package org.spongepowered.configurate.objectmapping;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.reflect.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.ExecutionException;

import static java.util.Objects.requireNonNull;

/**
 * Factory for a basic {@link ObjectMapper}.
 */
public class DefaultObjectMapperFactory implements ObjectMapperFactory {
    private static final ObjectMapperFactory INSTANCE = new DefaultObjectMapperFactory();

    public static ObjectMapperFactory getInstance() {
        return INSTANCE;
    }

    private final LoadingCache<TypeToken<?>, ObjectMapper<?>> mapperCache = CacheBuilder.newBuilder()
            .weakKeys()
            .maximumSize(500)
            .build(new CacheLoader<TypeToken<?>, ObjectMapper<?>>() {
                @Override
                public ObjectMapper<?> load(TypeToken<?> key) throws Exception {
                    return new ObjectMapper<>(key);
                }
            });

    @Override
    @SuppressWarnings("unchecked")
    public <T> ObjectMapper<T> getMapper(TypeToken<T> type) throws ObjectMappingException {
        requireNonNull(type, "type");
        try {
            return (ObjectMapper<T>) mapperCache.get(type);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof ObjectMappingException) {
                throw (ObjectMappingException) e.getCause();
            } else {
                throw new ObjectMappingException(e);
            }
        }
    }

    @Override
    public String toString() {
        return "DefaultObjectMapperFactory{}";
    }
}
