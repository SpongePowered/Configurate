/**
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
package ninja.leaping.configurate.util;

import com.google.common.base.Supplier;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Factories to create map implementations commonly used for maps
 */
public class MapFactories {
    private MapFactories() {
        // Nope
    }

    public static <V extends ConfigurationNode> Supplier<ConcurrentMap<Object, V>> unordered() {
        return new Supplier<ConcurrentMap<Object, V>>() {
            @Override
            public ConcurrentMap<Object, V> get() {
                return new ConcurrentHashMap<>();
            }
        };
    }

    public static <V extends ConfigurationNode> Supplier<ConcurrentMap<Object, V>> sorted(final Comparator<Object>
                                                                                            comparator) {
        return new Supplier<ConcurrentMap<Object, V>>() {
            @Override
            public ConcurrentMap<Object, V> get() {
                return new ConcurrentSkipListMap<>(comparator);
            }
        };
    }

    public static <V extends ConfigurationNode> Supplier<ConcurrentMap<Object, V>> sortedNatural() {
        return new Supplier<ConcurrentMap<Object, V>>() {
            @Override
            public ConcurrentMap<Object, V> get() {
                return new ConcurrentSkipListMap<>();
            }
        };
    }

    public static <V extends ConfigurationNode> Supplier<ConcurrentMap<Object, V>> insertionOrdered() {
        return new Supplier<ConcurrentMap<Object, V>>() {
            @Override
            public ConcurrentMap<Object, V> get() {
                return new ConcurrentLinkedHashMap.Builder<Object, V>().maximumWeightedCapacity(Long.MAX_VALUE).build();
            }
        };
    }


}
