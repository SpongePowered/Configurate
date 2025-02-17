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

import com.fasterxml.jackson.core.JsonGenerator;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationVisitor;

import java.io.IOException;

@SuppressWarnings("AlmostJavadoc")
final class JacksonVisitor implements ConfigurationVisitor<JsonGenerator, Void, ConfigurateException> {

    static final ThreadLocal<JacksonVisitor> INSTANCE = ThreadLocal.withInitial(JacksonVisitor::new);

    private @Nullable ConfigurationNode start;

    private JacksonVisitor() {
    }

    @Override
    public JsonGenerator newState() {
        throw new UnsupportedOperationException("Generator must be provided");
    }

    @Override
    public void beginVisit(final ConfigurationNode node, final JsonGenerator state) {
        this.start = node;
    }

    @Override
    public void enterNode(final ConfigurationNode node, final JsonGenerator generator) throws ConfigurateException {
        // generateComment(generator, ent.getValue(), false);
        final @Nullable ConfigurationNode parent = node.parent();
        if (node != this.start && parent != null && parent.isMap()) {
            final @Nullable Object key = node.key();
            if (key == null) {
                throw new ConfigurateException(node, "Node must have key to be a value in a mapping");
            }
            try {
                generator.writeFieldName(key.toString());
            } catch (final IOException ex) {
                throw new ConfigurateException(node, ex);
            }
        }
    }

    /*private void generateComment(JsonGenerator generator, ConfigurationNode node, boolean inArray) throws IOException {
        if (node instanceof CommentedConfigurationNode) {
            final Optional<String> comment = ((CommentedConfigurationNode) node).getComment();
            if (comment.isPresent()) {
                if (indent == 0) {
                    generator.writeRaw("/*");
                    generator.writeRaw(comment.get().replaceAll("\\* /", ""));
                    generator.writeRaw("* /");
                } else {
                    for (Iterator<String> it = LINE_SPLITTER.split(comment.get()).iterator(); it.hasNext();) {
                        generator.writeRaw("// ");
                        generator.writeRaw(it.next());
                        generator.getPrettyPrinter().beforeObjectEntries(generator);
                        if (it.hasNext()) {
                            generator.writeRaw(SYSTEM_LINE_SEPARATOR);
                        }
                    }
                    if (inArray) {
                        generator.writeRaw(SYSTEM_LINE_SEPARATOR);
                    }
                }
            }
        }
    }*/

    @Override
    public void enterMappingNode(final ConfigurationNode node, final JsonGenerator state) throws ConfigurateException {
        try {
            state.writeStartObject();
        } catch (final IOException ex) {
            throw new ConfigurateException(node, ex);
        }
    }

    @Override
    public void enterListNode(final ConfigurationNode node, final JsonGenerator state) throws ConfigurateException {
        try {
            state.writeStartArray();
        } catch (final IOException ex) {
            throw new ConfigurateException(node, ex);
        }
    }

    @Override
    public void enterScalarNode(final ConfigurationNode node, final JsonGenerator generator) throws ConfigurateException {
        final @Nullable Object value = node.rawScalar();
        try {
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
            } else if (value == null) {
                generator.writeNull();
            } else {
                generator.writeString(value.toString());
            }
        } catch (final IOException ex) {
            throw new ConfigurateException(node, ex);
        }
    }

    @Override
    public void exitMappingNode(final ConfigurationNode node, final JsonGenerator state) throws ConfigurateException {
        try {
            state.writeEndObject();
        } catch (final IOException ex) {
            throw new ConfigurateException(node, ex);
        }
    }

    @Override
    public void exitListNode(final ConfigurationNode node, final JsonGenerator state) throws ConfigurateException {
        try {
            state.writeEndArray();
        } catch (final IOException ex) {
            throw new ConfigurateException(node, ex);
        }
    }

    @Override
    public Void endVisit(final JsonGenerator state) throws ConfigurateException {
        final @Nullable ConfigurationNode start = this.start;
        this.start = null;
        try {
            state.flush();
        } catch (final IOException ex) {
            throw new ConfigurateException(start, ex);
        }
        return null;
    }

}
