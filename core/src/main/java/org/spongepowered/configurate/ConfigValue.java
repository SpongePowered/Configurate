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

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The value in a {@link ConfigurationNode}.
 *
 * @since 4.0.0
 */
interface ConfigValue<N extends ScopedConfigurationNode<N>, A extends AbstractConfigurationNode<N, A>> {

    /**
     * Gets the value encapsulated by this instance.
     *
     * @return the value
     */
    @Nullable Object get();

    /**
     * Sets the value encapsulated by this instance.
     *
     * @param value the value
     */
    void set(@Nullable Object value);

    /**
     * Put a child value, or null to remove value at that key.
     *
     * @param key the key
     * @param value the node to put at key
     * @return existing node at key, if present
     */
    @Nullable A putChild(Object key, @Nullable A value);

    /**
     * Put a child value, if one isn't already present at that key.
     *
     * @param key the key
     * @param value the node to put at key
     * @return existing node at key, if present
     */
    @Nullable A putChildIfAbsent(Object key, @Nullable A value);

    /**
     * Gets the currently present child for the given key. Returns null if no
     * child is present.
     *
     * @param key the key to get child at
     * @return the child if any
     */
    @Nullable A child(@Nullable Object key);

    /**
     * Returns an iterable over all child nodes.
     *
     * @return an iterator
     */
    Iterable<A> iterateChildren();

    /**
     * Creates a copy of this node.
     *
     * @return a copy
     */
    ConfigValue<N, A> copy(A holder);

    /**
     * Whether this value has any content.
     *
     * @return value
     */
    boolean isEmpty();

    /**
     * Clears the set value (or any attached child values) from this value.
     */
    void clear();

}
