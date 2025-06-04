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
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.NodePath;

import java.util.function.Supplier;

/**
 * Represents an action to be performed that transforms a node in the
 * configuration tree.
 *
 * @since 4.0.0
 */
@FunctionalInterface
public interface TransformAction {

    /**
     * Create a transform action that will remove the node at a specified path.
     *
     * @return new action
     * @since 4.0.0
     */
    static TransformAction remove() {
        return (path, value) -> {
            value.raw(null);
            return null;
        };
    }

    /**
     * Rename a node
     *
     * <p>This transformation cannot be applied to the root node.
     *
     * @param newKey the new key
     * @return new action
     * @since 4.0.0
     */
    static TransformAction rename(final Object newKey) {
        return (path, value) -> {
            final Object[] arr = path.array();
            if (arr.length == 0) {
                throw new ConfigurateException(value, "The root node cannot be renamed!");
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
     * @return new transformation action
     * @since 4.0.0
     */
    static <V> TransformAction set(final TypeToken<V> type, final @Nullable V value) {
        return (path, node) -> {
            node.set(type, value);
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
     * @return new transformation action
     * @since 4.0.0
     */
    static <V> TransformAction set(final TypeToken<V> type, final Supplier<@Nullable V> valueSupplier) {
        return (path, value) -> {
            value.set(type, valueSupplier.get());
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
     * @return new transformation action
     * @since 4.0.0
     */
    static <V> TransformAction set(final Class<V> type, final Supplier<V> valueSupplier) {
        return (path, value) -> {
            value.set(type, valueSupplier.get());
            return null;
        };
    }

    /**
     * Called at a certain path, with the node at that path.
     *
     * <p><strong>Caution:</strong> The state of the <code>path</code> is
     * only guaranteed to be accurate during a run of the transform function.
     * Use {@link NodePath#copy()} if the path's state needs to
     * be stored.
     *
     * @param path the path of the given node
     * @param value the node at the input path. May be modified
     * @return a modified path, or null if the path is to stay the same
     * @since 4.0.0
     */
    Object @Nullable[] visitPath(NodePath path, ConfigurationNode value) throws ConfigurateException;

}
