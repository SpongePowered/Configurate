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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 * Simple implementation of a configuration node
 */
public class SimpleConfigurationNode implements ConfigurationNode {
    private final ConfigurationOptions options;
    volatile boolean attached;
    /**
     * Path of this node.
     *
     * Internally, may only be modified when an operation that adds or removes a node at the same or higher level in the node tree
     */
    volatile Object key;
    private SimpleConfigurationNode parent;
    private volatile ConfigValue value;

    public static SimpleConfigurationNode root() {
        return root(ConfigurationOptions.defaults());
    }

    public static SimpleConfigurationNode root(ConfigurationOptions options) {
        return new SimpleConfigurationNode(null, null, options);
    }

    protected SimpleConfigurationNode(Object key, SimpleConfigurationNode parent, ConfigurationOptions options) {
        Preconditions.checkNotNull(options, "options");

        this.key = key;
        if (parent == null) {
            attached = true;
        }
        this.options = options;
        this.parent = parent == null ? null : parent;

        value = new NullConfigValue(this);

    }

    @Override
    public Object getValue(Object def) {
        Object ret = value.getValue();
        return ret == null ? calculateDef(def) : ret;
    }

    @Override
    public Object getValue(Supplier<Object> defSupplier) {
        Object ret = value.getValue();
        return ret == null ? calculateDef(defSupplier.get()) : ret;
    }

    // {{{ Typed values

    @Override
    public <T> T getValue(Function<Object, T> transformer, T def) {
        T ret = transformer.apply(getValue());
        return ret == null ? calculateDef(def) : ret;
    }

    @Override
    public <T> T getValue(Function<Object, T> transformer, Supplier<T> defSupplier) {
        T ret = transformer.apply(getValue());
        return ret == null ? calculateDef(defSupplier.get()) : ret;
    }

    private <T> T calculateDef(T defValue) {
        if (defValue != null && getOptions().shouldCopyDefaults()) {
            setValue(defValue);
        }
        return defValue;
    }

    @Override
    public <T> List<T> getList(Function<Object, T> transformer) {
        final ImmutableList.Builder<T> build = ImmutableList.builder();
        ConfigValue value = this.value;
        if (value instanceof ListConfigValue) {
            for (SimpleConfigurationNode o : value.iterateChildren()) {
                T transformed = transformer.apply(o.getValue());
                if (transformed != null) {
                    build.add(transformed);
                }
            }
        } else {
            T transformed = transformer.apply(value.getValue());
            if (transformed != null) {
                build.add(transformed);
            }
        }

        return build.build();
    }

    @Override
    public <T> List<T> getList(Function<Object, T> transformer, List<T> def) {
        List<T> ret = getList(transformer);
        return ret.isEmpty() ? calculateDef(def) : ret;
    }

    @Override
    public <T> List<T> getList(Function<Object, T> transformer, Supplier<List<T>> defSupplier) {
        List<T> ret = getList(transformer);
        return ret.isEmpty() ? calculateDef(defSupplier.get()) : ret;
    }

    @Override
    public <T> List<T> getList(TypeToken<T> type, List<T> def) throws ObjectMappingException {
        List<T> ret = getValue(new TypeToken<List<T>>() {}
                .where(new TypeParameter<T>() {}, type), def);
        return ret.isEmpty() ? calculateDef(def) : ret;
    }

    @Override
    public <T> List<T> getList(TypeToken<T> type, Supplier<List<T>> defSupplier) throws ObjectMappingException {
        List<T> ret = getValue(new TypeToken<List<T>>() {}
                .where(new TypeParameter<T>() {}, type), defSupplier);
        return ret.isEmpty() ? calculateDef(defSupplier.get()) : ret;
    }

    // }}}

