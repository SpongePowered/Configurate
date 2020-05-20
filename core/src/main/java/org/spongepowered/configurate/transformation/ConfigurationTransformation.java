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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ScopedConfigurationNode;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Represents a set of transformations on a configuration.
 */
public abstract class ConfigurationTransformation<T extends ConfigurationNode> {

    /**
     * A special object that represents a wildcard in a path provided to a
     * configuration transformer.
     */
    public static final Object WILDCARD_OBJECT = new Object();

    /**
     * Create a new builder to create a basic configuration transformation.
     *
     *
     * @param <T> the type of node being processed
     * @return a new transformation builder.
     */
    @NonNull
    public static <T extends ScopedConfigurationNode<T>> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * This creates a builder for versioned transformations.
     *
     * @param <T> the type of node being processed
     * @return A new builder for versioned transformations
     */
    @NonNull
    public static <T extends ConfigurationNode> VersionedBuilder<T> versionedBuilder() {
        return new VersionedBuilder<>();
    }

    /**
     * Creates a chain of {@link ConfigurationTransformation}s.
     *
     * @param <T> the type of node being processed
     * @param transformations The transformations
     * @return The resultant transformation chain
     */
    @SafeVarargs
    public static <T extends ConfigurationNode> ConfigurationTransformation<T>
        chain(final ConfigurationTransformation<? super T>... transformations) {
        return new ChainedConfigurationTransformation<>(transformations);
    }

    /**
     * Apply this transformation to a given node.
     *
     * @param node The target node
     */
    public abstract void apply(@NonNull T node);

    /**
     * Builds a basic {@link ConfigurationTransformation}.
     */
    public static final class Builder<T extends ScopedConfigurationNode<T>> {
        private MoveStrategy strategy = MoveStrategy.OVERWRITE;
        private final SortedMap<NodePath, TransformAction<? super T>> actions;

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
        public Builder<T> addAction(final NodePath path, final TransformAction<? super T> action) {
            this.actions.put(path, action);
            return this;
        }

        /**
         * Gets the move strategy to be used by the resultant transformation.
         *
         * @return The move strategy
         */
        @NonNull
        public MoveStrategy getMoveStrategy() {
            return this.strategy;
        }

        /**
         * Sets the mode strategy to be used by the resultant transformation.
         *
         * @param strategy The strategy
         * @return This builder (for chaining)
         */
        @NonNull
        public Builder<T> setMoveStrategy(final @NonNull MoveStrategy strategy) {
            this.strategy = strategy;
            return this;
        }

        /**
         * Builds the transformation.
         *
         * @return The transformation
         */
        @NonNull
        public ConfigurationTransformation<T> build() {
            return new SingleConfigurationTransformation<>(this.actions, this.strategy);
        }
    }

    /**
     * Builds a versioned {@link ConfigurationTransformation}.
     */
    public static final class VersionedBuilder<T extends ConfigurationNode> {
        private NodePath versionKey = NodePath.path("version");
        private final SortedMap<Integer, ConfigurationTransformation<? super T>> versions = new TreeMap<>();

        protected VersionedBuilder() {}

        /**
         * Sets the path of the version key within the configuration.
         *
         * @param versionKey The path to the version key
         * @return This builder (for chaining)
         */
        @NonNull
        public VersionedBuilder<T> setVersionKey(final @NonNull Object... versionKey) {
            this.versionKey = NodePath.create(versionKey);
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
        public VersionedBuilder<T> addVersion(final int version, final @NonNull ConfigurationTransformation<? super T> transformation) {
            this.versions.put(version, transformation);
            return this;
        }

        /**
         * Builds the transformation.
         *
         * @return The transformation
         */
        @NonNull
        public ConfigurationTransformation<T> build() {
            return new VersionedTransformation<>(this.versionKey, this.versions);
        }
    }

}
