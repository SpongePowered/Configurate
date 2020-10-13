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

import static java.util.Objects.requireNonNull;

import com.google.gson.stream.JsonWriter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationVisitor;

import java.io.IOException;

class GsonVisitor implements ConfigurationVisitor<JsonWriter, Void, IOException> {

    static final ThreadLocal<GsonVisitor> INSTANCE = ThreadLocal.withInitial(GsonVisitor::new);

    private @Nullable ConfigurationNode start;

    @Override
    public JsonWriter newState() {
        throw new UnsupportedOperationException("Writer must be provided");
    }

    @Override
    public void beginVisit(final ConfigurationNode node, final JsonWriter state) throws IOException {
        if (node.empty()) {
            state.beginObject();
            state.endObject();
        } else {
            this.start = node;
        }
    }

    @Override
    public void enterNode(final ConfigurationNode node, final JsonWriter state) throws IOException {
        final @Nullable ConfigurationNode parent = node.parent();
        if (node != this.start && parent != null && parent.isMap()) {
            state.name(requireNonNull(node.key(), "Node must have key to be a value in a mapping").toString());
        }
    }

    @Override
    public void enterMappingNode(final ConfigurationNode node, final JsonWriter state) throws IOException {
        state.beginObject();
    }

    @Override
    public void enterListNode(final ConfigurationNode node, final JsonWriter state) throws IOException {
        state.beginArray();
    }

    @Override
    public void enterScalarNode(final ConfigurationNode node, final JsonWriter writer) throws IOException {
        final @Nullable Object value = node.rawScalar();
        if (value == null) {
            writer.nullValue();
        } else if (value instanceof Double) {
            writer.value((Double) value);
        } else if (value instanceof Float) {
            writer.value((Float) value);
        } else if (value instanceof Long) {
            writer.value((Long) value);
        } else if (value instanceof Integer) {
            writer.value((Integer) value);
        } else if (value instanceof Boolean) {
            writer.value((Boolean) value);
        } else {
            writer.value(value.toString());
        }
    }

    @Override
    public void exitMappingNode(final ConfigurationNode node, final JsonWriter state) throws IOException {
        state.endObject();
    }

    @Override
    public void exitListNode(final ConfigurationNode node, final JsonWriter state) throws IOException {
        state.endArray();
    }

    @Override
    public Void endVisit(final JsonWriter state) throws IOException {
        state.flush();
        this.start = null;
        return null;
    }

}
