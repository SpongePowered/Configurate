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
package ninja.leaping.configurate.objectmapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class TypeSerializersTest {
    private static final TypeSerializerCollection SERIALIZERS = TypeSerializers.getDefaultSerializers();

    @Test
    public void testStringSerializer() throws ObjectMappingException {
        final TypeToken<String> stringType = TypeToken.of(String.class);
        final TypeSerializer<String> stringSerializer = SERIALIZERS.get(stringType);
        final ConfigurationNode node = SimpleConfigurationNode.root().setValue("foobar");

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
        final TypeSerializer<Integer> numberSerializer = SERIALIZERS.get(intType);
        assertEquals(numberSerializer, SERIALIZERS.get(longType));
        assertEquals(numberSerializer, SERIALIZERS.get(floatType));
        assertEquals(numberSerializer, SERIALIZERS.get(primitiveIntType));

        SimpleConfigurationNode node = SimpleConfigurationNode.root().setValue(45f);
        assertEquals((Object) 45, numberSerializer.deserialize(intType, node));
        assertEquals((Object) 45L, numberSerializer.deserialize(longType, node));
        assertEquals((Object) 45f, numberSerializer.deserialize(floatType, node));
        assertEquals((Object) 45, numberSerializer.deserialize(primitiveIntType, node));

        numberSerializer.serialize(intType, 42, node);
        assertEquals(42, node.getValue());
    }

    @Test
    public void testBooleanSerializer() throws ObjectMappingException {
        final TypeToken<Boolean> booleanType = TypeToken.of(Boolean.class);

        final TypeSerializer<Boolean> booleanSerializer = SERIALIZERS.get(booleanType);
        SimpleConfigurationNode node = SimpleConfigurationNode.root();
        node.getNode("direct").setValue(true);
        node.getNode("fromstring").setValue("true");

        assertEquals(true, booleanSerializer.deserialize(booleanType, node.getNode("direct")));
        assertEquals(true, booleanSerializer.deserialize(booleanType, node.getNode("fromstring")));
    }

    private enum TestEnum {
        FIRST,
        SECOND,
        Third,
        third
    }

    @Test
    public void testEnumValueSerializer() throws ObjectMappingException {
        final TypeToken<TestEnum> enumType = TypeToken.of(TestEnum.class);

        final TypeSerializer<TestEnum> enumSerializer = SERIALIZERS.get(enumType);

        SimpleConfigurationNode node = SimpleConfigurationNode.root();
        node.getNode("present_val").setValue("first");
        node.getNode("another_present_val").setValue("SECOND");
        node.getNode(("casematters_val")).setValue("tHiRd");
        node.getNode(("casematters_val_lowercase")).setValue("third");
        node.getNode("invalid_val").setValue("3rd");

        assertEquals(TestEnum.FIRST, enumSerializer.deserialize(enumType, node.getNode("present_val")));
        assertEquals(TestEnum.SECOND, enumSerializer.deserialize(enumType, node.getNode("another_present_val")));
        assertEquals(TestEnum.Third, enumSerializer.deserialize(enumType, node.getNode("casematters_val")));
        assertEquals(TestEnum.third, enumSerializer.deserialize(enumType, node.getNode("casematters_val_lowercase")));
        Assertions.assertThrows(ObjectMappingException.class, () -> {
            enumSerializer.deserialize(enumType, node.getNode("invalid_val"));
        });
    }

    @Test
    public void testListSerializer() throws ObjectMappingException {
        final TypeToken<List<String>> stringListType = new TypeToken<List<String>>() {};
        final TypeSerializer<List<String>> stringListSerializer = SERIALIZERS.get(stringListType);
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
    public void testListSerializerPreservesEmptyList() throws ObjectMappingException {
        final TypeToken<List<String>> listStringType = new TypeToken<List<String>>() {};
        final TypeSerializer<List<String>> listStringSerializer =
                SERIALIZERS.get(listStringType);

        final ConfigurationNode value = SimpleConfigurationNode.root();

        listStringSerializer.serialize(listStringType, ImmutableList.of(), value);

        assertTrue(value.hasListChildren());
    }

    @Test
    public void testListRawTypes() throws ObjectMappingException {
        final TypeToken<List> rawType = TypeToken.of(List.class);
        final TypeSerializer<List> serial = SERIALIZERS.get(rawType);

        final ConfigurationNode value = SimpleConfigurationNode.root();

        value.getAppendedNode().setValue(1);
        value.getAppendedNode().setValue("dog");
        value.getAppendedNode().setValue(2.4);

        Assertions.assertTrue(Assertions.assertThrows(Exception.class, () -> {
            serial.deserialize(rawType, value);
        }).getMessage().startsWith("Raw types"));
    }

    @Test
    public void testMapSerializer() throws ObjectMappingException {
        final TypeToken<Map<String, Integer>> mapStringIntType = new TypeToken<Map<String, Integer>>() {};
        final TypeSerializer<Map<String, Integer>> mapStringIntSerializer =
                SERIALIZERS.get(mapStringIntType);

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

    @Test
    public void testInvalidMapValueTypes() throws ObjectMappingException {
        final TypeToken<Map<TestEnum, Integer>> mapTestEnumIntType = new TypeToken<Map<TestEnum, Integer>>() {};
        final TypeSerializer<Map<TestEnum, Integer>> mapTestEnumIntSerializer =
                SERIALIZERS.get(mapTestEnumIntType);

        final ConfigurationNode value = SimpleConfigurationNode.root();
        value.getNode("FIRST").setValue(5);
        value.getNode("SECOND").setValue(8);

        Map<TestEnum, Integer> des = mapTestEnumIntSerializer.deserialize(mapTestEnumIntType, value);
        final ConfigurationNode serialVal = SimpleConfigurationNode.root();
        mapTestEnumIntSerializer.serialize(mapTestEnumIntType, des, serialVal);
        assertEquals(value.getValue(), serialVal.getValue());
        //assertEquals(value, serialVal);
    }

    @Test
    public void testMapSerializerRemovesDeletedKeys() throws ObjectMappingException {
        final TypeToken<Map<String, Integer>> mapStringIntType = new TypeToken<Map<String, Integer>>() {};
        final TypeSerializer<Map<String, Integer>> mapStringIntSerializer = SERIALIZERS.get(mapStringIntType);

        final ConfigurationNode value = SimpleConfigurationNode.root();
        value.getNode("fish").setValue(5);
        value.getNode("bugs").setValue("124880");
        value.getNode("time").setValue("-1");

        @SuppressWarnings("unchecked")
        final Map<String, Integer> deserialized = mapStringIntSerializer.deserialize(mapStringIntType, value);
        deserialized.remove("fish");

        mapStringIntSerializer.serialize(mapStringIntType, deserialized, value);
        assertTrue(value.getNode("fish").isVirtual());
        assertFalse(value.getNode("bugs").isVirtual());
    }

    @Test
    public void testMapSerializerPreservesEmptyMap() throws ObjectMappingException {
        final TypeToken<Map<String, Integer>> mapStringIntType = new TypeToken<Map<String, Integer>>() {};
        final TypeSerializer<Map<String, Integer>> mapStringIntSerializer =
                SERIALIZERS.get(mapStringIntType);

        final ConfigurationNode value = SimpleConfigurationNode.root();

        mapStringIntSerializer.serialize(mapStringIntType, ImmutableMap.of(), value);

        assertTrue(value.hasMapChildren());
    }

    @ConfigSerializable
    private static class TestObject {
        @Setting("int") private int value;
        @Setting private String name;
    }

    @Test
    public void testAnnotatedObjectSerializer() throws ObjectMappingException {
        final TypeToken<TestObject> testNodeType = TypeToken.of(TestObject.class);
        final TypeSerializer<TestObject> testObjectSerializer = SERIALIZERS.get(testNodeType);
        final ConfigurationNode node = SimpleConfigurationNode.root();
        node.getNode("int").setValue("42");
        node.getNode("name").setValue("Bob");

        TestObject object = testObjectSerializer.deserialize(testNodeType, node);
        assertEquals(42, object.value);
        assertEquals("Bob", object.name);
    }

    @Test
    public void testURISerializer() throws ObjectMappingException {
         final TypeToken<URI> uriType = TypeToken.of(URI.class);
         final TypeSerializer<URI> uriSerializer = SERIALIZERS.get(uriType);

         final String uriString = "http://google.com";
         final URI testUri = URI.create(uriString);

         SimpleConfigurationNode node = SimpleConfigurationNode.root().setValue(uriString);
         assertEquals(testUri, uriSerializer.deserialize(uriType, node));

         uriSerializer.serialize(uriType, testUri, node);
         assertEquals(uriString, node.getValue());
    }

    @Test
    public void testURLSerializer() throws ObjectMappingException, MalformedURLException {
         final TypeToken<URL> urlType = TypeToken.of(URL.class);
         final TypeSerializer<URL> urlSerializer = SERIALIZERS.get(urlType);

         final String urlString = "http://google.com";
         final URL testUrl = new URL(urlString);

         SimpleConfigurationNode node = SimpleConfigurationNode.root().setValue(urlString);
         assertEquals(testUrl, urlSerializer.deserialize(urlType, node));

         urlSerializer.serialize(urlType, testUrl, node);
         assertEquals(urlString, node.getValue());
    }

    @Test
    public void testUUIDSerializer() throws ObjectMappingException {
        final TypeToken<UUID> uuidType = TypeToken.of(UUID.class);
        final TypeSerializer<UUID> uuidSerializer = SERIALIZERS.get(uuidType);

        final UUID testUuid = UUID.randomUUID();

        SimpleConfigurationNode serializeTo = SimpleConfigurationNode.root();
        uuidSerializer.serialize(uuidType, testUuid, serializeTo);
        assertEquals(testUuid.toString(), serializeTo.getValue());

        assertEquals(testUuid, uuidSerializer.deserialize(uuidType, serializeTo));

    }

    @Test
    public void testPatternSerializer() throws ObjectMappingException {
        final TypeToken<Pattern> patternType = TypeToken.of(Pattern.class);
        final TypeSerializer<Pattern> patternSerializer = SERIALIZERS.get(patternType);

        final Pattern testPattern = Pattern.compile("(na )+batman");
        SimpleConfigurationNode serializeTo = SimpleConfigurationNode.root();
        patternSerializer.serialize(patternType, testPattern, serializeTo);
        assertEquals("(na )+batman", serializeTo.getValue());
        assertEquals(testPattern.pattern(), patternSerializer.deserialize(patternType, serializeTo).pattern());
    }
}
