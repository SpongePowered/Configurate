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
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import org.spongepowered.configurate.transformation.NodePath;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Simple implementation of {@link ConfigurationNode}.
 */
abstract class AbstractConfigurationNode<N extends ScopedConfigurationNode<N>, A extends AbstractConfigurationNode<N, A>> implements ScopedConfigurationNode<N>  {

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
    private A parent;

    /**
     * The current value of this node
     */
    @NonNull
    volatile ConfigValue<N, A> value;


    protected AbstractConfigurationNode(@Nullable Object key, @Nullable A parent, @NonNull ConfigurationOptions options) {
        requireNonNull(options, "options");
        this.key = key;
        this.options = options;
        this.parent = parent;
        this.value = new NullConfigValue<>(implSelf());

        // if the parent is null, this node is a root node, and is therefore "attached"
        if (parent == null) {
            attached = true;
        }
    }

    protected AbstractConfigurationNode(@Nullable A parent, A copyOf) {
        this.options = copyOf.getOptions();
        this.attached = true; // copies are always attached
        this.key = copyOf.key;
        this.parent = parent;
        this.value = copyOf.value.copy(implSelf());
    }

    /**
     * Handles the copying of applied defaults, if enabled.
     *
     * @param defValue the default value
     * @param <V> the value type
     * @return the same value
     */
    private <V> V storeDefault(V defValue) {
        if (defValue != null && getOptions().shouldCopyDefaults()) {
            setValue(defValue);
        }
        return defValue;
    }

