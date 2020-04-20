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

import com.google.common.collect.ImmutableMap;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

/**
 * A {@link ConfigValue} which holds a map of values.
 */
class MapConfigValue<N extends ScopedConfigurationNode<N>, A extends AbstractConfigurationNode<N, A>> extends ConfigValue<N, A> {
    private volatile @MonotonicNonNull ConcurrentMap<Object, A> values;

    public MapConfigValue(A holder) {
        super(holder);
    }

    @Override
    ValueType getType() {
        return ValueType.MAP;
    }

    private ConcurrentMap<Object, A> newMap() {
        return holder.getOptions().getMapFactory().create();
    }

    private ConcurrentMap<Object, A> getOrInitMap() {
        synchronized (this) {
            if (values == null) {
                values = newMap();
            }
            return values;
        }
    }

    @Override
    public @Nullable Object getValue() {
        Map<Object, @Nullable Object> value = new LinkedHashMap<>();
        ConcurrentMap<Object, A> thisVals = this.values;
        if (thisVals != null) {
            for (Map.Entry<Object, A> ent : thisVals.entrySet()) {
                value.put(ent.getKey(), ent.getValue().getValue()); // unwrap key from the backing node
            }
        }
        return value;
    }

    public Map<Object, N> getUnwrapped() {
        ConcurrentMap<Object, A> thisVals = this.values;
        if (thisVals == null) {
            return ImmutableMap.of();
        }
        ImmutableMap.Builder<Object, N> build = ImmutableMap.builderWithExpectedSize(thisVals.size());
        thisVals.forEach((k, v) -> build.put(k, v.self()));
        return build.build();
    }

    @Override
    public void setValue(@Nullable Object value) {
        if (value instanceof Map) {
            final ConcurrentMap<Object, A> newValue = newMap();
            for (Map.Entry<?, ?> ent : ((Map<?, ?>) value).entrySet()) {
                Object key = ent.getKey();
                if (key == null || ent.getValue() == null) {
                    continue;
                }
                A child = holder.createNode(key);
                newValue.put(key, child);
                child.attached = true;
                child.setValue(ent.getValue());
            }
            synchronized (this) {
                ConcurrentMap<Object, A> oldMap = this.values;
                this.values = newValue;
                detachChildren(oldMap);
            }
        } else {
            throw new IllegalArgumentException("Map configuration values can only be set to values of type Map");
        }
    }

    @Override
    @Nullable A putChild(Object key, @Nullable A value) {
        if (value == null) {
            ConcurrentMap<Object, A> values = this.values;
            return values == null ? null : values.remove(key);
        } else {
            return getOrInitMap().put(key, value);
        }
    }

    @Override
    @Nullable A putChildIfAbsent(Object key, @Nullable A value) {
        if (value == null) {
            ConcurrentMap<Object, A> values = this.values;
            return values == null ? null : values.remove(key);
        } else {
            return getOrInitMap().putIfAbsent(key, value);
        }
    }

    @Override
    public @Nullable A getChild(Object key) {
        ConcurrentMap<Object, A> values = this.values;
        return values == null ? null : values.get(key);
    }

    @Override
    public Iterable<A> iterateChildren() {
        ConcurrentMap<Object, A> values = this.values;
        return values == null ? Collections.emptySet() : values.values();
    }

    @Override
    MapConfigValue<N, A> copy(A holder) {
        MapConfigValue<N, A> copy = new MapConfigValue<>(holder);
        ConcurrentMap<Object, A> values = this.values;
        if (values != null) {
            ConcurrentMap<Object, A> copyMap = copy.getOrInitMap();
            for (Map.Entry<Object, A> ent : values.entrySet()) {
                copyMap.put(ent.getKey(), ent.getValue().copy(holder)); // recursively copy
            }
        }
        return copy;
    }

    @Override
    boolean isEmpty() {
        ConcurrentMap<Object, A> values = this.values;
        return values == null || values.isEmpty();
    }

    private static void detachChildren(@Nullable Map<Object, ? extends AbstractConfigurationNode<?, ?>> map) {
        if (map == null) {
            return;
        }

        for (AbstractConfigurationNode<?, ?> value : map.values()) {
            value.attached = false;
            value.clear();
        }
    }

    @Override
    public void clear() {
        synchronized (this) {
            ConcurrentMap<Object, A> oldMap = this.values;
            this.values = newMap();
            detachChildren(oldMap);
        }
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MapConfigValue<?, ?> that = (MapConfigValue<?, ?>) o;
        return Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return values == null ? 0 : values.hashCode();
    }

    @Override
    public String toString() {
        return "MapConfigValue{values=" + this.values + '}';
    }
}
