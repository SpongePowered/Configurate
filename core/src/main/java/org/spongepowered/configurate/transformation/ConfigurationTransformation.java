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

import java.util.NavigableMap;
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
     * A no-op configuration transformation.
     */
    private static final ConfigurationTransformation<?> EMPTY = new ConfigurationTransformation<ConfigurationNode>() {
        @Override
        public void apply(final @NonNull ConfigurationNode node) {
        }
    };

    /**
     * Get an empty transformation.
     *
     * <p>This transformation will perform no actions.</p>
     * @param <T> node type
     * @return empty transformation
     */
    @SuppressWarnings("unchecked") // shared empty instance
    public static <T extends ConfigurationNode> ConfigurationTransformation<T> empty() {
        return (ConfigurationTransformation<T>) EMPTY;
    }

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
        private final NavigableMap<NodePath, TransformAction<? super T>> actions;

        Builder() {
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
        private final NavigableMap<Integer, ConfigurationTransformation<? super T>> versions = new TreeMap<>();

        VersionedBuilder() {}

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
         * <p>The version must be between 0 and {@link Integer#MAX_VALUE}
         *
         * @param version The version
         * @param transformation The transformation
         * @return This builder (for chaining)
         */
        @NonNull
        public VersionedBuilder<T> addVersion(final int version, final @NonNull ConfigurationTransformation<? super T> transformation) {
            if (version < 0) {
                throw new IllegalArgumentException("Version must be at least 0");
            }
            this.versions.put(version, transformation);
            return this;
        }

        /**
         * Builds the transformation.
         *
         * @return The transformation
         */
        public ConfigurationTransformation.@NonNull Versioned<T> build() {
            return new VersionedTransformation<>(this.versionKey, this.versions);
        }
    }

    /**
     * A transformation that is aware of node versions.
     *
     * @param <N> node type
     */
    public abstract static class Versioned<N extends ConfigurationNode> extends ConfigurationTransformation<N> {
        public static final int VERSION_UNKNOWN = -1;

        /**
         * Get the path the node's current version is located at.
         *
         * @return version path
         */
        public abstract NodePath getVersionKey();

        /**
         * Get the latest version that nodes can be updated to.
         *
         * @return the most recent version
         */
        public abstract int getLatestVersion();

        /**
         * Get the version of a node hierarchy.
         *
         * <p>Note that the node checked here must be the same node passed to
         * {@link #apply(ConfigurationNode)}, not any node in a hierarchy.
         *
         * <p>If the node value is not present or not coercible to an integer,
         * {@link #VERSION_UNKNOWN} will be returned. When the transformation is
         * executed, every version transformation will be applied.
         *
         * @param node node to check
         * @return version, or {@link #VERSION_UNKNOWN} if no value is present
         */
        public int getVersion(final N node) {
            return node.getNode(getVersionKey()).getInt(VERSION_UNKNOWN);
        }
    }

}
