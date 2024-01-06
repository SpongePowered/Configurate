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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.serialize.Scalars;
import org.spongepowered.configurate.util.UnmodifiableCollections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * A {@link ConfigValue} which holds a list of values.
 */
final class ListConfigValue<N extends ScopedConfigurationNode<N>, A extends AbstractConfigurationNode<N, A>> implements ConfigValue<N, A> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    static final AtomicReferenceFieldUpdater<ListConfigValue, List> VALUES_HANDLE =
        AtomicReferenceFieldUpdater.newUpdater(ListConfigValue.class, List.class, "values");

    /**
     * A specific key for nodes who are destined to be part of a list.
     *
     * <p>This key will only exist on {@link ConfigurationNode#virtual()} nodes, since once a
     * node is attached an actual list index will be allocated.</p>
     */
    static final Object UNALLOCATED_IDX = new Object() {
        @Override
        public String toString() {
            return "<list unallocated>";
        }
    };

    /**
     * Return whether a key is likely to create a new list.
     *
     * @param key key to check
     * @return if the key is likely to create a new list.
     */
    static boolean likelyNewListKey(final @Nullable Object key) {
        return (key instanceof Integer && ((Integer) key).intValue() == 0) || key == UNALLOCATED_IDX;
    }

    /**
     * Return whether a key is likely to be an index into a list.
     *
     * @param configValue the list to check
     * @param key         key to check
     * @return if the key is likely to be  an index into a list.
     */
    static boolean likelyListKey(final @Nullable ConfigValue<?, ?> configValue, final @Nullable Object key) {
        if (!(configValue instanceof ListConfigValue<?, ?>)) {
            return false;
        }
        if (!(key instanceof Integer)) {
            return false;
        }
        final ListConfigValue<?, ?> listConfigValue = (ListConfigValue<?, ?>) configValue;
        final int keyAsInt = (Integer) key;
        return keyAsInt >= 0 && keyAsInt <= listConfigValue.values.size();
    }

    private final A holder;
    volatile List<A> values = new ArrayList<>();

    ListConfigValue(final A holder) {
        this.holder = holder;
    }

    ListConfigValue(final A holder, final @Nullable Object startValue) {
        this.holder = holder;
        if (startValue != null) {
            final A child = holder.createNode(0);
            child.attached = true;
            child.raw(startValue);
            this.values.add(child);
        }
    }

    @Override
    public Object get() {
        final List<A> values = this.values;
        synchronized (values) {
            final List<Object> ret = new ArrayList<>(values.size());
            for (A obj : values) {
                ret.add(obj.raw()); // unwrap
            }
            return ret;
        }
    }

    public List<N> unwrapped() {
        final List<A> orig = this.values;
        synchronized (orig) {
            final List<N> ret = new ArrayList<>(orig.size());
            for (A element : orig) {
                ret.add(element.self());
            }
            return Collections.unmodifiableList(ret);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void set(@Nullable Object value) {
        if (!(value instanceof Collection)) {
            value = Collections.singleton(value);
        }
        final Collection<@Nullable ?> valueAsList = (Collection<@Nullable ?>) value;
        final List<A> newValue = new ArrayList<>(valueAsList.size());

        int count = 0;
        for (@Nullable Object o : valueAsList) {
            if (o == null) {
                continue;
            }

            final A child = this.holder.createNode(count);
            newValue.add(count, child);
            child.attached = true;
            child.raw(o);
            ++count;
        }
        detachNodes(VALUES_HANDLE.getAndSet(this, newValue));
    }

    @Override
    public @Nullable A putChild(final Object key, final @Nullable A value) {
        return putChildInternal(key, value, false);
    }

    @Override
    public @Nullable A putChildIfAbsent(final Object key, final @Nullable A value) {
        return putChildInternal(key, value, true);
    }

    private @Nullable A putChildInternal(final Object index, final @Nullable A value, final boolean onlyIfAbsent) {
        if (index == UNALLOCATED_IDX) {
            if (value != null) { // can't remove an unallocated node
                List<A> values;
                do {
                    // Allocate an index for the newly added node
                    values = this.values;
                    values.add(value);
                    value.key = values.lastIndexOf(value);
                } while (!VALUES_HANDLE.compareAndSet(this, values, values));
            }
            return null;
        } else {
            return putChildInternal((int) index, value, onlyIfAbsent);
        }
    }

    private @Nullable A putChildInternal(final int index, final @Nullable A value, final boolean onlyIfAbsent) {
        @Nullable A ret = null;
        List<A> values;
        do {
            values = this.values;
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
                    } else {
                        values.add(index, value);
                    }
                }
            }
        } while (!VALUES_HANDLE.compareAndSet(this, values, values));
        return ret;
    }

    @Override
    public @Nullable A child(final @Nullable Object key) {
        final @Nullable Integer value = Scalars.INTEGER.tryDeserialize(key);
        if (value == null || value < 0) {
            return null;
        }

        final List<A> values = this.values;
        synchronized (values) {
            if (value >= values.size()) {
                return null;
            }
            return values.get(value);
        }
    }

    @Override
    public Iterable<A> iterateChildren() {
        final List<A> values = this.values;
        synchronized (values) {
            return UnmodifiableCollections.copyOf(values);
        }
    }

    @Override
    public ListConfigValue<N, A> copy(final A holder) {
        final ListConfigValue<N, A> copy = new ListConfigValue<>(holder);
        final List<A> copyValues;

        final List<A> values = this.values;
        synchronized (values) {
            copyValues = new ArrayList<>(values.size());
            for (A obj : values) {
                copyValues.add(obj.copy(holder)); // recursively copy
            }
        }

        copy.values = copyValues;
        return copy;
    }

    @Override
    public boolean isEmpty() {
        return this.values.isEmpty();
    }

    private void detachNodes(final List<? extends AbstractConfigurationNode<?, ?>> children) {
        synchronized (children) {
            for (AbstractConfigurationNode<?, ?> node : children) {
                node.attached = false;
                if (Objects.equals(node.parent(), this.holder)) {
                    node.clear();
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void clear() {
        final List<A> oldValues = VALUES_HANDLE.getAndSet(this, new ArrayList<>());
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
        return Objects.equals(this.values, that.values);
    }

    @Override
    public int hashCode() {
        return this.values.hashCode();
    }

    @Override
    public String toString() {
        return "ListConfigValue{values=" + this.values.toString() + '}';
    }

}
