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
package org.spongepowered.configurate.objectmapping.serialize;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * A calculated collection of {@link TypeSerializer}s
 */
public class TypeSerializerCollection {
    private static final TypeSerializerCollection DEFAULTS;

    static {
        DEFAULTS = TypeSerializerCollection.builder()
                .register(TypeToken.of(String.class), new StringSerializer())
                .register(TypeToken.of(Boolean.class), new BooleanSerializer())
                .register(new TypeToken<Map<?, ?>>() {}, new MapSerializer())
                .register(new TypeToken<List<?>>() {}, new ListSerializer())
                .register(NumberSerializer.getPredicate(), new NumberSerializer())
                .register(input -> input.getRawType().isAnnotationPresent(ConfigSerializable.class), new AnnotatedObjectSerializer())
                .register(new TypeToken<Enum<?>>() {}, new EnumValueSerializer())
                .register(CharSerializer.predicate(), new CharSerializer())
                .register(TypeToken.of(URI.class), new URISerializer())
                .register(TypeToken.of(URL.class), new URLSerializer())
                .register(TypeToken.of(UUID.class), new UUIDSerializer())
                .register(TypeToken.of(Pattern.class), new PatternSerializer())
                .register(ArraySerializer.Objects.predicate(), new ArraySerializer.Objects())
                .register(TypeToken.of(boolean[].class), new ArraySerializer.Booleans())
                .register(TypeToken.of(byte[].class), new ArraySerializer.Bytes())
                .register(TypeToken.of(char[].class), new ArraySerializer.Chars())
                .register(TypeToken.of(short[].class), new ArraySerializer.Shorts())
                .register(TypeToken.of(int[].class), new ArraySerializer.Ints())
                .register(TypeToken.of(long[].class), new ArraySerializer.Longs())
                .register(TypeToken.of(float[].class), new ArraySerializer.Floats())
                .register(TypeToken.of(double[].class), new ArraySerializer.Doubles())
                .register(new TypeToken<Set<?>>() {}, new SetSerializer())
                .build();
    }

    private final TypeSerializerCollection parent;
    private final List<RegisteredSerializer> serializers;
    private final Map<TypeToken<?>, TypeSerializer<?>> typeMatches = new ConcurrentHashMap<>();

    private TypeSerializerCollection(TypeSerializerCollection parent, List<RegisteredSerializer> serializers) {
        this.parent = parent;
        this.serializers = ImmutableList.copyOf(serializers);
    }

    /**
     * Resolve a type serializer. First, all registered serializers from this collection are queried,
     * then if a parent collection is set that collection is queried.
     *
     * @param type The type a serializer is required for
     * @param <T> The type to serialize
     * @return A serializer if any is present, or null if no applicable serializer is found
     */
    @SuppressWarnings("unchecked")
    public <T> TypeSerializer<T> get(TypeToken<T> type) {
        Preconditions.checkNotNull(type, "type");
        type = type.wrap();

        TypeSerializer<?> serial = typeMatches.computeIfAbsent(type, token -> {
            for (RegisteredSerializer ent : serializers) {
                if (ent.predicate.test(token)) {
                    return ent.serializer;
                }
            }
            return null;
        });

        if (serial == null && parent != null) {
            serial = parent.get(type);
        }

        return (TypeSerializer) serial;
    }

    /**
     * Create a new builder to begin building a collection of type serializers that inherits from this collection
     *
     * @return The new builder
     */
    public Builder childBuilder() {
        return new Builder(this);
    }

    /**
     * Create a builder for a new type serializer collection without a parent set.
     *
     * If <em>any</em> of the standard serializers provided by Configurate are desired,
     * either the default collection or a collection inheriting from the default collection should be applied.
     *
     * @return The builder
     */
    public static Builder builder() {
        return new Builder(null);
    }

    /**
     * Get a collection containing all of Configurate's built-in type serializers.
     *
     * @return The collection
     */
    public static TypeSerializerCollection defaults() {
        return DEFAULTS;
    }

    public static class Builder {
        private final TypeSerializerCollection parent;
        private final ImmutableList.Builder<RegisteredSerializer> serializers = ImmutableList.builder();

        Builder(TypeSerializerCollection parent) {
            this.parent = parent;
        }

        /**
         * Register a type serializer for a given type. Serializers registered will match all subclasses of the provided
         * type, as well as unwrapped primitive equivalents of the type.
         *
         * @param type The type to accept
         * @param serializer The serializer that will be serialized with
         * @param <T> The type to generify around
         * @return this
         */
        public <T> Builder register(TypeToken<T> type, TypeSerializer<? super T> serializer) {
            Preconditions.checkNotNull(type, "type");
            Preconditions.checkNotNull(serializer, "serializer");
            serializers.add(new RegisteredSerializer(new SuperTypePredicate(type), serializer));
            return this;
        }

        /**
         * Register a type serializer matching against a given predicate.
         *
         * @param test The predicate to match types against
         * @param serializer The serializer to serialize matching types with
         * @param <T> The type parameter
         * @return this
         */
        @SuppressWarnings("unchecked")
        public <T> Builder register(Predicate<TypeToken<T>> test, TypeSerializer<? super T> serializer) {
            requireNonNull(test, "test");
            requireNonNull(serializer, "serializer");
            serializers.add(new RegisteredSerializer((Predicate) test, serializer));
            return this;
        }

        /**
         * Create a new type serializer collection
         *
         * @return The resulting collection
         */
        public TypeSerializerCollection build() {
            return new TypeSerializerCollection(this.parent, this.serializers.build());
        }
    }

    private static final class RegisteredSerializer {
        private final Predicate<TypeToken<?>> predicate;
        private final TypeSerializer<?> serializer;

        private RegisteredSerializer(Predicate<TypeToken<?>> predicate, TypeSerializer<?> serializer) {
            this.predicate = predicate;
            this.serializer = serializer;
        }
    }
}
