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
package org.spongepowered.configurate.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.MalformedJsonException;
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
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.util.Strings;
import org.spongepowered.configurate.util.UnmodifiableCollections;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Set;

/**
 * A loader for JSON-formatted configurations, using the GSON library for
 * parsing and generation.
 *
 * @since 4.0.0
 */
public final class GsonConfigurationLoader extends AbstractConfigurationLoader<BasicConfigurationNode> {

    private static final Set<Class<?>> NATIVE_TYPES = UnmodifiableCollections.toSet(
            Double.class, Float.class, Long.class, Integer.class, Boolean.class, String.class);
    private static final TypeSerializerCollection GSON_SERIALIZERS = TypeSerializerCollection.defaults().childBuilder()
            .register(JsonElement.class, JsonElementSerializer.INSTANCE)
            .build();

    // visible for tests
    static final ConfigurationOptions DEFAULT_OPTIONS = ConfigurationOptions.defaults()
            .nativeTypes(NATIVE_TYPES)
            .serializers(GSON_SERIALIZERS);

    /**
     * Creates a new {@link GsonConfigurationLoader} builder.
     *
     * @return a new builder
     * @since 4.0.0
     */
    public static @NonNull Builder builder() {
        return new Builder();
    }

    /**
     * Get a {@link TypeSerializerCollection} for handling Gson types.
     *
     * <p>Currently, this serializer can handle:</p>
     * <ul>
     *     <li>{@link JsonElement} and its subtypes: {@link JsonArray}, {@link JsonObject},
     *          {@link JsonPrimitive}, and {@link JsonNull}</li>
     * </ul>
     *
     * @return gson type serializers
     * @since 4.1.0
     */
    public static TypeSerializerCollection gsonSerializers() {
        return GSON_SERIALIZERS;
    }

    /**
     * Builds a {@link GsonConfigurationLoader}.
     *
     * <p>This builder supports the following options:</p>
     * <ul>
     *     <li>{@link #INDENT}</li>
     *     <li>{@link #LENIENT}</li>
     * </ul>
     *
     * @since 4.0.0
     */
    public static final class Builder extends AbstractConfigurationLoader.Builder<Builder, GsonConfigurationLoader> {

        private static final OptionSchema.Mutable UNSAFE_SCHEMA = OptionSchema.childSchema(AbstractConfigurationLoader.Builder.SCHEMA);

        /**
         * A schema of options available on the Gson loader.
         *
         * @since 4.2.0
         */
        public static final OptionSchema SCHEMA = UNSAFE_SCHEMA.frozenView();

        /**
         * The level of indentation to be used by the resulting loader.
         *
         * @since 4.2.0
         */
        public static final Option<Integer> INDENT = UNSAFE_SCHEMA.intOption("gson:indent", 2);

        /**
         * If the resultant loader should parse leniently.
         *
         * @since 4.2.0
         */
        public static final Option<Boolean> LENIENT = UNSAFE_SCHEMA.booleanOption("gson:lenient", true);

