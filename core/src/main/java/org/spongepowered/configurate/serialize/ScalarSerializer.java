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

import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Predicate;

/**
 * Serialize a value that can be represented as a scalar value within a node.
 * Implementations must be able to serialize when one of the accepted types is
 * a {@link String}, and may support any other types as desired.
 *
 * <p>When serializing to a node, null values will be passed through directly.
 * If the type serialized by this serializer is one of the native types of the
 * backing node, it will be written directly to the node without
 * any transformation.
 *
 * <p>Any serialized value must be deserializable by the same serializer.
 *
 * @param <T> the object type to serialize
 * @since 4.0.0
 */
public abstract class ScalarSerializer<T> implements TypeSerializer.Annotated<T> {

    private final TypeToken<T> type;

    /**
     * Create a new scalar serializer that handles the provided type.
     *
     * @param type type to handle
     * @since 4.0.0
     */
    @SuppressWarnings("unchecked")
    protected ScalarSerializer(final TypeToken<T> type) {
        final Type boxed = GenericTypeReflector.box(type.getType());
        this.type = boxed == type.getType() ? type : (TypeToken<T>) TypeToken.get(boxed);
    }

    /**
     * Create a new scalar serializer that handles the provided type.
     *
     * <p>{@code type} must not be a raw parameterized type.</p>
     *
     * @param type type to handle
     * @since 4.0.0
     */
    protected ScalarSerializer(final Class<T> type) {
        if (type.getTypeParameters().length > 0) {
            throw new IllegalArgumentException("Provided type " + type + " has type parameters but was not provided as a TypeToken!");
        }
        this.type = TypeToken.get(type);
    }

    @SuppressWarnings("unchecked")
    ScalarSerializer(final Type type) {
        this.type = (TypeToken<T>) TypeToken.get(type);
    }

    /**
     * Get the general type token applicable for this serializer. This token may
     * be parameterized.
     *
     * @return the type token for this serializer
     * @since 4.0.0
     */
    public final TypeToken<T> type() {
        return this.type;
    }

