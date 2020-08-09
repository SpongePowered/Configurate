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

import com.google.gson.stream.JsonWriter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationVisitor;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

class GsonVisitor implements ConfigurationVisitor<JsonWriter, Void, IOException> {
    static ThreadLocal<GsonVisitor> INSTANCE = ThreadLocal.withInitial(GsonVisitor::new);

    private ConfigurationNode start;

    @Override
    public JsonWriter newState() {
        throw new UnsupportedOperationException("Writer must be provided");
    }

    @Override
    public void beginVisit(ConfigurationNode node, JsonWriter state) throws IOException {
        if (node.isEmpty()) {
            state.beginObject();
            state.endObject();
        } else {
            this.start = node;
        }
    }

    @Override
    public void enterNode(ConfigurationNode node, JsonWriter state) throws IOException {
        @Nullable ConfigurationNode parent = node.getParent();
        if (node != this.start && parent != null && parent.isMap()) {
            state.name(requireNonNull(node.getKey(), "Node must have key to be a value in a mapping").toString());
        }
    }

    @Override
    public void enterMappingNode(ConfigurationNode node, JsonWriter state) throws IOException {
        state.beginObject();
    }

    @Override
    public void enterListNode(ConfigurationNode node, JsonWriter state) throws IOException {
        state.beginArray();
    }

    @Override
    public void enterScalarNode(ConfigurationNode node, JsonWriter writer) throws IOException {
        @Nullable Object value = node.getValue();
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
    public void exitMappingNode(ConfigurationNode node, JsonWriter state) throws IOException {
        state.endObject();
    }

    @Override
    public void exitListNode(ConfigurationNode node, JsonWriter state) throws IOException {
        state.endArray();
    }

    @Override
    public Void endVisit(JsonWriter state) throws IOException {
        state.flush();
        this.start = null;
        return null;
    }
}
