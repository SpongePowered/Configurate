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
import org.spongepowered.configurate.loader.*;
import org.spongepowered.configurate.util.UnmodifiableCollections;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.*;
import org.yaml.snakeyaml.representer.Representer;

import java.io.BufferedReader;
import java.io.Writer;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;

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
     *     <dt>&lt;prefix&gt;.yaml.pretty-printing</dt>
     *     <dd>Equivalent to {@link #prettyPrinting(boolean)}</dd>
     *     <dt>&lt;prefix&gt;.yaml.node-style</dt>
     *     <dd>Equivalent to {@link #nodeStyle(NodeStyle)}</dd>
     *     <dt>&lt;prefix&gt;.yaml.emit-comments</dt>
     *     <dd>Equivalent to {@link #emitComments(boolean)}</dd>
     *     <dt>&lt;prefix&gt;.yaml.load-comments</dt>
     *     <dd>Equivalent to {@link #loadComments(boolean)}</dd>
     * </dl>
     *
     * @since 4.0.0
     */
    public static final class Builder extends AbstractConfigurationLoader.Builder<Builder, YamlConfigurationLoader> {
        private final DumperOptions options = new DumperOptions();
        private @Nullable NodeStyle style;
        private boolean loadComments;

        Builder() {
            this.indent(4);
            this.defaultOptions(o -> o.nativeTypes(NATIVE_TYPES));
            this.from(DEFAULT_OPTIONS_SOURCE);
        }

        @Override
        protected void populate(final LoaderOptionSource options) {
            this.options.setPrettyFlow(options.getBoolean(this.options.isPrettyFlow(), "yaml", "pretty-printing"));
            final @Nullable NodeStyle declared = options.getEnum(NodeStyle.class, "yaml", "node-style");
            if (declared != null) {
                this.style = declared;
            }
            this.options.setProcessComments(options.getBoolean(this.options.isProcessComments(), "yaml", "emit-comments"));
            this.loadComments = options.getBoolean(this.loadComments, "yaml", "load-comments");
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
         * Set whether output from this loader will be pretty-printed or not.
         *
         * @param prettyPrinting whether to pretty-print
         * @return this builder
         * @since 4.2.0
         */
        public Builder prettyPrinting(final boolean prettyPrinting) {
            this.options.setPrettyFlow(prettyPrinting);
            return this;
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
         * Set whether comments should be emitted.
         *
         * <p>Comments will always be loaded from files and
         * stored in memory.</p>
         *
         * @param emitComments whether to emit comments
         * @return this builder
         * @since 4.2.0
         */
        public Builder emitComments(final boolean emitComments) {
            this.options.setProcessComments(emitComments);
            return this;
        }

        /**
         * Set whether comments should be loaded and stored in memory.
         *
         * @param loadComments whether to load comments
         * @return this builder
         * @since 4.2.0
         */
        public Builder loadComments(final boolean loadComments) {
            this.loadComments = loadComments;
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

        @Override
        public YamlConfigurationLoader build() {
            return new YamlConfigurationLoader(this);
        }
    }

    private final ThreadLocal<PublicYaml> yaml;

    private YamlConfigurationLoader(final Builder builder) {
        super(builder, new CommentHandler[] {CommentHandlers.HASH});
        final LoaderOptions loaderOpts = new LoaderOptions()
            .setAcceptTabs(true)
            .setProcessComments(builder.loadComments);
        loaderOpts.setCodePointLimit(Integer.MAX_VALUE);

        final DumperOptions opts = builder.options;
        opts.setDefaultFlowStyle(NodeStyle.asSnakeYaml(builder.style));
        this.yaml = ThreadLocal.withInitial(() -> new PublicYaml(new PublicConstructor(loaderOpts), new Representer(opts), opts, loaderOpts));
    }

    @Override
    protected void loadInternal(final CommentedConfigurationNode node, final BufferedReader reader) {
        final PublicYaml yaml = this.yaml.get();
        readYamlNode(yaml.getConstructor(), realNode(yaml.compose(reader)), node);
    }

    private static Node realNode(final Node yamlNode) {
        if (yamlNode instanceof AnchorNode) {
            return ((AnchorNode) yamlNode).getRealNode();
        } else {
            return yamlNode;
        }
    }

    private static void readYamlNode(final PublicConstructor constructor, final Node yamlNode, final CommentedConfigurationNode node) {
        readComment(yamlNode.getBlockComments(), node);
        if (yamlNode instanceof MappingNode) {
            if (((MappingNode) yamlNode).getValue().isEmpty()) {
                node.raw(Collections.emptyMap());
            } else {
                for (NodeTuple tuple : ((MappingNode) yamlNode).getValue()) {
                    final ScalarNode keyNode = (ScalarNode) tuple.getKeyNode();
                    final CommentedConfigurationNode configNode = node.node(keyNode.getValue());

                    readComment(keyNode.getBlockComments(), configNode);
                    readYamlNode(constructor, realNode(tuple.getValueNode()), configNode);
                }
            }
        } else if (yamlNode instanceof SequenceNode) {
            if (((SequenceNode) yamlNode).getValue().isEmpty()) {
                node.raw(Collections.emptyList());
            } else {
                for (Node o : ((SequenceNode) yamlNode).getValue()) {
                    readYamlNode(constructor, realNode(o), node.appendListNode());
                }
            }
        } else {
            node.raw(constructor.constructObject(yamlNode));
        }
    }

    private static void readComment(final List<CommentLine> list, final CommentedConfigurationNode node) {
        if (list == null || node.comment() != null) {
            return;
        }
        final StringJoiner comment = new StringJoiner(CONFIGURATE_LINE_SEPARATOR);
        for (CommentLine line : list) {
            String s;
            if (line.getCommentType() == CommentType.BLANK_LINE) {
                s = "";
            } else {
                s = line.getValue().replace("\r", "");
                if (!s.isEmpty() && s.charAt(0) == ' ') {
                    s = s.substring(1);
                }
            }
            comment.add(s);
        }
        node.comment(comment.toString());
    }

    @Override
    protected void saveInternal(final ConfigurationNode node, final Writer writer) {
        final PublicYaml yaml = this.yaml.get();
        yaml.serialize(fromNode(yaml.getRepresenter(), node, true), writer);
    }

    private static Node fromNode(final Representer representer, final ConfigurationNode node, final boolean comment) {
        final Node yamlNode;
        if (node.isMap()) {
            final List<NodeTuple> value = new ArrayList<>();
            for (Map.Entry<Object, ? extends ConfigurationNode> entry : node.childrenMap().entrySet()) {
                final Node keyNode = representer.represent(entry.getKey());
                writeComment(entry.getValue(), keyNode);
                value.add(new NodeTuple(keyNode, fromNode(representer, entry.getValue(), false)));
            }
            yamlNode = new MappingNode(Tag.MAP, value, representer.getDefaultFlowStyle());
        } else if (node.isList()) {
            final List<Node> value = new ArrayList<>();
            for (ConfigurationNode child : node.childrenList()) {
                value.add(fromNode(representer, child, true));
            }
            yamlNode = new SequenceNode(Tag.SEQ, value, representer.getDefaultFlowStyle());
        } else {
            yamlNode = representer.represent(node.rawScalar());
        }
        if (comment) {
            writeComment(node, yamlNode);
        }
        return yamlNode;
    }

    private static void writeComment(final ConfigurationNode node, final Node yamlNode) {
        if (node instanceof CommentedConfigurationNode && ((CommentedConfigurationNode) node).comment() != null) {
            final List<CommentLine> comment = new ArrayList<>();
            for (String line : CONFIGURATE_LINE_PATTERN.split(((CommentedConfigurationNode) node).comment())) {
                if (line.trim().isEmpty()) {
                    comment.add(new CommentLine(null, null, "", CommentType.BLANK_LINE));
                } else {
                    comment.add(new CommentLine(null, null, ' ' + line, CommentType.BLOCK));
                }
            }
            yamlNode.setBlockComments(comment);
        }
    }

    @Override
    public CommentedConfigurationNode createNode(final ConfigurationOptions options) {
        return CommentedConfigurationNode.root(options);
    }

    static final class PublicYaml extends Yaml {

        private final PublicConstructor constructor;
        private final Representer representer;

        public PublicYaml(PublicConstructor constructor, Representer representer, DumperOptions dumperOptions, LoaderOptions loadingConfig) {
            super(constructor, representer, dumperOptions, loadingConfig);
            this.constructor = constructor;
            this.representer = representer;
        }

        public PublicConstructor getConstructor() {
            return constructor;
        }

        public Representer getRepresenter() {
            return representer;
        }
    }

    static final class PublicConstructor extends Constructor {

        public PublicConstructor(LoaderOptions loadingConfig) {
            super(loadingConfig);
        }

        @Override
        public Object constructObject(Node node) {
            return super.constructObject(node);
        }
    }

}
