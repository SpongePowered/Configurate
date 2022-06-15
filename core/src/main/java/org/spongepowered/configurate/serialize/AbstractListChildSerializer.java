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

import com.google.errorprone.annotations.ForOverride;
import io.leangen.geantyref.GenericTypeReflector;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.util.CheckedConsumer;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * A serializer for nodes that are 'list-like' (i.e may be stored in nodes where {@link ConfigurationNode#isList()} is
 * {@literal true}.
 *
 * @param <T> the type of collection to serialize
 * @since 4.1.0
 */
public abstract class AbstractListChildSerializer<T> implements TypeSerializer.Annotated<T> {

    /**
     * Create a new serializer, only for use by subclasses.
     *
     * @since 4.1.0
     */
    protected AbstractListChildSerializer() {
    }

    @Override
    public final T deserialize(final AnnotatedType type, final ConfigurationNode node) throws SerializationException {
        final AnnotatedType entryType = this.elementType(type);
        final @Nullable TypeSerializer<?> entrySerial = node.options().serializers().get(entryType);
        if (entrySerial == null) {
            throw new SerializationException(node, entryType, "No applicable type serializer for type");
        }

        if (node.isList()) {
            final List<? extends ConfigurationNode> values = node.childrenList();
            final T ret = this.createNew(values.size(), entryType);
            for (int i = 0; i < values.size(); ++i) {
                try {
                    this.deserializeSingle(i, ret, entrySerial.deserialize(entryType, values.get(i)));
                } catch (final SerializationException ex) {
                    ex.initPath(values.get(i)::path);
                    throw ex;
                }
            }
            return ret;
        } else {
            final @Nullable Object unwrappedVal = node.raw();
            if (unwrappedVal != null) {
                final T ret = this.createNew(1, entryType);
                this.deserializeSingle(0, ret, entrySerial.deserialize(entryType, node));
                return ret;
            }
        }
        return this.createNew(0, entryType);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public final void serialize(final AnnotatedType type, final @Nullable T obj, final ConfigurationNode node) throws SerializationException {
        final AnnotatedType entryType = this.elementType(type);
        final @Nullable TypeSerializer entrySerial = node.options().serializers().get(entryType);
        if (entrySerial == null) {
            throw new SerializationException(node, entryType, "No applicable type serializer for type");
        }

        node.raw(Collections.emptyList());
        if (obj != null) {
            this.forEachElement(obj, el -> {
                final ConfigurationNode child = node.appendListNode();
                try {
                    entrySerial.serialize(entryType, el, child);
                } catch (final SerializationException ex) {
                    ex.initPath(child::path);
                    throw ex;
                }
            });
        }
    }

    @Override
    public @Nullable T emptyValue(final AnnotatedType specificType, final ConfigurationOptions options) {
        try {
            return this.createNew(0, this.elementType(specificType));
        } catch (final SerializationException ex) {
            return null;
        }
    }

    /**
     * Given the type of container, provide the expected type of an element. If
     * the element type is not available, an exception must be thrown.
     *
     * @param containerType the type of container with type parameters resolved
     *                      to the extent possible.
     * @return the element type
     * @throws SerializationException if the element type could not be detected
     * @since 4.2.0
     */
    @ForOverride
    protected AnnotatedType elementType(final AnnotatedType containerType) throws SerializationException {
        return GenericTypeReflector.annotate(this.elementType(containerType.getType()));
    }

    /**
     * Given the type of container, provide the expected type of an element. If
     * the element type is not available, an exception must be thrown.
     *
     * @param containerType the type of container with type parameters resolved
     *                      to the extent possible.
     * @return the element type
     * @throws SerializationException if the element type could not be detected
     * @since 4.1.0
     * @deprecated for removal since 4.2.0, override {@link #elementType(AnnotatedType)} instead
     *     to pass through annotation information
     */
    @Deprecated
    protected Type elementType(final Type containerType) throws SerializationException {
        throw new IllegalStateException("AbstractListChildSerializer implementations should override elementType(AnnotatedType)");
    }

    /**
     * Create a new instance of the collection. The returned instance must be
     * mutable, but may have a fixed length.
     *
     * @param length the necessary collection length
     * @param elementType the type of element contained within the collection,
     *                    as provided by {@link #elementType(Type)}
     * @return a newly created collection
     * @throws SerializationException when an error occurs during the creation
     *                                of the collection
     * @since 4.2.0
     */
    @ForOverride
    protected T createNew(final int length, final AnnotatedType elementType) throws SerializationException {
        return this.createNew(length, elementType.getType());
    }

    /**
     * Create a new instance of the collection. The returned instance must be
     * mutable, but may have a fixed length.
     *
     * @param length the necessary collection length
     * @param elementType the type of element contained within the collection,
     *                    as provided by {@link #elementType(Type)}
     * @return a newly created collection
     * @throws SerializationException when an error occurs during the creation
     *                                of the collection
     * @since 4.1.0
     * @deprecated for removal since 4.2.0, override {@link #createNew(int, AnnotatedType)} instead
     *     to pass through annotation information
     */
    @ForOverride
    @Deprecated
    protected T createNew(final int length, final Type elementType) throws SerializationException {
        throw new IllegalStateException("AbstractListChildSerializer implementations should override createNew(int, AnnotatedType)");
    }

    /**
     * Perform the provided action on each element of the provided collection.
     *
     * <p>This is equivalent to a foreach loop on the collection
     *
     * @param collection the collection to act on
     * @param action the action to perform
     * @throws SerializationException when thrown by the underlying action
     * @since 4.1.0
     */
    @ForOverride
    protected abstract void forEachElement(T collection, CheckedConsumer<Object, SerializationException> action) throws SerializationException;

    /**
     * Place a single deserialized value into the collection being deserialized.
     *
     * @param index location to set value at
     * @param collection collection to modify
     * @param deserialized value to add
     * @throws SerializationException if object could not be coerced to an
     *         appropriate type.
     * @since 4.1.0
     */
    @ForOverride
    protected abstract void deserializeSingle(int index, T collection, @Nullable Object deserialized) throws SerializationException;

}
