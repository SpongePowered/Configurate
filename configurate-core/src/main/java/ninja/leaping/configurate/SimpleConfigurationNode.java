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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Simple implementation of a configuration node
 */
public class SimpleConfigurationNode implements ConfigurationNode {
    private static final int NUMBER_DEF = 0;
    private final ConfigurationOptions options;
    volatile boolean attached;
    /**
     * Path of this node.
     *
     * Internally, may only be modified when an operation that adds or removes a node at the same or higher level in the node tree
     */
    volatile Object key;
    private SimpleConfigurationNode parent;
    private final AtomicReference<ConfigValue> value = new AtomicReference<>();

    public static SimpleConfigurationNode root() {
        return root(ConfigurationOptions.defaults());
    }

    public static SimpleConfigurationNode root(ConfigurationOptions options) {
        return new SimpleConfigurationNode(null, null, options);
    }

    protected SimpleConfigurationNode(Object key, SimpleConfigurationNode parent, ConfigurationOptions options) {
        this.key = key;
        if (parent == null) {
            attached = true;
        }
        this.options = options;
        this.parent = parent == null ? null : parent;

        value.set(new NullConfigValue(this));

    }

    @Override
    public Object getValue() {
        return value.get().getValue();
    }

    @Override
    public Object getValue(Object def) {
        Object ret = getValue();
        return ret == null ? def : ret;
    }

    // {{{ Typed values

    @Override
    public <T> T getValue(Function<Object, T> transformer) {
        return transformer.apply(getValue());
    }

    @Override
    public <T> T getValue(Function<Object, T> transformer, T def) {
        T ret = transformer.apply(getValue());
        return ret == null ? def : ret;
    }

    @Override
    public <T> List<T> getList(Function<Object, T> transformer) {
        final ImmutableList.Builder<T> build = ImmutableList.builder();
        if (hasListChildren()) {
            for (SimpleConfigurationNode o : value.get().iterateChildren()) {
                T transformed = transformer.apply(o.getValue());
                if (transformed != null) {
                    build.add(transformed);
                }
            }
        } else {
            T transformed = transformer.apply(getValue());
            if (transformed != null) {
                build.add(transformed);
            }
        }

        return build.build();
    }

    @Override
    public <T> List<T> getList(Function<Object, T> transformer, List<T> def) {
        List<T> ret = getList(transformer);
        return ret.isEmpty() ? def : ret;
    }

    @Override
    public String getString() {
        return getString(null);
    }

    @Override
    public String getString(String def) {
        final String ret = Types.asString(getValue());
        return ret == null ? def : ret;
    }

    @Override
    public float getFloat() {
        return getFloat(NUMBER_DEF);
    }

    @Override
    public float getFloat(float def) {
        final Float ret = Types.asFloat(getValue());
        return ret == null ? def : ret;
    }

    @Override
    public double getDouble() {
        return getDouble(NUMBER_DEF);
    }

    @Override
    public double getDouble(double def) {
        final Double ret = Types.asDouble(getValue());
        return ret == null ? def : ret;
    }

    @Override
    public int getInt() {
        return getInt(NUMBER_DEF);
    }

    @Override
    public int getInt(int def) {
        final Integer ret = Types.asInt(getValue());
        return ret == null ? def : ret;
    }

    @Override
    public long getLong() {
        return getLong(NUMBER_DEF);
    }

    @Override
    public long getLong(long def) {
        final Long ret = Types.asLong(getValue());
        return ret == null ? def : ret;
    }

    @Override
    public boolean getBoolean() {
        return getBoolean(false);
    }

    @Override
    public boolean getBoolean(boolean def) {
        final Boolean ret = Types.asBoolean(getValue());
        return ret == null ? def : ret;
    }

    // }}}

    @Override
    public SimpleConfigurationNode setValue(Object newValue) {
        if (newValue instanceof ConfigurationNode) {
            newValue = ((ConfigurationNode) newValue).getValue(); // Unwrap existing nodes
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

    private void insertNewValue(Object newValue, boolean onlyIfNull) {
        attachIfNecessary();
        ConfigValue value, oldValue;
        do {
            value = this.value.get();
            oldValue = value;
            if (onlyIfNull && !(oldValue instanceof NullConfigValue)) {
                break;
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
        } while (!this.value.compareAndSet(oldValue, value));

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
            do {
                oldValue = newValue = value.get();
                if (!(oldValue instanceof MapConfigValue)) {
                    if (oldValue instanceof NullConfigValue) {
                        newValue = new MapConfigValue(this);
                    } else {
                        break;
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
            } while (!this.value.compareAndSet(oldValue, newValue));
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
        return this.value.get() instanceof ListConfigValue;
    }

    @Override
    public boolean hasMapChildren() {
        return this.value.get() instanceof MapConfigValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<? extends SimpleConfigurationNode> getChildrenList() {
        ConfigValue value = this.value.get();
        return value instanceof ListConfigValue ? ImmutableList.copyOf(((ListConfigValue) value).values.get()) : Collections
                .<SimpleConfigurationNode>emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<Object, ? extends SimpleConfigurationNode> getChildrenMap() {
        ConfigValue value = this.value.get();
        return value instanceof MapConfigValue ? ImmutableMap.copyOf(((MapConfigValue) value).values.get()) : Collections
                .<Object, SimpleConfigurationNode>emptyMap();
    }

    protected SimpleConfigurationNode getChild(Object key, boolean attach) {
        SimpleConfigurationNode child = value.get().getChild(key);

        if (attach) {
            attachIfNecessary();
            SimpleConfigurationNode existingChild = value.get().putChildIfAbsent(key, (child = createNode(key)));
            if (existingChild != null) {
                child = existingChild;
            } else {
                attachChild(child);
            }
        } else {
            if (child == null) { // Does not currently exist!
                child = createNode(key);
            }
        }

        return child;
    }

    @Override
    public boolean removeChild(Object key) {
        return possiblyDetach(value.get().putChild(key, null)) != null;
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
    SimpleConfigurationNode getParentInternal() {
        SimpleConfigurationNode parent = this.parent;
        if (parent.isVirtual()) {
            parent = parent.getParentInternal().getChild(parent.key, true);

        }
        return this.parent = parent;
    }

    protected SimpleConfigurationNode createNode(Object path) {
        return new SimpleConfigurationNode(path, this, options);
    }

    protected void attachIfNecessary() {
        if (!attached) {
            getParentInternal().attachChild(this);
        }
    }

    protected void attachChild(SimpleConfigurationNode child) {
        if (isVirtual()) {
            throw new IllegalStateException("This parent is not currently attached. This is an internal state violation.");
        }
        if (!child.getParentInternal().equals(this)) {
            throw new IllegalStateException("Child " +  child +
                    " path is not a direct parent of me (" + this + "), cannot attach");
        }
        final ConfigValue oldValue = value.get();
        ConfigValue newValue = oldValue;
        do {
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
        } while (!value.compareAndSet(oldValue, newValue));
        if (newValue != oldValue) {
            oldValue.clear();
        }
        child.attached = true;
    }

    protected void clear() {
        value.getAndSet(new NullConfigValue(this)).clear();
    }
    // }}}
}
