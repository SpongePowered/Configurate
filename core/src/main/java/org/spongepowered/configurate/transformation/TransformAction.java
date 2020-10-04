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
package org.spongepowered.configurate.transformation;

import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ScopedConfigurationNode;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;

import java.util.function.Supplier;

/**
 * Represents an action to be performed that transforms a node in the
 * configuration tree.
 */
@FunctionalInterface
public interface TransformAction<T extends ScopedConfigurationNode<T>> {

    /**
     * Create a transform action that will remove the node at a specified path.
     *
     * @param <N> node type
     * @return new action
     */
    static <N extends ScopedConfigurationNode<N>> TransformAction<N> remove() {
        return (path, value) -> {
            value.set(null);
            return null;
        };
    }

    /**
     * Rename a node
     *
     * <p>This transformation cannot be applied to the root node.
     *
     * @param newKey the new key
     * @param <N> node type
     * @return new action
     */
    static <N extends ScopedConfigurationNode<N>> TransformAction<N> rename(Object newKey) {
        return (path, value) -> {
            final Object[] arr = path.array();
            if (arr.length == 0) {
                throw new IllegalArgumentException("The root node cannot be renamed!");
            }
            arr[arr.length - 1] = newKey;
            return arr;
        };
    }

    /**
     * Create a transform action that will change the value of a node to one of
     * the specified type.
     *
     * @param type value type
     * @param value value
     * @param <V> value type
     * @param <N> node type
     * @return new transformation action
     */
    static <V, N extends ScopedConfigurationNode<N>> TransformAction<N> set(TypeToken<V> type, @Nullable V value) {
        return set(type, (Supplier<V>) () -> value);
    }

    /**
     * Create a transform action that will change the value of a node to one of
     * the specified type.
     *
     * @param type value type
     * @param valueSupplier supplier returning a value on each call
     * @param <V> value type
     * @param <N> node type
     * @return new transformation action
     */
    static <V, N extends ScopedConfigurationNode<N>> TransformAction<N> set(TypeToken<V> type, Supplier<V> valueSupplier) {
        return (path, value) -> {
            try {
                value.set(type, valueSupplier.get());
            } catch (ObjectMappingException e) {
                // TODO: Error handling
            }
            return null;
        };
    }

    /**
     * Create a transform action that will change the value of a node to one of
     * the specified type.
     *
     * @param type value type
     * @param valueSupplier supplier returning a value on each call
     * @param <V> value type
     * @param <N> node type
     * @return new transformation action
     */
    static <V, N extends ScopedConfigurationNode<N>> TransformAction<N> set(Class<V> type, Supplier<V> valueSupplier) {
        return (path, value) -> {
            try {
                value.set(type, valueSupplier.get());
            } catch (ObjectMappingException e) {
                // TODO: Error handling
            }
            return null;
        };
    }


    /**
     * Called at a certain path, with the node at that path.
     *
     * <p><strong>Caution:</strong> The state of the <code>inputPath</code> is
     * only guaranteed to be accurate during a run of the transform function.
     * Use {@link NodePath#clone()} if the path's state needs to
     * be stored.
     *
     * @param inputPath the path of the given node
     * @param valueAtPath the node at the input path. May be modified
     * @return a modified path, or null if the path is to stay the same
     */
    @Nullable Object @Nullable[] visitPath(NodePath inputPath, T valueAtPath);

}
