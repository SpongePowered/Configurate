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

import static java.util.Objects.requireNonNull;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ScopedConfigurationNode;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;

import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Consumer;

/**
 * Represents a set of transformations on a configuration.
 */
@FunctionalInterface
public interface ConfigurationTransformation<T extends ConfigurationNode> {

    /**
     * A special object that represents a wildcard in a path provided to a
     * configuration transformer.
     */
    Object WILDCARD_OBJECT = new Object();

    /**
     * Get an empty transformation.
     *
     * <p>This transformation will perform no actions.</p>
     *
     * @param <T> node type
     * @return empty transformation
     */
    static <T extends ConfigurationNode> ConfigurationTransformation<T> empty() {
        return node -> {};
    }

    /**
     * Create a new builder to create a basic configuration transformation.
     *
     *
     * @param <T> the type of node being processed
     * @return a new transformation builder.
     */
    static <T extends ScopedConfigurationNode<T>> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * This creates a builder for versioned transformations.
     *
     * @param <T> the type of node being processed
     * @return a new builder for versioned transformations
     */
    static <T extends ScopedConfigurationNode<T>> VersionedBuilder<T> versionedBuilder() {
        return new VersionedBuilder<>();
    }

    /**
     * Creates a chain of {@link ConfigurationTransformation}s.
     *
     * @param <T> the type of node being processed
     * @param transformations the transformations
     * @return a new transformation chain
     */
    @SafeVarargs
    @SuppressWarnings("unchecked")
    static <T extends ConfigurationNode> ConfigurationTransformation<T>
        chain(final ConfigurationTransformation<? super T>... transformations) {
        if (requireNonNull(transformations, "transformations").length == 0) {
            throw new IllegalArgumentException("Cannot chain an empty array of transformations!");
        }

        if (transformations.length == 1) {
            return (ConfigurationTransformation<T>) transformations[0];
        } else {
            return new ChainedConfigurationTransformation<>(transformations);
        }
    }

    /**
     * Apply this transformation to a given node.
     *
     * @param node the target node
     */
    void apply(T node) throws ObjectMappingException;

    /**
     * Builds a basic {@link ConfigurationTransformation}.
     */
    final class Builder<T extends ScopedConfigurationNode<T>> {
        private MoveStrategy strategy = MoveStrategy.OVERWRITE;
        private final NavigableMap<NodePath, TransformAction<? super T>> actions;

        Builder() {
            this.actions = new TreeMap<>(new NodePathComparator());
        }

        /**
         * Adds an action to the transformation.
         *
         * @param path the path to apply the action at
         * @param action the action
         * @return this builder (for chaining)
         */
        public Builder<T> addAction(final NodePath path, final TransformAction<? super T> action) {
            this.actions.put(requireNonNull(path, "path"), requireNonNull(action, "action"));
            return this;
        }

        /**
         * Gets the move strategy to be used by the resultant transformation.
         *
         * @return the move strategy
         */
        public MoveStrategy moveStrategy() {
            return this.strategy;
        }

        /**
         * Sets the mode strategy to be used by the resultant transformation.
         *
         * @param strategy the strategy
         * @return this builder (for chaining)
         */
        public Builder<T> moveStrategy(final MoveStrategy strategy) {
            this.strategy = requireNonNull(strategy, "strategy");
            return this;
        }

        /**
         * Builds the transformation.
         *
         * @return the transformation
         */
        public ConfigurationTransformation<T> build() {
            if (this.actions.isEmpty()) {
                throw new IllegalArgumentException("At least one action must be specified to build a transformation");
            }
            return new SingleConfigurationTransformation<>(this.actions, this.strategy);
        }
    }

    /**
     * Builds a versioned {@link ConfigurationTransformation}.
     */
    final class VersionedBuilder<T extends ScopedConfigurationNode<T>> {
        private NodePath versionKey = NodePath.path("version");
        private final NavigableMap<Integer, ConfigurationTransformation<? super T>> versions = new TreeMap<>();

        VersionedBuilder() {}

        /**
         * Sets the path of the version key within the configuration.
         *
         * @param versionKey the path to the version key
         * @return this builder (for chaining)
         */
        public VersionedBuilder<T> versionKey(final Object... versionKey) {
            this.versionKey = NodePath.of(versionKey);
            return this;
        }

        /**
         * Adds a transformation to this builder for the given version.
         *
         * <p>The version must be between 0 and {@link Integer#MAX_VALUE}, and a version cannot be specified multiple times.
         *
         * @param version the version
         * @param transformation the transformation
         * @return this builder (for chaining)
         */
        @NonNull
        public VersionedBuilder<T> addVersion(final int version, final @NonNull ConfigurationTransformation<? super T> transformation) {
            if (version < 0) {
                throw new IllegalArgumentException("Version must be at least 0");
            }
            if (this.versions.putIfAbsent(version, requireNonNull(transformation, "transformation")) != null) {
                throw new IllegalArgumentException("Version '" + version + "' has been specified multiple times.");
            }
            return this;
        }

        /**
         * Adds a new series of transformations for a version.
         *
         * <p>The version must be between 0 and {@link Integer#MAX_VALUE}.
         *
         * @param version the version
         * @param transformations the transformations. To perform a version
         *                        upgrade, these transformations will be
         *                        executed in order.
         * @return this builder
         */
        @SafeVarargs
        public final @NonNull VersionedBuilder<T> addVersion(final int version,
                final @NonNull ConfigurationTransformation<? super T>... transformations) {
            return this.addVersion(version, chain(transformations));
        }

        /**
         * Create and add a new transformation to this builder.
         *
         * <p>The transformation will be created from the builder passed to
         * the callback function</p>
         *
         * <p>The version must be between 0 and {@link Integer#MAX_VALUE}
         *
         * @param version the version
         * @param maker the transformation
         * @return this builder
         */
        public @NonNull VersionedBuilder<T> makeVersion(final int version, final @NonNull Consumer<? super Builder<? super T>> maker) {
            final Builder<T> builder = builder();
            maker.accept(builder);
            return this.addVersion(version, builder.build());
        }

        /**
         * Builds the transformation.
         *
         * @return the transformation
         */
        public ConfigurationTransformation.@NonNull Versioned<T> build() {
            if (this.versions.isEmpty()) {
                throw new IllegalArgumentException("At least one version must be specified to build a transformation");
            }
            return new VersionedTransformation<>(this.versionKey, this.versions);
        }
    }

    /**
     * A transformation that is aware of node versions.
     *
     * @param <N> node type
     */
    interface Versioned<N extends ConfigurationNode> extends ConfigurationTransformation<N> {
        int VERSION_UNKNOWN = -1;

        /**
         * Get the path the node's current version is located at.
         *
         * @return version path
         */
        NodePath versionKey();

        /**
         * Get the latest version that nodes can be updated to.
         *
         * @return the most recent version
         */
        int latestVersion();

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
        default int version(final N node) {
            return node.node(versionKey()).getInt(VERSION_UNKNOWN);
        }
    }

}
