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

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

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
    public static MapFactory sorted(final Comparator<Object> comparator) {
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
            public <K, V> ConcurrentMap<K, V> create() {
                return new ConcurrentHashMap<>();
            }
        },
        SORTED_NATURAL {
            @Override
            public <K, V> ConcurrentMap<K, V> create() {
                return new ConcurrentSkipListMap<>();
            }
        },
        INSERTION_ORDERED {
            @Override
            public <K, V> Map<K, V> create() {
                return new LinkedHashMap<>();
            }
        }
    }

    private static final class SortedMapFactory implements MapFactory {
        private final Comparator<Object> comparator;

        private SortedMapFactory(final Comparator<Object> comparator) {
            this.comparator = comparator;
        }

        @Override
        public <K, V> ConcurrentMap<K, V> create() {
            return new ConcurrentSkipListMap<>(this.comparator);
        }

        @Override
        public boolean equals(final Object obj) {
            return obj instanceof SortedMapFactory && this.comparator.equals(((SortedMapFactory) obj).comparator);
        }

        @Override
        public int hashCode() {
            return this.comparator.hashCode();
        }

        @Override
        public String toString() {
            return "SortedMapFactory{comparator=" + this.comparator + '}';
        }
    }

}
