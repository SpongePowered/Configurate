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
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import net.kyori.option.Option;
import net.kyori.option.OptionSchema;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.BasicConfigurationNode;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A loader for JSON-formatted configurations, using the jackson library for
 * parsing and generation.
 *
 * @since 4.0.0
 */
public final class JacksonConfigurationLoader extends AbstractConfigurationLoader<BasicConfigurationNode> {

    private static final Set<Class<?>> NATIVE_TYPES = UnmodifiableCollections.toSet(Map.class, List.class, Double.class, Float.class,
            Long.class, Integer.class, Boolean.class, String.class, byte[].class);

    /**
     * Creates a new {@link JacksonConfigurationLoader} builder.
     *
     * @return a new builder
     * @since 4.0.0
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builds a {@link JacksonConfigurationLoader}.
     *
     * <p>This builder supports the following options:</p>
     * <ul>
     *     <li>{@link #INDENT}</li>
     *     <li>{@link #FIELD_VALUE_SEPARATOR}</li>
     * </ul>
     *
     * @since 4.0.0
     */
    public static final class Builder extends AbstractConfigurationLoader.Builder<Builder, JacksonConfigurationLoader> {

        private static final OptionSchema.Mutable UNSAFE_SCHEMA = OptionSchema.childSchema(AbstractConfigurationLoader.Builder.SCHEMA);

        /**
         * A schema of options available to configure the Jackson loader.
         *
         * @since 4.2.0
         */
        public static final OptionSchema SCHEMA = UNSAFE_SCHEMA.frozenView();

        /**
         * Set the indentation to when emitting json.
         *
         * @see #indent(int)
         * @since 4.2.0
         */
        public static final Option<Integer> INDENT = UNSAFE_SCHEMA.intOption("jackson:indent", 2);

        /**
         * Set the field-value separator style to be used when emitting json.
         *
         * @see #fieldValueSeparatorStyle(FieldValueSeparatorStyle)
         * @since 4.2.0
         */
        public static final Option<FieldValueSeparatorStyle> FIELD_VALUE_SEPARATOR = UNSAFE_SCHEMA.enumOption(
            "jackson:field-value-separator",
            FieldValueSeparatorStyle.class,
            FieldValueSeparatorStyle.SPACE_AFTER
        );

        private final JsonFactoryBuilder factory = new JsonFactoryBuilder();

