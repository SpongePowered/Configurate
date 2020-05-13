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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigOrigin;
import com.typesafe.config.ConfigOriginFactory;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.*;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.loader.CommentHandler;
import org.spongepowered.configurate.loader.CommentHandlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A loader for HOCON (Hodor)-formatted configurations, using the typesafe config library for
 * parsing and generation.
 */
public class HoconConfigurationLoader extends AbstractConfigurationLoader<CommentedConfigurationNode> {

    /**
     * The pattern used to match newlines.
     */
    public static final Pattern CRLF_MATCH = Pattern.compile("\r?");

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
     * Gets the default {@link ConfigRenderOptions} used by configurate.
     *
     * @return The default render options
     */
    public static ConfigRenderOptions defaultRenderOptions() {
        return DEFAULT_RENDER_OPTIONS;
    }

    /**
     * Gets the default {@link ConfigParseOptions} used by configurate.
     *
     * @return The default parse options
     */
    public static ConfigParseOptions defaultParseOptions() {
        return ConfigParseOptions.defaults();
    }

    /**
     * Creates a new {@link HoconConfigurationLoader} builder.
     *
     * @return A new builder
     */
    @NonNull
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builds a {@link HoconConfigurationLoader}.
     */
    public static class Builder extends AbstractConfigurationLoader.Builder<Builder> {
        private ConfigRenderOptions render = defaultRenderOptions();
        private ConfigParseOptions parse = defaultParseOptions();

        protected Builder() {
        }

        /**
         * Sets the {@link ConfigRenderOptions} the resultant loader should use.
         *
         * @param options The render options
         * @return This builder (for chaining)
         */
        @NonNull
        public Builder setRenderOptions(@NonNull ConfigRenderOptions options) {
            this.render = options;
            return this;
        }

        /**
         * Gets the {@link ConfigRenderOptions} to be used by the resultant loader.
         *
         * @return The render options
         */
        @NonNull
        public ConfigRenderOptions getRenderOptions() {
            return render;
        }

        /**
         * Sets the {@link ConfigParseOptions} the resultant loader should use.
         *
         * @param options The parse options
         * @return This builder (for chaining)
         */
        @NonNull
        public Builder setParseOptions(ConfigParseOptions options) {
            this.parse = options;
            return this;
        }

        /**
         * Gets the {@link ConfigRenderOptions} to be used by the resultant loader.
         *
         * @return The render options
         */
        @NonNull
        public ConfigParseOptions getParseOptions() {
            return parse;
        }

        @NonNull
        @Override
        public HoconConfigurationLoader build() {
            return new HoconConfigurationLoader(this);
        }
    }

    private final ConfigRenderOptions render;
    private final ConfigParseOptions parse;

    private HoconConfigurationLoader(Builder build) {
        super(build, new CommentHandler[] {CommentHandlers.HASH, CommentHandlers.DOUBLE_SLASH});
        this.render = build.getRenderOptions();
        this.parse = build.getParseOptions();
    }

    @Override
    protected void loadInternal(CommentedConfigurationNode node, BufferedReader reader) throws IOException {
        Config hoconConfig = ConfigFactory.parseReader(reader, parse);
        hoconConfig = hoconConfig.resolve();
        for (Map.Entry<String, ConfigValue> ent : hoconConfig.root().entrySet()) {
            readConfigValue(ent.getValue(), node.getNode(ent.getKey()));
        }
    }

    private static void readConfigValue(ConfigValue value, CommentedConfigurationNode node) {
        if (!value.origin().comments().isEmpty()) {
            node.setComment(CRLF_MATCH.matcher(Joiner.on('\n').join(value.origin().comments())).replaceAll(""));
        }
        switch (value.valueType()) {
            case OBJECT:
                ConfigObject object = (ConfigObject) value;
                if (object.isEmpty()) {
                    node.setValue(ImmutableMap.of());
                } else {
                    for (Map.Entry<String, ConfigValue> ent : object.entrySet()) {
                        readConfigValue(ent.getValue(), node.getNode(ent.getKey()));
                    }
                }
                break;
            case LIST:
                ConfigList list = (ConfigList) value;
                if (list.isEmpty()) {
                    node.setValue(ImmutableList.of());
                } else {
                    for (int i = 0; i < list.size(); ++i) {
                        readConfigValue(list.get(i), node.getNode(i));
                    }
                }
                break;
            case NULL:
                return;
            default:
                node.setValue(value.unwrapped());
        }
    }

