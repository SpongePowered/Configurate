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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.configurate.objectmapping.serialize.TypeSerializer;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Simple implementation of {@link ConfigurationNode}.
 */
public abstract class AbstractConfigurationNode<T extends AbstractConfigurationNode<T>> implements ConfigurationNode<T> {

    /**
     * The options determining the behaviour of this node
     */
    @NonNull
    private final ConfigurationOptions options;

    /**
     * If this node is attached to a wider configuration structure
     */
    volatile boolean attached;

    /**
     * Path of this node.
     *
     * Internally, may only be modified when an operation that adds or removes a node at the same
     * or higher level in the node tree
     */
    @Nullable
    volatile Object key;

    /**
     * The parent of this node
     */
    @Nullable
    private T parent;

    /**
     * The current value of this node
     */
    @NonNull
    volatile ConfigValue<T> value;


    protected AbstractConfigurationNode(@Nullable Object key, @Nullable T parent, @NonNull ConfigurationOptions options) {
        Preconditions.checkNotNull(options, "options");
        this.key = key;
        this.options = options;
        this.parent = parent;
        this.value = new NullConfigValue<>(self());

        // if the parent is null, this node is a root node, and is therefore "attached"
        if (parent == null) {
            attached = true;
        }
    }

    protected AbstractConfigurationNode(T parent, T copyOf) {
        this.options = copyOf.getOptions();
        this.attached = true; // copies are always attached
        this.key = copyOf.key;
        this.parent = parent;
        this.value = copyOf.value.copy(self());
    }

    /**
     * Handles the copying of applied defaults, if enabled.
     *
     * @param defValue the default value
     * @param <T> the value type
     * @return the same value
     */
    private <T> T storeDefault(T defValue) {
        if (defValue != null && getOptions().shouldCopyDefaults()) {
            setValue(defValue);
        }
        return defValue;
    }

    private <T> T storeDefault(TypeToken<T> type, T defValue) throws ObjectMappingException {
        if (defValue != null && getOptions().shouldCopyDefaults()) {
            setValue(type, defValue);
        }
        return defValue;
    }

    @Override
    public Object getValue(Object def) {
        Object ret = value.getValue();
        return ret == null ? storeDefault(def) : ret;
    }

    @Override
    public Object getValue(@NonNull Supplier<Object> defSupplier) {
        Object ret = value.getValue();
        return ret == null ? storeDefault(defSupplier.get()) : ret;
    }

    @Override
    public <V> V getValue(@NonNull Function<Object, V> transformer, V def) {
        V ret = transformer.apply(getValue());
        return ret == null ? storeDefault(def) : ret;
    }

    @Override
    public <V> V getValue(@NonNull Function<Object, V> transformer, @NonNull Supplier<V> defSupplier) {
        V ret = transformer.apply(getValue());
        return ret == null ? storeDefault(defSupplier.get()) : ret;
    }

    @NonNull
    @Override
    public <V> List<V> getList(Function<Object, V> transformer) {
        final ImmutableList.Builder<V> ret = ImmutableList.builder();
        ConfigValue<T> value = this.value;
        if (value instanceof ListConfigValue) {
            // transform each value individually if the node is a list
            for (T o : value.iterateChildren()) {
                V transformed = transformer.apply(o.getValue());
                if (transformed != null) {
                    ret.add(transformed);
                }
            }
        } else {
            // transfer the value as a whole
            V transformed = transformer.apply(value.getValue());
            if (transformed != null) {
                ret.add(transformed);
            }
        }

        return ret.build();
    }

    @Override
    public <V> List<V> getList(@NonNull Function<Object, V> transformer, List<V> def) {
        List<V> ret = getList(transformer);
        return ret.isEmpty() ? storeDefault(def) : ret;
    }

    @Override
    public <V> List<V> getList(@NonNull Function<Object, V> transformer, @NonNull Supplier<List<V>> defSupplier) {
        List<V> ret = getList(transformer);
        return ret.isEmpty() ? storeDefault(defSupplier.get()) : ret;
    }

