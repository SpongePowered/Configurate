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
package org.spongepowered.configurate.objectmapping.guice;

import static java.util.Objects.requireNonNull;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.reflect.TypeToken;
import com.google.inject.Injector;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.ObjectMapperFactory;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;

import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A factory for {@link ObjectMapper}s that will inherit the injector from
 * wherever it is provided.
 *
 * <p>This class is intended to be constructed through Guice
 * dependency injection.
 */
@Singleton
public final class GuiceObjectMapperFactory implements ObjectMapperFactory {

    private final LoadingCache<TypeToken<?>, ObjectMapper<?>> cache = CacheBuilder.newBuilder()
            .weakKeys().maximumSize(512)
            .build(new CacheLoader<TypeToken<?>, ObjectMapper<?>>() {
                @Override
                public ObjectMapper<?> load(final TypeToken<?> key) throws Exception {
                    return new GuiceObjectMapper<>(GuiceObjectMapperFactory.this.injector, key);
                }
            });

    private final Injector injector;

    @Inject
    protected GuiceObjectMapperFactory(final Injector baseInjector) {
        this.injector = baseInjector;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T> ObjectMapper<T> getMapper(final @NonNull TypeToken<T> type) throws ObjectMappingException {
        requireNonNull(type, "type");
        try {
            return (ObjectMapper<T>) this.cache.get(type);
        } catch (final ExecutionException e) {
            if (e.getCause() instanceof ObjectMappingException) {
                throw (ObjectMappingException) e.getCause();
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String toString() {
        return "GuiceObjectMapperFactory{}";
    }

}
