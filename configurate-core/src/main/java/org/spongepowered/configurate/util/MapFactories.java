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
import org.checkerframework.checker.nullness.qual.NonNull;

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
            @NonNull
            @Override
            public <K, V> ConcurrentMap<K, V> create() {
                return new ConcurrentHashMap<>();
            }
        },
        SORTED_NATURAL {
            @NonNull
            @Override
            public <K, V> ConcurrentMap<K, V> create() {
                return new ConcurrentSkipListMap<>();
            }
        },
        INSERTION_ORDERED {
            @NonNull
            @Override
            public <K, V> ConcurrentMap<K, V> create() {
                return new SynchronizedWrapper<>(new LinkedHashMap<>());
            }
        }
    }

    private static final class SortedMapFactory implements MapFactory {
        private final Comparator<Object> comparator;

        private SortedMapFactory(Comparator<Object> comparator) {
            this.comparator = comparator;
        }

        @NonNull
        @Override
        public <K, V> ConcurrentMap<K, V> create() {
            return new ConcurrentSkipListMap<>(comparator);
        }

        @Override
        public boolean equals(Object obj) {
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

    private static class SynchronizedWrapper<K, V> implements ConcurrentMap<K, V> {
        private final Map<K, V> wrapped;

        private SynchronizedWrapper(Map<K, V> wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public V putIfAbsent(K k, V v) {
            synchronized (wrapped) {
                if (!wrapped.containsKey(k)) {
                    wrapped.put(k, v);
                } else {
                    return wrapped.get(k);
                }
            }
            return null;
        }

        @Override
        public boolean remove(Object key, Object expected) {
            synchronized (wrapped) {
                if (Objects.equals(expected, wrapped.get(key))) {
                    return wrapped.remove(key) != null;
                }
            }
            return false;
        }

        @Override
        public boolean replace(K key, V old, V replace) {
            synchronized (wrapped) {
                if (Objects.equals(old, wrapped.get(key))) {
                    wrapped.put(key, replace);
                    return true;
                }
            }
            return false;
        }

        @Override
        public V replace(K k, V v) {
            synchronized (wrapped) {
                if (wrapped.containsKey(k)) {
                    return wrapped.put(k, v);
                }
            }
            return null;
        }

        @Override
        public int size() {
            synchronized (wrapped) {
                return wrapped.size();
            }
        }

        @Override
        public boolean isEmpty() {
            synchronized (wrapped) {
                return wrapped.isEmpty();
            }
        }

        @Override
        public boolean containsKey(Object o) {
            synchronized (wrapped) {
                return wrapped.containsKey(o);
            }
        }

        @Override
        public boolean containsValue(Object o) {
            synchronized (wrapped) {
                return wrapped.containsKey(o);
            }
        }

        @Override
        public V get(Object o) {
            synchronized (wrapped) {
                return wrapped.get(o);
            }
        }

        @Override
        public V put(K k, V v) {
            synchronized (wrapped) {
                return wrapped.put(k, v);
            }
        }

        @Override
        public V remove(Object o) {
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
        public Set<K> keySet() {
            synchronized (wrapped) {
                return ImmutableSet.copyOf(wrapped.keySet());
            }
        }

        @Override
        public Collection<V> values() {
            synchronized (wrapped) {
                return ImmutableSet.copyOf(wrapped.values());
            }
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            synchronized (wrapped) {
                return ImmutableSet.copyOf(wrapped.entrySet());
            }
        }

        @Override
        public boolean equals(Object obj) {
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
