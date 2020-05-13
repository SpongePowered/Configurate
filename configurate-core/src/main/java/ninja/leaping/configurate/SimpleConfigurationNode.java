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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

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
public class SimpleConfigurationNode implements ConfigurationNode {

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
    private SimpleConfigurationNode parent;

    /**
     * The current value of this node
     */
    @NonNull
    private volatile ConfigValue value;

    /**
     * Create a new node with no parent and {@link ConfigurationOptions#defaults() default} options
     *
     * @return The newly created node
     * @deprecated Use {@link ConfigurationNode#root()} instead
     */
    @Deprecated
    @NonNull
    public static SimpleConfigurationNode root() {
        return root(ConfigurationOptions.defaults());
    }

    /**
     * Create a new node with no parent and defined options
     *
     * @param options The options to use in this node.
     * @return The newly created node
     * @deprecated Use {@link ConfigurationNode#root(ConfigurationOptions)} instead
     */
    @Deprecated
    @NonNull
    public static SimpleConfigurationNode root(@NonNull ConfigurationOptions options) {
        return new SimpleConfigurationNode(null, null, options);
    }

    protected SimpleConfigurationNode(@Nullable Object key, @Nullable SimpleConfigurationNode parent, @NonNull ConfigurationOptions options) {
        requireNonNull(options, "options");
        this.key = key;
        this.options = options;
        this.parent = parent;
        this.value = NullConfigValue.instance();

        // if the parent is null, this node is a root node, and is therefore "attached"
        if (parent == null) {
            attached = true;
        }
    }

