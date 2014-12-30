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
package ninja.leaping.configurate.json;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.util.InternCache;
import com.google.common.io.CharSink;
import com.google.common.io.CharSource;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.loader.FileConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.List;
import java.util.Map;


/**
 * A loader for JSON-formatted configurations, using the jackson library for parsing and generation
 */
public class JSONConfigurationLoader extends FileConfigurationLoader {
    private final JsonFactory factory = new JsonFactory();

    public JSONConfigurationLoader(File file) {
        super(file);
        configure(factory);
    }

    public JSONConfigurationLoader(URL url) {
        super(url);
        configure(factory);
    }

    public JSONConfigurationLoader(CharSource source, CharSink sink) {
        super(source, sink);
        configure(factory);
    }

    protected void configure(JsonFactory factory) {
        // Parse loosely, emit strictly
        factory.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        factory.enable(JsonParser.Feature.ALLOW_COMMENTS);
        factory.enable(JsonParser.Feature.ALLOW_YAML_COMMENTS);
        factory.enable(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
        factory.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        factory.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        factory.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
        factory.enable(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
    }

    @Override
    public ConfigurationNode load() throws IOException {
        if (!canLoad()) {
            throw new IOException("No source present to read from!");
        }
        final SimpleConfigurationNode node = SimpleConfigurationNode.root();
        try (Reader reader = source.openStream(); JsonParser parser = factory.createParser(reader)) {
            parser.nextToken();
            parseValue(parser, node);
        }
        return node;
    }

    private void parseValue(JsonParser parser, ConfigurationNode node) throws IOException {
        JsonToken token = parser.getCurrentToken();
        switch (token) {
            case START_OBJECT:
                parseObject(parser, node);
                break;
            case START_ARRAY:
                parseArray(parser, node);
                break;
            case VALUE_NUMBER_FLOAT:
                node.setValue(parser.getFloatValue());
                break;
            case VALUE_NUMBER_INT:
                node.setValue(parser.getIntValue());
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
                throw new IOException("Unsupported token type: " + token + " (at " + parser.getTokenLocation()
                        + ")");
        }
    }

    private void parseArray(JsonParser parser, ConfigurationNode node) throws IOException {
        JsonToken token;
        while ((token = parser.nextToken()) != null) {
            switch (token) {
                case END_ARRAY:
                    return;
                default:
                    parseValue(parser, node.getAppendedChild());
            }
        }
        throw new JsonParseException("Reached end of stream with unclosed array!", parser.getCurrentLocation());

    }

    private void parseObject(JsonParser parser, ConfigurationNode node) throws IOException {
        JsonToken token;
        while ((token = parser.nextToken()) != null) {
            switch (token) {
                case END_OBJECT:
                    return;
                default:
                    parseValue(parser, node.getChild(parser.getCurrentName()));
            }
        }
            throw new JsonParseException("Reached end of stream with unclosed array!", parser.getCurrentLocation());
    }

    @Override
    public void save(ConfigurationNode node) throws IOException {
        if (!canSave()) {
            throw new IOException("No sink present to write to!");
        }
        try (Writer writer = sink.openStream(); JsonGenerator generator = factory.createGenerator(writer)) {
            generator.useDefaultPrettyPrinter();
            generateValue(generator, node);
            generator.flush();
            writer.write('\n'); // Jackson doesn't add a newline at the end of files by default
        }
    }

    private void generateValue(JsonGenerator generator, ConfigurationNode node) throws IOException {
        if (node.hasMapChildren()) {
            generateObject(generator, node);
        } else if (node.hasListChildren()) {
            generateArray(generator, node);
        } else {
            Object value = node.getValue();
            if (value instanceof Double) {
                generator.writeNumber((Double) value);
            } else if (value instanceof Float) {
                generator.writeNumber((Float) value);
            } else if (value instanceof Long) {
                generator.writeNumber((Long) value);
            } else if (value instanceof Integer) {
                generator.writeNumber((Integer) value);
            } else if (value instanceof Boolean) {
                generator.writeBoolean((Boolean) value);
            } else if (value instanceof byte[]) {
                generator.writeBinary((byte[]) value);
            } else {
                generator.writeString(value.toString());
            }
        }
    }

    private void generateObject(JsonGenerator generator, ConfigurationNode node) throws IOException {
        if (!node.hasMapChildren()) {
            throw new IOException("Node passed to generateObject does not have map children!");
        }
        generator.writeStartObject();
        for (Map.Entry<Object, ? extends ConfigurationNode> ent : node.getChildrenMap().entrySet()) {
            generator.writeFieldName(ent.getKey().toString());
            generateValue(generator, ent.getValue());
        }
        generator.writeEndObject();

    }

    private void generateArray(JsonGenerator generator, ConfigurationNode node) throws IOException {
        if (!node.hasListChildren()) {
            throw new IOException("Node passed to generateArray does not have list children!");
        }
        List<? extends ConfigurationNode> children = node.getChildrenList();
        generator.writeStartArray(children.size());
        for (ConfigurationNode child : children) {
            generateValue(generator, child);
        }
        generator.writeEndArray();
    }
}