    @Override
    public <V> List<V> getList(@NonNull TypeToken<V> type, List<V> def) throws ObjectMappingException {
        List<V> ret = getValue(new TypeToken<List<V>>() {}
                .where(new TypeParameter<V>() {}, type), def);
        return ret.isEmpty() ? storeDefault(def) : ret;
    }

    @Override
    public <V> List<V> getList(@NonNull TypeToken<V> type, @NonNull Supplier<List<V>> defSupplier) throws ObjectMappingException {
        List<V> ret = getValue(new TypeToken<List<V>>(){}.where(new TypeParameter<V>(){}, type), defSupplier);
        return ret.isEmpty() ? storeDefault(defSupplier.get()) : ret;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V getValue(@NonNull TypeToken<V> type, V def) throws ObjectMappingException {
        Object value = getValue();
        if (value == null) {
            return storeDefault(type, def);
        }

        TypeSerializer<V> serial = getOptions().getSerializers().get(type);
        if (serial == null) {
            if (type.getRawType().isInstance(value)) {
                return (V) type.getRawType().cast(value);
            } else {
                return storeDefault(type, def);
            }
        }
        return serial.deserialize(type, this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V getValue(@NonNull TypeToken<V> type, @NonNull Supplier<V> defSupplier) throws ObjectMappingException {
        Object value = getValue();
        if (value == null) {
            return storeDefault(type, defSupplier.get());
        }

        TypeSerializer<V> serial = getOptions().getSerializers().get(type);
        if (serial == null) {
            if (type.getRawType().isInstance(value)) {
                return (V) type.getRawType().cast(value);
            } else {
                return storeDefault(type, defSupplier.get());
            }
        }
        return serial.deserialize(type, this);
    }

    @NonNull
    @Override
    public T setValue(@Nullable Object newValue) {
        // if the value to be set is a configuration node already, unwrap and store the raw data
        if (newValue instanceof ConfigurationNode) {
            ConfigurationNode<?> newValueAsNode = (ConfigurationNode<?>) newValue;

            if (newValueAsNode.hasListChildren()) {
                // handle list
                attachIfNecessary();
                ListConfigValue<T> newList = new ListConfigValue<>(self());
                synchronized (newValueAsNode) {
                    newList.setValue(newValueAsNode.getChildrenList());
                }
                this.value = newList;
                return self();

            } else if (newValueAsNode.hasMapChildren()) {
                // handle map
                attachIfNecessary();
                MapConfigValue<T> newMap = new MapConfigValue<>(self());
                synchronized (newValueAsNode) {
                    newMap.setValue(newValueAsNode.getChildrenMap());
                }
                this.value = newMap;
                return self();

            } else {
                // handle scalar/null
                newValue = newValueAsNode.getValue();
            }
        }

        // if the new value is null, handle detaching from this nodes parent
        if (newValue == null) {
            if (parent == null) {
                clear();
            } else {
                parent.removeChild(key);
            }
            return self();
        }

        insertNewValue(newValue, false);
        return self();
    }

    /**
     * Handles the process of setting a new value for this node.
     *
     * @param newValue The new value
     * @param onlyIfNull If the insertion should only take place if the current value is null
     */
    private void insertNewValue(Object newValue, boolean onlyIfNull) {
        attachIfNecessary();

        synchronized (this) {
            ConfigValue<T> oldValue, value;
            oldValue = value = this.value;

            if (onlyIfNull && !(oldValue instanceof NullConfigValue)){
                return;
            }

            // init new config value backing for the new value type if necessary
            if (newValue instanceof Collection) {
                if (!(value instanceof ListConfigValue)) {
                    value = new ListConfigValue<>(self());
                }
            } else if (newValue instanceof Map) {
                if (!(value instanceof MapConfigValue)) {
                    value = new MapConfigValue<>(self());
                }
            } else if (!(value instanceof ScalarConfigValue)) {
                value = new ScalarConfigValue<>(self());
            }

            // insert the data into the config value
            value.setValue(newValue);

            /*if (oldValue != null && oldValue != value) {
                oldValue.clear();
            }*/
            this.value = value;
        }
    }

    @NonNull
    @Override
    public T mergeValuesFrom(@NonNull ConfigurationNode<?> other) {
        if (other.hasMapChildren()) {
            ConfigValue<T> oldValue, newValue;
            synchronized (this) {
                oldValue = newValue = value;

                // ensure the current type is applicable.
                if (!(oldValue instanceof MapConfigValue)) {
                    if (oldValue instanceof NullConfigValue) {
                        newValue = new MapConfigValue<>(self());
                    } else {
                        return self();
                    }
                }

                // merge values from 'other'
                for (Map.Entry<Object, ? extends ConfigurationNode<?>> ent : other.getChildrenMap().entrySet()) {
                    T currentChild = newValue.getChild(ent.getKey());
                    // Never allow null values to overwrite non-null values
                    if ((currentChild != null && currentChild.getValue() != null) && ent.getValue().getValue() == null) {
                        continue;
                    }

                    // create a new child node for the value
                    T newChild = this.createNode(ent.getKey());
                    newChild.attached = true;
                    newChild.setValue(ent.getValue());
                    // replace the existing value, if absent
                    T existing = newValue.putChildIfAbsent(ent.getKey(), newChild);
                    // if an existing value was present, attempt to merge the new value into it
                    if (existing != null) {
                        existing.mergeValuesFrom(newChild);
                    }
                }
                this.value = newValue;
            }
        } else if (other.getValue() != null) {
            // otherwise, replace the value of this node, only if currently null
            insertNewValue(other.getValue(), true);
        }
        return self();
    }

    @NonNull
    @Override
    public T getNode(@NonNull Object... path) {
        T pointer = self();
        for (Object el : path) {
            pointer = pointer.getChild(el, false);
        }
        return pointer;
    }

    @Override
    public boolean isVirtual() {
        return !attached;
    }

    @NonNull
    @Override
    public ValueType getValueType() {
        return this.value.getType();
    }

    @NonNull
    @Override
    public List<T> getChildrenList() {
        ConfigValue<T> value = this.value;
        return value instanceof ListConfigValue ? ImmutableList.copyOf(((ListConfigValue<T>) value).values.get()) : Collections.emptyList();
    }

    @NonNull
    @Override
    public Map<Object, T> getChildrenMap() {
        ConfigValue<T> value = this.value;
        return value instanceof MapConfigValue ? ImmutableMap.copyOf(((MapConfigValue<T>) value).values) : Collections.emptyMap();
    }

    /**
     * Gets a child node, relative to this.
     *
     * @param key The key
     * @param attach If the resultant node should be automatically attached
     * @return The child node
     */
    protected T getChild(Object key, boolean attach) {
        T child = value.getChild(key);

        // child doesn't currently exist
        if (child == null) {
            if (attach) {
                // attach ourselves first
                attachIfNecessary();
                // insert the child node into the value
                T existingChild = value.putChildIfAbsent(key, (child = createNode(key)));
                if (existingChild != null) {
                    child = existingChild;
                } else {
                    attachChild(child);
                }
            } else {
                // just create a new virtual (detached) node
                child = createNode(key);
            }
        }

        return child;
    }

    @Override
    public boolean removeChild(@NonNull Object key) {
        return detachIfNonNull(value.putChild(key, null)) != null;
    }

    private static <T extends AbstractConfigurationNode<T>> T detachIfNonNull(T node) {
        if (node != null) {
            node.attached = false;
            node.clear();
        }
        return node;
    }

    @NonNull
    @Override
    public T getAppendedNode() {
        // the appended node can have a key of -1
        // the "real" key will be determined when the node is inserted into a list config value
        return getChild(-1, false);
    }

    @Nullable
    @Override
    public Object getKey() {
        return this.key;
    }

    @NonNull
    @Override
    public Object[] getPath() {
        LinkedList<Object> pathElements = new LinkedList<>();
        T pointer = self();
        if (pointer.getParent() == null) {
            return new Object[]{this.getKey()};
        }

        do {
            pathElements.addFirst(pointer.getKey());
        } while ((pointer = pointer.getParent()).getParent() != null);
        return pathElements.toArray();
    }

    @Nullable
    public T getParent() {
        return this.parent;
    }

    @NonNull
    @Override
    public ConfigurationOptions getOptions() {
        return this.options;
    }

    @NonNull
    @Override
    public T copy() {
        return copy(null);
    }


    /**
     * The same as {@link #getParent()} - but ensuring that 'parent' is attached via
     * {@link #attachChildIfAbsent(T)}.
     *
     * @return The parent
     */
    T getParentEnsureAttached() {
        T parent = this.parent;
        if (parent != null && parent.isVirtual()) {
            parent = parent.getParentEnsureAttached().attachChildIfAbsent(parent);

        }
        return this.parent = parent;
    }

    protected void attachIfNecessary() {
        if (!attached) {
            getParentEnsureAttached().attachChild(self());
        }
    }


    protected final T attachChildIfAbsent(T child) {
        return attachChild(child, true);
    }

    void attachChild(T child) {
        attachChild(child, false);
    }

    /**
     * Attaches a child to this node
     *
     * @param child The child
     * @return The resultant value
     */
    private T attachChild(T child, boolean onlyIfAbsent) {
        // ensure this node is attached
        if (isVirtual()) {
            throw new IllegalStateException("This parent is not currently attached. This is an internal state violation.");
        }

        // ensure the child actually is a child
        if (!child.getParentEnsureAttached().equals(this)) {
            throw new IllegalStateException("Child " +  child + " path is not a direct parent of me (" + this + "), cannot attach");
        }

        // update the value
        ConfigValue<T> oldValue, newValue;
        synchronized (this) {
            newValue = oldValue = this.value;

            // if the existing value isn't a map, we need to update it's type
            if (!(oldValue instanceof MapConfigValue)) {
                if (child.key instanceof Integer) {
                    // if child.key is an integer, we can infer that the type of this node should be a list
                    if (oldValue instanceof NullConfigValue) {
                        // if the oldValue was null, we can just replace it with an empty list
                        newValue = new ListConfigValue<>(self());
                    } else if (!(oldValue instanceof ListConfigValue)) {
                        // if the oldValue contained a value, we add it as the first element of the
                        // new list
                        newValue = new ListConfigValue<>(self(), oldValue.getValue());
                    }
                } else {
                    // if child.key isn't an integer, assume map
                    newValue = new MapConfigValue<>(self());
                }
            }

            /// now the value has been updated to an appropriate type, we can insert the value
            if (onlyIfAbsent) {
                T oldChild = newValue.putChildIfAbsent(child.key, child);
                if (oldChild != null) {
                    return oldChild;
                }
                this.value = newValue;
            } else {
                detachIfNonNull(newValue.putChild(child.key, child));
                value = newValue;
            }
        }

        if (newValue != oldValue) {
            oldValue.clear();
        }
        child.attached = true;
        return child;
    }

    protected void clear() {
        synchronized (this) {
            ConfigValue<T> oldValue = this.value;
            value = new NullConfigValue<>(self());
            oldValue.clear();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractConfigurationNode)) return false;
        AbstractConfigurationNode<?> that = (AbstractConfigurationNode<?>) o;

        return Objects.equals(this.key, that.key) && Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key) ^ Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return "SimpleConfigurationNode{key=" + key + ", value=" + value + '}';
    }

    // Methods to be implemented for type-safety

    @NonNull
    protected abstract T copy(@Nullable T parent);
    protected abstract T createNode(Object path);
}
