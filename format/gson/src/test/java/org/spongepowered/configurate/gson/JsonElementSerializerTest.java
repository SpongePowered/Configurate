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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.util.CheckedConsumer;

public class JsonElementSerializerTest {

    ConfigurationNode node() {
        return BasicConfigurationNode.root(GsonConfigurationLoader.DEFAULT_OPTIONS);
    }

    <E extends Exception> BasicConfigurationNode node(final CheckedConsumer<BasicConfigurationNode, E> action) throws E {
        return BasicConfigurationNode.root(GsonConfigurationLoader.DEFAULT_OPTIONS, action);
    }

    @Test
    void testDeserializeArray() throws SerializationException {
        final ConfigurationNode source = node(n -> {
            n.appendListNode().set("one");
            n.appendListNode().set(2);
            n.appendListNode().act(c -> {
                c.node("colour").set(0xFEFEFE);
            });
        });

        final @Nullable JsonArray array = source.get(JsonArray.class);

        assertNotNull(array);
        assertEquals("one", array.get(0).getAsString());
        assertEquals(2, array.get(1).getAsNumber());
        assertEquals(0xFEFEFE, array.get(2).getAsJsonObject().get("colour").getAsNumber());
    }

    @Test
    void testSerializeArray() throws SerializationException {
        final JsonArray source = new JsonArray();
        source.add("one");
        source.add(2);
        final JsonObject child = new JsonObject();
        child.addProperty("colour", 0xFEFEFE);
        source.add(child);

        final ConfigurationNode dest = node().set(source);
        assertTrue(dest.isList());

        assertEquals("one", dest.node(0).getString());
        assertEquals(2, dest.node(1).getInt());
        assertEquals(0xFEFEFE, dest.node(2, "colour").getInt());
    }

    @Test
    void testDeserializeObject() throws SerializationException {
        final ConfigurationNode source = node(n -> {
            n.node("yes").set("no");
            n.node("eee").set("ffff");
            n.node("a number").set(4.45f);
        });

        final @Nullable JsonObject object = source.get(JsonObject.class);

        assertNotNull(object);
        assertEquals("no", object.get("yes").getAsString());
        assertEquals("ffff", object.get("eee").getAsString());
        assertEquals(4.45f, object.get("a number").getAsFloat());
    }

    @Test
    void testSerializeObject() throws SerializationException {
        final JsonObject source = new JsonObject();
        source.addProperty("yes", "no");
        source.addProperty("eee", "ffff");
        source.addProperty("a number", 4.45f);

        final ConfigurationNode result = node().set(source);

        assertEquals("no", result.node("yes").raw());
        assertEquals("ffff", result.node("eee").raw());
        assertEquals(4.45f, result.node("a number").raw());
    }

    @Test
    void testRoundtripObjectPreservesComments() throws SerializationException {
        final CommentedConfigurationNode target = CommentedConfigurationNode.root(GsonConfigurationLoader.DEFAULT_OPTIONS, n -> {
            n.node("test1").comment("giving commentary").set("hi");
        });

        target.set(target.get(JsonObject.class));
        assertEquals("giving commentary", target.node("test1").comment());
    }

    @Test
    void testDeserializeString() throws SerializationException {
        final @Nullable JsonElement string = node().set("meow").get(JsonElement.class);

        assertNotNull(string);
        assertEquals("meow", string.getAsString());
    }

    @Test
    void testSerializeString() throws SerializationException {
        final ConfigurationNode node = node().set(new JsonPrimitive("meow"));

        assertEquals("meow", node.raw());
    }

    @Test
    void testDeserializeBoolean() throws SerializationException {
        final @Nullable JsonElement bool = node().set(false).get(JsonElement.class);

        assertNotNull(bool);
        assertEquals(false, bool.getAsBoolean());
    }

    @Test
    void testSerializeBoolean() throws SerializationException {
        final ConfigurationNode node = node().set(new JsonPrimitive(false));

        assertEquals(false, node.raw());
    }

    @Test
    void testDeserializeNumber() throws SerializationException {
        final @Nullable JsonElement num = node().set(2.24d).get(JsonElement.class);

        assertNotNull(num);
        assertEquals(2.24d, num.getAsNumber());
    }

    @Test
    void testSerializeNumber() throws SerializationException {
        final ConfigurationNode node = node().set(new JsonPrimitive(2.24d));

        assertEquals(2.24d, node.raw());
    }

}
