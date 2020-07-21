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
package org.spongepowered.configurate.extra.dfu.v3;

import static java.util.Objects.requireNonNull;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ScopedConfigurationNode;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;
import org.spongepowered.configurate.transformation.NodePath;

import java.util.HashSet;
import java.util.Set;

/**
 * A transformation that exposes a single DataFixer to a configuration in a
 * friendly way.
 *
 * <p>Because Configurate does not have a schema model and DFU does, this
 * transformation works by explicitly providing a mapping between configurate
 * node paths and DFU TypeReferences.</p>
 */
public final class DataFixerTransformation<N extends ConfigurationNode> implements ConfigurationTransformation.Versioned<N> {

    private final NodePath versionPath;
    private final int targetVersion;
    private final ConfigurationTransformation<N> wrapped;
    private final ThreadLocal<Integer> versionHolder;

    /**
     * Create a builder that can work with any DFU DataFixer.
     *
     * @param <N> node type
     * @return the builder
     */
    public static <N extends ScopedConfigurationNode<N>> Builder<N> dfuBuilder() {
        return new Builder<>();
    }

    DataFixerTransformation(final NodePath versionPath, final int targetVersion, final ConfigurationTransformation<N> wrapped,
            final ThreadLocal<Integer> versionHolder) {
        this.versionPath = versionPath;
        this.targetVersion = targetVersion;
        this.wrapped = wrapped;
        this.versionHolder = versionHolder;
    }

    @Override
    public void apply(@NonNull final N node) {
        final ConfigurationNode versionNode = node.getNode(this.versionPath);
        final int currentVersion = versionNode.getInt(-1);
        if (currentVersion < this.targetVersion) {
            this.versionHolder.set(currentVersion);
            this.wrapped.apply(node);
            versionNode.setValue(this.targetVersion);
        } else if (currentVersion > this.targetVersion) {
            // TODO: Logging or throw error
        }
    }

    /**
     * Get the version from a specific configuration node, using the configured
     * {@linkplain #getVersionKey() version key}.
     *
     * @param root Base node to query
     * @return version, or -1 if this node is unversioned.
     */
    @Override
    public int getVersion(final ConfigurationNode root) {
        return requireNonNull(root, "root").getNode(getVersionKey()).getInt(-1);
    }

    @Override
    public NodePath getVersionKey() {
        return this.versionPath;
    }

    @Override
    public int getLatestVersion() {
        return this.targetVersion;
    }

    /**
     * Builder for {@link DataFixerTransformation}.
     */
    public static class Builder<N extends ScopedConfigurationNode<N>> {
        private NodePath versionPath = NodePath.path("dfu-version");
        private int targetVersion = -1;
        private @Nullable DataFixer fixer;
        private final Set<Pair<DSL.TypeReference, NodePath>> dataFixes = new HashSet<>();

        /**
         * Set the fixer to use to process.
         *
         * @param fixer the fixer
         * @return this
         */
        public Builder<N> setDataFixer(final DataFixer fixer) {
            this.fixer = requireNonNull(fixer);
            return this;
        }

        /**
         * Set the path of the node to query and store the node's schema
         * version at.
         *
         * @param path the path
         * @return this
         */
        public Builder<N> setVersionPath(final Object... path) {
            this.versionPath = NodePath.create(requireNonNull(path, "path"));
            return this;
        }

        /**
         * Set the path of the node to query and store the node's schema
         * version at.
         *
         * @param path the path
         * @return this
         */
        public Builder<N> setVersionPath(final NodePath path) {
            this.versionPath = requireNonNull(path, "path");
            return this;
        }

        /**
         * Set the desired target version. If none is specified, the newest
         * available version will be determined from the DataFixer.
         *
         * @param targetVersion target version
         * @return this
         */
        public Builder<N> setTargetVersion(final int targetVersion) {
            this.targetVersion = targetVersion;
            return this;
        }

        public Builder<N> type(final DSL.TypeReference type, final Object... path) {
            return type(type, NodePath.create(path));
        }

        public Builder<N> type(final DSL.TypeReference type, final NodePath path) {
            this.dataFixes.add(Pair.of(type, path));
            return this;
        }

        /**
         * Create a new transformation based on the provided info.
         *
         * @return new transformation
         */
        public DataFixerTransformation<N> build() {
            requireNonNull(this.fixer, "A fixer must be provided!");
            if (this.targetVersion == -1) {
                // DataFixer gets a schema by subsetting the sorted list of schemas with (0, version + 1), so we do max int - 1 to avoid overflow
                this.targetVersion = DataFixUtils.getVersion(this.fixer.getSchema(Integer.MAX_VALUE - 1).getVersionKey());
            }
            final ConfigurationTransformation.Builder<N> wrappedBuilder = ConfigurationTransformation.builder();
            final ThreadLocal<Integer> versionHolder = new ThreadLocal<>();
            for (Pair<DSL.TypeReference, NodePath> fix : this.dataFixes) {
                wrappedBuilder.addAction(fix.getSecond(), (path, valueAtPath) -> {
                    valueAtPath.setValue(this.fixer.update(fix.getFirst(), ConfigurateOps.wrap(valueAtPath),
                            versionHolder.get(), this.targetVersion).getValue());
                    return null;
                });
            }
            return new DataFixerTransformation<>(this.versionPath, this.targetVersion, wrappedBuilder.build(), versionHolder);
        }

    }

}
