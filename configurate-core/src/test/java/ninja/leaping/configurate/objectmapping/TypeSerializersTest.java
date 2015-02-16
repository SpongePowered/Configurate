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
package ninja.leaping.configurate.objectmapping;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TypeSerializersTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Test
    public void testStringSerializer() throws ObjectMappingException {
        final TypeToken<String> stringType = TypeToken.of(String.class);
        final TypeSerializer stringSerializer = TypeSerializers.getSerializer(stringType);
        final ConfigurationNode node = SimpleConfigurationNode.root().setValue("foobar");

        assertTrue(stringSerializer.isApplicable(stringType));
        assertEquals("foobar", stringSerializer.deserialize(stringType, node));
        stringSerializer.serialize(stringType, "foobarbaz", node);
        assertEquals("foobarbaz", node.getString());
    }

    @Test
    public void testNumberSerializer() throws ObjectMappingException {
        final TypeToken<Integer> intType = TypeToken.of(Integer.class);
        final TypeToken<Long> longType = TypeToken.of(Long.class);
        final TypeToken<Float> floatType = TypeToken.of(Float.class);
        final TypeToken<?> primitiveIntType = TypeToken.of(int.class);

        // They must all be the same serializer
        final TypeSerializer numberSerializer = TypeSerializers.getSerializer(intType);
        assertEquals(numberSerializer, TypeSerializers.getSerializer(longType));
        assertEquals(numberSerializer, TypeSerializers.getSerializer(floatType));
        assertEquals(numberSerializer, TypeSerializers.getSerializer(primitiveIntType));

        SimpleConfigurationNode node = SimpleConfigurationNode.root().setValue(45f);
        assertEquals(45, numberSerializer.deserialize(intType, node));
        assertEquals(45L, numberSerializer.deserialize(longType, node));
        assertEquals(45f, numberSerializer.deserialize(floatType, node));
        assertEquals(45, numberSerializer.deserialize(primitiveIntType, node));

        numberSerializer.serialize(intType, 42, node);
        assertEquals(42, node.getValue());
    }

    @Test
    public void testBooleanSerializer() throws ObjectMappingException {
        final TypeToken<Boolean> booleanType = TypeToken.of(Boolean.class);

        final TypeSerializer booleanSerializer = TypeSerializers.getSerializer(booleanType);
        SimpleConfigurationNode node = SimpleConfigurationNode.root();
        node.getNode("direct").setValue(true);
        node.getNode("fromstring").setValue("true");

        assertEquals(true, booleanSerializer.deserialize(booleanType, node.getNode("direct")));
        assertEquals(true, booleanSerializer.deserialize(booleanType, node.getNode("fromstring")));
    }

    private static enum TestEnum {
        FIRST,
        SECOND,
    }

    @Test
    public void testEnumValueSerializer() throws ObjectMappingException {
        final TypeToken<TestEnum> enumType = TypeToken.of(TestEnum.class);

        final TypeSerializer enumSerializer = TypeSerializers.getSerializer(enumType);

        SimpleConfigurationNode node = SimpleConfigurationNode.root();
        node.getNode("present_val").setValue("first");
        node.getNode("another_present_val").setValue("SECOND");
        node.getNode("invalid_val").setValue("3rd");

        assertEquals(TestEnum.FIRST, enumSerializer.deserialize(enumType, node.getNode("present_val")));
        assertEquals(TestEnum.SECOND, enumSerializer.deserialize(enumType, node.getNode("another_present_val")));
        expectedException.expect(ObjectMappingException.class);
        enumSerializer.deserialize(enumType, node.getNode("invalid_val"));

    }

    @Test
    public void testListSerializer() throws ObjectMappingException {
        final TypeToken<List<String>> stringListType = new TypeToken<List<String>>() {};
        final TypeSerializer stringListSerializer = TypeSerializers.getSerializer(stringListType);
        final ConfigurationNode value = SimpleConfigurationNode.root();
        value.getAppendedNode().setValue("hi");
        value.getAppendedNode().setValue("there");
        value.getAppendedNode().setValue("beautiful");
        value.getAppendedNode().setValue("people");

        assertEquals(Arrays.asList("hi", "there", "beautiful", "people"), stringListSerializer.deserialize(stringListType, value));
        value.setValue(null);

        stringListSerializer.serialize(stringListType, Arrays.asList("hi", "there", "lame", "people"), value);
        assertEquals("hi", value.getNode(0).getString());
        assertEquals("there", value.getNode(1).getString());
        assertEquals("lame", value.getNode(2).getString());
        assertEquals("people", value.getNode(3).getString());
    }

    @Test
    public void testMapSerializer() throws ObjectMappingException {
        final TypeToken<Map<String, Integer>> mapStringIntType = new TypeToken<Map<String, Integer>>() {};
        final TypeSerializer mapStringIntSerializer = TypeSerializers.getSerializer(mapStringIntType);

        final ConfigurationNode value = SimpleConfigurationNode.root();
        value.getNode("fish").setValue(5);
        value.getNode("bugs").setValue("124880");
        value.getNode("time").setValue("-1");

        final Map<String, Integer> expectedValues = ImmutableMap.of("fish", 5, "bugs", 124880, "time", -1);

        assertEquals(expectedValues, mapStringIntSerializer.deserialize(mapStringIntType, value));

        value.setValue(null);

        mapStringIntSerializer.serialize(mapStringIntType, expectedValues, value);
        assertEquals(5, value.getNode("fish").getInt());
        assertEquals(124880, value.getNode("bugs").getInt());
        assertEquals(-1, value.getNode("time").getInt());
    }

    @ConfigSerializable
    private static class TestObject {
        @Setting({"int"}) private int value;
        @Setting({"name"}) private String name;
    }

    @Test
    public void testAnnotatedObjectSerializer() throws ObjectMappingException {
        final TypeToken<TestObject> testNodeType = TypeToken.of(TestObject.class);
        final TypeSerializer testObjectSerializer = TypeSerializers.getSerializer(testNodeType);
        final ConfigurationNode node = SimpleConfigurationNode.root();
        node.getNode("int").setValue("42");
        node.getNode("name").setValue("Bob");

        TestObject object = (TestObject) testObjectSerializer.deserialize(testNodeType, node);
        assertEquals(42, object.value);
        assertEquals("Bob", object.name);
    }
}
