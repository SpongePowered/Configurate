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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.serialize.Scalars;
import org.spongepowered.configurate.util.UnmodifiableCollections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A {@link ConfigValue} which holds a list of values.
 */
final class ListConfigValue<N extends ScopedConfigurationNode<N>, T extends AbstractConfigurationNode<N, T>> extends ConfigValue<N, T> {

    final AtomicReference<List<T>> values = new AtomicReference<>(new ArrayList<>());

    ListConfigValue(final T holder) {
        super(holder);
    }

    ListConfigValue(final T holder, final @Nullable Object startValue) {
        super(holder);
        if (startValue != null) {
            final T child = holder.createNode(0);
            child.attached = true;
            child.raw(startValue);
            this.values.get().add(child);
        }
    }

    @Nullable
    @Override
    public Object get() {
        final List<T> values = this.values.get();
        synchronized (values) {
            final List<Object> ret = new ArrayList<>(values.size());
            for (T obj : values) {
                ret.add(obj.raw()); // unwrap
            }
            return ret;
        }
    }

    public List<N> unwrapped() {
        final List<T> orig = this.values.get();
        synchronized (orig) {
            final List<N> ret = new ArrayList<>(orig.size());
            for (T element : orig) {
                ret.add(element.self());
            }
            return Collections.unmodifiableList(ret);
        }
    }

    @Override
    public void set(@Nullable Object value) {
        if (!(value instanceof Collection)) {
            value = Collections.singleton(value);
        }
        final Collection<@Nullable ?> valueAsList = (Collection<@Nullable ?>) value;
        final List<T> newValue = new ArrayList<>(valueAsList.size());

        int count = 0;
        for (@Nullable Object o : valueAsList) {
            if (o == null) {
                continue;
            }

            final T child = this.holder.createNode(count);
            newValue.add(count, child);
            child.attached = true;
            child.raw(o);
            ++count;
        }
        detachNodes(this.values.getAndSet(newValue));
    }

    @Override
    public @Nullable T putChild(final @NonNull Object key, final @Nullable T value) {
        return putChildInternal((int) key, value, false);
    }

    @Override
    @Nullable T putChildIfAbsent(final @NonNull Object key, final @Nullable T value) {
        return putChildInternal((int) key, value, true);
    }

    private T putChildInternal(final int index, final @Nullable T value, final boolean onlyIfAbsent) {
        T ret = null;
        List<T> values;
        do {
            values = this.values.get();
            synchronized (values) {
                if (value == null) {
                    // only remove actually existing values
                    if (index >= 0 && index < values.size()) {
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
    public @Nullable T child(final @Nullable Object key) {
        final @Nullable Integer value = Scalars.INTEGER.tryDeserialize(key);
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
        final List<T> values = this.values.get();
        synchronized (values) {
            return UnmodifiableCollections.copyOf(values);
        }
    }

    @NonNull
    @Override
    ListConfigValue<N, T> copy(final @NonNull T holder) {
        final ListConfigValue<N, T> copy = new ListConfigValue<>(holder);
        final List<T> copyValues;

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

    private static void detachNodes(final List<? extends AbstractConfigurationNode<?, ?>> children) {
        synchronized (children) {
            for (AbstractConfigurationNode<?, ?> node : children) {
                node.attached = false;
                node.clear();
            }
        }
    }

    @Override
    public void clear() {
        final List<T> oldValues = this.values.getAndSet(new ArrayList<>());
        detachNodes(oldValues);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ListConfigValue<?, ?>)) {
            return false;
        }
        final ListConfigValue<?, ?> that = (ListConfigValue<?, ?>) other;
        return Objects.equals(this.values.get(), that.values.get());
    }

    @Override
    public int hashCode() {
        return this.values.get().hashCode();
    }

    @Override
    public String toString() {
        return "ListConfigValue{values=" + this.values.get().toString() + '}';
    }

}
