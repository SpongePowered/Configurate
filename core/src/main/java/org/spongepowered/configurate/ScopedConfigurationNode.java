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

import com.google.common.reflect.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface ScopedConfigurationNode<N extends ScopedConfigurationNode<N>> extends ConfigurationNode {

    /**
     * Get a correctly typed instance of this node.
     * @return The node type
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
    @NonNull N getNode(@NonNull Object... path);

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull N getNode(@NonNull Iterable<?> path);

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable N getParent();

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull N mergeValuesFrom(@NonNull ConfigurationNode other);

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull N setValue(@Nullable Object value);

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    default <V> N setValue(@NonNull TypeToken<V> type, @Nullable V value) throws ObjectMappingException {
        if (value == null) {
            return setValue(null);
        }

        final @Nullable TypeSerializer<V> serial = getOptions().getSerializers().get(type);
        if (serial != null) {
            serial.serialize(type, value, self());
        } else if (getOptions().acceptsType(value.getClass())) {
            setValue(value); // Just write if no applicable serializer exists?
        } else {
            throw new ObjectMappingException("No serializer available for type " + type);
        }
        return self();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull List<N> getChildrenList();

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull Map<Object, N> getChildrenMap();

    /**
     * Execute an action on this node. This allows performing multiple
     * operations on a single node without having to clutter up the surrounding
     * scope.
     *
     * @param action The action to perform on this node
     * @return this
     */
    default N act(Consumer<? super N> action) {
        action.accept(self());
        return self();
    }

    /**
     * Visit this node hierarchy as described in {@link ConfigurationVisitor}.
     *
     * @param visitor The visitor
     * @param <S> The state type
     * @param <T> The terminal type
     * @param <E> exception type that may be thrown
     * @return returned terminal from the visitor
     * @throws E when throw by visitor implementation
     */
    default <S, T, E extends Exception> T visit(ConfigurationVisitor<N, S, T, E> visitor) throws E {
        return visit(visitor, visitor.newState());
    }

    /**
     * Visit this node hierarchy as described in {@link ConfigurationVisitor}.
     *
     * @param visitor The visitor
     * @param state The state to start with
     * @param <T> The terminal type
     * @param <S> The state type
     * @param <E> exception type that may be thrown
     * @return returned terminal from the visitor
     * @throws E when throw by visitor implementation
     */
    <S, T, E extends Exception> T visit(ConfigurationVisitor<? super N, S, T, E> visitor, S state) throws E;

    /**
     * Visit this node hierarchy as described in {@link ConfigurationVisitor}.
     *
     * <p>This overload will remove the need for exception handling for visitors
     * that do not have any checked exceptions.</p>
     *
     * @param visitor The visitor
     * @param <S> The state type
     * @param <T> The terminal type
     * @return The returned terminal from the visitor
     */
    default <S, T> T visit(ConfigurationVisitor.Safe<N, S, T> visitor) {
        return visit(visitor, visitor.newState());
    }

    /**
     * Visit this node hierarchy as described in {@link ConfigurationVisitor}.
     *
     * <p>This overload will remove the need for exception handling for visitors
     * that do not have any checked exceptions.</p>
     *
     * @param visitor The visitor
     * @param state The state to start with
     * @param <T> The terminal type
     * @param <S> The state type
     * @return The returned terminal from the visitor
     */
    <S, T> T visit(ConfigurationVisitor.Safe<? super N, S, T> visitor, S state);

    @Override
    <V> N setHint(RepresentationHint<V> hint, @Nullable V value);

}
