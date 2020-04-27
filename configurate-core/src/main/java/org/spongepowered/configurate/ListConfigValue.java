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

import com.google.common.collect.ImmutableList;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.serialize.Scalars;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A {@link ConfigValue} which holds a list of values.
 */
class ListConfigValue<N extends ScopedConfigurationNode<N>, T extends AbstractConfigurationNode<N, T>> extends ConfigValue<N, T> {
    final AtomicReference<List<T>> values = new AtomicReference<>(new ArrayList<>());

    ListConfigValue(T holder) {
        super(holder);
    }

    ListConfigValue(T holder, Object startValue) {
        super(holder);

        T child = holder.createNode(0);
        child.attached = true;
        child.setValue(startValue);
        this.values.get().add(child);
    }

    @Nullable
    @Override
    public Object getValue() {
        final List<T> values = this.values.get();
        synchronized (values) {
            final List<Object> ret = new ArrayList<>(values.size());
            for (T obj : values) {
                ret.add(obj.getValue()); // unwrap
            }
            return ret;
        }
    }

    public List<N> getUnwrapped() {
        final List<T> orig = values.get();
        synchronized (orig) {
            ImmutableList.Builder<N> ret = ImmutableList.builderWithExpectedSize(orig.size());
            for (T element : orig) {
                ret.add(element.self());
            }
            return ret.build();
        }
    }

    @Override
    public void setValue(@Nullable Object value) {
        if (!(value instanceof Collection)) {
            value = Collections.singleton(value);
        }
        final Collection<?> valueAsList = (Collection<?>) value;
        final List<T> newValue = new ArrayList<>(valueAsList.size());

        int count = 0;
        for (Object o : valueAsList) {
            if (o == null) {
                continue;
            }

            T child = holder.createNode(count);
            newValue.add(count, child);
            child.attached = true;
            child.setValue(o);
            ++count;
        }
        detachNodes(values.getAndSet(newValue));
    }

    @Nullable
    @Override
    public T putChild(@NonNull Object key, @Nullable T value) {
        return putChild((int) key, value, false);
    }

    @Nullable
    @Override
    T putChildIfAbsent(@NonNull Object key, @Nullable T value) {
        return putChild((int) key, value, true);
    }

    private T putChild(int index, @Nullable T value, boolean onlyIfAbsent) {
        T ret = null;
        List<T> values;
        do {
            values = this.values.get();
            synchronized (values) {
                if (value == null) {
                    if (index < values.size()) {
                        // remove the value
                        ret = values.remove(index);
                        // update indexes for subsequent elements
                        for (int i = index; i < values.size(); ++i) {
                            values.get(i).key = index;
                        }
                    }
                } else {
                    // check if the index is in range
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
    public @Nullable T getChild(@Nullable Object key) {
        @Nullable Integer value = Scalars.INTEGER.tryDeserialize(key);
        if (value == null || value < 0) {
            return null;
        }

        final List<T> values = this.values.get();
        synchronized (values) {
            if (value >= values.size()) {
                return null;
            }
            return values.get(value);
        }
    }

    @NonNull
    @Override
    public Iterable<T> iterateChildren() {
        List<T> values = this.values.get();
        synchronized (values) {
            return ImmutableList.copyOf(values);
        }
    }

    @NonNull
    @Override
    ListConfigValue<N, T> copy(@NonNull T holder) {
        ListConfigValue<N, T> copy = new ListConfigValue<>(holder);
        List<T> copyValues;

        final List<T> values = this.values.get();
        synchronized (values) {
            copyValues = new ArrayList<>(values.size());
            for (T obj : values) {
                copyValues.add(obj.copy(holder)); // recursively copy
            }
        }

        copy.values.set(copyValues);
        return copy;
    }

    @Override
    boolean isEmpty() {
        return this.values.get().isEmpty();
    }

    private static void detachNodes(List<? extends AbstractConfigurationNode<?, ?>> children) {
        synchronized (children) {
            for (AbstractConfigurationNode<?, ?> node : children) {
                node.attached = false;
                node.clear();
            }
        }
    }

    @Override
    public void clear() {
        List<T> oldValues = values.getAndSet(new ArrayList<>());
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
        ListConfigValue<?, ?> that = (ListConfigValue<?, ?>) o;
        return Objects.equals(values.get(), that.values.get());
    }

    @Override
    public int hashCode() {
        return values.get().hashCode();
    }

    @Override
    public String toString() {
        return "ListConfigValue{values=" + this.values.get().toString() + '}';
    }
}
