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
package ninja.leaping.configurate.gson;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.loader.AbstractConfigurationLoader;
import ninja.leaping.configurate.loader.CommentHandler;
import ninja.leaping.configurate.loader.CommentHandlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;


/**
 * A loader for JSON-formatted configurations, using the jackson library for parsing and generation
 */
public class GsonConfigurationLoader extends AbstractConfigurationLoader<ConfigurationNode> {
    private final boolean lenient;
    private final String indent;

    public static class Builder extends AbstractConfigurationLoader.Builder<Builder> {
        private boolean lenient = true;
        private int indent = 2;

        public Builder setIndent(int indent) {
            this.indent = indent;
            return this;
        }

        public int getIndent() {
            return this.indent;
        }

        /**
         * @see JsonReader#setLenient(boolean)
         * @param lenient Whether the parser should parse leniently
         * @return this
         */
        public Builder setLenient(boolean lenient) {
            this.lenient = lenient;
            return this;
        }

        public boolean isLenient() {
            return this.lenient;
        }

        @Override
        public GsonConfigurationLoader build() {
            return new GsonConfigurationLoader(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    protected GsonConfigurationLoader(Builder builder) {
        super(builder, new CommentHandler[] {CommentHandlers.DOUBLE_SLASH, CommentHandlers.SLASH_BLOCK,
                        CommentHandlers.HASH});
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
                double nextDouble = parser.nextDouble();
                int nextInt = (int) nextDouble;
                long nextLong = (long) nextDouble;
                if (nextInt == nextDouble) {
                    node.setValue(nextInt); // They don't do much for us here in Gsonland
                } else if (nextLong == nextDouble) {
                    node.setValue(nextLong);
                } else {
                    node.setValue(nextDouble);
                }
                break;
            case STRING:
                node.setValue(parser.nextString());
                break;
            case BOOLEAN:
                node.setValue(parser.nextBoolean());
                break;
            case NULL: // Ignored values
            case NAME:
                break;
            default:
                throw new IOException("Unsupported token type: " + token);
        }
    }

    private void parseArray(JsonReader parser, ConfigurationNode node) throws IOException {
        parser.beginArray();
        JsonToken token;
        while ((token = parser.peek()) != null) {
            switch (token) {
                case END_ARRAY:
                    parser.endArray();
                    return;
                default:
                    parseValue(parser, node.getAppendedNode());
            }
        }
        throw new JsonParseException("Reached end of stream with unclosed array at!");

    }

    private void parseObject(JsonReader parser, ConfigurationNode node) throws IOException {
        parser.beginObject();
        JsonToken token;
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
        if (!lenient && !node.hasMapChildren()) {
            throw new IOException("Non-lenient json generators must have children of map type");
        }
        try (JsonWriter generator = new JsonWriter(writer)) {
            generator.setIndent(indent);
            generator.setLenient(lenient);
            generateValue(generator, node);
            generator.flush();
            writer.write(LINE_SEPARATOR); // Jackson doesn't add a newline at the end of files by default
        }
    }

    @Override
    public ConfigurationNode createEmptyNode(ConfigurationOptions options) {
        options = options.setAcceptedTypes(ImmutableSet.of(Map.class, List.class, Double.class, Float.class,
                Long.class, Integer.class, Boolean.class, String.class));
        return SimpleConfigurationNode.root(options);
    }

    private void generateValue(JsonWriter generator, ConfigurationNode node) throws IOException {
        if (node.hasMapChildren()) {
            generateObject(generator, node);
        } else if (node.hasListChildren()) {
            generateArray(generator, node);
        } else if (node.getKey() == null && node.getValue() == null) {
            generator.beginObject();
            generator.endObject();
        } else {
            Object value = node.getValue();
            if (value instanceof Double) {
                generator.value((Double) value);
            } else if (value instanceof Float) {
                generator.value((Float) value);
            } else if (value instanceof Long) {
                generator.value((Long) value);
            } else if (value instanceof Integer) {
                generator.value((Integer) value);
            } else if (value instanceof Boolean) {
                generator.value((Boolean) value);
            } else if (value instanceof byte[]) {
                //generator.value((byte[]) value);
            } else {
                generator.value(value.toString());
            }
        }
    }

    private void generateObject(JsonWriter generator, ConfigurationNode node) throws IOException {
        if (!node.hasMapChildren()) {
            throw new IOException("Node passed to generateObject does not have map children!");
        }
        generator.beginObject();
        for (Map.Entry<Object, ? extends ConfigurationNode> ent : node.getChildrenMap().entrySet()) {
            generator.name(ent.getKey().toString());
            generateValue(generator, ent.getValue());
        }
        generator.endObject();

    }

    private void generateArray(JsonWriter generator, ConfigurationNode node) throws IOException {
        if (!node.hasListChildren()) {
            throw new IOException("Node passed to generateArray does not have list children!");
        }
        List<? extends ConfigurationNode> children = node.getChildrenList();
        generator.beginArray();
        for (ConfigurationNode child : children) {
            generateValue(generator, child);
        }
        generator.endArray();
    }
}
