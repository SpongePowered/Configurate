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
package org.spongepowered.configurate.hocon;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigOrigin;
import com.typesafe.config.ConfigOriginFactory;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;
import com.typesafe.config.impl.ConfigNodeComment;
import net.kyori.option.Option;
import net.kyori.option.OptionSchema;
import net.kyori.option.OptionState;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.CommentedConfigurationNodeIntermediary;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.loader.CommentHandler;
import org.spongepowered.configurate.loader.CommentHandlers;
import org.spongepowered.configurate.loader.ParsingException;
import org.spongepowered.configurate.util.UnmodifiableCollections;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A loader for HOCON (Hodor)-formatted configurations, using the
 * <a href="https://github.com/lightbend/config">lightbend config</a> library
 * for parsing and generation.
 *
 * @since 4.0.0
 */
public final class HoconConfigurationLoader extends AbstractConfigurationLoader<CommentedConfigurationNode> {

    private static final Set<Class<?>> NATIVE_TYPES = UnmodifiableCollections.toSet(
            Double.class, Long.class, Integer.class, Boolean.class, String.class, Number.class);

    /**
     * The default render options used by configurate.
     */
    private static final ConfigRenderOptions DEFAULT_RENDER_OPTIONS = ConfigRenderOptions.defaults()
            .setOriginComments(false)
            .setJson(false);

    /**
     * An instance of {@link ConfigOrigin} for configurate.
     */
    private static final ConfigOrigin CONFIGURATE_ORIGIN = ConfigOriginFactory.newSimple("configurate-hocon");

    /**
     * Creates a new {@link HoconConfigurationLoader} builder.
     *
     * @return a new builder
     * @since 4.0.0
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builds a {@link HoconConfigurationLoader}.
     *
     * <p>This builder supports the following options:</p>
     * <ul>
     *     <li>{@link #HEADER_MODE}</li>
     *     <li>{@link #PRETTY_PRINTING}</li>
     *     <li>{@link #INDENT}</li>
     *     <li>{@link #COMMENTS}</li>
     *     <li>{@link #JSON_COMPATIBLE}</li>
     * </ul>
     *
     * @since 4.0.0
     */
    public static final class Builder extends AbstractConfigurationLoader.Builder<Builder, HoconConfigurationLoader> {
        private static final OptionSchema.Mutable UNSAFE_SCHEMA = OptionSchema.childSchema(AbstractConfigurationLoader.Builder.SCHEMA);

        /**
         * A schema of options available to configure the Hocon loader.
         *
         * @since 4.2.0
         */
        public static final OptionSchema SCHEMA = UNSAFE_SCHEMA.frozenView();

        /**
         * Set whether output from this loader will be pretty-printed or not.
         *
         * @see #prettyPrinting(boolean)
         * @since 4.2.0
         */
        public static final Option<Boolean> PRETTY_PRINTING =
                UNSAFE_SCHEMA.booleanOption("hocon:pretty-printing", DEFAULT_RENDER_OPTIONS.getFormatted());

        /**
         * Set the amount of spaces to indent with when {@link #prettyPrinting(boolean)} is on.
         *
         * @see #indent(int)
         * @since 4.2.0
         */
        public static final Option<Integer> INDENT = UNSAFE_SCHEMA.intOption("hocon:indent", DEFAULT_RENDER_OPTIONS.getIndent());

        /**
         * Set whether comments will be emitted.
         *
         * @see #emitComments(boolean)
         * @since 4.2.0
         */
        public static final Option<Boolean> COMMENTS = UNSAFE_SCHEMA.booleanOption("hocon:emit-comments", DEFAULT_RENDER_OPTIONS.getComments());

        /**
         * Set whether JSON compatible output mode will be used.
         *
         * @see #emitJsonCompatible(boolean)
         * @since 4.2.0
         */
        public static final Option<Boolean> JSON_COMPATIBLE = UNSAFE_SCHEMA.booleanOption("hocon:json-compatible", DEFAULT_RENDER_OPTIONS.getJson());

        @Override
        protected OptionSchema optionSchema() {
            return SCHEMA;
        }