    private <V> V storeDefault(TypeToken<V> type, V defValue) throws ObjectMappingException {
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
    public <V> List<V> getList(@NonNull Function<Object, V> transformer) {
        final ImmutableList.Builder<V> ret = ImmutableList.builder();
        ConfigValue<N, A> value = this.value;
        if (value instanceof ListConfigValue) {
            // transform each value individually if the node is a list
            for (A o : value.iterateChildren()) {
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
        return serial.deserialize(type, self());
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
        return serial.deserialize(type, self());
    }

    @NonNull
    @Override
    public N setValue(@Nullable Object newValue) {
        // if the value to be set is a configuration node already, unwrap and store the raw data
        if (newValue instanceof ConfigurationNode) {
            ConfigurationNode newValueAsNode = (ConfigurationNode) newValue;
            if (newValueAsNode == this) { // this would be a no-op whoop
                return self();
            }

            if (newValueAsNode.isList()) {
                // handle list
                attachIfNecessary();
                ListConfigValue<N, A> newList = new ListConfigValue<>(implSelf());
                synchronized (newValueAsNode) {
                    newList.setValue(newValueAsNode.getChildrenList());
                }
                this.value = newList;
                return self();

            } else if (newValueAsNode.isMap()) {
                // handle map
                attachIfNecessary();
                MapConfigValue<N, A> newMap = new MapConfigValue<>(implSelf());
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
            ConfigValue<N, A> oldValue, value;
            oldValue = value = this.value;

            if (onlyIfNull && !(oldValue instanceof NullConfigValue)){
                return;
            }

            // init new config value backing for the new value type if necessary
            if (newValue instanceof Collection) {
                if (!(value instanceof ListConfigValue)) {
                    value = new ListConfigValue<>(implSelf());
                }
            } else if (newValue instanceof Map) {
                if (!(value instanceof MapConfigValue)) {
                    value = new MapConfigValue<>(implSelf());
                }
            } else if (!(value instanceof ScalarConfigValue)) {
                value = new ScalarConfigValue<>(implSelf());
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
    public N mergeValuesFrom(@NonNull ConfigurationNode other) {
        if (other.isMap()) {
            ConfigValue<N, A> oldValue, newValue;
            synchronized (this) {
                oldValue = newValue = value;

                // ensure the current type is applicable.
                if (!(oldValue instanceof MapConfigValue)) {
                    if (oldValue instanceof NullConfigValue) {
                        newValue = new MapConfigValue<>(implSelf());
                    } else {
                        return self();
                    }
                }

                // merge values from 'other'
                for (Map.Entry<Object, ? extends ConfigurationNode> ent : other.getChildrenMap().entrySet()) {
                    A currentChild = newValue.getChild(ent.getKey());
                    // Never allow null values to overwrite non-null values
                    if ((currentChild != null && currentChild.getValue() != null) && ent.getValue().getValue() == null) {
                        continue;
                    }

                    // create a new child node for the value
                    A newChild = this.createNode(ent.getKey());
                    newChild.attached = true;
                    newChild.setValue(ent.getValue());
                    // replace the existing value, if absent
                    A existing = newValue.putChildIfAbsent(ent.getKey(), newChild);
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
    public N getNode(@NonNull Object... path) {
        A pointer = implSelf();
        for (Object el : path) {
            pointer = pointer.getChild(el, false);
        }
        return pointer.self();
    }

    @NonNull
    @Override
    public N getNode(@NonNull Iterable<Object> path) {
        A pointer = implSelf();
        for (Object el : path) {
            pointer = pointer.getChild(el, false);
        }
        return pointer.self();
    }

    @Override
    public boolean isVirtual() {
        return !attached;
    }

    @Override
    public boolean isList() {
        return this.value instanceof ListConfigValue;
    }

    @Override
    public boolean isMap() {
        return this.value instanceof MapConfigValue;
    }

    @NonNull
    @Override
    public List<N> getChildrenList() {
        ConfigValue<N, A> value = this.value;
        return value instanceof ListConfigValue ? ((ListConfigValue<N, A>) value).getUnwrapped() : Collections.emptyList();
    }

    @NonNull
    @Override
    public Map<Object, N> getChildrenMap() {
        ConfigValue<N, A> value = this.value;
        return value instanceof MapConfigValue ? ((MapConfigValue<N, A>) value).getUnwrapped() : Collections.emptyMap();
    }

    @Override
    public boolean isEmpty() {
        return this.value.isEmpty();
    }

    /**
     * Gets a child node, relative to this.
     *
     * @param key The key
     * @param attach If the resultant node should be automatically attached
     * @return The child node
     */
    protected A getChild(Object key, boolean attach) {
        A child = value.getChild(key);

        // child doesn't currently exist
        if (child == null) {
            if (attach) {
                // attach ourselves first
                attachIfNecessary();
                // insert the child node into the value
                A existingChild = value.putChildIfAbsent(key, (child = createNode(key)));
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

    private static <N extends ScopedConfigurationNode<N>, T extends AbstractConfigurationNode<N, T>> T detachIfNonNull(T node) {
        if (node != null) {
            node.attached = false;
            node.clear();
        }
        return node;
    }

    @NonNull
    @Override
    public N appendListNode() {
        // the appended node can have a key of -1
        // the "real" key will be determined when the node is inserted into a list config value
        return getChild(-1, false).self();
    }

    @Nullable
    @Override
    public Object getKey() {
        return this.key;
    }

    @NonNull
    @Override
    public NodePath getPath() {
        N pointer = self();
        if (pointer.getParent() == null) {
            return NodePath.create(new Object[] {getKey()});
        }

        LinkedList<Object> pathElements = new LinkedList<>();
        do {
            pathElements.addFirst(pointer.getKey());
        } while ((pointer = pointer.getParent()).getParent() != null);
        return NodePath.create(pathElements);
    }

    @Nullable
    public N getParent() {
        A parent = this.parent;
        return parent == null ? null : parent.self();
    }

    @NonNull
    @Override
    public ConfigurationOptions getOptions() {
        return this.options;
    }

    @NonNull
    @Override
    public N copy() {
        return copy(null).self();
    }


    /**
     * The same as {@link #getParent()} - but ensuring that 'parent' is attached via
     * {@link #attachChildIfAbsent(A)}.
     *
     * @return The parent
     */
    A getParentEnsureAttached() {
        A parent = this.parent;
        if (parent != null && parent.isVirtual()) {
            parent = parent.getParentEnsureAttached().attachChildIfAbsent(parent);

        }
        return this.parent = parent;
    }

    protected void attachIfNecessary() {
        if (!attached) {
            getParentEnsureAttached().attachChild(implSelf());
        }
    }


    protected final A attachChildIfAbsent(A child) {
        return attachChild(child, true);
    }

    void attachChild(A child) {
        attachChild(child, false);
    }

    /**
     * Attaches a child to this node
     *
     * @param child The child
     * @return The resultant value
     */
    private A attachChild(A child, boolean onlyIfAbsent) {
        // ensure this node is attached
        if (isVirtual()) {
            throw new IllegalStateException("This parent is not currently attached. This is an internal state violation.");
        }

        // ensure the child actually is a child
        if (!child.getParentEnsureAttached().equals(this)) {
            throw new IllegalStateException("Child " +  child + " path is not a direct parent of me (" + this + "), cannot attach");
        }

        // update the value
        ConfigValue<N, A> oldValue, newValue;
        synchronized (this) {
            newValue = oldValue = this.value;

            // if the existing value isn't a map, we need to update it's type
            if (!(oldValue instanceof MapConfigValue)) {
                if (child.key instanceof Integer) {
                    // if child.key is an integer, we can infer that the type of this node should be a list
                    if (oldValue instanceof NullConfigValue) {
                        // if the oldValue was null, we can just replace it with an empty list
                        newValue = new ListConfigValue<>(implSelf());
                    } else if (!(oldValue instanceof ListConfigValue)) {
                        // if the oldValue contained a value, we add it as the first element of the
                        // new list
                        newValue = new ListConfigValue<>(implSelf(), oldValue.getValue());
                    }
                } else {
                    // if child.key isn't an integer, assume map
                    newValue = new MapConfigValue<>(implSelf());
                }
            }

            /// now the value has been updated to an appropriate type, we can insert the value
            if (onlyIfAbsent) {
                A oldChild = newValue.putChildIfAbsent(child.key, child);
                if (oldChild != null) {
                    return oldChild;
                }
            } else {
                detachIfNonNull(newValue.putChild(child.key, child));
            }
            this.value = newValue;
        }

        if (newValue != oldValue) {
            oldValue.clear();
        }
        child.attached = true;
        return child;
    }

    protected void clear() {
        synchronized (this) {
            ConfigValue<N, A> oldValue = this.value;
            value = new NullConfigValue<>(implSelf());
            oldValue.clear();
        }
    }

    @Override
    public <S, T, E extends Exception> T visit(ConfigurationVisitor<? super N, S, T, E> visitor, S state) throws E {
        return visitInternal(visitor, state);
    }

    @Override
    public <S, T> T visit(ConfigurationVisitor.Safe<? super N, S, T> visitor, S state) {
        try {
            return visitInternal(visitor, state);
        } catch (VisitorSafeNoopException e) {
            throw new Error("Exception was thrown on a Safe visitor");
        }
    }

    private <S, T, E extends Exception> T visitInternal(ConfigurationVisitor<? super N, S, T, E> visitor, S state) throws E {
        visitor.beginVisit(self(), state);
        if (!(this.value instanceof NullConfigValue)) { // only visit if we have an actual value
            LinkedList<Object> toVisit = new LinkedList<>();
            toVisit.add(this);

            @Nullable Object active;
            while ((active = toVisit.pollFirst()) != null) {
                // try to pop a node from the stack, or handle the node exit if applicable
                @Nullable A current = VisitorNodeEnd.popFromVisitor(active, visitor, state);
                if (current == null) {
                    continue;
                }

                visitor.enterNode(current.self(), state);
                ConfigValue<N, A> value = current.value;
                if (value instanceof MapConfigValue) {
                    visitor.enterMappingNode(current.self(), state);
                    toVisit.addFirst(new VisitorNodeEnd(current));
                    toVisit.addAll(0, ((MapConfigValue<N, A>) value).values.values());
                } else if (value instanceof ListConfigValue) {
                    visitor.enterListNode(current.self(), state);
                    toVisit.addFirst(new VisitorNodeEnd(current));
                    toVisit.addAll(0, ((ListConfigValue<N, A>) value).values.get());
                } else if (value instanceof ScalarConfigValue) {
                    visitor.enterScalarNode(current.self(), state);
                } else {
                    throw new IllegalStateException("Unknown value type " + value.getClass());
                }
            }
        }
        return visitor.endVisit(state);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractConfigurationNode)) return false;
        AbstractConfigurationNode<?, ?> that = (AbstractConfigurationNode<?, ?>) o;

        return Objects.equals(this.key, that.key) && Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key) ^ Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return "AbstractConfigurationNode{key=" + key + ", value=" + value + '}';
    }

    // Methods to be implemented for type-safety

    @NonNull
    protected abstract A copy(@Nullable A parent);
    protected abstract A createNode(Object path);
    protected abstract A implSelf();

}
