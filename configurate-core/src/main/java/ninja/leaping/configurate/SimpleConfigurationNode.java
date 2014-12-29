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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.util.*;


/**
 * Simple implementation of a configuration node
 */
public class SimpleConfigurationNode implements ConfigurationNode {
    private final SimpleConfigurationNode root;
    private boolean attached;
    /**
     * Path of this node.
     *
     * Internally, may only be modified when an operation that adds or removes a node at the same or higher level in the node tree
     */
    private final Object[] path;
    private Object value;

    public static SimpleConfigurationNode root() {
        return new SimpleConfigurationNode(new Object[0], null);
    }

    protected SimpleConfigurationNode(Object[] path, SimpleConfigurationNode root) {
        this.path = path;
        this.root = root == null ? this : root;
        if (root == null) {
            attached = true;
        }

    }

    @Override
    public Object getValue() {
        final Object value = this.value;
        if (value instanceof List) {
            final List<Object> ret = new ArrayList<>(((List) value).size());
            for (Object obj : (List) value) {
                ret.add(((ConfigurationNode) obj).getValue());
            }
            return ret.isEmpty() ? null : ret;
        } else if (value instanceof Map) {
            final Map<Object, Object> ret = new HashMap<>(((Map) value).size());
            for (Map.Entry<?, ?> ent : ((Map<?, ?>) value).entrySet()) {
                ret.put(ent.getKey(), ((ConfigurationNode) ent.getValue()).getValue());
            }
            return ret.isEmpty() ? null : ret;
        } else {
            return value;
        }
    }

    @Override
    public Object getValue(Object def) {
        Object ret = getValue();
        return ret == null ? def : ret;
    }

    @Override
    public SimpleConfigurationNode setValue(Object value) {
        if (value instanceof ConfigurationNode) {
            value = ((ConfigurationNode) value).getValue(); // Unwrap existing nodes
        }

        if (value == null) {
            detachChildren();
            getParent().removeChild(this);
            return this;
        }

        attachIfNecessary();
        if (value instanceof Collection) {
            final Collection<?> valueList = (Collection<?>) value;
            final List<SimpleConfigurationNode> newValue = new ArrayList<>(valueList.size());
            int count = 0;
            for (Object o : valueList) {
                SimpleConfigurationNode child = createNode(PathUtils.appendPath(this.path, count));
                newValue.add(count, child);
                child.attached = true;
                child.setValue(o);
                ++count;
            }
            this.value = newValue;
        } else if (value instanceof Map) {
            final Map<Object, SimpleConfigurationNode> newValue = new LinkedHashMap<>();
            for (Map.Entry<?, ?> ent : ((Map<?, ?>) value).entrySet()) {
                SimpleConfigurationNode child = createNode(PathUtils.appendPath(this.path, ent.getKey()));
                newValue.put(ent.getKey(), child);
                child.attached = true;
                child.setValue(ent.getValue());
            }
            this.value = newValue;
        } else {
            this.value = value;
        }
        return this;
    }

    // {{{ Children
    @Override
    public SimpleConfigurationNode getNode(Object... path) {
        SimpleConfigurationNode ret = this;
        for (Object el : path) {
            if (!ret.attached) { // Child is not attached, so won't have any attached children, so skip on traversing
                return createNode(path);
            }
            ret = ret.getChild(el);
        }
        return ret;
    }

    @Override
    public boolean isVirtual() {
        return !attached;
    }

    @Override
    public boolean hasListChildren() {
        return this.value instanceof List;
    }