    protected SimpleConfigurationNode(@Nullable  SimpleConfigurationNode parent, SimpleConfigurationNode copyOf) {
        this.options = copyOf.options;
        this.attached = true; // copies are always attached
        this.key = copyOf.key;
        this.parent = parent;
        this.value = copyOf.value.copy(this);
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
    public <T> T getValue(@NonNull Function<Object, T> transformer, T def) {
        T ret = transformer.apply(getValue());
        return ret == null ? storeDefault(def) : ret;
    }

    @Override
    public <T> T getValue(@NonNull Function<Object, T> transformer, @NonNull Supplier<T> defSupplier) {
        T ret = transformer.apply(getValue());
        return ret == null ? storeDefault(defSupplier.get()) : ret;
    }

    @NonNull
    @Override
    public <T> List<T> getList(@NonNull Function<Object, T> transformer) {
        final ImmutableList.Builder<T> ret = ImmutableList.builder();
        ConfigValue value = this.value;
        if (value instanceof ListConfigValue) {
            // transform each value individually if the node is a list
            for (SimpleConfigurationNode o : value.iterateChildren()) {
                T transformed = transformer.apply(o.getValue());
                if (transformed != null) {
                    ret.add(transformed);
                }
            }
        } else {
            // transfer the value as a whole
            T transformed = transformer.apply(value.getValue());
            if (transformed != null) {
                ret.add(transformed);
            }
        }

        return ret.build();
    }

    @Override
    public <T> List<T> getList(@NonNull Function<Object, T> transformer, List<T> def) {
        List<T> ret = getList(transformer);
        return ret.isEmpty() ? storeDefault(def) : ret;
    }

    @Override
    public <T> List<T> getList(@NonNull Function<Object, T> transformer, @NonNull Supplier<List<T>> defSupplier) {
        List<T> ret = getList(transformer);
        return ret.isEmpty() ? storeDefault(defSupplier.get()) : ret;
    }

    @Override
    public <T> List<T> getList(@NonNull TypeToken<T> type, List<T> def) throws ObjectMappingException {
        List<T> ret = getValue(new TypeToken<List<T>>() {}
                .where(new TypeParameter<T>() {}, type), def);
        return ret.isEmpty() ? storeDefault(def) : ret;
    }

    @Override
    public <T> List<T> getList(@NonNull TypeToken<T> type, @NonNull Supplier<List<T>> defSupplier) throws ObjectMappingException {
        List<T> ret = getValue(new TypeToken<List<T>>(){}.where(new TypeParameter<T>(){}, type), defSupplier);
        return ret.isEmpty() ? storeDefault(defSupplier.get()) : ret;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getValue(@NonNull TypeToken<T> type, T def) throws ObjectMappingException {
        Object value = getValue();
        if (value == null) {
            return storeDefault(type, def);
        }

        TypeSerializer<?> serial = getOptions().getSerializers().get(type);
        if (serial == null) {
            if (type.getRawType().isInstance(value)) {
                return (T) type.getRawType().cast(value);
            } else {
                return storeDefault(type, def);
            }
        }
        return (T) serial.deserialize(type, this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getValue(@NonNull TypeToken<T> type, @NonNull Supplier<T> defSupplier) throws ObjectMappingException {
        Object value = getValue();
        if (value == null) {
            return storeDefault(type, defSupplier.get());
        }

        TypeSerializer<?> serial = getOptions().getSerializers().get(type);
        if (serial == null) {
            if (type.getRawType().isInstance(value)) {
                return (T) type.getRawType().cast(value);
            } else {
                return storeDefault(type, defSupplier.get());
            }
        }
        return (T) serial.deserialize(type, this);
    }

    @NonNull
    @Override
    public SimpleConfigurationNode setValue(@Nullable Object newValue) {
        // if the value to be set is a configuration node already, unwrap and store the raw data
        if (newValue instanceof ConfigurationNode) {
            ConfigurationNode newValueAsNode = (ConfigurationNode) newValue;
            if (newValueAsNode == this) { // this would be a no-op whoop
                return this;
            }

            if (newValueAsNode.isList()) {
                // handle list
                attachIfNecessary();
                ListConfigValue newList = new ListConfigValue(this);
                synchronized (newValueAsNode) {
                    newList.setValue(newValueAsNode.getChildrenList());
                }
                this.value = newList;
                return this;

            } else if (newValueAsNode.isMap()) {
                // handle map
                attachIfNecessary();
                MapConfigValue newMap = new MapConfigValue(this);
                synchronized (newValueAsNode) {
                    newMap.setValue(newValueAsNode.getChildrenMap());
                }
                this.value = newMap;
                return this;

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
            return this;
        }

        insertNewValue(newValue, false);
        return this;
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
            ConfigValue oldValue, value;
            oldValue = value = this.value;

            if (onlyIfNull && !(oldValue instanceof NullConfigValue)){
                return;
            }

            // init new config value backing for the new value type if necessary
            if (newValue instanceof Collection) {
                if (!(value instanceof ListConfigValue)) {
                    value = new ListConfigValue(this);
                }
            } else if (newValue instanceof Map) {
                if (!(value instanceof MapConfigValue)) {
                    value = new MapConfigValue(this);
                }
            } else if (!(value instanceof ScalarConfigValue)) {
                value = new ScalarConfigValue(this);
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
    public ConfigurationNode mergeValuesFrom(@NonNull ConfigurationNode other) {
        if (other.isMap()) {
            ConfigValue oldValue, newValue;
            synchronized (this) {
                oldValue = newValue = value;

                // ensure the current type is applicable.
                if (!(oldValue instanceof MapConfigValue)) {
                    if (oldValue instanceof NullConfigValue) {
                        newValue = new MapConfigValue(this);
                    } else {
                        return this;
                    }
                }

                // merge values from 'other'
                for (Map.Entry<Object, ? extends ConfigurationNode> ent : other.getChildrenMap().entrySet()) {
                    SimpleConfigurationNode currentChild = newValue.getChild(ent.getKey());
                    // Never allow null values to overwrite non-null values
                    if ((currentChild != null && currentChild.getValue() != null) && ent.getValue().getValue() == null) {
                        continue;
                    }

                    // create a new child node for the value
                    SimpleConfigurationNode newChild = this.createNode(ent.getKey());
                    newChild.attached = true;
                    newChild.setValue(ent.getValue());
                    // replace the existing value, if absent
                    SimpleConfigurationNode existing = newValue.putChildIfAbsent(ent.getKey(), newChild);
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
        return this;
    }

    @NonNull
    @Override
    public SimpleConfigurationNode getNode(@NonNull Object @NonNull... path) {
        SimpleConfigurationNode pointer = this;
        for (Object el : path) {
            pointer = pointer.getChild(el, false);
        }
        return pointer;
    }

    @Override
    public @NonNull SimpleConfigurationNode getNode(@NonNull Iterable<?> path) {
        SimpleConfigurationNode pointer = this;
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
    @SuppressWarnings("unchecked")
    public List<? extends SimpleConfigurationNode> getChildrenList() {
        ConfigValue value = this.value;
        return value instanceof ListConfigValue ? ImmutableList.copyOf(((ListConfigValue) value).values.get()) : Collections.emptyList();
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public Map<Object, ? extends SimpleConfigurationNode> getChildrenMap() {
        ConfigValue value = this.value;
        return value instanceof MapConfigValue ? ImmutableMap.copyOf(((MapConfigValue) value).values) : Collections.emptyMap();
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
    protected SimpleConfigurationNode getChild(Object key, boolean attach) {
        SimpleConfigurationNode child = value.getChild(key);

        // child doesn't currently exist
        if (child == null) {
            if (attach) {
                // attach ourselves first
                attachIfNecessary();
                // insert the child node into the value
                SimpleConfigurationNode existingChild = value.putChildIfAbsent(key, (child = createNode(key)));
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

    private static SimpleConfigurationNode detachIfNonNull(SimpleConfigurationNode node) {
        if (node != null) {
            node.attached = false;
            node.clear();
        }
        return node;
    }

    @NonNull
    @Override
    @Deprecated
    public SimpleConfigurationNode getAppendedNode() {
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
        ConfigurationNode pointer = this;
        if (pointer.getParent() == null) {
            return new Object[]{}; // we're the root node, no key here
        }

        do {
            pathElements.addFirst(pointer.getKey());
        } while ((pointer = pointer.getParent()).getParent() != null);
        return pathElements.toArray();
    }

    @Nullable
    public SimpleConfigurationNode getParent() {
        return this.parent;
    }

    @NonNull
    @Override
    public ConfigurationOptions getOptions() {
        return this.options;
    }

    @NonNull
    @Override
    public SimpleConfigurationNode copy() {
        return copy(null);
    }

    @NonNull
    protected SimpleConfigurationNode copy(@Nullable SimpleConfigurationNode parent) {
        return new SimpleConfigurationNode(parent, this);
    }

    /**
     * The same as {@link #getParent()} - but ensuring that 'parent' is attached via
     * {@link #attachChildIfAbsent(SimpleConfigurationNode)}.
     *
     * @return The parent
     */
    SimpleConfigurationNode getParentEnsureAttached() {
        SimpleConfigurationNode parent = this.parent;
        if (parent.isVirtual()) {
            parent = parent.getParentEnsureAttached().attachChildIfAbsent(parent);

        }
        return this.parent = parent;
    }

    protected void attachIfNecessary() {
        if (!attached) {
            getParentEnsureAttached().attachChild(this);
        }
    }

    protected SimpleConfigurationNode createNode(Object path) {
        return new SimpleConfigurationNode(path, this, options);
    }

    protected SimpleConfigurationNode attachChildIfAbsent(SimpleConfigurationNode child) {
        return attachChild(child, true);
    }

    private void attachChild(SimpleConfigurationNode child) {
        attachChild(child, false);
    }

    /**
     * Attaches a child to this node
     *
     * @param child The child
     * @return The resultant value
     */
    private SimpleConfigurationNode attachChild(SimpleConfigurationNode child, boolean onlyIfAbsent) {
        // ensure this node is attached
        if (isVirtual()) {
            throw new IllegalStateException("This parent is not currently attached. This is an internal state violation.");
        }

        // ensure the child actually is a child
        if (!child.getParentEnsureAttached().equals(this)) {
            throw new IllegalStateException("Child " +  child + " path is not a direct parent of me (" + this + "), cannot attach");
        }

        // update the value
        ConfigValue oldValue, newValue;
        synchronized (this) {
            newValue = oldValue = this.value;

            // if the existing value isn't a map, we need to update it's type
            if (!(oldValue instanceof MapConfigValue)) {
                if (child.key instanceof Integer) {
                    // if child.key is an integer, we can infer that the type of this node should be a list
                    if (oldValue instanceof NullConfigValue) {
                        // if the oldValue was null, we can just replace it with an empty list
                        newValue = new ListConfigValue(this);
                    } else if (!(oldValue instanceof ListConfigValue)) {
                        // if the oldValue contained a value, we add it as the first element of the
                        // new list
                        newValue = new ListConfigValue(this, oldValue.getValue());
                    }
                } else {
                    // if child.key isn't an integer, assume map
                    newValue = new MapConfigValue(this);
                }
            }

            /// now the value has been updated to an appropriate type, we can insert the value
            if (onlyIfAbsent) {
                SimpleConfigurationNode oldChild = newValue.putChildIfAbsent(child.key, child);
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
            ConfigValue oldValue = this.value;
            value = NullConfigValue.instance();
            oldValue.clear();
        }
    }

    @Override
    public <S, T, E extends Exception> T visit(ConfigurationVisitor<S, T, E> visitor, S state) throws E {
        return visitInternal(visitor, state);
    }

    @Override
    public <S, T> T visit(ConfigurationVisitor.Safe<S, T> visitor, S state) {
        try {
            return visitInternal(visitor, state);
        } catch (VisitorSafeNoopException e) {
            throw new Error("Exception was thrown on a Safe visitor");
        }
    }

    private <S, T, E extends Exception> T visitInternal(ConfigurationVisitor<S, T, E> visitor, S state) throws E {
        visitor.beginVisit(this, state);
        if (!(this.value instanceof NullConfigValue)) { // only visit if we have an actual value
            LinkedList<Object> toVisit = new LinkedList<>();
            toVisit.add(this);

            @Nullable Object active;
            while ((active = toVisit.pollFirst()) != null) {
                // try to pop a node from the stack, or handle the node exit if applicable
                @Nullable SimpleConfigurationNode current = VisitorNodeEnd.popFromVisitor(active, visitor, state);
                if (current == null) {
                    continue;
                }

                visitor.enterNode(current, state);
                ConfigValue value = current.value;
                if (value instanceof MapConfigValue) {
                    visitor.enterMappingNode(current, state);
                    toVisit.addFirst(new VisitorNodeEnd(current, true));
                    toVisit.addAll(0, ((MapConfigValue) value).values.values());
                } else if (value instanceof ListConfigValue) {
                    visitor.enterListNode(current, state);
                    toVisit.addFirst(new VisitorNodeEnd(current, false));
                    toVisit.addAll(0, ((ListConfigValue) value).values.get());
                } else if (value instanceof ScalarConfigValue) {
                    visitor.enterScalarNode(current, state);
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
        if (!(o instanceof SimpleConfigurationNode)) return false;
        SimpleConfigurationNode that = (SimpleConfigurationNode) o;

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
}
