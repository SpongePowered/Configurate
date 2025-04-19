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
package org.spongepowered.configurate.extra.dfu.v7;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Keyable;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.math.vector.Vector3i;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Unit tests to validate ConfigurateOps functions properly.
 */
final class ConfigurateOpsTests {

    private static void compareToJson(final ConfigurationNode node, final JsonElement element) throws IOException, ConfigurateException {
        final StringWriter configurate = new StringWriter();
        final GsonConfigurationLoader loader = GsonConfigurationLoader.builder()
                .sink(() -> new BufferedWriter(configurate)).build();
        loader.save(node);

        final StringWriter json = new StringWriter();
        try (JsonWriter jw = new JsonWriter(json)) {
            jw.setIndent("  ");
            Streams.write(element, jw);
            jw.flush();
            json.append(System.lineSeparator());
        }

        assertEquals(configurate.toString(), json.toString());
    }

    private static <V> V assertResult(final DataResult<V> result) {
        final @Nullable V success = result.result().orElse(null);
        if (success == null) {
            throw new IllegalArgumentException(result.error()
                    .orElseThrow(() -> new IllegalStateException("Neither success nor failure were present")).message());
        }
        return success;
    }

    @Test
    @DisplayName("Configurate (Empty) -> Gson (Null)")
    void emptyToGson() {
        final ConfigurationNode node = BasicConfigurationNode.root();
        final Dynamic<ConfigurationNode> wrapped = new Dynamic<>(ConfigurateOps.instance(), node);
        final JsonElement element = wrapped.convert(JsonOps.INSTANCE).getValue();

        assertTrue(element.isJsonNull(), "Resulting element was not a json null");
    }

    @Test
    @DisplayName("Gson (Null) -> Configurate (Empty)")
    void emptyFromGson() {
        final JsonNull jsonNull = JsonNull.INSTANCE;
        final Dynamic<JsonElement> wrapped = new Dynamic<>(JsonOps.INSTANCE, jsonNull);
        final ConfigurationNode result = wrapped.convert(ConfigurateOps.instance()).getValue();

        assertTrue(result.empty(), "Resulting configuration node was not empty");
    }

    @Test
    @DisplayName("Configurate (String) -> Gson")
    void toGsonFromString() {
        final ConfigurationNode node = BasicConfigurationNode.root().raw("Test String");
        final Dynamic<ConfigurationNode> wrapped = new Dynamic<>(ConfigurateOps.instance(), node);
        final JsonElement output = wrapped.convert(JsonOps.INSTANCE).getValue();

        assertTrue(output instanceof JsonPrimitive, "Resulting Element was not a Json Primitive");
        assertTrue(output.getAsJsonPrimitive().isString(), "Resulting Element was not a String");
        assertEquals("Test String", output.getAsString());
    }

    @Test
    @DisplayName("Gson (String) -> Configurate")
    void fromGsonFromString() {
        final JsonPrimitive string = new JsonPrimitive("Test String");
        final Dynamic<JsonElement> wrapped = new Dynamic<>(JsonOps.INSTANCE, string);
        final ConfigurationNode output = wrapped.convert(ConfigurateOps.instance()).getValue();

        assertTrue(output.raw() instanceof String, "Resulting configuration node was not a String");
        assertEquals("Test String", output.getString());
    }

    @Test
    @DisplayName("Gson (Integer Array) -> Configurate")
    void fromGsonFromList() {
        final List<Integer> expectedElements = new ArrayList<>();
        expectedElements.add(1);
        expectedElements.add(3);
        expectedElements.add(4);

        final JsonArray jsonArray = new JsonArray();
        jsonArray.add(1);
        jsonArray.add(3);
        jsonArray.add(4);
        final Dynamic<JsonElement> wrapped = new Dynamic<>(JsonOps.INSTANCE, jsonArray);
        final ConfigurationNode output = wrapped.convert(ConfigurateOps.instance()).getValue();

        assertTrue(output.isList(), "Resulting configuration node was not a list.");
        assertEquals(3, output.childrenList().size(),
                "Resulting configuration node had wrong amount of child elements in list");
        assertTrue(output.childrenList().stream()
                        .map(ConfigurationNode::getInt)
                        .collect(Collectors.toList()).containsAll(expectedElements),
                "Resulting configuration node did not contain every element in original JsonArray");
    }

    @Test
    @DisplayName("Configurate (Integer List) -> Gson")
    void toGsonFromList() throws SerializationException {
        final List<Integer> expectedElements = new ArrayList<>();
        expectedElements.add(1);
        expectedElements.add(3);
        expectedElements.add(4);

        final ConfigurationNode node = BasicConfigurationNode.root();
        node.set(expectedElements);
        //node.appendListNode().setValue(1).setValue(3).setValue(4);
        final Dynamic<ConfigurationNode> wrapped = new Dynamic<>(ConfigurateOps.instance(), node);
        final JsonElement output = wrapped.convert(JsonOps.INSTANCE).getValue();

        assertTrue(output.isJsonArray(), "Resulting Element was not an array");
        assertEquals(3, output.getAsJsonArray().size(), "Resulting array had the wrong amount of elements");

        final List<Integer> elements = StreamSupport.stream(output.getAsJsonArray().spliterator(), false)
                .map(JsonElement::getAsInt).collect(Collectors.toList());
        assertTrue(elements.containsAll(expectedElements),
                "Resulting array did not contain all the same elements as the original configuration node");
    }

