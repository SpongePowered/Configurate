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

    volatile Map<Object, A> values;

    MapConfigValue(final A holder) {
        super(holder);
        this.values = newMap();
    }

    private Map<Object, A> newMap() {
        final Map<Object, A> ret = this.holder.getOptions().getMapFactory().create();
        if (!(ret instanceof ConcurrentMap)) {
            return Collections.synchronizedMap(ret);
        } else {
            return ret;
        }
    }

    @Nullable
    @Override
    public Object getValue() {
        final Map<Object, Object> value = new LinkedHashMap<>();
        for (Map.Entry<Object, A> ent : this.values.entrySet()) {
            value.put(ent.getKey(), ent.getValue().getValue()); // unwrap key from the backing node
        }
        return value;
    }

    public Map<Object, N> getUnwrapped() {
        final ImmutableMap.Builder<Object, N> build = ImmutableMap.builderWithExpectedSize(this.values.size());
        this.values.forEach((k, v) -> build.put(k, v.self()));
        return build.build();
    }

    @Override
    public void setValue(final @Nullable Object value) {
        if (value instanceof Map) {
            final Map<Object, A> newValue = newMap();
            for (Map.Entry<?, ?> ent : ((Map<?, ?>) value).entrySet()) {
                if (ent.getValue() == null) {
                    continue;
                }
                final A child = this.holder.createNode(ent.getKey());
                newValue.put(ent.getKey(), child);
                child.attached = true;
                child.setValue(ent.getValue());
            }
            synchronized (this) {
                final Map<Object, A> oldMap = this.values;
                this.values = newValue;
                detachChildren(oldMap);
            }
        } else {
            throw new IllegalArgumentException("Map configuration values can only be set to values of type Map");
        }
    }

    @Nullable
    @Override
    A putChild(final @NonNull Object key, final @Nullable A value) {
        if (value == null) {
            return this.values.remove(key);
        } else {
            return this.values.put(key, value);
        }
    }

    @Nullable
    @Override
    A putChildIfAbsent(final @NonNull Object key, final @Nullable A value) {
        if (value == null) {
            return this.values.remove(key);
        } else {
            return this.values.putIfAbsent(key, value);
        }
    }

    @Nullable
    @Override
    public A getChild(final @Nullable Object key) {
        return this.values.get(key);
    }

    @NonNull
    @Override
    public Iterable<A> iterateChildren() {
        return this.values.values();
    }

    @NonNull
    @Override
    MapConfigValue<N, A> copy(final @NonNull A holder) {
        final MapConfigValue<N, A> copy = new MapConfigValue<>(holder);
        for (Map.Entry<Object, A> ent : this.values.entrySet()) {
            copy.values.put(ent.getKey(), ent.getValue().copy(holder)); // recursively copy
        }
        return copy;
    }

    @Override
    boolean isEmpty() {
        return this.values.isEmpty();
    }

    private static void detachChildren(final Map<Object, ? extends AbstractConfigurationNode<?, ?>> map) {
        for (AbstractConfigurationNode<?, ?> value : map.values()) {
            value.attached = false;
            value.clear();
        }
    }

    @Override
    public void clear() {
        synchronized (this) {
            final Map<Object, A> oldMap = this.values;
            this.values = newMap();
            detachChildren(oldMap);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final MapConfigValue<?, ?> that = (MapConfigValue<?, ?>) o;
        return Objects.equals(this.values, that.values);
    }

    @Override
    public int hashCode() {
        return this.values.hashCode();
    }

    @Override
    public String toString() {
        return "MapConfigValue{values=" + this.values + '}';
    }

}