    @Override
    public boolean hasMapChildren() {
        return this.value instanceof Map;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ConfigurationNode> getChildrenList() {
        return hasListChildren() ? ImmutableList.copyOf((List <ConfigurationNode>) value) : Collections.<ConfigurationNode>emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<Object, ConfigurationNode> getChildrenMap() {
        return hasMapChildren() ? ImmutableMap.copyOf((Map<Object, ConfigurationNode>) value) : Collections.<Object, ConfigurationNode>emptyMap();
    }

    @SuppressWarnings("unchecked")
    List<SimpleConfigurationNode> getImplChildrenList() {
        return hasListChildren() ? (List<SimpleConfigurationNode>) value : Collections.<SimpleConfigurationNode>emptyList();

    }

    @SuppressWarnings("unchecked")
    Map<Object, SimpleConfigurationNode> getImplChildrenMap() {
        return hasMapChildren() ? (Map<Object, SimpleConfigurationNode>) value : Collections.<Object, SimpleConfigurationNode>emptyMap();
    }

    @SuppressWarnings("unchecked")
    Iterable<SimpleConfigurationNode> iterChildren() {
        if (value instanceof Map) {
            return ((Map<Object, SimpleConfigurationNode>) value).values();
        } else if (value instanceof List) {
            return (List<SimpleConfigurationNode>) value;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public SimpleConfigurationNode getChild(Object key) {
        SimpleConfigurationNode child = null;
        if (hasListChildren() && key instanceof Integer) {
            final List<SimpleConfigurationNode> valueList = getImplChildrenList();
            int index = (Integer) key;
            child = (index < 0 || index >= valueList.size()) ? null : valueList.get(index);
        } else if (hasMapChildren()) {
            child = getImplChildrenMap().get(key);
        }

        if (child == null) { // Does not currently exist!
            child = createNode(PathUtils.appendPath(path, key));
        }

        return child;
    }

    @Override
    public boolean removeChild(Object key) {
        SimpleConfigurationNode removedNode = null;
        if (key instanceof Integer && hasListChildren()) {
            int index = (Integer) key;
            List<SimpleConfigurationNode> nodes = getImplChildrenList();
            if (index < nodes.size()) {
                removedNode = nodes.remove(index);
                for (int i = index; i < nodes.size(); ++i) {
                    Object[] path = nodes.get(i).path;
                    path[path.length - 1] = index;
                    nodes.get(i).updateChildPaths();
                }
            }
        } else if (hasMapChildren()) {
            removedNode = getImplChildrenMap().remove(key);
        }

        if (removedNode != null) {
            removedNode.attached = false;
            removedNode.detachChildren();
        }
        return removedNode != null;
    }

    @Override
    public SimpleConfigurationNode getAppendedChild() {
        return getChild(-1);
    }

    // }}}

    // {{{ Internal methods

    private SimpleConfigurationNode getParent() {
        return root.getNode(PathUtils.dropPathTail(this.path));
    }

    protected SimpleConfigurationNode createNode(Object[] path) {
        return new SimpleConfigurationNode(path, root);
    }

    protected void attachIfNecessary() {
        if (!attached) {
            getParent().attachChild(this);
        }
    }

    protected void attachChild(SimpleConfigurationNode child) {
        if (!PathUtils.isDirectChild(child.path, path)) {
            throw new IllegalStateException("Child " + Arrays.toString(child.path) +
                    " path is not a direct parent of me " + Arrays.toString(this.path) + ", cannot attach");
        }
        attachIfNecessary();
        Object id = child.path[child.path.length - 1];
        if (id instanceof Integer && !hasMapChildren()) {
            List<SimpleConfigurationNode> list;
            if (this.value == null) {
                this.value = list = new ArrayList<>();

            } else if (!(this.value instanceof List)) {
                SimpleConfigurationNode newChild = createNode(PathUtils.appendPath(this.path, 0));
                newChild.attached = true;
                newChild.setValue(this.value);
                this.value = list = Lists.newArrayList(newChild);
            } else {
                list = getImplChildrenList();
            }

            final int index = (Integer) id;

            if (index >= 0 && index < list.size()) {
                SimpleConfigurationNode old = list.set(index, child);
                old.attached = false;
                old.detachChildren();
            } else if (index == -1) { // Gotta correct the child path for the correct path name
                list.add(child);
                id = list.size() - 1;
                child.path[child.path.length - 1] = id;
                child.updateChildPaths();
            } else {
                list.add(index, child);
            }
        } else {
            if (hasListChildren()) {
                detachChildren();
            }
            Map<Object, SimpleConfigurationNode> map;
            if (!(this.value instanceof Map)) {
                this.value = map = new LinkedHashMap<>();
            } else {
                map = getImplChildrenMap();
            }
            SimpleConfigurationNode oldNode = map.put(id, child);
            if (oldNode != null) {
                oldNode.attached = false;
                oldNode.detachChildren();
            }
        }
        child.attached = true;
    }

    protected void updateChildPaths() {
        updateChildPaths(path.length - 1);
    }

    protected void updateChildPaths(int startIndex) {
        for (SimpleConfigurationNode node : iterChildren()) {
            System.arraycopy(path, startIndex, node.path, startIndex, path.length - startIndex);
        }
    }

    protected void detachChildren() {
        if (hasListChildren() || hasMapChildren()) {
            Iterable<SimpleConfigurationNode> iter = iterChildren();
            this.value = null;
            for (SimpleConfigurationNode node : iter) {
                node.attached = false;
                node.detachChildren();
            }
        }
    }
    // }}}
}
