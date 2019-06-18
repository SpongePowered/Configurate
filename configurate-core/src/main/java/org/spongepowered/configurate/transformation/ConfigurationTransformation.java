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
package org.spongepowered.configurate.transformation;

import com.google.common.collect.Iterators;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.Arrays;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Represents a set of transformations on a configuration.
 */
public abstract class ConfigurationTransformation {

    /**
     * A special object that represents a wildcard in a path provided to a configuration transformer
     */
    public static final Object WILDCARD_OBJECT = new Object();

    /**
     * Create a new builder to create a basic configuration transformation.
     *
     * @return a new transformation builder.
     */
    @NonNull
    public static Builder builder() {
        return new Builder();
    }

    /**
     * This creates a builder for versioned transformations.
     *
     * @return A new builder for versioned transformations
     */
    @NonNull
    public static VersionedBuilder versionedBuilder() {
        return new VersionedBuilder();
    }

    /**
     * Creates a chain of {@link ConfigurationTransformation}s.
     *
     * @param transformations The transformations
     * @return The resultant transformation chain
     */
    @NonNull
    public static ConfigurationTransformation chain(ConfigurationTransformation... transformations) {
        return new ChainedConfigurationTransformation(transformations);
    }

    /**
     * Apply this transformation to a given node
     *
     * @param node The target node
     */
    public abstract void apply(@NonNull ConfigurationNode node);

    /**
     * Builds a basic {@link ConfigurationTransformation}.
     */
    public static final class Builder {
        private MoveStrategy strategy = MoveStrategy.OVERWRITE;
        private final SortedMap<Object[], TransformAction> actions;

        protected Builder() {
            this.actions = new TreeMap<>(new NodePathComparator());
        }

        /**
         * Adds an action to the transformation.
         *
         * @param path The path to apply the action at
         * @param action The action
         * @return This builder (for chaining)
         */
        @NonNull
        public Builder addAction(Object[] path, TransformAction action) {
            actions.put(path, action);
            return this;
        }

        /**
         * Gets the move strategy to be used by the resultant transformation.
         *
         * @return The move strategy
         */
        @NonNull
        public MoveStrategy getMoveStrategy() {
            return strategy;
        }

        /**
         * Sets the mode strategy to be used by the resultant transformation.
         *
         * @param strategy The strategy
         * @return This builder (for chaining)
         */
        @NonNull
        public Builder setMoveStrategy(@NonNull MoveStrategy strategy) {
            this.strategy = strategy;
            return this;
        }

        /**
         * Builds the transformation.
         *
         * @return The transformation
         */
        @NonNull
        public ConfigurationTransformation build() {
            return new SingleConfigurationTransformation(actions, strategy);
        }
    }

    /**
     * Builds a versioned {@link ConfigurationTransformation}.
     */
    public static final class VersionedBuilder {
        private Object[] versionKey = new Object[] {"version"};
        private final SortedMap<Integer, ConfigurationTransformation> versions = new TreeMap<>();

        protected VersionedBuilder() {}

        /**
         * Sets the path of the version key within the configuration.
         *
         * @param versionKey The path to the version key
         * @return This builder (for chaining)
         */
        @NonNull
        public VersionedBuilder setVersionKey(@NonNull Object... versionKey) {
            this.versionKey = Arrays.copyOf(versionKey, versionKey.length, Object[].class);
            return this;
        }

        /**
         * Adds a transformation to this builder for the given version.
         *
         * @param version The version
         * @param transformation The transformation
         * @return This builder (for chaining)
         */
        @NonNull
        public VersionedBuilder addVersion(int version, @NonNull ConfigurationTransformation transformation) {
            versions.put(version, transformation);
            return this;
        }

        /**
         * Builds the transformation.
         *
         * @return The transformation
         */
        @NonNull
        public ConfigurationTransformation build() {
            return new VersionedTransformation(versionKey, versions);
        }
    }

    /**
     * Implementation of {@link org.spongepowered.configurate.transformation.NodePath} used by this class.
     */
    // TODO Remove usages of this class in favour of the NodePath interface (breaking change for 4.0)
    public static final class NodePath implements org.spongepowered.configurate.transformation.NodePath {
        Object[] arr;

        NodePath() {
        }

        @Override
        public Object get(int i) {
            return arr[i];
        }

        @Override
        public int size() {
            return arr.length;
        }

        @Override
        public Object[] getArray() {
            return Arrays.copyOf(arr, arr.length);
        }

        @NonNull
        @Override
        public Iterator<Object> iterator() {
            return Iterators.forArray(arr);
        }
    }
}
