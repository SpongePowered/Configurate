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
package ninja.leaping.configurate;

import com.google.common.base.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * A {@link ConfigValue} which holds a map of values.
 */
class MapConfigValue extends ConfigValue {
    volatile ConcurrentMap<Object, SimpleConfigurationNode> values;

    public MapConfigValue(SimpleConfigurationNode holder) {
        super(holder);
        values = newMap();
    }

    private ConcurrentMap<Object, SimpleConfigurationNode> newMap() {
        return holder.getOptions().getMapFactory().create();
    }

    @Nullable
    @Override
    public Object getValue() {
        Map<Object, Object> value = new LinkedHashMap<>();
        for (Map.Entry<Object, ? extends SimpleConfigurationNode> ent : values.entrySet()) {
            value.put(ent.getKey(), ent.getValue().getValue()); // unwrap key from the backing node
        }
        return value;
    }

    @Override
    public void setValue(@Nullable Object value) {
        if (value instanceof Map) {
            final ConcurrentMap<Object, SimpleConfigurationNode> newValue = newMap();
            for (Map.Entry<?, ?> ent : ((Map<?, ?>) value).entrySet()) {
                if (ent.getValue() == null) {
                    continue;
                }
                SimpleConfigurationNode child = holder.createNode(ent.getKey());
                newValue.put(ent.getKey(), child);
                child.attached = true;
                child.setValue(ent.getValue());
            }
            synchronized (this) {
                ConcurrentMap<Object, SimpleConfigurationNode> oldMap = this.values;
                this.values = newValue;
                detachChildren(oldMap);
            }
        } else {
            throw new IllegalArgumentException("Map configuration values can only be set to values of type Map");
        }
    }

    @Nullable
    @Override
    SimpleConfigurationNode putChild(@NonNull Object key, @Nullable SimpleConfigurationNode value) {
        if (value == null) {
            return values.remove(key);
        } else {
            return values.put(key, value);
        }
    }

    @Nullable
    @Override
    SimpleConfigurationNode putChildIfAbsent(@NonNull Object key, @Nullable SimpleConfigurationNode value) {
        if (value == null) {
            return values.remove(key);
        } else {
            return values.putIfAbsent(key, value);
        }
    }

    @Nullable
    @Override
    public SimpleConfigurationNode getChild(@Nullable Object key) {
        return values.get(key);
    }

    @NonNull
    @Override
    public Iterable<SimpleConfigurationNode> iterateChildren() {
        return values.values();
    }

    private static void detachChildren(Map<Object, SimpleConfigurationNode> map) {
        for (SimpleConfigurationNode value : map.values()) {
            value.attached = false;
            value.clear();
        }
    }

    @Override
    public void clear() {
        synchronized (this) {
            ConcurrentMap<Object, SimpleConfigurationNode> oldMap = this.values;
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
        MapConfigValue that = (MapConfigValue) o;
        return Objects.equal(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(values);
    }
}