        /**
         * Set whether output from this loader will be pretty-printed or not.
         *
         * @param prettyPrinting whether to pretty-print
         * @return this builder
         * @since 4.0.0
         */
        public Builder prettyPrinting(final boolean prettyPrinting) {
            this.optionStateBuilder().value(PRETTY_PRINTING, prettyPrinting);
            return this;
        }

        /**
         * Set the amount of spaces to indent with when
         * {@link #prettyPrinting(boolean)} is on.
         *
         * <p>Defaults to 4.</p>
         *
         * @param indent indent level
         * @return this builder
         * @since 4.2.0
         */
        public Builder indent(final int indent) {
            this.optionStateBuilder().value(INDENT, indent);
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
         * @since 4.0.0
         */
        public Builder emitComments(final boolean emitComments) {
            this.optionStateBuilder().value(COMMENTS, emitComments);
            return this;
        }

        /**
         * Set whether output generated by this loader should be
         * json-compatible.
         *
         * <p>Whatever format input is received in, this will output
         * JSON. To be fully spec-compliant, comment output must also
         * be disabled.</p>
         *
         * @param jsonCompatible to emit json-format output
         * @return this builder
         * @since 4.0.0
         */
        public Builder emitJsonCompatible(final boolean jsonCompatible) {
            this.optionStateBuilder().value(JSON_COMPATIBLE, jsonCompatible);
            return this;
        }

        ConfigRenderOptions renderOptions() {
            final OptionState opt = this.optionState();
            return DEFAULT_RENDER_OPTIONS
                    .setFormatted(opt.value(PRETTY_PRINTING))
                    .setIndent(opt.value(INDENT))
                    .setComments(opt.value(COMMENTS))
                    .setJson(opt.value(JSON_COMPATIBLE));
        }

        @Override
        public HoconConfigurationLoader build() {
            defaultOptions(o -> o.nativeTypes(NATIVE_TYPES));
            return new HoconConfigurationLoader(this);
        }
    }

    private final ConfigRenderOptions render;

    private HoconConfigurationLoader(final Builder build) {
        super(build, new CommentHandler[] {CommentHandlers.HASH, CommentHandlers.DOUBLE_SLASH});
        this.render = build.renderOptions();
    }

    @Override
    protected void checkCanWrite(final ConfigurationNode node) throws ConfigurateException {
        if (!node.isMap() && !node.virtual() && node.raw() != null) {
            throw new ConfigurateException(node, "HOCON can only write nodes that are in map format!");
        }
    }

    @Override
    protected void loadInternal(final CommentedConfigurationNode node, final BufferedReader reader) throws ParsingException {
        Config hoconConfig;
        try {
            hoconConfig = ConfigFactory.parseReader(reader);
            hoconConfig = hoconConfig.resolve();
        } catch (final ConfigException ex) {
            throw new ParsingException(node, ex.origin().lineNumber(), 0, ex.origin().description(), null, ex);
        }

        for (Map.Entry<String, ConfigValue> ent : hoconConfig.root().entrySet()) {
            readConfigValue(ent.getValue(), node.node(ent.getKey()));
        }
    }

    private static void readConfigValue(final ConfigValue value, final CommentedConfigurationNode node) {
        if (!value.origin().comments().isEmpty()) {
            node.comment(value.origin().comments().stream()
                .map(input -> {
                    final String lineStripped = input.commentText().replace("\r", "");
                    if (!lineStripped.isEmpty() && lineStripped.charAt(0) == ' ') {
                        return lineStripped.substring(1);
                    } else {
                        return lineStripped;
                    }
                })
                .collect(Collectors.joining("\n")));
        }

        switch (value.valueType()) {
            case OBJECT:
                final ConfigObject object = (ConfigObject) value;
                if (object.isEmpty()) {
                    node.raw(Collections.emptyMap());
                } else {
                    for (Map.Entry<String, ConfigValue> ent : object.entrySet()) {
                        readConfigValue(ent.getValue(), node.node(ent.getKey()));
                    }
                }
                break;
            case LIST:
                final ConfigList list = (ConfigList) value;
                if (list.isEmpty()) {
                    node.raw(Collections.emptyList());
                } else {
                    for (int i = 0; i < list.size(); ++i) {
                        readConfigValue(list.get(i), node.node(i));
                    }
                }
                break;
            case NULL:
                return;
            default:
                node.raw(value.unwrapped());
                break;
        }
    }