    @Override
    public SimpleConfigurationNode setValue(Object newValue) {
        if (newValue instanceof ConfigurationNode) {
            ConfigurationNode newNode = (ConfigurationNode) newValue;
            if (newNode.hasListChildren()) {
                attachIfNecessary();
                ListConfigValue newList = new ListConfigValue(this);
                synchronized (newNode) {
                    List<? extends ConfigurationNode> children = newNode.getChildrenList();
                    for (int i = 0; i < children.size(); ++i) {
                        SimpleConfigurationNode child = createNode(i);
                        child.attached = true;
                        newList.putChild(i, child);
                        child.setValue(children.get(i));
                    }
                }
                this.value = newList;
                return this;
            } else if (newNode.hasMapChildren()) {
                attachIfNecessary();
                MapConfigValue newMap = new MapConfigValue(this);
                synchronized (newNode) {
                    Map<Object, ? extends ConfigurationNode> children = newNode.getChildrenMap();
                    for (Map.Entry<Object, ? extends ConfigurationNode> ent : children.entrySet()) {
                        SimpleConfigurationNode child = createNode(ent.getKey());
                        child.attached = true;
                        newMap.putChild(ent.getKey(), child);
                        child.setValue(ent.getValue());
                    }
                }
                this.value = newMap;
                return this;
            } else {
                newValue = newNode.getValue();
            }
        }

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

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getValue(TypeToken<T> type, T def) throws ObjectMappingException {
        Object value = getValue();
        if (value == null) {
            if (def != null && getOptions().shouldCopyDefaults()) {
                setValue(type, def);
            }
            return def;
        }
        TypeSerializer serial = getOptions().getSerializers().get(type);
        if (serial == null) {
            if (type.getRawType().isInstance(value)) {
                return (T) type.getRawType().cast(value);
            } else {
                if (def != null && getOptions().shouldCopyDefaults()) {
                    setValue(type, def);
                }
                return def;
            }
        }
        return (T) serial.deserialize(type, this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getValue(TypeToken<T> type, Supplier<T> defSupplier) throws ObjectMappingException {
        Object value = getValue();
        if (value == null) {
            T def = defSupplier.get();
            if (def != null && getOptions().shouldCopyDefaults()) {
                setValue(type, def);
            }
            return def;
        }
        TypeSerializer serial = getOptions().getSerializers().get(type);
        if (serial == null) {
            if (type.getRawType().isInstance(value)) {
                return (T) type.getRawType().cast(value);
            } else {
                T def = defSupplier.get();
                if (def != null && getOptions().shouldCopyDefaults()) {
                    setValue(type, def);
                }
                return def;
            }
        }
        return (T) serial.deserialize(type, this);
    }

    private void insertNewValue(Object newValue, boolean onlyIfNull) {
        attachIfNecessary();
        synchronized (this) {
            ConfigValue oldValue, value;
            oldValue = value = this.value;
            if (onlyIfNull && !(oldValue instanceof NullConfigValue)){
                return;
            }
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
            value.setValue(newValue);

            /*if (oldValue != null && oldValue != value) {
                oldValue.clear();
            }*/
            this.value = value;
        }
    }

    @Override
    public ConfigurationNode mergeValuesFrom(ConfigurationNode other) {
        /*if (other.hasListChildren()) {
            ConfigValue oldValue, newValue;
            do {
                oldValue = newValue = value.get();
                if (!(oldValue instanceof ListConfigValue)) {
                    if (oldValue instanceof NullConfigValue) {
                        oldValue = new ListConfigValue(this);
                    } else {
                        break;
                    }
                }
               // TODO: How to merge list values?

            } while (!this.value.compareAndSet(oldValue, newValue));

        } else */if (other.hasMapChildren()) {
            ConfigValue oldValue, newValue;
            synchronized (this) {
                oldValue = newValue = value;
                if (!(oldValue instanceof MapConfigValue)) {
                    if (oldValue instanceof NullConfigValue) {
                        newValue = new MapConfigValue(this);
                    } else {
                        return this;
                    }
                }
                for (Map.Entry<Object, ? extends ConfigurationNode> ent : other.getChildrenMap().entrySet()) {
                    SimpleConfigurationNode newChild = createNode(ent.getKey());
                    newChild.attached = true;
                    newChild.setValue(ent.getValue());
                    SimpleConfigurationNode existing = newValue.putChildIfAbsent(ent.getKey(), newChild);
                    if (existing != null) {
                        existing.mergeValuesFrom(newChild);
                    }
                }
                this.value = newValue;
            }
        } else if (other.getValue() != null) {
            insertNewValue(other.getValue(), true);
        }
        return this;
    }

    // {{{ Children
    @Override
    public SimpleConfigurationNode getNode(Object... path) {
        SimpleConfigurationNode ret = this;
        for (Object el : path) {
            ret = ret.getChild(el, false);
        }
        return ret;
    }

    @Override
    public boolean isVirtual() {
        return !attached;
    }

    @Override
    public boolean hasListChildren() {
        return this.value instanceof ListConfigValue;
    }

    @Override
    public boolean hasMapChildren() {
        return this.value instanceof MapConfigValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<? extends SimpleConfigurationNode> getChildrenList() {
        ConfigValue value = this.value;
        return value instanceof ListConfigValue ? ImmutableList.copyOf(((ListConfigValue) value).values.get()) : Collections
                .<SimpleConfigurationNode>emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<Object, ? extends SimpleConfigurationNode> getChildrenMap() {
        ConfigValue value = this.value;
        return value instanceof MapConfigValue ? ImmutableMap.copyOf(((MapConfigValue) value).values) : Collections
                .<Object, SimpleConfigurationNode>emptyMap();
    }

    protected SimpleConfigurationNode getChild(Object key, boolean attach) {
        SimpleConfigurationNode child = value.getChild(key);

        if (child == null) { // Does not currently exist!
            if (attach) {
                attachIfNecessary();
                SimpleConfigurationNode existingChild = value.putChildIfAbsent(key, (child = createNode(key)));
                if (existingChild != null) {
                    child = existingChild;
                } else {
                    attachChild(child);
                }
            } else {
                child = createNode(key);
            }
        }

        return child;
    }

    @Override
    public boolean removeChild(Object key) {
        return possiblyDetach(value.putChild(key, null)) != null;
    }

    private SimpleConfigurationNode possiblyDetach(SimpleConfigurationNode node) {
        if (node != null) {
            node.attached = false;
            node.clear();
        }
        return node;
    }

    @Override
    public SimpleConfigurationNode getAppendedNode() {
        return getChild(-1, false);
    }

    @Override
    public Object getKey() {
        return this.key;
    }

    @Override
    public Object[] getPath() {
        LinkedList<Object> pathElements = new LinkedList<>();
        ConfigurationNode ptr = this;
        if (ptr.getParent() == null) {
            return new Object[] {this.getKey()};
        }

        do {
            pathElements.addFirst(ptr.getKey());
        } while ((ptr = ptr.getParent()).getParent() != null);
        return pathElements.toArray();
    }

    public SimpleConfigurationNode getParent() {
        return this.parent;
    }

    @Override
    public ConfigurationOptions getOptions() {
        return this.options;
    }
    // }}}

    // {{{ Internal methods
    SimpleConfigurationNode getParentAttached() {
        SimpleConfigurationNode parent = this.parent;
        if (parent.isVirtual()) {
            parent = parent.getParentAttached().attachChildIfAbsent(parent);

        }
        return this.parent = parent;
    }

    private SimpleConfigurationNode attachChildIfAbsent(SimpleConfigurationNode child) {
        if (isVirtual()) {
            throw new IllegalStateException("This parent is not currently attached. This is an internal state violation.");
        }
        if (!child.getParentAttached().equals(this)) {
            throw new IllegalStateException("Child " +  child +
                    " path is not a direct parent of me (" + this + "), cannot attach");
        }
        ConfigValue oldValue, newValue;
        synchronized (this) {
            newValue = oldValue = this.value;
            if (!(oldValue instanceof MapConfigValue)) {
                if (child.key instanceof Integer) {
                    if (oldValue instanceof NullConfigValue) {
                        newValue = new ListConfigValue(this);

                    } else if (!(oldValue instanceof ListConfigValue)) {
                        newValue = new ListConfigValue(this, oldValue.getValue());
                    }
                } else {
                    newValue = new MapConfigValue(this);
                }
            }
            SimpleConfigurationNode oldChild = newValue.putChildIfAbsent(child.key, child);
            if (oldChild != null) {
                return oldChild;
            }
            value = newValue;
        }
        if (newValue != oldValue) {
            oldValue.clear();
        }
        child.attached = true;
        return child;
    }

    protected SimpleConfigurationNode createNode(Object path) {
        return new SimpleConfigurationNode(path, this, options);
    }

    protected void attachIfNecessary() {
        if (!attached) {
            getParentAttached().attachChild(this);
        }
    }

    protected void attachChild(SimpleConfigurationNode child) {
        if (isVirtual()) {
            throw new IllegalStateException("This parent is not currently attached. This is an internal state violation.");
        }
        if (!child.getParentAttached().equals(this)) {
            throw new IllegalStateException("Child " +  child +
                    " path is not a direct parent of me (" + this + "), cannot attach");
        }
        ConfigValue oldValue, newValue;
        synchronized (this) {
            newValue = oldValue = this.value;
                if (!(oldValue instanceof MapConfigValue)) {
                    if (child.key instanceof Integer) {
                        if (oldValue instanceof NullConfigValue) {
                            newValue = new ListConfigValue(this);

                        } else if (!(oldValue instanceof ListConfigValue)) {
                            newValue = new ListConfigValue(this, oldValue.getValue());
                        }
                    } else {
                        newValue = new MapConfigValue(this);
                    }
                }
                possiblyDetach(newValue.putChild(child.key, child));
            value = newValue;
        }
        if (newValue != oldValue) {
            oldValue.clear();
        }
        child.attached = true;
    }

    protected void clear() {
        synchronized (this) {
            ConfigValue oldValue = this.value;
            value = new NullConfigValue(this);
            oldValue.clear();
        }
    }
    // }}}


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleConfigurationNode)) return false;

        SimpleConfigurationNode that = (SimpleConfigurationNode) o;

        if (attached != that.attached) return false;
        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (!options.equals(that.options)) return false;
        if (parent != null ? !parent.equals(that.parent) : that.parent != null)
            return false;
        if (!value.equals(that.value)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = options.hashCode();
        result = 31 * result + (attached ? 1 : 0);
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SimpleConfigurationNode{" +
                "options=" + options +
                ", attached=" + attached +
                ", key=" + key +
                ", parent=" + parent +
                ", value=" + value +
                '}';
    }
}