    @Override
    protected void saveInternal(ConfigurationNode node, Writer writer) throws IOException {
        if (!node.isMap()) {
            if (node.getValue() == null) {
                writer.write(SYSTEM_LINE_SEPARATOR);
                return;
            } else {
                throw new IOException("HOCON cannot write nodes not in map format!");
            }
        }
        final ConfigValue value = fromValue(node);
        final String renderedValue = value.render(render);
        writer.write(renderedValue);
    }

    private static ConfigValue fromValue(ConfigurationNode node) {
        ConfigValue ret;
        if (node.isMap()) {
            Map<String, ConfigValue> children = node.getOptions().getMapFactory().create();
            for (Map.Entry<Object, ? extends ConfigurationNode> ent : node.getChildrenMap().entrySet()) {
                children.put(String.valueOf(ent.getKey()), fromValue(ent.getValue()));
            }
            ret = newConfigObject(children);
        } else if (node.isList()) {
            List<ConfigValue> children = new ArrayList<>();
            for (ConfigurationNode ent : node.getChildrenList()) {
                children.add(fromValue(ent));
            }
            ret = newConfigList(children);

        } else {
            ret = ConfigValueFactory.fromAnyRef(node.getValue(), CONFIGURATE_ORIGIN.description());
        }
        if (node instanceof CommentedConfigurationNodeIntermediary<?>) {
            CommentedConfigurationNodeIntermediary<?> commentedNode = ((CommentedConfigurationNodeIntermediary<?>) node);
            final ConfigValue finalRet = ret;
            ret = commentedNode.getComment().map(comment -> finalRet.withOrigin(finalRet.origin().withComments(LINE_SPLITTER.splitToList(comment)))).orElse(ret);
        }
        return ret;
    }

    static ConfigValue newConfigObject(Map<String, ConfigValue> vals) {
        try {
            return CONFIG_OBJECT_CONSTRUCTOR.newInstance(CONFIGURATE_ORIGIN, vals);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e); // rethrow
        }

    }

    static ConfigValue newConfigList(List<ConfigValue> vals) {
        try {
            return CONFIG_LIST_CONSTRUCTOR.newInstance(CONFIGURATE_ORIGIN, vals);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e); // rethrow
        }
    }

    @NonNull
    @Override
    public CommentedConfigurationNode createEmptyNode(@NonNull ConfigurationOptions options) {
        options = options.withNativeTypes(ImmutableSet.of(Map.class, List.class, Double.class,
                Long.class, Integer.class, Boolean.class, String.class, Number.class));
        return CommentedConfigurationNode.root(options);
    }

    // -- Comment handling -- this might have to be updated as the hocon dep changes (But tests should detect this
    // breakage
    private static final Constructor<? extends ConfigValue> CONFIG_OBJECT_CONSTRUCTOR;
    private static final Constructor<? extends ConfigValue> CONFIG_LIST_CONSTRUCTOR;
    static {
        Class<? extends ConfigValue> objectClass, listClass;
        try {
            objectClass = Class.forName("com.typesafe.config.impl.SimpleConfigObject").asSubclass(ConfigValue.class);
            listClass = Class.forName("com.typesafe.config.impl.SimpleConfigList").asSubclass(ConfigValue.class);
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }

        try {
            CONFIG_OBJECT_CONSTRUCTOR = objectClass.getDeclaredConstructor(ConfigOrigin.class, Map.class);
            CONFIG_OBJECT_CONSTRUCTOR.setAccessible(true);
            CONFIG_LIST_CONSTRUCTOR = listClass.getDeclaredConstructor(ConfigOrigin.class, List.class);
            CONFIG_LIST_CONSTRUCTOR.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
