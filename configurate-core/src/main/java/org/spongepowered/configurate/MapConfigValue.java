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

import com.google.common.base.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * A {@link ConfigValue} which holds a map of values.
 */
class MapConfigValue<T extends AbstractConfigurationNode<T>> extends ConfigValue<T> {
    volatile ConcurrentMap<Object, T> values;

    public MapConfigValue(T holder) {
        super(holder);
        values = newMap();
    }

    @Override
    ValueType getType() {
        return ValueType.MAP;
    }

    private ConcurrentMap<Object, T> newMap() {
        return holder.getOptions().getMapFactory().create();
    }

    @Nullable
    @Override
    public Object getValue() {
        Map<Object, Object> value = new LinkedHashMap<>();
        for (Map.Entry<Object, T> ent : values.entrySet()) {
            value.put(ent.getKey(), ent.getValue().getValue()); // unwrap key from the backing node
        }
        return value;
    }

    @Override
    public void setValue(@Nullable Object value) {
        if (value instanceof Map) {
            final ConcurrentMap<Object, T> newValue = newMap();
            for (Map.Entry<?, ?> ent : ((Map<?, ?>) value).entrySet()) {
                if (ent.getValue() == null) {
                    continue;
                }
                T child = holder.createNode(ent.getKey());
                newValue.put(ent.getKey(), child);
                child.attached = true;
                child.setValue(ent.getValue());
            }
            synchronized (this) {
                ConcurrentMap<Object, T> oldMap = this.values;
                this.values = newValue;
                detachChildren(oldMap);
            }
        } else {
            throw new IllegalArgumentException("Map configuration values can only be set to values of type Map");
        }
    }

    @Nullable
    @Override
    T putChild(@NonNull Object key, @Nullable T value) {
        if (value == null) {
            return values.remove(key);
        } else {
            return values.put(key, value);
        }
    }

    @Nullable
    @Override
    T putChildIfAbsent(@NonNull Object key, @Nullable T value) {
        if (value == null) {
            return values.remove(key);
        } else {
            return values.putIfAbsent(key, value);
        }
    }

    @Nullable
    @Override
    public T getChild(@Nullable Object key) {
        return values.get(key);
    }

    @NonNull
    @Override
    public Iterable<T> iterateChildren() {
        return values.values();
    }

    @NonNull
    @Override
    MapConfigValue<T> copy(@NonNull T holder) {
        MapConfigValue<T> copy = new MapConfigValue<>(holder);
        for (Map.Entry<Object, T> ent : this.values.entrySet()) {
            copy.values.put(ent.getKey(), ent.getValue().copy(holder)); // recursively copy
        }
        return copy;
    }

    private static void detachChildren(Map<Object, ? extends AbstractConfigurationNode<?>> map) {
        for (AbstractConfigurationNode<?> value : map.values()) {
            value.attached = false;
            value.clear();
        }
    }

    @Override
    public void clear() {
        synchronized (this) {
            ConcurrentMap<Object, T> oldMap = this.values;
            this.values = newMap();
            detachChildren(oldMap);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MapConfigValue<?> that = (MapConfigValue<?>) o;
        return Objects.equal(values, that.values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public String toString() {
        return "MapConfigValue{values=" + this.values + '}';
    }
}
