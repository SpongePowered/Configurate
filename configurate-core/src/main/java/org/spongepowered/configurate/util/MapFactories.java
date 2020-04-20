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

import com.google.common.collect.ImmutableSet;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.framework.qual.Covariant;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static java.util.Objects.requireNonNull;

/**
 * Default implementations of {@link MapFactory}.
 */
public final class MapFactories {
    private MapFactories() {}

    /**
     * Returns a {@link MapFactory} which creates maps without an order.
     *
     * @return A map factory which produces unordered maps
     */
    public static MapFactory unordered() {
        return DefaultFactory.UNORDERED;
    }

    /**
     * Returns a {@link MapFactory} which creates maps which are sorted using the given comparator.
     *
     * @param comparator The comparator used to sort the map keys
     * @return A map factory which produces sorted maps
     */
    public static MapFactory sorted(Comparator<Object> comparator) {
        return new SortedMapFactory(requireNonNull(comparator, "comparator"));
    }

    /**
     * Returns a {@link MapFactory} which creates maps which are naturally sorted.
     *
     * @return A map factory which produces naturally sorted maps
     * @see Comparator#naturalOrder()
     */
    public static MapFactory sortedNatural() {
        return DefaultFactory.SORTED_NATURAL;
    }

    /**
     * Returns a {@link MapFactory} which creates maps which are sorted by insertion order.
     *
     * @return A map factory which produces maps sorted by insertion order
     */
    public static MapFactory insertionOrdered() {
        return DefaultFactory.INSERTION_ORDERED;
    }

    private enum DefaultFactory implements MapFactory {
        UNORDERED {
            @Override
            public <@NonNull K, @NonNull V> ConcurrentMap<K, V> create() {
                return new ConcurrentHashMap<>();
            }
        },
        SORTED_NATURAL {
            @Override
            public <@NonNull K, @NonNull V> ConcurrentMap<K, V> create() {
                return new ConcurrentSkipListMap<>();
            }
        },
        INSERTION_ORDERED {
            @Override
            public <@NonNull K, @NonNull V> ConcurrentMap<K, V> create() {
                return new SynchronizedWrapper<>(new LinkedHashMap<>());
            }
        }
    }

    private static final class SortedMapFactory implements MapFactory {
        private final Comparator<Object> comparator;

        private SortedMapFactory(Comparator<Object> comparator) {
            this.comparator = comparator;
        }

        @Override
        public <K, V> ConcurrentMap<K, V> create() {
            return new ConcurrentSkipListMap<>(comparator);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            return obj instanceof SortedMapFactory && comparator.equals(((SortedMapFactory) obj).comparator);
        }

        @Override
        public int hashCode() {
            return comparator.hashCode();
        }

        @Override
        public String toString() {
            return "SortedMapFactory{comparator=" + comparator + '}';
        }
    }

    @Covariant(0)
    private static class SynchronizedWrapper<K extends @NonNull Object, V extends @NonNull Object> implements ConcurrentMap<@NonNull K, @NonNull V> {
        private final Map<K, V> wrapped;

        private SynchronizedWrapper(Map<K, V> wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        @EnsuresKeyFor(value = "#1", map = "this")
        public @Nullable V putIfAbsent(K k, V v) {
            synchronized (wrapped) {
                return wrapped.putIfAbsent(k, v);
            }
        }

        @Override
        public boolean remove(Object key, Object expected) {
            synchronized (wrapped) {
                return wrapped.remove(key, expected);
            }
        }

        @Override
        public boolean replace(K key, V old, V replace) {
            synchronized (wrapped) {
                return wrapped.replace(key, old, replace);
            }
        }

        @Override
        public @Nullable V replace(K k, V v) {
            synchronized (wrapped) {
                wrapped.replace(k, v);
            }
            return null;
        }

        @Override
        @Pure
        public @NonNegative int size() {
            synchronized (wrapped) {
                return wrapped.size();
            }
        }

        @Override
        @Pure
        public boolean isEmpty() {
            synchronized (wrapped) {
                return wrapped.isEmpty();
            }
        }

        @Override
        @EnsuresKeyForIf(result = true, expression = "#1", map = "this")
        public boolean containsKey(Object o) {
            synchronized (wrapped) {
                return wrapped.containsKey(o);
            }
        }

        @Override
        @Pure
        public boolean containsValue(Object o) {
            synchronized (wrapped) {
                return wrapped.containsKey(o);
            }
        }

        @Override
        @Pure
        public @Nullable V get(Object o) {
            synchronized (wrapped) {
                return wrapped.get(o);
            }
        }

        @Override
        @EnsuresKeyFor(value = "#1", map = "this")
        public @Nullable V put(K k, V v) {
            synchronized (wrapped) {
                return wrapped.put(k, v);
            }
        }

        @Override
        public @Nullable V remove(Object o) {
            synchronized (wrapped) {
                return wrapped.remove(o);
            }
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> map) {
            synchronized (wrapped) {
                wrapped.putAll(map);
            }
        }

        @Override
        public void clear() {
            synchronized (wrapped) {
                wrapped.clear();
            }
        }

        @Override
        @SideEffectFree
        public Set<K> keySet() {
            synchronized (wrapped) {
                return ImmutableSet.copyOf(wrapped.keySet());
            }
        }

        @Override
        @SideEffectFree
        public Collection<V> values() {
            synchronized (wrapped) {
                return ImmutableSet.copyOf(wrapped.values());
            }
        }

        @Override
        @SideEffectFree
        public Set<Entry<K, V>> entrySet() {
            synchronized (wrapped) {
                return ImmutableSet.copyOf(wrapped.entrySet());
            }
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj == this) {
                return true;
            }
            synchronized (wrapped) {
                return wrapped.equals(obj);
            }
        }

        @Override
        public int hashCode() {
            synchronized (wrapped) {
                return wrapped.hashCode();
            }
        }

        @Override
        public String toString() {
            synchronized (wrapped) {
                return "SynchronizedWrapper{backing=" + wrapped.toString() + '}';
            }
        }
    }

}