        Builder() {
            this.defaultOptions(DEFAULT_OPTIONS);
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
        public @NonNull Builder indent(final int indent) {
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
         * Sets if the resultant loader should parse leniently.
         *
         * @param lenient whether the parser should parse leniently
         * @return this builder (for chaining)
         * @see JsonReader#setLenient(boolean)
         * @since 4.0.0
         */
        public @NonNull Builder lenient(final boolean lenient) {
            this.optionStateBuilder().value(LENIENT, lenient);
            return this;
        }

        /**
         * Gets if the resultant loader should parse leniently.
         *
         * @return whether the parser should parse leniently
         * @since 4.0.0
         */
        public boolean lenient() {
            return this.optionState().value(LENIENT);
        }

        @Override
        public @NonNull GsonConfigurationLoader build() {
            this.defaultOptions(o -> o.nativeTypes(NATIVE_TYPES));
            return new GsonConfigurationLoader(this);
        }
    }

    private final boolean lenient;
    private final String indent;

    GsonConfigurationLoader(final Builder builder) {
        super(builder, new CommentHandler[] {CommentHandlers.DOUBLE_SLASH, CommentHandlers.SLASH_BLOCK, CommentHandlers.HASH});
        this.lenient = builder.lenient();
        this.indent = Strings.repeat(" ", builder.indent());
    }

    @Override
    protected void checkCanWrite(final ConfigurationNode node) throws ConfigurateException {
        if (!this.lenient && !node.isMap()) {
            throw new ConfigurateException(node, "Non-lenient json generators must have children of map type");
        }
    }

    @Override
    protected void loadInternal(final BasicConfigurationNode node, final BufferedReader reader) throws ParsingException {
        try {
            reader.mark(1);
            if (reader.read() == -1) {
                return;
            }
            reader.reset();
        } catch (final IOException ex) {
            throw new ParsingException(node, 0, 0, null, "peeking file size", ex);
        }

        try (JsonReader parser = new JsonReader(reader)) {
            parser.setLenient(this.lenient);
            this.parseValue(parser, node);
        } catch (final IOException ex) {
            throw ParsingException.wrap(node, ex);
        }
    }

    private void parseValue(final JsonReader parser, final BasicConfigurationNode node) throws ParsingException {
        final JsonToken token;
        try {
            token = parser.peek();
        } catch (final IOException ex) {
            throw this.newException(parser, node, ex.getMessage(), ex);
        }

        try {
            switch (token) {
                case BEGIN_OBJECT:
                    this.parseObject(parser, node);
                    break;
                case BEGIN_ARRAY:
                    this.parseArray(parser, node);
                    break;
                case NUMBER:
                    node.raw(this.readNumber(parser));
                    break;
                case STRING:
                    node.raw(parser.nextString());
                    break;
                case BOOLEAN:
                    node.raw(parser.nextBoolean());
                    break;
                case NULL: // Ignored values
                    parser.nextNull();
                    node.raw(null);
                    break;
                case NAME:
                    break;
                default:
                    throw this.newException(parser, node, "Unsupported token type: " + token, null);
            }
        } catch (final JsonParseException | MalformedJsonException ex) {
            throw this.newException(parser, node, ex.getMessage(), ex.getCause());
        } catch (final ParsingException ex) {
            ex.initPath(node::path);
            throw ex;
        } catch (final IOException ex) {
            throw this.newException(parser, node, "An underlying exception occurred", ex);
        }
    }

    private ParsingException newException(final JsonReader reader, final ConfigurationNode node, final @Nullable String message,
            final @Nullable Throwable cause) {
        return new ParsingException(node, JsonReaderAccess.lineNumber(reader), JsonReaderAccess.column(reader), null, message, cause);
    }

    private Number readNumber(final JsonReader reader) throws IOException {
        final String number = reader.nextString();
        if (number.contains(".")) {
            return Double.parseDouble(number);
        }
        final long nextLong = Long.parseLong(number);
        final int nextInt = (int) nextLong;
        if (nextInt == nextLong) {
            return nextInt;
        }
        return nextLong;
    }

    private void parseArray(final JsonReader parser, final BasicConfigurationNode node) throws IOException {
        parser.beginArray();

        boolean written = false;
        @Nullable JsonToken token;
        while ((token = parser.peek()) != null) {
            if (token == JsonToken.END_ARRAY) {
                parser.endArray();
                // ensure the type is preserved
                if (!written) {
                    node.raw(Collections.emptyList());
                }
                return;
            } else {
                this.parseValue(parser, node.appendListNode());
                written = true;
            }
        }
        throw this.newException(parser, node, "Reached end of stream with unclosed array!", null);
    }

    private void parseObject(final JsonReader parser, final BasicConfigurationNode node) throws ParsingException, IOException {
        parser.beginObject();

        boolean written = false;
        @Nullable JsonToken token;
        while ((token = parser.peek()) != null) {
            switch (token) {
                case END_OBJECT:
                case END_DOCUMENT:
                    parser.endObject();
                    // ensure the type is preserved
                    if (!written) {
                        node.raw(Collections.emptyMap());
                    }
                    return;
                case NAME:
                    this.parseValue(parser, node.node(parser.nextName()));
                    written = true;
                    break;
                default:
                    throw new JsonParseException("Received improper object value " + token);
            }
        }
        throw new JsonParseException("Reached end of stream with unclosed object!");
    }

    @Override
    protected void saveInternal(final ConfigurationNode node, final Writer writer) throws ConfigurateException {
        try {
            try (JsonWriter generator = new JsonWriter(writer)) {
                generator.setIndent(this.indent);
                generator.setLenient(this.lenient);
                node.visit(GsonVisitor.INSTANCE.get(), generator);
                writer.write(SYSTEM_LINE_SEPARATOR); // Jackson doesn't add a newline at the end of files by default
            }
        } catch (final IOException ex) {
            throw ConfigurateException.wrap(node, ex);
        }
    }

    @Override
    public BasicConfigurationNode createNode(final ConfigurationOptions options) {
        return BasicConfigurationNode.root(options.nativeTypes(NATIVE_TYPES));
    }

}
