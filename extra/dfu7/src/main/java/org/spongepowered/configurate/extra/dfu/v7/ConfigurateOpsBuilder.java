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

import com.mojang.serialization.Dynamic;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationNodeFactory;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

/**
 * A builder for {@link ConfigurateOps} instances.
 *
 * @since 4.3.0
 */
public final class ConfigurateOpsBuilder {

    private ConfigurationNodeFactory<? extends ConfigurationNode> nodeSupplier = CommentedConfigurationNode.factory();
    private boolean compressed;
    private ConfigurateOps.Protection readProtection = ConfigurateOps.Protection.COPY_DEEP;
    private ConfigurateOps.Protection writeProtection = ConfigurateOps.Protection.COPY_DEEP;

    ConfigurateOpsBuilder() {}

    /**
     * Set the node factory for the returned ops.
     *
     * <p>The default node factory wil create {@link CommentedConfigurationNode}
     * instances using Confabricate's minecraft serializers.
     *
     * @param supplier source for new nodes created to store values in
     *     the {@code create*} methods
     * @return this builder
     * @since 4.3.0
     */
    public ConfigurateOpsBuilder factory(final ConfigurationNodeFactory<? extends ConfigurationNode> supplier) {
        this.nodeSupplier = requireNonNull(supplier, "nodeSupplier");
        return this;
    }

    /**
     * Set a node factory that will use the provided collection.
     *
     * <p>This will replace any set {@link #factory(ConfigurationNodeFactory)}.
     *
     * @param collection type serializers to use for nodes.
     * @return this builder
     * @since 4.3.0
     */
    public ConfigurateOpsBuilder factoryFromSerializers(final TypeSerializerCollection collection) {
        requireNonNull(collection, "collection");
        return factory(options -> CommentedConfigurationNode.root(options.serializers(collection)));
    }

    /**
     * Set the node factory based on the options of the provided node.
     *
     * <p>This will replace any set {@link #factory(ConfigurationNodeFactory)}.
     *
     * @param node node to use
     * @return this builder
     * @since 4.3.0
     */
    public ConfigurateOpsBuilder factoryFromNode(final ConfigurationNode node) {
        final ConfigurationOptions options = requireNonNull(node, "node").options();
        return factory(new ConfigurationNodeFactory<ConfigurationNode>() {
            @Override
            public ConfigurationNode createNode(final ConfigurationOptions options) {
                return CommentedConfigurationNode.root(options);
            }

            @Override public ConfigurationOptions defaultOptions() {
                return options;
            }
        });
    }

    /**
     * Set whether {@link com.mojang.serialization.Keyable} values should be compressed.
     *
     * @param compressed whether to compress values
     * @return this builder
     * @see ConfigurateOps#compressMaps() for more about what compression is
     * @since 4.3.0
     */
    public ConfigurateOpsBuilder compressed(final boolean compressed) {
        this.compressed = compressed;
        return this;
    }

    /**
     * Set how nodes returned from read methods will be protected
     * from modification.
     *
     * <p>For read protection, the protection level refers to how the attached
     * node will be affected by modifications made to the nodes returned from
     * {@code get*} methods.
     *
     * @param readProtection protection level
     * @return this builder
     * @since 4.3.0
     */
    public ConfigurateOpsBuilder readProtection(final ConfigurateOps.Protection readProtection) {
        this.readProtection = requireNonNull(readProtection, "readProtection");
        return this;
    }

    /**
     * Set how nodes provided to mutator methods will be protected
     * from modification.
     *
     * <p>For write protection, the protection level refers to how the provided
     * {@code prefix} node will be protected from seeing changes to the
     * operation
     *
     * @param writeProtection protection level
     * @return this builder
     * @since 4.3.0
     */
    public ConfigurateOpsBuilder writeProtection(final ConfigurateOps.Protection writeProtection) {
        this.writeProtection = requireNonNull(writeProtection, "writeProtection");
        return this;
    }

    /**
     * Set how nodes will be protected from both read and write modifications.
     *
     * @param protection protection level
     * @return this builder
     * @see #readProtection(ConfigurateOps.Protection) for how this level
     *      affects value reads
     * @see #writeProtection(ConfigurateOps.Protection) for how this level
     *      affects value writes
     * @since 4.3.0
     */
    public ConfigurateOpsBuilder readWriteProtection(final ConfigurateOps.Protection protection) {
        requireNonNull(protection, "protection");
        this.readProtection = protection;
        this.writeProtection = protection;
        return this;
    }

    /**
     * Create a new ops instance.
     *
     * <p>All options have defaults provided and all setters validate their
     * input, so by the time this method is reached the builder will be in a
     * valid state.
     *
     * @return the new instance
     * @since 4.3.0
     */
    public ConfigurateOps build() {
        return new ConfigurateOps(this.nodeSupplier, this.compressed, this.readProtection, this.writeProtection);
    }

    /**
     * Build a new ops instance, returned as part of a {@linkplain Dynamic}.
     *
     * <p>Returned ops instances will not take type serializers or other options
     * from the provided node. For that, use {@link #factoryFromNode(ConfigurationNode)}.
     *
     * @param node wrapped node
     * @return new dynamic
     * @since 4.3.0
     */
    public Dynamic<ConfigurationNode> buildWrapping(final ConfigurationNode node) {
        return new Dynamic<>(build(), node);
    }

}
