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

import static io.leangen.geantyref.GenericTypeReflector.isSuperType;
import static java.util.Objects.requireNonNull;
import static org.spongepowered.configurate.util.Types.requireCompleteParameters;

import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.util.UnmodifiableCollections;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * A calculated collection of {@link TypeSerializer}s.
 *
 * @since 4.0.0
 */
public final class TypeSerializerCollection {

    private static final TypeSerializerCollection DEFAULTS;

    static {
        DEFAULTS = TypeSerializerCollection.builder()
                .registerExact(Scalars.STRING)
                .registerExact(Scalars.BOOLEAN)
                .register(MapSerializer.TYPE, new MapSerializer())
                .register(ListSerializer.TYPE, new ListSerializer())
                .registerExact(Scalars.BYTE)
                .registerExact(Scalars.SHORT)
                .registerExact(Scalars.INTEGER)
                .registerExact(Scalars.LONG)
                .registerExact(Scalars.FLOAT)
                .registerExact(Scalars.DOUBLE)
                .registerAnnotatedObjects(ObjectMapper.factory())
                .register(Scalars.ENUM)
                .registerExact(Scalars.CHAR)
                .registerExact(Scalars.URI)
                .registerExact(Scalars.URL)
                .registerExact(Scalars.UUID)
                .registerExact(Scalars.PATTERN)
                .register(ArraySerializer.Objects::accepts, new ArraySerializer.Objects())
                .registerExact(ArraySerializer.Booleans.TYPE, new ArraySerializer.Booleans())
                .registerExact(ArraySerializer.Bytes.TYPE, new ArraySerializer.Bytes())
                .registerExact(ArraySerializer.Chars.TYPE, new ArraySerializer.Chars())
                .registerExact(ArraySerializer.Shorts.TYPE, new ArraySerializer.Shorts())
                .registerExact(ArraySerializer.Ints.TYPE, new ArraySerializer.Ints())
                .registerExact(ArraySerializer.Longs.TYPE, new ArraySerializer.Longs())
                .registerExact(ArraySerializer.Floats.TYPE, new ArraySerializer.Floats())
                .registerExact(ArraySerializer.Doubles.TYPE, new ArraySerializer.Doubles())
                .register(SetSerializer::accepts, new SetSerializer())
                .register(ConfigurationNodeSerializer.TYPE, new ConfigurationNodeSerializer())
                .register(PathSerializer.TYPE, PathSerializer.INSTANCE)
                .registerExact(FileSerializer.TYPE, FileSerializer.INSTANCE)
                .register(OptionalSerializer.TYPE, OptionalSerializer.INSTANCE)
                .registerExact(OptionalSerializer.OfInt.TYPE, OptionalSerializer.OfInt.INSTANCE)
                .registerExact(OptionalSerializer.OfLong.TYPE, OptionalSerializer.OfLong.INSTANCE)
                .registerExact(OptionalSerializer.OfDouble.TYPE, OptionalSerializer.OfDouble.INSTANCE)
                .build();
    }

    private final @Nullable TypeSerializerCollection parent;
    final List<RegisteredSerializer> serializers;
    private final Map<Type, TypeSerializer<?>> typeMatches = new ConcurrentHashMap<>();
    private final Map<AnnotatedType, TypeSerializer<?>> annotatedTypeMatches = new ConcurrentHashMap<>();

    private TypeSerializerCollection(final @Nullable TypeSerializerCollection parent, final List<RegisteredSerializer> serializers) {
        this.parent = parent;
        this.serializers = UnmodifiableCollections.copyOf(serializers);
    }

    /**
     * Resolve a type serializer.
     *
     * <p>First, all registered serializers from this collection are queried in
     * registration order, then if a parent collection is set, that collection
     * is queried.</p>
     *
     * @param token the type a serializer is required for
     * @param <T> the type to serialize
     * @return a serializer if any is present, or null if no applicable
     *          serializer is found
     * @since 4.0.0
     */
    @SuppressWarnings("unchecked")
    public <T> @Nullable TypeSerializer<T> get(final TypeToken<T> token) {
        requireNonNull(token, "type");
        return (TypeSerializer<T>) this.get0(token.getAnnotatedType());
    }

