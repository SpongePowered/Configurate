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

import net.kyori.option.Option;
import net.kyori.option.OptionSchema;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.loader.CommentHandler;
import org.spongepowered.configurate.loader.CommentHandlers;
import org.spongepowered.configurate.util.UnmodifiableCollections;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

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
     * </dl>
     *
     * @since 4.0.0
     */
    public static final class Builder extends AbstractConfigurationLoader.Builder<Builder, YamlConfigurationLoader> {

        private static final OptionSchema.Mutable UNSAFE_SCHEMA = OptionSchema.childSchema(AbstractConfigurationLoader.Builder.SCHEMA);

        /**
         * A schema of options available to configure the YAML loader.
         *
         * @since 4.2.0
         */
        public static final OptionSchema SCHEMA = UNSAFE_SCHEMA.frozenView();

        /**
         * The collection node style to use globally when emitting with
         * this loader.
         *
         * @see #nodeStyle(NodeStyle)
         * @since 4.2.0
         */
        public static final Option<NodeStyle> NODE_STYLE = UNSAFE_SCHEMA.enumOption("yaml:node_style", NodeStyle.class, null);

        /**
         * The indent size (in spaces) to use for documents emitted by
         * the created loader.
         *
         * @see #indent(int)
         * @since 4.2.0
         */
        public static final Option<Integer> INDENT = UNSAFE_SCHEMA.intOption("yaml:indent", 4);

        private final DumperOptions options = new DumperOptions();

        Builder() {
            this.defaultOptions(o -> o.nativeTypes(NATIVE_TYPES));
        }

        @Override
        protected OptionSchema optionSchema() {
            return SCHEMA;
        }

        /**
         * Sets the level of indentation the resultant loader should use.
         *
         * @param indent the indent level
         * @return this builder (for chaining)
         * @since 4.0.0
         */
        public Builder indent(final int indent) {
            this.optionStateBuilder().value(INDENT, indent);
            return this;
        }

        /**
         * Gets the level of indentation to be used by the resultant loader.
         *
         * @return the indent level
         * @since 4.0.0
         */
        public int indent() {
            return this.optionState().value(INDENT);
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
            this.optionStateBuilder().value(NODE_STYLE, style);
            return this;
        }

        /**
         * Gets the node style to be used by the resultant loader.
         *
         * @return the node style
         * @since 4.0.0
         */
        public @Nullable NodeStyle nodeStyle() {
            return this.optionState().value(NODE_STYLE);
        }

        @Override
        public YamlConfigurationLoader build() {
            return new YamlConfigurationLoader(this);
        }
    }

    private final LoaderOptions loaderOpts;
    private final DumperOptions dumperOpts;

    private YamlConfigurationLoader(final Builder builder) {
        super(builder, new CommentHandler[] {CommentHandlers.HASH});
        this.loaderOpts = new LoaderOptions()
            .setAcceptTabs(true)
            .setProcessComments(false);
        this.loaderOpts.setCodePointLimit(Integer.MAX_VALUE);

        this.dumperOpts = builder.options;
        this.dumperOpts.setDefaultFlowStyle(NodeStyle.asSnakeYaml(builder.optionState().value(Builder.NODE_STYLE)));
        this.dumperOpts.setIndent(builder.optionState().value(Builder.INDENT));
    }

    @Override
    protected void loadInternal(final CommentedConfigurationNode node, final BufferedReader reader) {
        final Yaml yaml = new Yaml(new Constructor(this.loaderOpts), new Representer(this.dumperOpts), this.dumperOpts, this.loaderOpts);
        node.raw(yaml.load(reader));
    }

    @Override
    protected void saveInternal(final ConfigurationNode node, final Writer writer) {
        final Yaml yaml = new Yaml(new Constructor(this.loaderOpts), new Representer(this.dumperOpts), this.dumperOpts, this.loaderOpts);
        yaml.dump(node.raw(), writer);
    }

    @Override
    public CommentedConfigurationNode createNode(final ConfigurationOptions options) {
        return CommentedConfigurationNode.root(options);
    }

}
