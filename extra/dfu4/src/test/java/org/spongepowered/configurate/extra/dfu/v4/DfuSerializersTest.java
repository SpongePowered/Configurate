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
package org.spongepowered.configurate.extra.dfu.v4;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.io.Resources;
import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.serialize.ConfigSerializable;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.PrimitiveIterator;
import java.util.stream.IntStream;

public class DfuSerializersTest {

    private static final TypeToken<Vector3i> VEC3I_TYPE = TypeToken.get(Vector3i.class);
    static final Codec<Vector3i> VEC3I_CODEC = Codec.INT_STREAM.xmap(stream -> {
        final int[] values = new int[3];
        final PrimitiveIterator.OfInt ints = stream.iterator();
        values[0] = ints.nextInt();
        values[1] = ints.nextInt();
        values[2] = ints.nextInt();
        assertFalse(ints.hasNext());
        return new Vector3i(values[0], values[1], values[2]);
    }, vec -> IntStream.of(vec.getX(), vec.getY(), vec.getZ()));

    @Test
    public void testCodecSerializer() throws ObjectMappingException {
        final TypeSerializerCollection serializers = TypeSerializerCollection.defaults().childBuilder()
            .register(VEC3I_TYPE, DfuSerializers.forCodec(VEC3I_CODEC))
            .build();

        final ConfigurationNode testElement = BasicConfigurationNode.root(ConfigurationOptions.defaults().withSerializers(serializers), n -> {
            n.appendListNode().setValue(4);
            n.appendListNode().setValue(5);
            n.appendListNode().setValue(8);
        });

        final Vector3i pos = testElement.getValue(VEC3I_TYPE);

        assertEquals(new Vector3i(4, 5, 8), pos);
    }

    @ConfigSerializable
    @SuppressWarnings("UnusedVariable")
    static class TestSerializable {
        @Setting
        private String testValue = "hello world";

        @Setting
        private Vector3i position = new Vector3i(1, 8, 4);
    }

    @Test
    public void testSerializerCodec() throws IOException {
        final TypeSerializerCollection serializers = TypeSerializerCollection.defaults().childBuilder()
            .register(VEC3I_TYPE, DfuSerializers.forCodec(VEC3I_CODEC))
            .build();

        final @Nullable Codec<TestSerializable> codec = DfuSerializers.forSerializer(TypeToken.get(TestSerializable.class), serializers);
        assertNotNull(codec);

        final DataResult<JsonElement> out = codec.encode(new TestSerializable(), JsonOps.INSTANCE, JsonOps.INSTANCE.empty());
        out.error().ifPresent(err -> {
            throw new RuntimeException(err.message());
        });

        final StringWriter buffer = new StringWriter();
        try (JsonWriter writer = new JsonWriter(buffer)) {
            writer.setIndent("    ");
            Streams.write(out.result().orElseThrow(() -> new RuntimeException("No result present!")), writer);
        }

        assertLinesMatch(Resources.readLines(getClass().getResource("test-serialize-codec.json"), StandardCharsets.UTF_8),
                Arrays.asList(buffer.toString().split("\n")));

    }

}
