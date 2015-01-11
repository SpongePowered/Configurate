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
package ninja.leaping.configurate.transformation;

import com.google.common.collect.Iterators;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Utility to perform bulk transformations of configuration data
 * Transformations are executed from deepest in the configuration hierarchy outwards
 */
public class ConfigurationTransformation {
    /**
     * A special object that represents a wildcard in a path provided to a configuration transformer
     */
    public static final Object WILDCARD_OBJECT = new Object();
    private final Map<Object[], TransformAction> actions;
    private final ThreadLocal<NodePath> sharedPath = new ThreadLocal<NodePath>() {
        @Override
        protected NodePath initialValue() {
            return new NodePath();
        }
    };

    /**
     * Wrapper around path Object[]s to prevent transformers modifying what is supposed to be a immutable path.
     * There is one node path per thread -- data within this object is only guaranteed to be the same during a run of
     * a transform function
     */
    public static class NodePath implements Iterable<Object> {
        private Object[] arr;
        private NodePath() {

        }

        /**
         * Gets a specific element from the path array
         * @param i the index to get
         * @return object at index
         */
        public Object get(int i) {
            return arr[i];
        }

        /**
         *
         * @return Length of the path array
         */
        public int size() {
            return arr.length;
        }

        /**
         * Returns a copy of the original path array
         *
         * @return the copied array
         */
        public Object[] getArray() {
            return Arrays.copyOf(arr, arr.length);
        }

        @Override
        public Iterator<Object> iterator() {
            return Iterators.forArray(arr);
        }
    }

    private static class NodePathComparator implements Comparator<Object[]> {

        @Override
        public int compare(Object[] a, Object[] b) {
            for (int i = 0; i < Math.min(a.length, b.length); ++i) {
                if (a[i] == WILDCARD_OBJECT || b[i] == WILDCARD_OBJECT) {
                    if (a[i] == WILDCARD_OBJECT && b[i] == WILDCARD_OBJECT) {
                        return 0;
                    } else {
                        return a[i] == WILDCARD_OBJECT ? 1 : -1;
                    }

                } else if (a[i] instanceof Comparable) {
                    @SuppressWarnings("unchecked")
                    final int comp = ((Comparable) a[i]).compareTo(b[i]);
                    switch (comp) {
                        case 0:
                            break;
                        default:
                            return comp;
                    }
                } else {
                    return a[i].equals(b[i]) ? 0 : Integer.valueOf(a[i].hashCode()).compareTo(b[i].hashCode());
                }
            }
            if (a.length > b.length) {
                return 1;
            } else if (b.length > a.length) {
                return -1;
            } else {
                return 0;
            }

        }
    }

    protected ConfigurationTransformation(Map<Object[], TransformAction> actions) {
        this.actions = actions;
    }

    public static final class Builder {
        private final Map<Object[], TransformAction> actions;

        protected Builder() {
            this.actions = new TreeMap<>(new NodePathComparator());
        }

        public Builder addAction(Object[] path, TransformAction action) {
            actions.put(path, action);
            return this;
        }

        public ConfigurationTransformation build() {
            return new ConfigurationTransformation(actions);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Apply this transformation to a given node
     *
     * @param node The target node
     */
    public void apply(ConfigurationNode node) {
        for (Map.Entry<Object[], TransformAction> ent : actions.entrySet()) {
            applySingleAction(node, ent.getKey(), 0, node, ent.getValue());
        }
    }

    protected void applySingleAction(ConfigurationNode start, Object[] path, int startIdx, ConfigurationNode node,
                                     TransformAction action) {
        for (int i = startIdx; i < path.length; ++i) {
            if (path[i] == WILDCARD_OBJECT) {
                if (node.hasListChildren()) {
                    List<? extends ConfigurationNode> children = node.getChildrenList();
                    for (int cI = 0; cI < children.size(); ++cI) {
                        path[i] = cI;
                        applySingleAction(start, path, i + 1, children.get(cI), action);
                    }
                    path[i] = WILDCARD_OBJECT;
                } else if (node.hasMapChildren()) {
                    for (Map.Entry<Object, ? extends ConfigurationNode> ent : node.getChildrenMap().entrySet()) {
                        path[i] = ent.getKey();
                        applySingleAction(start, path, i + 1, ent.getValue(), action);
                    }
                    path[i] = WILDCARD_OBJECT;
                } else {
                    // No children
                    return;
                }
                return;
            } else {
                node = node.getChild(path[i]);
                if (node.isVirtual()) {
                    return;
                }
            }
        }
        NodePath immutablePath = sharedPath.get();
        immutablePath.arr = path;
        Object[] transformedPath = action.visitPath(immutablePath, start);
        if (transformedPath != null && !Arrays.equals(path, transformedPath)) {
            start.getNode(transformedPath).setValue(node);
            node.setValue(null);
        }
    }
}