    /**
     * Resolve a type serializer.
     *
     * <p>First, all registered serializers from this collection are queried in
     * registration order, then if a parent collection is set, that collection
     * is queried.</p>
     *
     * <p>This method will fail when provided a raw parameterized type</p>
     *
     * @param token the type a serializer is required for
     * @param <T> the type to serialize
     * @return a serializer if any is present, or null if no applicable
     *          serializer is found
     * @since 4.0.0
     */
    @SuppressWarnings("unchecked")
    public <T> @Nullable TypeSerializer<T> get(final Class<T> token) {
        requireNonNull(token, "type");
        requireCompleteParameters(token);

        return (TypeSerializer<T>) this.get((Type) token);
    }

    /**
     * Resolve a type serializer.
     *
     * <p>First, all registered serializers from this collection are queried
     * then if a parent collection is set, that collection is queried.
     *
     * @param type the type a serializer is required for
     * @return a serializer if any is present, or null if no applicable
     *          serializer is found
     * @since 4.0.0
     */
    public @Nullable TypeSerializer<?> get(final Type type) {
        return this.get0(GenericTypeReflector.box(type));
    }

    /**
     * Resolve a type serializer with annotation information.
     *
     * <p>First, all registered serializers from this collection are queried
     * then if a parent collection is set, that collection is queried.</p>
     *
     * <p>The returned serializer may not necessarily use the provided
     * annotation information.</p>
     *
     * @param type the type a serializer is required for
     * @return a serializer if any is present, or null if no applicable
     *          serializer is found
     * @since 4.2.0
     */
    public @Nullable TypeSerializer<?> get(final AnnotatedType type) {
        return this.get0(GenericTypeReflector.toCanonicalBoxed(type));
    }

    private @Nullable TypeSerializer<?> get0(final AnnotatedType canonical) {
        @Nullable TypeSerializer<?> serial = this.annotatedTypeMatches.computeIfAbsent(canonical, param -> {
            for (final RegisteredSerializer ent : this.serializers) {
                if (ent.matches(param)) {
                    return ent.serializer();
                }
            }
            return NoOp.INSTANCE;
        });

        if (serial == NoOp.INSTANCE) {
            serial = null;
        }

        if (serial == null && this.parent != null) {
            serial = this.parent.get0(canonical);
        }
        return serial;
    }

    private @Nullable TypeSerializer<?> get0(final Type canonical) {
        @Nullable TypeSerializer<?> serial = this.typeMatches.computeIfAbsent(canonical, param -> {
            for (final RegisteredSerializer ent : this.serializers) {
                if (ent.matches(param)) {
                    return ent.serializer();
                }
            }
            return NoOp.INSTANCE;
        });

        if (serial == NoOp.INSTANCE) {
            serial = null;
        }

        if (serial == null && this.parent != null) {
            serial = this.parent.get0(canonical);
        }
        return serial;
    }

    /**
     * Create a new builder to begin building a collection of type serializers
     * that inherits from this collection.
     *
     * @return the new builder
     * @since 4.0.0
     */
    public Builder childBuilder() {
        return new Builder(this);
    }

    @Override
    public String toString() {
        return "TypeSerializerCollection{"
                + "parent=" + this.parent
                + ", serializers=" + this.serializers
                + '}';
    }