    @Override
    protected void saveInternal(final ConfigurationNode node, final Writer writer) throws ConfigurateException {
        try {
            if (!node.isMap() && (node.virtual() || node.raw() == null)) {
                writer.write(SYSTEM_LINE_SEPARATOR);
                return;
            }
            final ConfigValue value = fromValue(node);
            final String renderedValue = value.render(this.render);
            writer.write(renderedValue);
        } catch (final IOException io) {
            throw new ConfigurateException(node, io);
        }
    }

    private static ConfigValue fromValue(final ConfigurationNode node) {
        ConfigValue ret;
        if (node.isMap()) {
            final Map<String, ConfigValue> children = node.options().mapFactory().create();
            for (Map.Entry<Object, ? extends ConfigurationNode> ent : node.childrenMap().entrySet()) {
                children.put(String.valueOf(ent.getKey()), fromValue(ent.getValue()));
            }
            ret = newConfigObject(children);
        } else if (node.isList()) {
            final List<ConfigValue> children = new ArrayList<>();
            for (ConfigurationNode ent : node.childrenList()) {
                children.add(fromValue(ent));
            }
            ret = newConfigList(children);

        } else {
            ret = ConfigValueFactory.fromAnyRef(node.rawScalar(), CONFIGURATE_ORIGIN.description());
        }
        if (node instanceof CommentedConfigurationNodeIntermediary<?>) {
            final CommentedConfigurationNodeIntermediary<?> commentedNode = (CommentedConfigurationNodeIntermediary<?>) node;
            final @Nullable String origComment = commentedNode.comment();
            if (origComment != null) {
                final List<ConfigNodeComment> nodes = new ArrayList<>();
                for (final String line : CONFIGURATE_LINE_PATTERN.split(origComment, -1)) {
                    if (line.length() != 0 && line.charAt(0) == '#') {
                        // allow lines that are only the comment character, for box drawing
                        nodes.add(ConfigNodeComment.hashComment(line));
                    } else {
                        nodes.add(ConfigNodeComment.hashComment(' ' + line));
                    }
                }
                ret = ret.withOrigin(ret.origin().withComments(nodes));
            }
        }
        return ret;
    }

    static ConfigValue newConfigObject(final Map<String, ConfigValue> vals) {
        try {
            return CONFIG_OBJECT_CONSTRUCTOR.newInstance(CONFIGURATE_ORIGIN, vals);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e); // rethrow
        }

    }

    static ConfigValue newConfigList(final List<ConfigValue> vals) {
        try {
            return CONFIG_LIST_CONSTRUCTOR.newInstance(CONFIGURATE_ORIGIN, vals);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e); // rethrow
        }
    }

    @Override
    public CommentedConfigurationNode createNode(final ConfigurationOptions options) {
        return CommentedConfigurationNode.root(options.nativeTypes(NATIVE_TYPES));
    }

    // -- Comment handling -- this might have to be updated as the hocon dep changes
    // (But tests should detect this breakage)
    private static final Constructor<? extends ConfigValue> CONFIG_OBJECT_CONSTRUCTOR;
    private static final Constructor<? extends ConfigValue> CONFIG_LIST_CONSTRUCTOR;

    static {
        final Class<? extends ConfigValue> objectClass;
        final Class<? extends ConfigValue> listClass;
        try {
            objectClass = Class.forName("com.typesafe.config.impl.SimpleConfigObject").asSubclass(ConfigValue.class);
            listClass = Class.forName("com.typesafe.config.impl.SimpleConfigList").asSubclass(ConfigValue.class);
        } catch (final ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }

        try {
            CONFIG_OBJECT_CONSTRUCTOR = objectClass.getDeclaredConstructor(ConfigOrigin.class, Map.class);
            CONFIG_OBJECT_CONSTRUCTOR.setAccessible(true);
            CONFIG_LIST_CONSTRUCTOR = listClass.getDeclaredConstructor(ConfigOrigin.class, List.class);
            CONFIG_LIST_CONSTRUCTOR.setAccessible(true);
        } catch (final NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

}
