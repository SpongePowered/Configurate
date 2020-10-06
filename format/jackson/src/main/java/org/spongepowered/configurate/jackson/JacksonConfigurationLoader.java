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
package org.spongepowered.configurate.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.loader.CommentHandler;
import org.spongepowered.configurate.loader.CommentHandlers;
import org.spongepowered.configurate.util.UnmodifiableCollections;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A loader for JSON-formatted configurations, using the jackson library for
 * parsing and generation.
 */
public final class JacksonConfigurationLoader extends AbstractConfigurationLoader<BasicConfigurationNode> {

    private static final Set<Class<?>> NATIVE_TYPES = UnmodifiableCollections.toSet(Map.class, List.class, Double.class, Float.class,
            Long.class, Integer.class, Boolean.class, String.class, byte[].class);

    /**
     * Creates a new {@link JacksonConfigurationLoader} builder.
     *
     * @return a new builder
     */
    @NonNull
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builds a {@link JacksonConfigurationLoader}.
     */
    public static final class Builder extends AbstractConfigurationLoader.Builder<Builder, JacksonConfigurationLoader> {
        private final JsonFactoryBuilder factory = new JsonFactoryBuilder();
        private int indent = 2;
        private FieldValueSeparatorStyle fieldValueSeparatorStyle = FieldValueSeparatorStyle.SPACE_AFTER;

        Builder() {
            this.factory.enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
                    .enable(JsonReadFeature.ALLOW_YAML_COMMENTS)
                    .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
                    .enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)
                    .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
                    .enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS)
                    .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS);
        }

        /**
         * Gets the {@link JsonFactory} used to configure the implementation.
         *
         * @return the json factory
         */
        @NonNull
        public JsonFactoryBuilder getFactoryBuilder() {
            return this.factory;
        }

        /**
         * Sets the level of indentation the resultant loader should use.
         *
         * @param indent the indent level
         * @return this builder (for chaining)
         */
        @NonNull
        public Builder setIndent(final int indent) {
            this.indent = indent;
            return this;
        }

        /**
         * Gets the level of indentation to be used by the resultant loader.
         *
         * @return the indent level
         */
        public int getIndent() {
            return this.indent;
        }

        /**
         * Sets the field value separator style the resultant loader should use.
         *
         * @param style the style
         * @return this builder (for chaining)
         */
        @NonNull
        public Builder setFieldValueSeparatorStyle(final @NonNull FieldValueSeparatorStyle style) {
            this.fieldValueSeparatorStyle = style;
            return this;
        }

        /**
         * Gets the field value separator style to be used by the built loader.
         *
         * @return the style
         */
        @NonNull
        public FieldValueSeparatorStyle getFieldValueSeparatorStyle() {
            return this.fieldValueSeparatorStyle;
        }

        @NonNull
        @Override
        public JacksonConfigurationLoader build() {
            setDefaultOptions(o -> o.withNativeTypes(NATIVE_TYPES));
            return new JacksonConfigurationLoader(this);
        }
    }

    private final JsonFactory factory;
    private final int indent;
    private final FieldValueSeparatorStyle fieldValueSeparatorStyle;

    private JacksonConfigurationLoader(final Builder builder) {
        super(builder, new CommentHandler[]{CommentHandlers.DOUBLE_SLASH, CommentHandlers.SLASH_BLOCK, CommentHandlers.HASH});
        this.factory = builder.getFactoryBuilder().build();
        this.factory.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        this.indent = builder.getIndent();
        this.fieldValueSeparatorStyle = builder.getFieldValueSeparatorStyle();
    }

    @Override
    protected void loadInternal(final BasicConfigurationNode node, final BufferedReader reader) throws IOException {
        try (JsonParser parser = this.factory.createParser(reader)) {
            parser.nextToken();
            parseValue(parser, node);
        }
    }

    private static void parseValue(final JsonParser parser, final ConfigurationNode node) throws IOException {
        final JsonToken token = parser.getCurrentToken();
        switch (token) {
            case START_OBJECT:
                parseObject(parser, node);
                break;
            case START_ARRAY:
                parseArray(parser, node);
                break;
            case VALUE_NUMBER_FLOAT:
                final double doubleVal = parser.getDoubleValue();
                if ((float) doubleVal != doubleVal) {
                    node.setValue(parser.getDoubleValue());
                } else {
                    node.setValue(parser.getFloatValue());
                }
                break;
            case VALUE_NUMBER_INT:
                final long longVal = parser.getLongValue();
                if ((int) longVal != longVal) {
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

    private static void parseArray(final JsonParser parser, final ConfigurationNode node) throws IOException {
        boolean written = false;
        JsonToken token;
        while ((token = parser.nextToken()) != null) {
            switch (token) {
                case END_ARRAY:
                    // ensure the type is preserved
                    if (!written) {
                        node.setValue(Collections.emptyList());
                    }
                    return;
                default:
                    parseValue(parser, node.appendListNode());
                    written = true;
            }
        }
        throw new JsonParseException(parser, "Reached end of stream with unclosed array!", parser.getCurrentLocation());
    }

    private static void parseObject(final JsonParser parser, final ConfigurationNode node) throws IOException {
        boolean written = false;
        JsonToken token;
        while ((token = parser.nextToken()) != null) {
            switch (token) {
                case END_OBJECT:
                    // ensure the type is preserved
                    if (!written) {
                        node.setValue(Collections.emptyMap());
                    }
                    return;
                default:
                    parseValue(parser, node.getNode(parser.getCurrentName()));
                    written = true;
            }
        }
        throw new JsonParseException(parser, "Reached end of stream with unclosed array!", parser.getCurrentLocation());
    }

    @Override
    public void saveInternal(final ConfigurationNode node, final Writer writer) throws IOException {
        try (JsonGenerator generator = this.factory.createGenerator(writer)) {
            generator.setPrettyPrinter(new ConfiguratePrettyPrinter(this.indent, this.fieldValueSeparatorStyle));
            JacksonVisitor.INSTANCE.get().visit(node, generator);
            writer.write(SYSTEM_LINE_SEPARATOR); // Jackson doesn't add a newline at the end of files by default
        }
    }

    @NonNull
    @Override
    public BasicConfigurationNode createNode(@NonNull ConfigurationOptions options) {
        options = options.withNativeTypes(NATIVE_TYPES);
        return BasicConfigurationNode.root(options);
    }

}