    @Override public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TypeSerializerCollection)) {
            return false;
        }
        final TypeSerializerCollection that = (TypeSerializerCollection) other;
        return Objects.equals(this.parent, that.parent)
                && this.serializers.equals(that.serializers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.parent, this.serializers);
    }

    /**
     * Create a builder for a new type serializer collection without a
     * parent set.
     *
     * <p>If <em>any</em> of the standard serializers provided by Configurate
     * are desired, either the default collection or a collection inheriting
     * from the default collection should be applied.
     *
     * @return the builder
     * @since 4.0.0
     */
    public static Builder builder() {
        return new Builder(null);
    }

    /**
     * Get a collection containing all of Configurate's built-in
     * type serializers.
     *
     * @return the collection
     * @since 4.0.0
     */
    public static TypeSerializerCollection defaults() {
        return DEFAULTS;
    }

    /**
     * A builder to construct new serializer collections.
     *
     * <p>Serializers added to a builder will be prioritized based on
     * registration order, so if multiple serializers could match a single type,
     * the first-registered one will be used.</p>
     *
     * @since 4.0.0
     */
    public static class Builder {
        private final @Nullable TypeSerializerCollection parent;
        private final List<RegisteredSerializer> serializers = new ArrayList<>();

        Builder(final @Nullable TypeSerializerCollection parent) {
            this.parent = parent;
        }

        /**
         * Register a type serializer for a given type.
         *
         * <p>Serializers registered will match all subclasses of the provided
         * type, as well as unwrapped primitive equivalents of the type.
         *
         * @param type the type to accept
         * @param serializer the serializer that will be serialized with
         * @param <T> the type to generify around
         * @return this builder
         * @since 4.0.0
         */
        public <T> Builder register(final TypeToken<T> type, final TypeSerializer<? super T> serializer) {
            return this.register0(type.getType(), serializer);
        }

        /**
         * Register a type serializer for a given type.
         *
         * <p>Serializers registered will match all subclasses of the provided
         * type, as well as unboxed primitive equivalents of the type.
         *
         * @param type the type to accept
         * @param serializer the serializer that will be serialized with
         * @param <T> the type to generify around
         * @return this builder
         * @since 4.0.0
         */
        public <T> Builder register(final Class<T> type, final TypeSerializer<? super T> serializer) {
            return this.register0(type, serializer);
        }

        /**
         * Register a type serializer matching against a given predicate.
         *
         * @param test the predicate to match types against
         * @param serializer the serializer to serialize matching types with
         * @param <T> the type parameter
         * @return this builder
         * @since 4.0.0
         */
        public <T> Builder register(final Predicate<Type> test, final TypeSerializer<? super T> serializer) {
            requireNonNull(test, "test");
            requireNonNull(serializer, "serializer");
            this.serializers.add(new TypeRegistration(test, serializer));
            return this;
        }

        /**
         * Register a scalar serializer with its own attached type token.
         *
         * <p>Serializers registered will match all subclasses of the provided
         * type, as well as unboxed primitive equivalents of the type.</p>
         *
         * @param serializer serializer to register
         * @param <T> value type
         * @return this builder
         * @since 4.0.0
         */
        public <T> Builder register(final ScalarSerializer<T> serializer) {
            requireNonNull(serializer, "serializer");
            return this.register(serializer.type(), serializer);
        }

        /**
         * Register a type serializer matching against a given predicate,
         * with type annotation information.
         *
         * @param test the predicate to match annotated types against
         * @param serializer the serializer to serialize matching types with
         * @param <T> the type parameter
         * @return this builder
         * @see TypeSerializer.Annotated
         * @since 4.2.0
         */
        public <T> Builder registerAnnotated(final Predicate<AnnotatedType> test, final TypeSerializer<? super T> serializer) {
            requireNonNull(test, "test");
            requireNonNull(serializer, "serializer");
            this.serializers.add(new AnnotatedTypeRegistration(test, serializer));
            return this;
        }

        private Builder register0(final Type type, final TypeSerializer<?> serializer) {
            requireNonNull(type, "type");
            requireNonNull(serializer, "serializer");
            this.serializers.add(new TypeRegistration(test -> {
                // Test direct type
                if (GenericTypeReflector.isSuperType(type, test)) {
                    return true;
                }

                // And upper bounds
                if (test instanceof WildcardType) {
                    final Type[] upperBounds = ((WildcardType) test).getUpperBounds();
                    if (upperBounds.length == 1) {
                        return isSuperType(type, upperBounds[0]);
                    }
                }
                return false;
            }, serializer));
            return this;
        }

        /**
         * Register an <em>exact</em> type serializer for a given type.
         *
         * <p>Serializers will only match exact object types. For example, a
         * serializer registered for {@code List<String>} would not match when
         * {@code ArrayList<String>} is queried.</p>
         *
         * @param type the type to accept
         * @param serializer the serializer that will be serialized with
         * @param <T> the type to generify around
         * @return this builder
         * @since 4.0.0
         */
        public <T> Builder registerExact(final TypeToken<T> type, final TypeSerializer<? super T> serializer) {
            return this.registerExact0(type.getType(), serializer);
        }

        /**
         * Register an <em>exact</em> type serializer for a given type.
         *
         * <p>Serializers will only match exact object types. For example, a
         * serializer registered for {@code List<String>} would not match when
         * {@code ArrayList<String>} is queried.</p>
         *
         * @param type the type to accept
         * @param serializer the serializer that will be serialized with
         * @param <T> the type to generify around
         * @return this builder
         * @since 4.0.0
         */
        public <T> Builder registerExact(final Class<T> type, final TypeSerializer<? super T> serializer) {
            return this.registerExact0(type, serializer);
        }

        /**
         * Register a scalar serializer with its own attached type token.
         *
         * <p>Serializers will only match exact object types. For example, a
         * serializer registered for {@code List<String>} would not match when
         * {@code ArrayList<String>} is queried.</p>
         *
         * @param serializer serializer to register
         * @param <T> value type
         * @return this builder
         * @since 4.0.0
         */
        public <T> Builder registerExact(final ScalarSerializer<T> serializer) {
            requireNonNull(serializer, "serializer");
            return this.registerExact(serializer.type(), serializer);
        }

        private Builder registerExact0(final Type type, final TypeSerializer<?> serializer) {
            requireNonNull(type, "type");
            requireNonNull(serializer, "serializer");
            this.serializers.add(new TypeRegistration(test -> test.equals(type), serializer));
            return this;
        }

        /**
         * Register all serializers from {@code other} into this collection.
         *
         * <p>Creating a child collection should be preferred, but when merging
         * multiple sets of serializers together, directly adding other
         * collections may be the best choice.</p>
         *
         * @param other source collection
         * @return this builder
         * @since 4.0.0
         */
        public Builder registerAll(final TypeSerializerCollection other) {
            this.serializers.addAll(requireNonNull(other, "other").serializers);
            return this;
        }

        /**
         * Register a customized object mapper to handle
         * {@link ConfigSerializable}-annotated objects.
         *
         * @param factory factory to retrieve object mappers from
         * @return this builder
         * @since 4.0.0
         */
        public Builder registerAnnotatedObjects(final ObjectMapper.Factory factory) {
            return this.register(Builder::isAnnotatedTarget, factory.asTypeSerializer());
        }

        /**
         * A predicate to restrict the type serializer created by
         * {@link ObjectMapper.Factory#asTypeSerializer()} to annotated types.
         *
         * @return whether a type is annotated with {@link ConfigSerializable}
         * @since 4.0.0
         */
        static boolean isAnnotatedTarget(final Type type) {
            return GenericTypeReflector.annotate(type).isAnnotationPresent(ConfigSerializable.class);
        }

        /**
         * Create a new type serializer collection.
         *
         * @return a newly created collection
         * @since 4.0.0
         */
        public TypeSerializerCollection build() {
            return new TypeSerializerCollection(this.parent, this.serializers);
        }
    }

    /**
     * A serializer registration.
     *
     * @since 4.2.0
     */
    interface RegisteredSerializer {

        boolean matches(Type test);

        boolean matches(AnnotatedType annotated);

        TypeSerializer<?> serializer();

    }

    static final class TypeRegistration implements RegisteredSerializer {

        private final Predicate<Type> predicate;
        private final TypeSerializer<?> serializer;

        TypeRegistration(final Predicate<Type> predicate, final TypeSerializer<?> serializer) {
            this.predicate = predicate;
            this.serializer = serializer;
        }

        @Override
        public boolean matches(final Type test) {
            return this.predicate.test(test);
        }

        @Override
        public boolean matches(final AnnotatedType annotated) {
            return this.predicate.test(annotated.getType());
        }

        @Override
        public TypeSerializer<?> serializer() {
            return this.serializer;
        }

    }

    static final class AnnotatedTypeRegistration implements RegisteredSerializer {

        private final Predicate<AnnotatedType> predicate;
        private final TypeSerializer<?> serializer;

        AnnotatedTypeRegistration(final Predicate<AnnotatedType> predicate, final TypeSerializer<?> serializer) {
            this.predicate = predicate;
            this.serializer = serializer;
        }

        @Override
        public boolean matches(final Type test) {
            return this.predicate.test(GenericTypeReflector.annotate(test));
        }

        @Override
        public boolean matches(final AnnotatedType annotated) {
            return this.predicate.test(annotated);
        }

        @Override
        public TypeSerializer<?> serializer() {
            return this.serializer;
        }

    }

    static final class NoOp implements TypeSerializer<Void> {

        static final NoOp INSTANCE = new NoOp();

        private NoOp() {
        }

        @Override
        public Void deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
            throw new UnsupportedOperationException("this is a placeholder for null, should not be called directly");
        }

        @Override
        public void serialize(final Type type, final @Nullable Void obj, final ConfigurationNode node) throws SerializationException {
            throw new UnsupportedOperationException("this is a placeholder for null, should not be called directly");
        }
    }

}
