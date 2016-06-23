/**
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
package ninja.leaping.configurate.hocon;

import com.google.common.base.Joiner;
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
import com.typesafe.config.ConfigResolveOptions;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.loader.AbstractConfigurationLoader;
import ninja.leaping.configurate.loader.CommentHandler;
import ninja.leaping.configurate.loader.CommentHandlers;

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
 * A loader for HOCON (Hodor)-formatted configurations, using the typesafe config library for parsing
 */
public class HoconConfigurationLoader extends AbstractConfigurationLoader<CommentedConfigurationNode> {
    public static final Pattern CRLF_MATCH = Pattern.compile("\r\n?");
    private final ConfigRenderOptions render;
    private final ConfigParseOptions parse;

    public static class Builder extends AbstractConfigurationLoader.Builder<Builder> {
        private ConfigRenderOptions render = ConfigRenderOptions.defaults()
                .setOriginComments(false)
                .setJson(false);
        private ConfigParseOptions parse = ConfigParseOptions.defaults();

        protected Builder() {
        }

        public ConfigRenderOptions getRenderOptions() {
            return render;
        }

        public ConfigParseOptions getParseOptions() {
            return parse;
        }

        public Builder setRenderOptions(ConfigRenderOptions options) {
            this.render = options;
            return this;
        }

        public Builder setParseOptions(ConfigParseOptions options) {
            this.parse = options;
            return this;
        }

        @Override
        public HoconConfigurationLoader build() {
            return new HoconConfigurationLoader(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private HoconConfigurationLoader(Builder build) {
        super(build, new CommentHandler[] {CommentHandlers.HASH, CommentHandlers.DOUBLE_SLASH});
        this.render = build.getRenderOptions();
        this.parse = build.getParseOptions();
    }

    @Override
    public void loadInternal(CommentedConfigurationNode node, BufferedReader reader) throws IOException {
        Config hoconConfig = ConfigFactory.parseReader(reader, parse);
        hoconConfig = hoconConfig.resolve();
        for (Map.Entry<String, ConfigValue> ent : hoconConfig.root().entrySet()) {
            readConfigValue(ent.getValue(), node.getNode(ent.getKey()));
        }
    }

    private void readConfigValue(ConfigValue value, CommentedConfigurationNode node) {
        if (!value.origin().comments().isEmpty()) {
            node.setComment(CRLF_MATCH.matcher(Joiner.on('\n').join(value.origin().comments())).replaceAll("\n"));
        }
        switch (value.valueType()) {
            case OBJECT:
                if (((ConfigObject) value).isEmpty()) {
                    node.setValue(ImmutableMap.of());
                } else {
                    for (Map.Entry<String, ConfigValue> ent : ((ConfigObject) value).entrySet()) {
                        readConfigValue(ent.getValue(), node.getNode(ent.getKey()));
                    }
                }
                break;
            case LIST:
                List<ConfigValue> values = (ConfigList) value;
                for (int i = 0; i < values.size(); ++i) {
                    readConfigValue(values.get(i), node.getNode(i));
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
        if (!node.hasMapChildren()) {
            if (node.getValue() == null) {
                writer.write(LINE_SEPARATOR);
                return;
            } else {
                throw new IOException("HOCON cannot write nodes not in map format!");
            }
        }
        final ConfigValue value = fromValue(node);
        final String renderedValue = value.render(render);
        writer.write(renderedValue);
    }

    private static final ConfigOrigin CONFIGURATE_ORIGIN = ConfigOriginFactory.newSimple("configurate-hocon");

    private ConfigValue fromValue(ConfigurationNode node) {
        ConfigValue ret;
        if (node.hasMapChildren()) {
            Map<String, ConfigValue> children = node.getOptions().getMapFactory().create();
            for (Map.Entry<Object, ? extends ConfigurationNode> ent : node.getChildrenMap().entrySet()) {
                children.put(String.valueOf(ent.getKey()), fromValue(ent.getValue()));
            }
            ret = newConfigObject(children);
        } else if (node.hasListChildren()) {
            List<ConfigValue> children = new ArrayList<>();
            for (ConfigurationNode ent : node.getChildrenList()) {
                children.add(fromValue(ent));
            }
            ret = newConfigList(children);

        } else {
            ret = ConfigValueFactory.fromAnyRef(node.getValue(), "configurate-hocon");
        }
        if (node instanceof CommentedConfigurationNode) {
            CommentedConfigurationNode commentedNode = ((CommentedConfigurationNode) node);
            final ConfigValue finalRet = ret;
            ret = commentedNode.getComment().map(comment -> finalRet.withOrigin(finalRet.origin().withComments(LINE_SPLITTER.splitToList(comment)))).orElse(ret);
        }
        return ret;
    }

    ConfigValue newConfigObject(Map<String, ConfigValue> vals) {
        try {
            return CONFIG_OBJECT_CONSTRUCTOR.newInstance(CONFIGURATE_ORIGIN, vals);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e); // Again,rethrow
        }

    }

    ConfigValue newConfigList(List<ConfigValue> vals) {
        try {
            return CONFIG_LIST_CONSTRUCTOR.newInstance(CONFIGURATE_ORIGIN, vals);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e); // Should be rethrown
        }

    }


    @Override
    public CommentedConfigurationNode createEmptyNode(ConfigurationOptions options) {
        options = options.setAcceptedTypes(ImmutableSet.of(Map.class, List.class, Double.class,
                Long.class, Integer.class, Boolean.class, String.class, Number.class));
        return SimpleCommentedConfigurationNode.root(options);
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
