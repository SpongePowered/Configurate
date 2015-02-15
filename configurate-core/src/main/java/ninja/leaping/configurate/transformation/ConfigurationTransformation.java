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
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public abstract class ConfigurationTransformation {
    /**
     * A special object that represents a wildcard in a path provided to a configuration transformer
     */
    public static final Object WILDCARD_OBJECT = new Object();

    /**
     * Apply this transformation to a given node
     *
     * @param node The target node
     */
    public abstract void apply(ConfigurationNode node);

    /**
     * Wrapper around path Object[]s to prevent transformers modifying what is supposed to be a immutable path.
     * There is one node path per thread -- data within this object is only guaranteed to be the same during a run of
     * a transform function
     */
    public static class NodePath implements Iterable<Object> {
        Object[] arr;
        NodePath() {
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
            return new SingleConfigurationTransformation(actions);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ConfigurationTransformation chain(ConfigurationTransformation... transformations) {
        return new ChainedConfigurationTransformation(transformations);
    }
}
