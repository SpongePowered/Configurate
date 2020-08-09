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
package ninja.leaping.configurate.gson;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.loader.AbstractConfigurationLoader;
import ninja.leaping.configurate.loader.CommentHandler;
import ninja.leaping.configurate.loader.CommentHandlers;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * A loader for JSON-formatted configurations, using the GSON library for parsing and generation.
 */
public class GsonConfigurationLoader extends AbstractConfigurationLoader<ConfigurationNode> {

    /**
     * Creates a new {@link GsonConfigurationLoader} builder.
     *
     * @return A new builder
     */
    @NonNull
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builds a {@link GsonConfigurationLoader}.
     */
    public static class Builder extends AbstractConfigurationLoader.Builder<Builder> {
        private boolean lenient = true;
        private int indent = 2;

        protected Builder() {
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
         * Sets if the resultant loader should parse leniently.
         *
         * @see JsonReader#setLenient(boolean)
         * @param lenient Whether the parser should parse leniently
         * @return This builder (for chaining)
         */
        @NonNull
        public Builder setLenient(boolean lenient) {
            this.lenient = lenient;
            return this;
        }

        /**
         * Gets if the resultant loader should parse leniently.
         *
         * @return Whether the parser should parse leniently
         */
        public boolean isLenient() {
            return this.lenient;
        }

        @NonNull
        @Override
        public GsonConfigurationLoader build() {
            return new GsonConfigurationLoader(this);
        }
    }

    private final boolean lenient;
    private final String indent;

    private GsonConfigurationLoader(Builder builder) {
        super(builder, new CommentHandler[] {CommentHandlers.DOUBLE_SLASH, CommentHandlers.SLASH_BLOCK, CommentHandlers.HASH});
        this.lenient = builder.isLenient();
        this.indent = Strings.repeat(" ", builder.getIndent());
    }

    @Override
    protected void loadInternal(ConfigurationNode node, BufferedReader reader) throws IOException {
        reader.mark(1);
        if (reader.read() == -1) {
            return;
        }
        reader.reset();
        try (JsonReader parser = new JsonReader(reader)) {
            parser.setLenient(lenient);
            parseValue(parser, node);
        }
    }

    private void parseValue(JsonReader parser, ConfigurationNode node) throws IOException {
        JsonToken token = parser.peek();
        switch (token) {
            case BEGIN_OBJECT:
                parseObject(parser, node);
                break;
            case BEGIN_ARRAY:
                parseArray(parser, node);
                break;
            case NUMBER:
                node.setValue(readNumber(parser));
                break;
            case STRING:
                node.setValue(parser.nextString());
                break;
            case BOOLEAN:
                node.setValue(parser.nextBoolean());
                break;
            case NULL: // Ignored values
                parser.nextNull();
                node.setValue(null);
                break;
            case NAME:
                break;
            default:
                throw new IOException("Unsupported token type: " + token);
        }
    }

    private Number readNumber(JsonReader reader) throws IOException {
        String number = reader.nextString();
        if (number.contains(".")) {
            return Double.parseDouble(number);
        }
        long nextLong = Long.parseLong(number);
        int nextInt = (int) nextLong;
        if (nextInt == nextLong) {
            return nextInt;
        }
        return nextLong;
    }

    private void parseArray(JsonReader parser, ConfigurationNode node) throws IOException {
        parser.beginArray();

        boolean written = false;
        @Nullable JsonToken token;
        while ((token = parser.peek()) != null) {
            switch (token) {
                case END_ARRAY:
                    parser.endArray();
                    return;
                default:
                    parseValue(parser, node.appendListNode());
            }
        }
        throw new JsonParseException("Reached end of stream with unclosed array at!");

    }

    private void parseObject(JsonReader parser, ConfigurationNode node) throws IOException {
        parser.beginObject();
        boolean written = false;
        @Nullable JsonToken token;
        while ((token = parser.peek()) != null) {
            switch (token) {
                case END_OBJECT:
                case END_DOCUMENT:
                    parser.endObject();
                    return;
                case NAME:
                    parseValue(parser, node.getNode(parser.nextName()));
                    break;
                default:
                    throw new JsonParseException("Received improper object value " + token);
            }
        }
        throw new JsonParseException("Reached end of stream with unclosed object!");
    }

    @Override
    public void saveInternal(ConfigurationNode node, Writer writer) throws IOException {
        if (!lenient && !node.isMap()) {
            throw new IOException("Non-lenient json generators must have children of map type");
        }
        try (JsonWriter generator = new JsonWriter(writer)) {
            generator.setIndent(indent);
            generator.setLenient(lenient);
            node.visit(GsonVisitor.INSTANCE.get(), generator);
            writer.write(SYSTEM_LINE_SEPARATOR); // Jackson doesn't add a newline at the end of files by default
        }
    }

    @Override
    public ConfigurationNode createEmptyNode(@NonNull ConfigurationOptions options) {
        options = options.withNativeTypes(ImmutableSet.of(Map.class, List.class, Double.class, Float.class,
                Long.class, Integer.class, Boolean.class, String.class));
        return ConfigurationNode.root(options);
    }
}
