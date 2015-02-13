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
package ninja.leaping.configurate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

class MapConfigValue extends ConfigValue {
    AtomicReference<ConcurrentMap<Object, SimpleConfigurationNode>> values = new AtomicReference<>();

    public MapConfigValue(SimpleConfigurationNode holder) {
        super(holder);
        this.values.set(new ConcurrentHashMap<Object, SimpleConfigurationNode>());
    }

    @Override
    public Object getValue() {
        Map<Object, Object> value = new HashMap<>();
        for (Map.Entry<Object, ? extends SimpleConfigurationNode> ent : values.get().entrySet()) {
            value.put(ent.getKey(), ent.getValue().getValue());
        }
        return value;
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Map) {
            final ConcurrentMap<Object, SimpleConfigurationNode> newValue = new ConcurrentHashMap<>();
            for (Map.Entry<?, ?> ent : ((Map<?, ?>) value).entrySet()) {
                SimpleConfigurationNode child = holder.createNode(ent.getKey());
                newValue.put(ent.getKey(), child);
                child.attached = true;
                child.setValue(ent.getValue());
            }
            detachChildren(this.values.getAndSet(newValue));
        } else {
            throw new IllegalArgumentException("Map configuration values can only be set to values of type Map");
        }

    }

    @Override
    SimpleConfigurationNode putChild(Object key, SimpleConfigurationNode value) {
        if (value == null) {
            return values.get().remove(key);
        } else {
            return values.get().put(key, value);
        }
    }

    @Override
    SimpleConfigurationNode putChildIfAbsent(Object key, SimpleConfigurationNode value) {
        if (value == null) {
            return values.get().remove(key);
        } else {
            return values.get().putIfAbsent(key, value);
        }
    }

    @Override
    public SimpleConfigurationNode getChild(Object key) {
        return values.get().get(key);
    }

    @Override
    public Iterable<SimpleConfigurationNode> iterateChildren() {
        return values.get().values();
    }

    private void detachChildren(Map<Object, SimpleConfigurationNode> map) {
        for (SimpleConfigurationNode value : map.values()) {
            value.attached = false;
            value.clear();
        }
    }

    @Override
    public void clear() {
        detachChildren(values.getAndSet(new ConcurrentHashMap<Object, SimpleConfigurationNode>())/**/);
    }
}