    @Override
    public final T deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
        return TypeSerializer.Annotated.super.deserialize(type, node);
    }

    @Override
    public final T deserialize(AnnotatedType type, final ConfigurationNode node) throws SerializationException {
        ConfigurationNode deserializeFrom = node;
        if (node.isList()) {
            final List<? extends ConfigurationNode> children = node.childrenList();
            if (children.size() == 1) {
                deserializeFrom = children.get(0);
            }
        }

        if (deserializeFrom.isList() || deserializeFrom.isMap()) {
            throw new SerializationException(type, "Value must be provided as a scalar!");
        }

        final @Nullable Object value = deserializeFrom.rawScalar();
        if (value == null) {
            throw new SerializationException(type, "No scalar value present");
        }

        type = GenericTypeReflector.toCanonicalBoxed(type); // every primitive type should be boxed (cuz generics!)
        final @Nullable T possible = this.cast(value);
        if (possible != null) {
            return possible;
        }

        return this.deserialize(type, value);
    }

    /**
     * Attempt to deserialize the provided object using an unspecialized type.
     * This may fail on more complicated deserialization processes such as with
     * enum types.
     *
     * @param value the object to deserialize.
     * @return the deserialized object, if possible
     * @throws SerializationException if unable to coerce the value to the
     *                                requested type.
     * @since 4.0.0
     */
    public final T deserialize(final Object value) throws SerializationException {
        final @Nullable T possible = this.cast(value);
        if (possible != null) {
            return possible;
        }

        return this.deserialize(this.type().getAnnotatedType(), value);
    }

    /**
     * Given an object of unknown type, attempt to convert it into the given
     * type.
     *
     * @param type the specific type of the type's usage
     * @param obj the object to convert
     * @return a converted object
     * @throws SerializationException if the object could not be converted for
     *                                any reason
     * @since 4.2.0
     */
    public T deserialize(final AnnotatedType type, final Object obj) throws SerializationException {
        return this.deserialize(type.getType(), obj);
    }

    /**
     * Given an object of unknown type, attempt to convert it into the given
     * type.
     *
     * @param type the specific type of the type's usage
     * @param obj the object to convert
     * @return a converted object
     * @throws SerializationException if the object could not be converted for
     *                                any reason
     * @since 4.0.0
     */
    public abstract T deserialize(Type type, Object obj) throws SerializationException;

    @Override
    public final void serialize(final AnnotatedType type, final @Nullable T obj, final ConfigurationNode node) {
        if (obj == null) {
            node.raw(null);
            return;
        }

        if (node.options().acceptsType(obj.getClass())) {
            node.raw(obj);
            return;
        }

        node.raw(this.serialize(type, obj, node.options()::acceptsType));
    }

    @Override
    public final void serialize(final Type type, final @Nullable T obj, final ConfigurationNode node) {
        this.serialize(GenericTypeReflector.annotate(type), obj, node);
    }

    /**
     * Serialize the provided value to a supported type, testing against the
     * provided predicate.
     *
     * <p>Annotated type information is provided for reference.</p>
     *
     * @param type the annotated type of the field being serialized
     * @param item the value to serialize
     * @param typeSupported a predicate to allow choosing which types are
     *                      supported
     * @return a serialized form of this object
     * @since 4.2.0
     */
    protected Object serialize(final AnnotatedType type, final T item, final Predicate<Class<?>> typeSupported) {
        return this.serialize(item, typeSupported);
    }

    /**
     * Serialize the provided value to a supported type, testing against the
     * provided predicate.
     *
     * @param item the value to serialize
     * @param typeSupported a predicate to allow choosing which types are
     *                      supported
     * @return a serialized form of this object
     * @since 4.0.0
     */
    protected abstract Object serialize(T item, Predicate<Class<?>> typeSupported);

    @SuppressWarnings("unchecked")
    private @Nullable T cast(final Object value) {
        final Class<?> rawType = GenericTypeReflector.erase(this.type().getType());
        if (rawType.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    /**
     * Attempt to deserialize the provided object, but rather than throwing an
     * exception when a deserialization error occurs, return null instead.
     *
     * @param obj the object to try to deserialize
     * @return an instance of the appropriate type, or null
     * @see #deserialize(Object)
     * @since 4.0.0
     */
    public final @Nullable T tryDeserialize(final @Nullable Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            return this.deserialize(obj);
        } catch (final SerializationException ex) {
            return null;
        }
    }

    /**
     * Serialize the item to a {@link String}, in a representation that can be
     * interpreted by this serializer again.
     *
     * @param item the item to serialize
     * @return the serialized form of the item
     * @since 4.0.0
     */
    public final String serializeToString(final T item) {
        if (item instanceof CharSequence) {
            return item.toString();
        }
        // Otherwise, use the serializer
        return (String) this.serialize(GenericTypeReflector.annotate(item.getClass()), item, clazz -> clazz.isAssignableFrom(String.class));
    }

    /**
     * A specialization of the scalar serializer that favors
     * annotated type methods over unannotated methods.
     *
     * @param <V> the value to deserialize
     * @since 4.2.0
     */
    public abstract static class Annotated<V> extends ScalarSerializer<V> {

        /**
         * Create a new annotated scalar serializer
         * that handles the provided type.
         *
         * <p>{@code type} must not be a raw parameterized type.</p>
         *
         * @param type type to handle
         * @since 4.2.0
         */
        protected Annotated(final Class<V> type) {
            super(type);
        }

        /**
         * Create a new annotated scalar serializer
         * that handles the provided type.
         *
         * @param type type to handle
         * @since 4.2.0
         */
        protected Annotated(final TypeToken<V> type) {
            super(type);
        }

        @Override
        public abstract V deserialize(
            AnnotatedType type,
            Object obj
        ) throws SerializationException;

        @Override
        public V deserialize(
            final Type type,
            final Object obj
        ) throws SerializationException {
            return this.deserialize(GenericTypeReflector.annotate(type), obj);
        }

        @Override
        protected abstract Object serialize(AnnotatedType type, V item, Predicate<Class<?>> typeSupported);

        @Override
        protected Object serialize(final V item, final Predicate<Class<?>> typeSupported) {
            return this.serialize(GenericTypeReflector.annotate(item.getClass()), item, typeSupported);
        }

    }

}