    @Test
    @DisplayName("Gson (JsonObject) -> Configurate")
    void fromGsonToMap() throws SerializationException {
        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("foo", "bar");
        expectedValues.put("bar", "baz");

        final JsonObject object = new JsonObject();
        object.addProperty("foo", "bar");
        object.addProperty("bar", "baz");
        final Dynamic<JsonElement> wrapped = new Dynamic<>(JsonOps.INSTANCE, object);
        final ConfigurationNode output = wrapped.convert(ConfigurateOps.instance()).getValue();

        assertTrue(output.isMap(), "Resulting configuration node was not a map");
        assertEquals(2, output.childrenMap().size(), "Resulting configuration node had wrong amount of child elements");
        assertEquals(expectedValues, output.get(new TypeToken<Map<String, String>>() {}));
    }

    @Test
    @DisplayName("Configurate (Map) -> Gson")
    void toGsonFromMap() throws SerializationException {
        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("foo", "bar");
        expectedValues.put("bar", "baz");

        final ConfigurationNode node = BasicConfigurationNode.root();
        node.set(expectedValues);
        final Dynamic<ConfigurationNode> wrapped = new Dynamic<>(ConfigurateOps.instance(), node);
        final JsonElement element = wrapped.convert(JsonOps.INSTANCE).getValue();

        assertTrue(element.isJsonObject(), "Resulting element was not a json object");
        assertEquals(2, element.getAsJsonObject().size(), "Resulting json object had wrong amount of child elements");
        // TODO: Verify the values in the resulting maps are equal
    }

    @Test
    void testCompressed() throws IOException, ConfigurateException {
        final TestRegistry<Vector3i> positionRegistry = new TestRegistry<>();
        positionRegistry.put("test1", new Vector3i(1, 2, 3));
        positionRegistry.put("spawn", new Vector3i(32, 85, 884));
        positionRegistry.put("test3", new Vector3i(1, 0, -1));
        final Codec<List<Vector3i>> listCodec = Codec.list(positionRegistry);
        final List<Vector3i> positions = ImmutableList.copyOf(positionRegistry.indices);


        final JsonElement compressedBlocks = assertResult(listCodec.encode(positions, JsonOps.COMPRESSED, JsonOps.COMPRESSED.empty()));
        final JsonElement regularBlocks = assertResult(listCodec.encode(positions, JsonOps.INSTANCE, JsonOps.INSTANCE.empty()));
        final ConfigurationNode compressedNode = assertResult(listCodec.encode(positions,
                                                                ConfigurateOps.instance(true), BasicConfigurationNode.root()));
        final ConfigurationNode uncompressedNode = assertResult(listCodec.encode(positions,
                                                                ConfigurateOps.instance(false), BasicConfigurationNode.root()));

        compareToJson(compressedNode, compressedBlocks);
        compareToJson(uncompressedNode, regularBlocks);
    }

    static class TestRegistry<V> implements Codec<V>, Keyable {

        private final Map<String, V> items = new HashMap<>();
        private final Map<V, String> reverse = new HashMap<>();
        private final List<V> indices = new ArrayList<>();

        public int put(final String key, final V value) {
            if (this.items.putIfAbsent(key, value) == null) {
                this.reverse.put(value, key);
                this.indices.add(value);
                return this.indices.size() - 1;
            } else {
                return this.indices.indexOf(value);
            }
        }

        @Override
        public <T> Stream<T> keys(final DynamicOps<T> ops) {
            return this.indices.stream()
                .map(value -> ops.createString(this.reverse.get(value)));
        }

        @Override
        public <T> DataResult<Pair<V, T>> decode(final DynamicOps<T> ops, final T input) {
            if (ops.compressMaps()) {
                return ops.getNumberValue(input)
                    .flatMap(key -> {
                        final V value = this.indices.get(key.intValue());
                        return value == null ? DataResult.error(() -> "No value for key " + key) : DataResult.success(value);
                    }).map(val -> Pair.of(val, ops.empty()));
            } else {
                return ops.getStringValue(input)
                    .flatMap(key -> {
                        final V value = this.items.get(key);
                        return value == null ? DataResult.error(() -> "No value for key " + key) : DataResult.success(value);
                    }).map(val -> Pair.of(val, ops.empty()));
            }
        }

        @Override
        public <T> DataResult<T> encode(final V input, final DynamicOps<T> ops, final T prefix) {
            if (ops.compressMaps()) {
                return ops.mergeToPrimitive(prefix, ops.createInt(this.indices.indexOf(input)));
            }
            return ops.mergeToPrimitive(prefix, ops.createString(this.reverse.get(input)));
        }

    }

}
