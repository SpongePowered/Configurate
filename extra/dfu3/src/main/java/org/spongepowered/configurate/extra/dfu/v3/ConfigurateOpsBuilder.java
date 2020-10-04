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

import com.mojang.serialization.Dynamic;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.util.function.Supplier;

/**
 * A builder for {@link ConfigurateOps} instances.
 */
public final class ConfigurateOpsBuilder {

    private Supplier<ConfigurationNode> nodeSupplier = ConfigurateOpsBuilder::createDefaultNode;
    private boolean compressed = false;
    private ConfigurateOps.Protection readProtection = ConfigurateOps.Protection.COPY_DEEP;
    private ConfigurateOps.Protection writeProtection = ConfigurateOps.Protection.COPY_DEEP;

    ConfigurateOpsBuilder() {}

    static ConfigurationNode createDefaultNode() {
        return CommentedConfigurationNode.root();
    }

    /**
     * Set the node factory for the returned ops.
     *
     * <p>The default node factory wil create {@link CommentedConfigurationNode}
     * instances using Confabricate's minecraft serializers.
     *
     * @param supplier source for new nodes created to store values in
     *     the {@code create*} methods
     * @return this
     */
    public ConfigurateOpsBuilder factory(final Supplier<ConfigurationNode> supplier) {
        this.nodeSupplier = requireNonNull(supplier, "nodeSupplier");
        return this;
    }

    /**
     * Set a node factory that will use the provided collection.
     *
     * <p>This will replace any set {@link #factory(Supplier)}.
     *
     * @param collection type serializers to use for nodes.
     * @return this
     */
    public ConfigurateOpsBuilder factoryFromSerializers(final TypeSerializerCollection collection) {
        requireNonNull(collection, "collection");
        return factory(() -> CommentedConfigurationNode.root(ConfigurationOptions.defaults().withSerializers(collection)));
    }

    /**
     * Set the node factory based on the options of the provided node.
     *
     * @param node node to use
     * @return this builder
     */
    public ConfigurateOpsBuilder factoryFromNode(final ConfigurationNode node) {
        final ConfigurationOptions options = requireNonNull(node, "node").getOptions();
        return factory(() -> CommentedConfigurationNode.root(options));
    }

    /**
     * Set whether {@link com.mojang.serialization.Keyable} values should be compressed.
     *
     * @param compressed whether to compress values
     * @return this
     * @see ConfigurateOps#compressMaps() for more about what compression is
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
     * @return this
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
     * @return this
     */
    public ConfigurateOpsBuilder writeProtection(final ConfigurateOps.Protection writeProtection) {
        this.writeProtection = requireNonNull(writeProtection, "writeProtection");
        return this;
    }

    /**
     * Set how nodes will be protected from both read and write modifications.
     *
     * @param protection protection level
     * @return this
     * @see #readProtection(ConfigurateOps.Protection) for how this level
     *      affects value reads
     * @see #writeProtection(ConfigurateOps.Protection) for how this level
     *      affects value writes
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
     * @return The new instance
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
     */
    public Dynamic<ConfigurationNode> buildWrapping(final ConfigurationNode node) {
        return new Dynamic<>(build(), node);
    }

}
