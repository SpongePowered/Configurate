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

import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.loader.CommentHandler;
import org.spongepowered.configurate.loader.CommentHandlers;
import org.spongepowered.configurate.util.Strings;
import org.spongepowered.configurate.util.UnmodifiableCollections;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A loader for JSON-formatted configurations, using the GSON library for
 * parsing and generation.
 */
public final class GsonConfigurationLoader extends AbstractConfigurationLoader<BasicConfigurationNode> {

    private static final Set<Class<?>> NATIVE_TYPES = UnmodifiableCollections.toSet(Map.class, List.class, Double.class, Float.class,
            Long.class, Integer.class, Boolean.class, String.class);

    /**
     * Creates a new {@link GsonConfigurationLoader} builder.
     *
     * @return a new builder
     */
    @NonNull
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builds a {@link GsonConfigurationLoader}.
     */
    public static final class Builder extends AbstractConfigurationLoader.Builder<Builder, GsonConfigurationLoader> {
        private boolean lenient = true;
        private int indent = 2;

        Builder() { }

        /**
         * Sets the level of indentation the resultant loader should use.
         *
         * @param indent the indent level
         * @return this builder (for chaining)
         */
        @NonNull
        public Builder indent(final int indent) {
            this.indent = indent;
            return this;
        }

        /**
         * Gets the level of indentation to be used by the resultant loader.
         *
         * @return the indent level
         */
        public int indent() {
            return this.indent;
        }

        /**
         * Sets if the resultant loader should parse leniently.
         *
         * @see JsonReader#setLenient(boolean)
         * @param lenient whether the parser should parse leniently
         * @return this builder (for chaining)
         */
        @NonNull
        public Builder lenient(final boolean lenient) {
            this.lenient = lenient;
            return this;
        }

        /**
         * Gets if the resultant loader should parse leniently.
         *
         * @return whether the parser should parse leniently
         */
        public boolean lenient() {
            return this.lenient;
        }

        @Override
        public @NonNull GsonConfigurationLoader build() {
            this.defaultOptions(o -> o.nativeTypes(NATIVE_TYPES));
            return new GsonConfigurationLoader(this);
        }
    }

    private final boolean lenient;
    private final String indent;

    private GsonConfigurationLoader(final Builder builder) {
        super(builder, new CommentHandler[] {CommentHandlers.DOUBLE_SLASH, CommentHandlers.SLASH_BLOCK, CommentHandlers.HASH});
        this.lenient = builder.lenient();
        this.indent = Strings.repeat(" ", builder.indent());
    }

    @Override
    protected void loadInternal(final BasicConfigurationNode node, final BufferedReader reader) throws IOException {
        reader.mark(1);
        if (reader.read() == -1) {
            return;
        }
        reader.reset();
        try (JsonReader parser = new JsonReader(reader)) {
            parser.setLenient(this.lenient);
            parseValue(parser, node);
        }
    }

    private void parseValue(final JsonReader parser, final BasicConfigurationNode node) throws IOException {
        final JsonToken token = parser.peek();
        switch (token) {
            case BEGIN_OBJECT:
                parseObject(parser, node);
                break;
            case BEGIN_ARRAY:
                parseArray(parser, node);
                break;
            case NUMBER:
                node.set(readNumber(parser));
                break;
            case STRING:
                node.set(parser.nextString());
                break;
            case BOOLEAN:
                node.set(parser.nextBoolean());
                break;
            case NULL: // Ignored values
                parser.nextNull();
                node.set(null);
                break;
            case NAME:
                break;
            default:
                throw new IOException("Unsupported token type: " + token);
        }
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
            switch (token) {
                case END_ARRAY:
                    parser.endArray();
                    // ensure the type is preserved
                    if (!written) {
                        node.set(Collections.emptyList());
                    }
                    return;
                default:
                    parseValue(parser, node.appendListNode());
                    written = true;
            }
        }
        throw new JsonParseException("Reached end of stream with unclosed array at!");

    }

    private void parseObject(final JsonReader parser, final BasicConfigurationNode node) throws IOException {
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
                        node.set(Collections.emptyMap());
                    }
                    return;
                case NAME:
                    parseValue(parser, node.node(parser.nextName()));
                    written = true;
                    break;
                default:
                    throw new JsonParseException("Received improper object value " + token);
            }
        }
        throw new JsonParseException("Reached end of stream with unclosed object!");
    }

    @Override
    protected void saveInternal(final ConfigurationNode node, final Writer writer) throws IOException {
        if (!this.lenient && !node.isMap()) {
            throw new IOException("Non-lenient json generators must have children of map type");
        }
        try (JsonWriter generator = new JsonWriter(writer)) {
            generator.setIndent(this.indent);
            generator.setLenient(this.lenient);
            GsonVisitor.INSTANCE.get().visit(node, generator);
            writer.write(SYSTEM_LINE_SEPARATOR); // Jackson doesn't add a newline at the end of files by default
        }
    }

    @Override
    public BasicConfigurationNode createNode(ConfigurationOptions options) {
        options = options.nativeTypes(NATIVE_TYPES);
        return BasicConfigurationNode.root(options);
    }

}
