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
package org.spongepowered.configurate.serialize;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * A calculated collection of {@link TypeSerializer}s.
 */
public final class TypeSerializerCollection {

    private static final TypeSerializerCollection DEFAULTS;

    static {
        DEFAULTS = TypeSerializerCollection.builder()
                .register(Scalars.STRING)
                .register(Scalars.BOOLEAN)
                .register(MapSerializer.TYPE, new MapSerializer())
                .register(ListSerializer.TYPE, new ListSerializer())
                .register(Scalars.BYTE)
                .register(Scalars.SHORT)
                .register(Scalars.INTEGER)
                .register(Scalars.LONG)
                .register(Scalars.FLOAT)
                .register(Scalars.DOUBLE)
                .register(AnnotatedObjectSerializer.predicate(), new AnnotatedObjectSerializer())
                .register(Scalars.ENUM)
                .register(Scalars.CHAR)
                .register(Scalars.URI)
                .register(Scalars.URL)
                .register(Scalars.UUID)
                .register(Scalars.PATTERN)
                .register(ArraySerializer.Objects.predicate(), new ArraySerializer.Objects())
                .register(ArraySerializer.Booleans.TYPE, new ArraySerializer.Booleans())
                .register(ArraySerializer.Bytes.TYPE, new ArraySerializer.Bytes())
                .register(ArraySerializer.Chars.TYPE, new ArraySerializer.Chars())
                .register(ArraySerializer.Shorts.TYPE, new ArraySerializer.Shorts())
                .register(ArraySerializer.Ints.TYPE, new ArraySerializer.Ints())
                .register(ArraySerializer.Longs.TYPE, new ArraySerializer.Longs())
                .register(ArraySerializer.Floats.TYPE, new ArraySerializer.Floats())
                .register(ArraySerializer.Doubles.TYPE, new ArraySerializer.Doubles())
                .register(SetSerializer.TYPE, new SetSerializer())
                .register(ConfigurationNodeSerializer.TYPE, new ConfigurationNodeSerializer())
                .build();
    }

    private final @Nullable TypeSerializerCollection parent;
    private final List<RegisteredSerializer> serializers;
    private final Map<TypeToken<?>, TypeSerializer<?>> typeMatches = new ConcurrentHashMap<>();

    private TypeSerializerCollection(final @Nullable TypeSerializerCollection parent, final List<RegisteredSerializer> serializers) {
        this.parent = parent;
        this.serializers = ImmutableList.copyOf(serializers);
    }

    /**
     * Resolve a type serializer.
     *
     * <p>First, all registered serializers from this collection are queried
     * then if a parent collection is set, that collection is queried.
     *
     * @param type The type a serializer is required for
     * @param <T> The type to serialize
     * @return A serializer if any is present, or null if no applicable
     *          serializer is found
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> @Nullable TypeSerializer<T> get(TypeToken<T> type) {
        requireNonNull(type, "type");
        type = type.wrap();

        @Nullable TypeSerializer<?> serial = this.typeMatches.computeIfAbsent(type, token -> {
            for (RegisteredSerializer ent : this.serializers) {
                if (ent.predicate.test(token)) {
                    return ent.serializer;
                }
            }
            return null;
        });

        if (serial == null && this.parent != null) {
            serial = this.parent.get(type);
        }

        return (TypeSerializer) serial;
    }

    /**
     * Create a new builder to begin building a collection of type serializers
     * that inherits from this collection.
     *
     * @return The new builder
     */
    public Builder childBuilder() {
        return new Builder(this);
    }

    /**
     * Create a builder for a new type serializer collection without a
     * parent set.
     *
     * <p>If <em>any</em> of the standard serializers provided by Configurate
     * are desired, either the default collection or a collection inheriting
     * from the default collection should be applied.
     *
     * @return The builder
     */
    public static Builder builder() {
        return new Builder(null);
    }

    /**
     * Get a collection containing all of Configurate's built-in
     * type serializers.
     *
     * @return The collection
     */
    public static TypeSerializerCollection defaults() {
        return DEFAULTS;
    }

    public static class Builder {
        private final @Nullable TypeSerializerCollection parent;
        private final ImmutableList.Builder<RegisteredSerializer> serializers = ImmutableList.builder();

        Builder(final @Nullable TypeSerializerCollection parent) {
            this.parent = parent;
        }

        /**
         * Register a type serializer for a given type.
         *
         * <p>Serializers registered will match all subclasses of the provided
         * type, as well as unwrapped primitive equivalents of the type.
         *
         * @param type The type to accept
         * @param serializer The serializer that will be serialized with
         * @param <T> The type to generify around
         * @return this
         */
        public <T> Builder register(final TypeToken<T> type, final TypeSerializer<? super T> serializer) {
            requireNonNull(type, "type");
            requireNonNull(serializer, "serializer");
            this.serializers.add(new RegisteredSerializer(new SuperTypePredicate(type), serializer));
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
        @SuppressWarnings({"unchecked", "rawtypes"})
        public <T> Builder register(final Predicate<TypeToken<T>> test, final TypeSerializer<? super T> serializer) {
            requireNonNull(test, "test");
            requireNonNull(serializer, "serializer");
            this.serializers.add(new RegisteredSerializer((Predicate) test, serializer));
            return this;
        }

        public <T> Builder register(final ScalarSerializer<T> serializer) {
            requireNonNull(serializer, "serializer");
            return register(serializer.type(), serializer);
        }

        /**
         * Create a new type serializer collection.
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

        private RegisteredSerializer(final Predicate<TypeToken<?>> predicate, final TypeSerializer<?> serializer) {
            this.predicate = predicate;
            this.serializer = serializer;
        }

    }

}
