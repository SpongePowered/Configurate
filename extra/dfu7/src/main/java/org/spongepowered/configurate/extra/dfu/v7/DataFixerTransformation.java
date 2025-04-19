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
package org.spongepowered.configurate.extra.dfu.v7;

import static java.util.Objects.requireNonNull;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;

import java.util.HashSet;
import java.util.Set;

/**
 * A transformation that exposes a single DataFixer to a configuration in a
 * friendly way.
 *
 * <p>Because Configurate does not have a schema model and DFU does, this
 * transformation works by explicitly providing a mapping between configurate
 * node paths and DFU TypeReferences.</p>
 *
 * @since 4.3.0
 */
public final class DataFixerTransformation implements ConfigurationTransformation.Versioned {

    private final NodePath versionPath;
    private final int targetVersion;
    private final ConfigurationTransformation wrapped;
    private final ThreadLocal<Integer> versionHolder;

    /**
     * Create a builder that can work with any DFU DataFixer.
     *
     * @return the builder
     * @since 4.3.0
     */
    public static Builder dfuBuilder() {
        return new Builder();
    }

    DataFixerTransformation(final NodePath versionPath, final int targetVersion, final ConfigurationTransformation wrapped,
            final ThreadLocal<Integer> versionHolder) {
        this.versionPath = versionPath;
        this.targetVersion = targetVersion;
        this.wrapped = wrapped;
        this.versionHolder = versionHolder;
    }

    @Override
    public void apply(final @NonNull ConfigurationNode node) throws ConfigurateException {
        final ConfigurationNode versionNode = node.node(this.versionPath);
        final int currentVersion = versionNode.getInt(-1);
        if (currentVersion < this.targetVersion) {
            this.versionHolder.set(currentVersion);
            this.wrapped.apply(node);
            versionNode.set(this.targetVersion);
        } else if (currentVersion > this.targetVersion) {
            throw new ConfigurateException(node, "The target version (" + this.targetVersion
                    + ") is older than the data's current version (" + currentVersion + ")");
        }
    }

    /**
     * Get the version from a specific configuration node, using the configured
     * {@linkplain #versionKey() version key}.
     *
     * @param root base node to query
     * @return version, or -1 if this node is unversioned.
     */
    @Override
    public int version(final ConfigurationNode root) {
        return requireNonNull(root, "root").node(this.versionKey()).getInt(-1);
    }

    @Override
    public NodePath versionKey() {
        return this.versionPath;
    }

    @Override
    public int latestVersion() {
        return this.targetVersion;
    }

    /**
     * Builder for {@link DataFixerTransformation}.
     *
     * @since 4.3.0
     */
    public static class Builder {
        private NodePath versionPath = NodePath.path("dfu-version");
        private int targetVersion = -1;
        private @Nullable DataFixer fixer;
        private final Set<Pair<DSL.TypeReference, NodePath>> dataFixes = new HashSet<>();

        /**
         * Set the fixer to use to process.
         *
         * @param fixer the fixer
         * @return this builder
         * @since 4.3.0
         */
        public Builder dataFixer(final DataFixer fixer) {
            this.fixer = requireNonNull(fixer);
            return this;
        }

        /**
         * Set the path of the node to query and store the node's schema
         * version at.
         *
         * @param path the path
         * @return this builder
         * @since 4.3.0
         */
        public Builder versionKey(final Object... path) {
            this.versionPath = NodePath.of(requireNonNull(path, "path"));
            return this;
        }

        /**
         * Set the path of the node to query and store the node's schema
         * version at.
         *
         * @param path the path
         * @return this builder
         * @since 4.3.0
         */
        public Builder versionKey(final NodePath path) {
            this.versionPath = requireNonNull(path, "path");
            return this;
        }

        /**
         * Set the desired target version. If none is specified, the newest
         * available version will be determined from the DataFixer.
         *
         * @param targetVersion target version
         * @return this builder
         * @since 4.3.0
         */
        public Builder targetVersion(final int targetVersion) {
            this.targetVersion = targetVersion;
            return this;
        }

        /**
         * Map values at {@code path} to being of {@code type}.
         *
         * @param type value type reference
         * @param path target path
         * @return this builder
         * @since 4.3.0
         */
        public Builder addType(final DSL.TypeReference type, final Object... path) {
            return this.addType(type, NodePath.of(path));
        }

        /**
         * Map values at {@code path} to being of {@code type}.
         *
         * @param type value type reference
         * @param path target path
         * @return this builder
         * @since 4.3.0
         */
        public Builder addType(final DSL.TypeReference type, final NodePath path) {
            this.dataFixes.add(Pair.of(type, path));
            return this;
        }

        /**
         * Create a new transformation based on the provided info.
         *
         * @return new transformation
         * @since 4.3.0
         */
        public DataFixerTransformation build() {
            requireNonNull(this.fixer, "A fixer must be provided!");
            if (this.targetVersion == -1) {
                // DataFixer gets a schema by subsetting the sorted list of schemas with (0, version + 1), so we do max int - 1 to avoid overflow
                this.targetVersion = DataFixUtils.getVersion(this.fixer.getSchema(Integer.MAX_VALUE - 1).getVersionKey());
            }
            final ConfigurationTransformation.Builder wrappedBuilder = ConfigurationTransformation.builder();
            final ThreadLocal<Integer> versionHolder = new ThreadLocal<>();
            for (final Pair<DSL.TypeReference, NodePath> fix : this.dataFixes) {
                wrappedBuilder.addAction(fix.getSecond(), (path, valueAtPath) -> {
                    valueAtPath.from(this.fixer.update(fix.getFirst(), ConfigurateOps.wrap(valueAtPath),
                            versionHolder.get(), this.targetVersion).getValue());
                    return null;
                });
            }
            return new DataFixerTransformation(this.versionPath, this.targetVersion, wrappedBuilder.build(), versionHolder);
        }

    }

}
