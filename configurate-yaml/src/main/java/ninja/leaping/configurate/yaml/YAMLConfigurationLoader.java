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
package ninja.leaping.configurate.yaml;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.loader.AbstractConfigurationLoader;
import ninja.leaping.configurate.loader.CommentHandler;
import ninja.leaping.configurate.loader.CommentHandlers;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;


/**
 * A loader for YAML-formatted configurations, using the snakeyaml library for parsing
 */
public class YAMLConfigurationLoader extends AbstractConfigurationLoader<ConfigurationNode> {
    private final ThreadLocal<Yaml> yaml;

    public static class Builder extends AbstractConfigurationLoader.Builder<Builder> {
        private final DumperOptions options = new DumperOptions();

        protected Builder() {
            setIndent(4);
        }

        public Builder setIndent(int indent) {
            options.setIndent(indent);
            return this;
        }

        /**
         * Sets the flow style for this configuration
         * Flow: the compact, json-like representation.<br>
         * Example: <code>
         *     {value: [list, of, elements], another: value}
         * </code>
         *
         * Block: expanded, traditional YAML<br>
         * Example: <code>
         *     value:
         *     - list
         *     - of
         *     - elements
         *     another: value
         * </code>
         *
         * @param style the appropritae flow style to use
         * @return this
         */
        public Builder setFlowStyle(DumperOptions.FlowStyle style) {
            options.setDefaultFlowStyle(style);
            return this;
        }


        @Override
        public YAMLConfigurationLoader build() {
            return new YAMLConfigurationLoader(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private YAMLConfigurationLoader(Builder builder) {
        super(builder, new CommentHandler[] {CommentHandlers.HASH});
        final DumperOptions opts = builder.options;
        this.yaml = new ThreadLocal<Yaml>() {
            @Override
            protected Yaml initialValue() {
                return new Yaml(opts);
            }
        };
    }

    @Override
    protected void loadInternal(ConfigurationNode node, BufferedReader reader) throws IOException {
        node.setValue(yaml.get().load(reader));
    }

    @Override
    protected void saveInternal(ConfigurationNode node, Writer writer) throws IOException {
        yaml.get().dump(node.getValue(), writer);
    }

    @Override
    public ConfigurationNode createEmptyNode(ConfigurationOptions options) {
        return SimpleConfigurationNode.root(options);
    }
}
