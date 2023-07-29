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
package org.spongepowered.configurate.yaml;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.loader.CommentHandler;
import org.spongepowered.configurate.loader.CommentHandlers;
import org.spongepowered.configurate.loader.LoaderOptionSource;
import org.spongepowered.configurate.util.UnmodifiableCollections;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.Writer;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

/**
 * A loader for YAML-formatted configurations, using the SnakeYAML library for
 * parsing and generation.
 *
 * @since 4.0.0
 */
public final class YamlConfigurationLoader extends AbstractConfigurationLoader<CommentedConfigurationNode> {

    /**
     * YAML native types from <a href="https://yaml.org/type/">YAML 1.1 Global tags</a>.
     *
     * <p>using SnakeYaml representation: https://bitbucket.org/snakeyaml/snakeyaml/wiki/Documentation#markdown-header-yaml-tags-and-java-types
     */
    private static final Set<Class<?>> NATIVE_TYPES = UnmodifiableCollections.toSet(
            Boolean.class, Integer.class, Long.class, BigInteger.class, Double.class, // numeric
            byte[].class, String.class, Date.class, java.sql.Date.class, Timestamp.class); // complex types

    /**
     * Creates a new {@link YamlConfigurationLoader} builder.
     *
     * @return a new builder
     * @since 4.0.0
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builds a {@link YamlConfigurationLoader}.
     *
     * <p>This builder supports the following options:</p>
     * <dl>
     *     <dt>&lt;prefix&gt;.yaml.node-style</dt>
     *     <dd>Equivalent to {@link #nodeStyle(NodeStyle)}</dd>
     *     <dt>&lt;prefix&gt;.yaml.comments-enabled</dt>
     *     <dd>Equivalent to {@link #commentsEnabled(boolean)}</dd>
     * </dl>
     *
     * @since 4.0.0
     */
    public static final class Builder extends AbstractConfigurationLoader.Builder<Builder, YamlConfigurationLoader> {
        private final DumperOptions options = new DumperOptions();
        private @Nullable NodeStyle style;
        private boolean enableComments;

        Builder() {
            this.indent(4);
            this.defaultOptions(o -> o.nativeTypes(NATIVE_TYPES));
            this.from(DEFAULT_OPTIONS_SOURCE);
        }

        @Override
        protected void populate(final LoaderOptionSource options) {
            final @Nullable NodeStyle declared = options.getEnum(NodeStyle.class, "yaml", "node-style");
            if (declared != null) {
                this.style = declared;
            }
            this.enableComments = options.getBoolean(true, "yaml", "comments-enabled");
        }

        /**
         * Sets the level of indentation the resultant loader should use.
         *
         * @param indent the indent level
         * @return this builder (for chaining)
         * @since 4.0.0
         */
        public Builder indent(final int indent) {
            this.options.setIndent(indent);
            return this;
        }

        /**
         * Gets the level of indentation to be used by the resultant loader.
         *
         * @return the indent level
         * @since 4.0.0
         */
        public int indent() {
            return this.options.getIndent();
        }

        /**
         * Sets the node style the built loader should use.
         *
         * <dl><dt>Flow</dt>
         * <dd>the compact, json-like representation.<br>
         * Example: <code>
         *     {value: [list, of, elements], another: value}
         * </code></dd>
         *
         * <dt>Block</dt>
         * <dd>expanded, traditional YAML<br>
         * Example: <code>
         *     value:
         *     - list
         *     - of
         *     - elements
         *     another: value
         * </code></dd>
         * </dl>
         *
         * <p>A {@code null} value will tell the loader to pick a value
         * automatically based on the contents of each non-scalar node.</p>
         *
         * @param style the node style to use
         * @return this builder (for chaining)
         * @since 4.0.0
         */
        public Builder nodeStyle(final @Nullable NodeStyle style) {
            this.style = style;
            return this;
        }

        /**
         * Gets the node style to be used by the resultant loader.
         *
         * @return the node style
         * @since 4.0.0
         */
        public @Nullable NodeStyle nodeStyle() {
            return this.style;
        }

        /**
         * Set whether comment handling is enabled on this loader.
         *
         * <p>When comment handling is enabled, comments will be read from files
         * and written back to files where possible.</p>
         *
         * <p>The default value is {@code true}</p>
         *
         * @param enableComments whether comment handling should be enabled
         * @return this builder (for chaining)
         * @since 4.2.0
         */
        public Builder commentsEnabled(final boolean enableComments) {
            this.enableComments = enableComments;
            return this;
        }

        /**
         * Get whether comment handling is enabled.
         *
         * @return whether comment handling is enabled
         * @see #commentsEnabled(boolean) for details on comment handling
         * @since 4.2.0
         */
        public boolean commentsEnabled() {
            return this.enableComments;
        }

        @Override
        public YamlConfigurationLoader build() {
            return new YamlConfigurationLoader(this);
        }
    }

    private final ThreadLocal<YamlConstructor> constructor;
    private final ThreadLocal<Yaml> yaml;

    private YamlConfigurationLoader(final Builder builder) {
        super(builder, new CommentHandler[] {CommentHandlers.HASH});
        final LoaderOptions loaderOpts = new LoaderOptions()
            .setAcceptTabs(true)
            .setProcessComments(builder.commentsEnabled());
        loaderOpts.setCodePointLimit(Integer.MAX_VALUE);

        final DumperOptions opts = builder.options;
        opts.setDefaultFlowStyle(NodeStyle.asSnakeYaml(builder.style));
        opts.setProcessComments(builder.commentsEnabled());
        // the constructor needs ConfigurationOptions, which is only available when called (loadInternal)
        this.constructor = ThreadLocal.withInitial(() -> new YamlConstructor(loaderOpts));
        this.yaml = ThreadLocal.withInitial(() -> new Yaml(this.constructor.get(), new YamlRepresenter(opts), opts, loaderOpts));
    }

    @Override
    protected void loadInternal(final CommentedConfigurationNode node, final BufferedReader reader) {
        // the constructor needs ConfigurationOptions for the to be created nodes
        // and since it's a thread-local, this won't cause any issues
        this.constructor.get().options = node.options();
        node.from(this.yaml.get().load(reader));
    }

    @Override
    protected void saveInternal(final ConfigurationNode node, final Writer writer) {
        this.yaml.get().dump(node, writer);
    }

    @Override
    public CommentedConfigurationNode createNode(final ConfigurationOptions options) {
        return CommentedConfigurationNode.root(options);
    }

}
