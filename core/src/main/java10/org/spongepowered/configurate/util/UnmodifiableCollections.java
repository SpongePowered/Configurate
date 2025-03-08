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
package org.spongepowered.configurate.util;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Provides a set of methods that produce unmodifiable copies of collections.
 *
 * @since 4.0.0
 */
public final class UnmodifiableCollections {

    private UnmodifiableCollections() {}

    /**
     * Creates an unmodifiable copy of the given {@link List} instance.
     *
     * @param original the list to be copied
     * @param <E> the type of every item in the entry
     * @return a unmodifiable copy of the given {@link List} instance
     * @since 4.0.0
     */
    public static <E> List<E> copyOf(final List<? extends E> original) {
        return List.copyOf(original);
    }

    /**
     * Creates an unmodifiable copy of the given {@link Set} instance.
     *
     * @param original the set to be copied
     * @param <E> the type of every item in the entry
     * @return a unmodifiable copy of the given {@link Set} instance
     * @since 4.0.0
     */
    public static <E> Set<E> copyOf(final Set<? extends E> original) {
        return Set.copyOf(original);
    }

    /**
     * Creates an unmodifiable copy of the given {@link Map} instance.
     *
     * @param original the map to be copied
     * @param <K> key type of the map
     * @param <V> value type of the map
     * @return an unmodifiable copy of the given {@link Map} instance.
     * @since 4.1.0
     */
    public static <K, V> Map<K, V> copyOf(final Map<? extends K, ? extends V> original) {
        return Map.copyOf(original);
    }

    /**
     * Creates an unmodifiable copy of the given array as a list, preserving
     * order.
     *
     * @param original the array to be copied into a list
     * @param <E> the type of every item in the entry
     * @return a unmodifiable copy of the given array as a {@link List}
     *         instance
     * @since 4.0.0
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <E> List<E> toList(final E... original) {
        return List.of(original);
    }

    /**
     * Creates an unmodifiable copy of the given array as a set.
     *
     * @param original the array to be copied into a set
     * @param <E> the type of every item in the entry
     * @return a unmodifiable copy of the given array as a {@link Set} instance
     * @since 4.0.0
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <E> Set<E> toSet(final E... original) {
        return Set.of(original);
    }

    /**
     * Build an unmodifiable map.
     *
     * @param <K> key type
     * @param <V> value type
     * @param handler consumer that will populate the map wih keys
     * @return a new unmodifiable map
     * @since 4.0.0
     */
    public static <K, V> Map<K, V> buildMap(final Consumer<Map<K, V>> handler) {
        final var builder = new LinkedHashMap<K, V>();
        requireNonNull(handler, "handler").accept(builder);
        return Collections.unmodifiableMap(builder);
    }

    /**
     * Creates an immutable instance of {@link Map.Entry}.
     *
     * @param key the key in the entry
     * @param value the value in the entry
     * @param <K> the key's type
     * @param <V> the value's type
     * @return the new map entry
     * @since 4.0.0
     */
    public static <K, V> Map.Entry<K, V> immutableMapEntry(final K key, final V value) {
        return Map.entry(key, value);
    }

}
