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

import static io.leangen.geantyref.GenericTypeReflector.isMissingTypeParameters;
import static java.util.Objects.requireNonNull;

import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.AnnotatedType;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Factory for a basic {@link ObjectMapper}.
 */
public class DefaultObjectMapperFactory implements ObjectMapperFactory {

    private static final int MAXIMUM_MAPPERS_SIZE = 64;
    private static final ObjectMapperFactory INSTANCE = new DefaultObjectMapperFactory();

    @NonNull
    public static ObjectMapperFactory getInstance() {
        return INSTANCE;
    }

    private final Map<AnnotatedType, ObjectMapper<?>> mappers = new LinkedHashMap<AnnotatedType, ObjectMapper<?>>() {
        @Override
        protected boolean removeEldestEntry(final Map.Entry<AnnotatedType, ObjectMapper<?>> eldest) {
            return this.size() > MAXIMUM_MAPPERS_SIZE;
        }
    };

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T> ObjectMapper<T> getMapper(final @NonNull TypeToken<T> type) throws ObjectMappingException {
        requireNonNull(type, "type");
        final AnnotatedType canonical = type.getCanonicalType();
        if (isMissingTypeParameters(canonical.getType())) {
            throw new ObjectMappingException("Raw types are not supported!");
        }

        // TODO: abstract this
        try {
            synchronized (this.mappers) {
                return (ObjectMapper<T>) this.mappers.computeIfAbsent(canonical, key -> {
                    try {
                        return new ObjectMapper<>(key);
                    } catch (final ObjectMappingException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (final RuntimeException e) {
            if (e.getCause() instanceof ObjectMappingException) {
                throw (ObjectMappingException) e.getCause();
            } else {
                throw e;
            }
        }
    }

    @Override
    public String toString() {
        return "DefaultObjectMapperFactory{}";
    }

}
