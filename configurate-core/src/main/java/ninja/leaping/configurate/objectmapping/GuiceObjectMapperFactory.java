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
package ninja.leaping.configurate.objectmapping;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Injector;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A factory for {@link ObjectMapper}s that will inherit the injector from wherever it is provided.
 *
 * <p>This class is intended to be constructed through Guice dependency injection.</p>
 */
@Singleton
public final class GuiceObjectMapperFactory implements ObjectMapperFactory {
    private final LoadingCache<Class<?>, ObjectMapper<?>> cache = CacheBuilder.newBuilder()
            .weakKeys().maximumSize(512)
            .build(new CacheLoader<Class<?>, ObjectMapper<?>>() {
                @Override
                public ObjectMapper<?> load(Class<?> key) throws Exception {
                    return new GuiceObjectMapper<>(injector, key);
                }
            });

    private final Injector injector;

    @Inject
    protected GuiceObjectMapperFactory(Injector baseInjector) {
        this.injector = baseInjector;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T> ObjectMapper<T> getMapper(@NonNull Class<T> type) throws ObjectMappingException {
        Preconditions.checkNotNull(type, "type");
        try {
            return (ObjectMapper<T>) cache.get(type);
        } catch (ExecutionException e) {
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
