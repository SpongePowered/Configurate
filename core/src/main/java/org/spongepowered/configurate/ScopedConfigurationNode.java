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
package org.spongepowered.configurate;

import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collector;

/**
 * Intermediate node type to reduce need for casting.
 *
 * <p>Any methods that return {@link ConfigurationNode} in
 * {@link ConfigurationNode} should be overridden to return the {@link N}
 * self-type instead.</p>
 *
 * @param <N> self type
 */
public interface ScopedConfigurationNode<N extends ScopedConfigurationNode<N>> extends ConfigurationNode {

    /**
     * Get a correctly typed instance of this node.
     *
     * @return the node type
     */
    N self();

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull N appendListNode();

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull N copy();

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull N node(@NonNull Object... path);

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull N node(@NonNull Iterable<?> path);

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable N parent();

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull N mergeFrom(@NonNull ConfigurationNode other);

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull N set(@Nullable Object value);

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    @SuppressWarnings({"unchecked", "rawtypes"}) // for TypeSerializer.serialize
    default N set(@NonNull Type type, @Nullable Object value) throws ObjectMappingException {
        if (value == null) {
            return set(null);
        }

        final @Nullable TypeSerializer<?> serial = options().serializers().get(type);
        if (serial != null) {
            ((TypeSerializer) serial).serialize(type, value, self());
        } else if (options().acceptsType(value.getClass())) {
            set(value); // Just write if no applicable serializer exists?
        } else {
            throw new ObjectMappingException("No serializer available for type " + type);
        }
        return self();
    }

    @Override
    default <V> N set(Class<V> type, @Nullable V value) throws ObjectMappingException {
        return set((Type) type, value);
    }

    @Override
    default <V> N set(TypeToken<V> type, @Nullable V value) throws ObjectMappingException {
        return set(type.getType(), value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull List<N> childrenList();

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull Map<Object, N> childrenMap();

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    default <V> Collector<Map.Entry<?, V>, N, N> toMapCollector(final TypeToken<V> valueType) {
        return (Collector) ConfigurationNode.super.toMapCollector(valueType);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    default <V> Collector<Map.Entry<?, V>, N, N> toMapCollector(final Class<V> valueType) {
        return (Collector) ConfigurationNode.super.toMapCollector(valueType);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    default <V> Collector<V, N, N> toListCollector(final TypeToken<V> valueType) {
        return (Collector) ConfigurationNode.super.toListCollector(valueType);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    default <V> Collector<V, N, N> toListCollector(final Class<V> valueType) {
        return (Collector) ConfigurationNode.super.toListCollector(valueType);
    }

    /**
     * Execute an action on this node. This allows performing multiple
     * operations on a single node without having to clutter up the surrounding
     * scope.
     *
     * @param action the action to perform on this node
     * @return this node
     */
    default N act(Consumer<? super N> action) {
        action.accept(self());
        return self();
    }

    @Override
    <V> N hint(RepresentationHint<V> hint, @Nullable V value);

}
