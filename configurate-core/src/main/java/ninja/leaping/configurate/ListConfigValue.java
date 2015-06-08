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


import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

class ListConfigValue extends ConfigValue {
    final AtomicReference<List<SimpleConfigurationNode>> values = new AtomicReference<>();

    ListConfigValue(SimpleConfigurationNode holder) {
        super(holder);
        values.set(new ArrayList<SimpleConfigurationNode>());
    }

    ListConfigValue(SimpleConfigurationNode holder, Object startValue) {
        super(holder);
        this.values.set(new ArrayList<SimpleConfigurationNode>());
        SimpleConfigurationNode child = holder.createNode(0);
        child.attached = true;
        child.setValue(startValue);
        this.values.get().add(child);
    }

    @Override
    public Object getValue() {
        final List<SimpleConfigurationNode> values = this.values.get();
        synchronized (values) {
            final List<Object> ret = new ArrayList<>(values.size());
            for (SimpleConfigurationNode obj : values) {
                ret.add(obj.getValue());
            }
            return ret.isEmpty() ? null : ret;
        }
    }

    @Override
    public void setValue(Object value) {
        if (!(value instanceof Collection)) {
            value = Collections.singleton(value);
        }
        final Collection<?> valueList = (Collection<?>) value;
        final List<SimpleConfigurationNode> newValue = new ArrayList<>(valueList.size());
        int count = 0;
        for (Object o : valueList) {
            SimpleConfigurationNode child = holder.createNode(count);
            newValue.add(count, child);
            child.attached = true;
            child.setValue(o);
            ++count;
        }
        detachNodes(values.getAndSet(newValue));

    }

    @Override
    public SimpleConfigurationNode putChild(Object key, SimpleConfigurationNode value) {
        return putChild(key, value, false);
    }

    @Override
    SimpleConfigurationNode putChildIfAbsent(Object key, SimpleConfigurationNode value) {
        return putChild(key, value, true);
    }

    private SimpleConfigurationNode putChild(Object key, SimpleConfigurationNode value, boolean onlyIfAbsent) {
        SimpleConfigurationNode ret = null;
        List<SimpleConfigurationNode> values;
        do {
            values = this.values.get();
            synchronized (values) {
                final int index = (Integer) key;
                if (value == null) {
                    if (index < values.size()) {
                        ret = values.remove(index);
                        for (int i = index; i < values.size(); ++i) {
                            values.get(i).key = index;
                        }
                    }
                } else {
                    if (index >= 0 && index < values.size()) {
                        if (onlyIfAbsent) {
                            return values.get(index);
                        } else {
                            ret = values.set(index, value);
                        }
                    } else if (index == -1) { // Gotta correct the child path for the correct path name
                        values.add(value);
                        value.key = values.lastIndexOf(value);
                    } else {
                        values.add(index, value);
                    }
                }
            }
        } while (!this.values.compareAndSet(values, values));
        return ret;
    }


    @Override
    public SimpleConfigurationNode getChild(Object key) {
        final List<SimpleConfigurationNode> values = this.values.get();
        synchronized (values) {
            Integer value = Types.asInt(key);
            if (value == null || value < 0 || value >= values.size()) {
                return null;
            }
            return values.get(value);
        }
    }

    @Override
    public Iterable<SimpleConfigurationNode> iterateChildren() {
        List<SimpleConfigurationNode> values = this.values.get();
        synchronized (values) {
            return ImmutableList.copyOf(values);
        }
    }

    private void detachNodes(List<SimpleConfigurationNode> children) {
        synchronized (children) {
            for (SimpleConfigurationNode node : children) {
                node.attached = false;
                node.clear();
            }
        }
    }

    @Override
    public void clear() {
        List<SimpleConfigurationNode> oldValues = values.getAndSet(new ArrayList<SimpleConfigurationNode>());
        detachNodes(oldValues);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ListConfigValue that = (ListConfigValue) o;
        return Objects.equal(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(values);
    }
}
