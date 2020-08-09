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
package ninja.leaping.configurate.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.collect.ImmutableSet;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.AbstractConfigurationLoader;
import ninja.leaping.configurate.loader.CommentHandler;
import ninja.leaping.configurate.loader.CommentHandlers;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * A loader for JSON-formatted configurations, using the jackson library for parsing and generation.
 */
public class JSONConfigurationLoader extends AbstractConfigurationLoader<ConfigurationNode> {

    /**
     * Creates a new {@link JSONConfigurationLoader} builder.
     *
     * @return A new builder
     */
    @NonNull
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builds a {@link JSONConfigurationLoader}.
     */
    public static class Builder extends AbstractConfigurationLoader.Builder<Builder> {
        private final JsonFactory factory = new JsonFactory();
        private int indent = 2;
        private FieldValueSeparatorStyle fieldValueSeparatorStyle = FieldValueSeparatorStyle.SPACE_AFTER;

        protected Builder() {
            factory.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
            factory.enable(JsonParser.Feature.ALLOW_COMMENTS);
            factory.enable(JsonParser.Feature.ALLOW_YAML_COMMENTS);
            factory.enable(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
            factory.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
            factory.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
            factory.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
            factory.enable(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
        }

        /**
         * Gets the {@link JsonFactory} used to configure the implementation.
         *
         * @return The json factory
         */
        @NonNull
        public JsonFactory getFactory() {
            return this.factory;
        }

        /**
         * Sets the level of indentation the resultant loader should use.
         *
         * @param indent The indent level
         * @return This builder (for chaining)
         */
        @NonNull
        public Builder setIndent(int indent) {
            this.indent = indent;
            return this;
        }

        /**
         * Gets the level of indentation to be used by the resultant loader.
         *
         * @return The indent level
         */
        public int getIndent() {
            return this.indent;
        }

        /**
         * Sets the field value separator style the resultant loader should use.
         *
         * @param style The style
         * @return  This builder (for chaining)
         */
        @NonNull
        public Builder setFieldValueSeparatorStyle(@NonNull FieldValueSeparatorStyle style) {
            this.fieldValueSeparatorStyle = style;
            return this;
        }

        /**
         * Gets the field value separator style to be used by the resultant loader.
         *
         * @return The style
         */
        @NonNull
        public FieldValueSeparatorStyle getFieldValueSeparatorStyle() {
            return fieldValueSeparatorStyle;
        }

        @NonNull
        @Override
        public JSONConfigurationLoader build() {
            return new JSONConfigurationLoader(this);
        }
    }

    private final JsonFactory factory;
    private final int indent;
    private final FieldValueSeparatorStyle fieldValueSeparatorStyle;

    private JSONConfigurationLoader(Builder builder) {
        super(builder, new CommentHandler[]{CommentHandlers.DOUBLE_SLASH, CommentHandlers.SLASH_BLOCK, CommentHandlers.HASH});
        this.factory = builder.getFactory();
        this.indent = builder.getIndent();
        this.fieldValueSeparatorStyle = builder.getFieldValueSeparatorStyle();
    }

    @Override
    protected void loadInternal(ConfigurationNode node, BufferedReader reader) throws IOException {
        try (JsonParser parser = factory.createParser(reader)) {
            parser.nextToken();
            parseValue(parser, node);
        }
    }

    private static void parseValue(JsonParser parser, ConfigurationNode node) throws IOException {
        JsonToken token = parser.getCurrentToken();
        switch (token) {
            case START_OBJECT:
                parseObject(parser, node);
                break;
            case START_ARRAY:
                parseArray(parser, node);
                break;
            case VALUE_NUMBER_FLOAT:
                double doubleVal = parser.getDoubleValue();
                if ((float)doubleVal != doubleVal) {
                    node.setValue(parser.getDoubleValue());
                } else {
                    node.setValue(parser.getFloatValue());
                }
                break;
            case VALUE_NUMBER_INT:
                long longVal = parser.getLongValue();
                if ((int)longVal != longVal) {
                    node.setValue(parser.getLongValue());
                } else {
                    node.setValue(parser.getIntValue());
                }
                break;
            case VALUE_STRING:
                node.setValue(parser.getText());
                break;
            case VALUE_TRUE:
            case VALUE_FALSE:
                node.setValue(parser.getBooleanValue());
                break;
            case VALUE_NULL: // Ignored values
            case FIELD_NAME:
                break;
            default:
                throw new IOException("Unsupported token type: " + token + " (at " + parser.getTokenLocation() + ")");
        }
    }

    private static void parseArray(JsonParser parser, ConfigurationNode node) throws IOException {
        JsonToken token;
        while ((token = parser.nextToken()) != null) {
            switch (token) {
                case END_ARRAY:
                    return;
                default:
                    parseValue(parser, node.appendListNode());
            }
        }
        throw new JsonParseException(parser, "Reached end of stream with unclosed array!", parser.getCurrentLocation());
    }

    private static void parseObject(JsonParser parser, ConfigurationNode node) throws IOException {
        JsonToken token;
        while ((token = parser.nextToken()) != null) {
            switch (token) {
                case END_OBJECT:
                    return;
                default:
                    parseValue(parser, node.getNode(parser.getCurrentName()));
            }
        }
        throw new JsonParseException(parser, "Reached end of stream with unclosed array!", parser.getCurrentLocation());
    }

    @Override
    public void saveInternal(ConfigurationNode node, Writer writer) throws IOException {
        try (JsonGenerator generator = factory.createGenerator(writer)) {
            generator.setPrettyPrinter(new ConfiguratePrettyPrinter(indent, fieldValueSeparatorStyle));
            node.visit(JacksonVisitor.INSTANCE.get(), generator);
            writer.write(SYSTEM_LINE_SEPARATOR); // Jackson doesn't add a newline at the end of files by default
        }
    }

    @NonNull
    @Override
    public CommentedConfigurationNode createEmptyNode(@NonNull ConfigurationOptions options) {
        options = options.withNativeTypes(ImmutableSet.of(Map.class, List.class, Double.class, Float.class,
                Long.class, Integer.class, Boolean.class, String.class, byte[].class));
        return CommentedConfigurationNode.root(options);
    }
}