        Builder() {
            this.factory.enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
                    .enable(JsonReadFeature.ALLOW_YAML_COMMENTS)
                    .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
                    .enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)
                    .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
                    .enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS)
                    .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS);
        }

        @Override
        protected OptionSchema optionSchema() {
            return SCHEMA;
        }

        /**
         * Gets the {@link JsonFactory} used to configure the implementation.
         *
         * @return the json factory
         * @since 4.0.0
         */
        public JsonFactoryBuilder factoryBuilder() {
            return this.factory;
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
         * Sets the field value separator style the resultant loader should use.
         *
         * @param style the style
         * @return this builder (for chaining)
         * @since 4.0.0
         */
        public Builder fieldValueSeparatorStyle(final FieldValueSeparatorStyle style) {
            this.optionStateBuilder().value(FIELD_VALUE_SEPARATOR, style);
            return this;
        }

        /**
         * Gets the field value separator style to be used by the built loader.
         *
         * @return the style
         * @since 4.0.0
         */
        public FieldValueSeparatorStyle fieldValueSeparatorStyle() {
            return this.optionState().value(FIELD_VALUE_SEPARATOR);
        }

        @Override
        public JacksonConfigurationLoader build() {
            defaultOptions(o -> o.nativeTypes(NATIVE_TYPES));
            return new JacksonConfigurationLoader(this);
        }
    }

    private final JsonFactory factory;
    private final int indent;
    private final FieldValueSeparatorStyle fieldValueSeparatorStyle;

    private JacksonConfigurationLoader(final Builder builder) {
        super(builder, new CommentHandler[]{CommentHandlers.DOUBLE_SLASH, CommentHandlers.SLASH_BLOCK, CommentHandlers.HASH});
        this.factory = builder.factoryBuilder().build();
        this.factory.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        this.indent = builder.optionState().value(Builder.INDENT);
        this.fieldValueSeparatorStyle = builder.optionState().value(Builder.FIELD_VALUE_SEPARATOR);
    }

    private static final int MAX_CTX_LENGTH = 80;

    @Override
    protected void loadInternal(final BasicConfigurationNode node, final BufferedReader reader) throws ParsingException {
        try (JsonParser parser = this.factory.createParser(reader)) {
            parser.nextToken();
            parseValue(parser, node);
        } catch (final StreamReadException ex) {
            throw newException(node, ex.getLocation(), ex.getRequestPayloadAsString(), ex.getMessage(), ex.getCause());
        } catch (final IOException ex) {
            throw ParsingException.wrap(node, ex);
        }
    }

    private static void parseValue(final JsonParser parser, final ConfigurationNode node) throws IOException {
        try {
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
                        node.raw(parser.getDoubleValue());
                    } else {
                        node.raw(parser.getFloatValue());
                    }
                    break;
                case VALUE_NUMBER_INT:
                    final long longVal = parser.getLongValue();
                    if ((int) longVal != longVal) {
                        node.raw(parser.getLongValue());
                    } else {
                        node.raw(parser.getIntValue());
                    }
                    break;
                case VALUE_STRING:
                    node.raw(parser.getText());
                    break;
                case VALUE_TRUE:
                case VALUE_FALSE:
                    node.raw(parser.getBooleanValue());
                    break;
                case VALUE_NULL: // Ignored values
                case FIELD_NAME:
                    break;
                default:
                    final JsonLocation loc = JacksonCompat.currentTokenLocation(parser);
                    throw new ParsingException(node, loc.getLineNr(), loc.getColumnNr(), parser.getText(), "Unsupported token type: " + token, null);
            }
        } catch (final StreamReadException ex) {
            throw newException(node, ex.getLocation(), ex.getRequestPayloadAsString(), ex.getMessage(), ex.getCause());
        }
    }

    private static void parseArray(final JsonParser parser, final ConfigurationNode node) throws IOException {
        boolean written = false;
        JsonToken token;
        while ((token = parser.nextToken()) != null) {
            if (token == JsonToken.END_ARRAY) { // ensure the type is preserved
                if (!written) {
                    node.raw(Collections.emptyList());
                }
                return;
            } else {
                parseValue(parser, node.appendListNode());
                written = true;
            }
        }
        throw newException(node, JacksonCompat.currentLocation(parser), null, "Reached end of stream with unclosed array!", null);
    }

    private static void parseObject(final JsonParser parser, final ConfigurationNode node) throws IOException {
        boolean written = false;
        JsonToken token;
        while ((token = parser.nextToken()) != null) {
            if (token == JsonToken.END_OBJECT) { // ensure the type is preserved
                if (!written) {
                    node.raw(Collections.emptyMap());
                }
                return;
            } else {
                parseValue(parser, node.node(JacksonCompat.currentName(parser)));
                written = true;
            }
        }
        throw newException(node, JacksonCompat.currentLocation(parser), null, "Reached end of stream with unclosed object!", null);
    }

    @Override
    protected void saveInternal(final ConfigurationNode node, final Writer writer) throws ConfigurateException {
        try (JsonGenerator generator = this.factory.createGenerator(writer)) {
            generator.setPrettyPrinter(new ConfiguratePrettyPrinter(this.indent, this.fieldValueSeparatorStyle));
            node.visit(JacksonVisitor.INSTANCE.get(), generator);
            writer.write(SYSTEM_LINE_SEPARATOR); // Jackson doesn't add a newline at the end of files by default
        } catch (final IOException ex) {
            throw ConfigurateException.wrap(node, ex);
        }
    }

    @Override
    public BasicConfigurationNode createNode(final @NonNull ConfigurationOptions options) {
        return BasicConfigurationNode.root(options.nativeTypes(NATIVE_TYPES));
    }

    private static ParsingException newException(final ConfigurationNode node,
            final JsonLocation position,
            final @Nullable String content,
            final @Nullable String message,
            final @Nullable Throwable cause) {
        @Nullable String context = content == null ? null : content.substring((int) position.getCharOffset());
        if (context != null) {
            int nextLine = context.indexOf('\n');
            if (nextLine == -1) {
                nextLine = context.length();
            } else if (context.charAt(nextLine - 1) == '\r') {
                nextLine--;
            }

            if (nextLine > MAX_CTX_LENGTH) {
                context = context.substring(0, MAX_CTX_LENGTH) + "...";
            } else {
                context = context.substring(0, nextLine);
            }
        }
        // no newline: set to length
        // too long: truncate
        // otherwise: trim to position of next newline
        return new ParsingException(node, position.getLineNr(), position.getColumnNr(), context, message, cause);
    }

}
