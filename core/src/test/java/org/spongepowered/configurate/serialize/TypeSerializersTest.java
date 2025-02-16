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
package org.spongepowered.configurate.serialize;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.util.UnmodifiableCollections;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@SuppressWarnings({
    "checkstyle:IllegalType", // for Optional
    "PMD.LooseCoupling" // testing specific type implementations
})
class TypeSerializersTest {

    private <T> TypeSerializer<T> serializer(final TypeToken<T> type) {
        final @Nullable TypeSerializer<T> ret = TypeSerializerCollection.defaults().get(type);
        assertNotNull(ret);
        return ret;
    }

    private <T> TypeSerializer<T> serializer(final Class<T> type) {
        final @Nullable TypeSerializer<T> ret = TypeSerializerCollection.defaults().get(type);
        assertNotNull(ret);
        return ret;
    }

    @Test
    void testStringSerializer() throws SerializationException {
        final TypeToken<String> stringType = TypeToken.get(String.class);
        final TypeSerializer<String> stringSerializer = this.serializer(stringType);
        final BasicConfigurationNode node = BasicConfigurationNode.root().set("foobar");

        assertEquals("foobar", stringSerializer.deserialize(stringType.getType(), node));
        stringSerializer.serialize(stringType.getType(), "foobarbaz", node);
        assertEquals("foobarbaz", node.getString());
    }

    @Test
    void testAsBoolean() throws Exception {
        final boolean actual = true;
        final String[] trueEvaluating = new String[] {"true", "yes", "1", "t", "y"};
        final String[] falseEvaluating = new String[] {"false", "no", "0", "f", "n"};
        assertEquals(actual, Scalars.BOOLEAN.deserialize(actual));
        for (final String val : trueEvaluating) {
            assertEquals(true, Scalars.BOOLEAN.deserialize(val));
        }

        for (final String val : falseEvaluating) {
            assertEquals(false, Scalars.BOOLEAN.deserialize(val));
        }
    }

    @Test
    void testBooleanSerializer() throws SerializationException {
        final TypeToken<Boolean> booleanType = TypeToken.get(Boolean.class);

        final TypeSerializer<Boolean> booleanSerializer = this.serializer(booleanType);
        final BasicConfigurationNode node = BasicConfigurationNode.root();
        node.node("direct").set(true);
        node.node("fromstring").set("true");

        assertEquals(true, booleanSerializer.deserialize(Boolean.class, node.node("direct")));
        assertEquals(true, booleanSerializer.deserialize(Boolean.class, node.node("fromstring")));
    }

    private enum TestEnum {
        FIRST,
        SECOND,
        Third,
        third
    }

    @Test
    void testEnumValueSerializer() throws SerializationException {
        final TypeToken<TestEnum> enumType = TypeToken.get(TestEnum.class);

        final TypeSerializer<TestEnum> enumSerializer = this.serializer(enumType);

        final BasicConfigurationNode node = BasicConfigurationNode.root();
        node.node("present_val").set("first");
        node.node("another_present_val").set("SECOND");
        node.node("casematters_val").set("tHiRd");
        node.node("casematters_val_lowercase").set("third");
        node.node("invalid_val").set("3rd");

        assertEquals(TestEnum.FIRST, enumSerializer.deserialize(enumType.getType(), node.node("present_val")));
        assertEquals(TestEnum.SECOND, enumSerializer.deserialize(enumType.getType(), node.node("another_present_val")));
        assertEquals(TestEnum.Third, enumSerializer.deserialize(enumType.getType(), node.node("casematters_val")));
        assertEquals(TestEnum.third, enumSerializer.deserialize(enumType.getType(), node.node("casematters_val_lowercase")));
        Assertions.assertThrows(SerializationException.class, () -> {
            enumSerializer.deserialize(enumType.getType(), node.node("invalid_val"));
        });
    }

    @Test
    void testListSerializer() throws SerializationException {
        final TypeToken<List<String>> stringListType = new TypeToken<List<String>>() {};
        final TypeSerializer<List<String>> stringListSerializer = this.serializer(stringListType);
        final BasicConfigurationNode value = BasicConfigurationNode.root();
        value.appendListNode().set("hi");
        value.appendListNode().set("there");
        value.appendListNode().set("beautiful");
        value.appendListNode().set("people");

        assertEquals(Arrays.asList("hi", "there", "beautiful", "people"), stringListSerializer.deserialize(stringListType.getType(), value));
        value.set(null);

        stringListSerializer.serialize(stringListType.getType(), Arrays.asList("hi", "there", "lame", "people"), value);
        assertEquals("hi", value.node(0).getString());
        assertEquals("there", value.node(1).getString());
        assertEquals("lame", value.node(2).getString());
        assertEquals("people", value.node(3).getString());
    }

    @Test
    void testSetSerializer() throws SerializationException {
        final TypeToken<Set<String>> stringListType = new TypeToken<Set<String>>() {};
        final TypeSerializer<Set<String>> stringListSerializer = this.serializer(stringListType);
        final BasicConfigurationNode value = BasicConfigurationNode.root();
        value.appendListNode().set("hi");
        value.appendListNode().set("there");
        value.appendListNode().set("beautiful");
        value.appendListNode().set("people");

        assertEquals(UnmodifiableCollections.toSet("hi", "there", "beautiful", "people"),
            stringListSerializer.deserialize(stringListType.getType(), value));
        value.set(null);

        final Set<String> testSet = UnmodifiableCollections.toSet("hi", "there", "tired", "people");
        stringListSerializer.serialize(stringListType.getType(), testSet, value);
        final List<BasicConfigurationNode> children = value.childrenList();
        // Test equality without expecting a specific order
        assertEquals(testSet.size(), children.size());
        for (final BasicConfigurationNode child : children) {
            assertTrue(testSet.contains(child.getString()));
        }
    }

    @Test
    void testListSerializerPreservesEmptyList() throws SerializationException {
        final TypeToken<List<String>> listStringType = new TypeToken<List<String>>() {};
        final TypeSerializer<List<String>> listStringSerializer =
                this.serializer(listStringType);

        final BasicConfigurationNode value = BasicConfigurationNode.root();

        listStringSerializer.serialize(listStringType.getType(), Collections.emptyList(), value);

        assertTrue(value.isList());
    }

    @Test
    @SuppressWarnings("rawtypes")
    void testListRawTypes() {
        final TypeToken<List> rawType = TypeToken.get(List.class);
        final TypeSerializer<List> serial = this.serializer(rawType);

        final BasicConfigurationNode value = BasicConfigurationNode.root();

        value.appendListNode().raw(1);
        value.appendListNode().raw("dog");
        value.appendListNode().raw(2.4);

        Assertions.assertTrue(Assertions.assertThrows(Exception.class, () -> {
            serial.deserialize(rawType.getType(), value);
        }).getMessage().contains("Raw types"));
    }

    @Test
    void testMapSerializer() throws SerializationException {
        final TypeToken<Map<String, Integer>> mapStringIntType = new TypeToken<Map<String, Integer>>() {};
        final TypeSerializer<Map<String, Integer>> mapStringIntSerializer =
                this.serializer(mapStringIntType);

        final BasicConfigurationNode value = BasicConfigurationNode.root();
        value.node("fish").set(5);
        value.node("bugs").set("124880");
        value.node("time").set("-1");

        final Map<String, Integer> expectedValues = ImmutableMap.of("fish", 5, "bugs", 124880, "time", -1);

        assertEquals(expectedValues, mapStringIntSerializer.deserialize(mapStringIntType.getType(), value));

        value.set(null);

        mapStringIntSerializer.serialize(mapStringIntType.getType(), expectedValues, value);
        assertEquals(5, value.node("fish").getInt());
        assertEquals(124880, value.node("bugs").getInt());
        assertEquals(-1, value.node("time").getInt());
    }

    @Test
    void testInvalidMapValueTypes() throws SerializationException {
        final TypeToken<Map<TestEnum, Integer>> mapTestEnumIntType = new TypeToken<Map<TestEnum, Integer>>() {};
        final TypeSerializer<Map<TestEnum, Integer>> mapTestEnumIntSerializer =
                this.serializer(mapTestEnumIntType);

        final BasicConfigurationNode value = BasicConfigurationNode.root();
        value.node("FIRST").set(5);
        value.node("SECOND").set(8);

        final @Nullable Map<TestEnum, Integer> des = mapTestEnumIntSerializer.deserialize(mapTestEnumIntType.getType(), value);
        final BasicConfigurationNode serialVal = BasicConfigurationNode.root(ConfigurationOptions.defaults()
                .nativeTypes(UnmodifiableCollections.toSet(String.class, Integer.class)));
        mapTestEnumIntSerializer.serialize(mapTestEnumIntType.getType(), des, serialVal);
        assertEquals(value.raw(), serialVal.raw());
        //assertEquals(value, serialVal);
    }

    @Test
    void testMapSerializerRemovesDeletedKeys() throws SerializationException {
        final TypeToken<Map<String, Integer>> mapStringIntType = new TypeToken<Map<String, Integer>>() {};
        final TypeSerializer<Map<String, Integer>> mapStringIntSerializer = this.serializer(mapStringIntType);

        final BasicConfigurationNode value = BasicConfigurationNode.root();
        value.node("fish").set(5);
        value.node("bugs").set("124880");
        value.node("time").set("-1");

        @SuppressWarnings("unchecked")
        final @Nullable Map<String, Integer> deserialized = mapStringIntSerializer.deserialize(mapStringIntType.getType(), value);
        requireNonNull(deserialized).remove("fish");

        mapStringIntSerializer.serialize(mapStringIntType.getType(), deserialized, value);
        assertTrue(value.node("fish").virtual());
        assertFalse(value.node("bugs").virtual());
    }

    @Test
    void testMapSerializerPreservesEmptyMap() throws SerializationException {
        final TypeToken<Map<String, Integer>> mapStringIntType = new TypeToken<Map<String, Integer>>() {};
        final TypeSerializer<Map<String, Integer>> mapStringIntSerializer =
                this.serializer(mapStringIntType);

        final BasicConfigurationNode value = BasicConfigurationNode.root();

        mapStringIntSerializer.serialize(mapStringIntType.getType(), Collections.emptyMap(), value);

        assertTrue(value.isMap());
    }

    @Test
    void testMapSerializerPreservesChildComments() throws SerializationException {
        final TypeToken<Map<String, Integer>> mapStringIntType = new TypeToken<Map<String, Integer>>() {};
        final TypeSerializer<Map<String, Integer>> mapStringIntSerializer =
                this.serializer(mapStringIntType);

        final CommentedConfigurationNode commentNode = CommentedConfigurationNode.root();

        commentNode.node("hi").comment("test").set(3);

        mapStringIntSerializer.serialize(mapStringIntType.getType(), ImmutableMap.of("hi", 5, "no", 2), commentNode);

        assertEquals(5, commentNode.node("hi").raw());
        assertEquals("test", commentNode.node("hi").comment());

    }

    @ConfigSerializable
    private static final class TestObject {
        @Setting("int") private int value;
        @Setting private String name;
    }

    @Test
    void testAnnotatedObjectSerializer() throws SerializationException {
        final TypeToken<TestObject> testNodeType = TypeToken.get(TestObject.class);
        final TypeSerializer<TestObject> testObjectSerializer = this.serializer(testNodeType);
        final BasicConfigurationNode node = BasicConfigurationNode.root();
        node.node("int").set("42");
        node.node("name").set("Bob");

        final TestObject object = testObjectSerializer.deserialize(testNodeType.getType(), node);
        assertEquals(42, object.value);
        assertEquals("Bob", object.name);
    }

    @Test
    void testUriSerializer() throws SerializationException {
        final TypeToken<URI> uriType = TypeToken.get(URI.class);
        final TypeSerializer<URI> uriSerializer = this.serializer(uriType);

        final String uriString = "http://google.com";
        final URI testUri = URI.create(uriString);

        final BasicConfigurationNode node = BasicConfigurationNode.root(ConfigurationOptions.defaults()
                .nativeTypes(UnmodifiableCollections.toSet(String.class, Integer.class)))
                .set(uriString);
        assertEquals(testUri, uriSerializer.deserialize(uriType.getType(), node));

        uriSerializer.serialize(uriType.getType(), testUri, node);
        assertEquals(uriString, node.raw());
    }

    @Test
    void testUrlSerializer() throws SerializationException, MalformedURLException {
        final TypeToken<URL> urlType = TypeToken.get(URL.class);
        final TypeSerializer<URL> urlSerializer = this.serializer(urlType);

        final String urlString = "http://google.com";
        final URL testUrl = new URL(urlString);

        final BasicConfigurationNode node = BasicConfigurationNode.root(ConfigurationOptions.defaults()
                .nativeTypes(UnmodifiableCollections.toSet(String.class, Integer.class)))
                .set(urlString);
        assertEquals(testUrl, urlSerializer.deserialize(urlType.getType(), node));

        urlSerializer.serialize(urlType.getType(), testUrl, node);
        assertEquals(urlString, node.raw());
    }

    @Test
    void testUuidSerializer() throws SerializationException {
        final TypeToken<UUID> uuidType = TypeToken.get(UUID.class);
        final TypeSerializer<UUID> uuidSerializer = this.serializer(uuidType);

        final UUID testUuid = UUID.randomUUID();

        final BasicConfigurationNode serializeTo = BasicConfigurationNode.root(ConfigurationOptions.defaults()
                .nativeTypes(UnmodifiableCollections.toSet(String.class, Integer.class)));
        uuidSerializer.serialize(uuidType.getType(), testUuid, serializeTo);
        assertEquals(testUuid.toString(), serializeTo.raw());

        assertEquals(testUuid, uuidSerializer.deserialize(uuidType.getType(), serializeTo));

    }

    @Test
    void testPatternSerializer() throws SerializationException {
        final TypeToken<Pattern> patternType = TypeToken.get(Pattern.class);
        final TypeSerializer<Pattern> patternSerializer = this.serializer(patternType);

        final Pattern testPattern = Pattern.compile("(na )+batman");
        final BasicConfigurationNode serializeTo = BasicConfigurationNode.root(ConfigurationOptions.defaults()
                .nativeTypes(UnmodifiableCollections.toSet(String.class, Integer.class)));
        patternSerializer.serialize(patternType.getType(), testPattern, serializeTo);
        assertEquals("(na )+batman", serializeTo.raw());
        assertEquals(testPattern.pattern(), patternSerializer.deserialize(patternType.getType(), serializeTo).pattern());
    }

    @Test
    void testCharSerializer() throws SerializationException {
        final TypeToken<Character> charType = TypeToken.get(Character.class);
        final TypeSerializer<Character> charSerializer = this.serializer(charType);

        final BasicConfigurationNode serializeTo = BasicConfigurationNode.root();

        serializeTo.set("e");
        assertEquals(Character.valueOf('e'), charSerializer.deserialize(charType.getType(), serializeTo));

        serializeTo.set('P');
        assertEquals(Character.valueOf('P'), charSerializer.deserialize(charType.getType(), serializeTo));

        serializeTo.set(0x2a);
        assertEquals(Character.valueOf('*'), charSerializer.deserialize(charType.getType(), serializeTo));

        charSerializer.serialize(charType.getType(), 'z', serializeTo);
        assertEquals('z', serializeTo.raw());
    }

    @Test
    void testArraySerializer() throws SerializationException {
        final TypeToken<String[]> arrayType = TypeToken.get(String[].class);
        final TypeSerializer<String[]> arraySerializer = this.serializer(arrayType);

        final String[] testArray = new String[] {"hello", "world"};
        final BasicConfigurationNode serializeTo = BasicConfigurationNode.root();
        arraySerializer.serialize(arrayType.getType(), testArray, serializeTo);
        assertEquals(Arrays.asList("hello", "world"), serializeTo.raw());
        assertArrayEquals(testArray, arraySerializer.deserialize(arrayType.getType(), serializeTo));
    }

    @Test
    void testArraySerializerBooleanPrimitive() throws SerializationException {
        final TypeToken<boolean[]> booleanArrayType = TypeToken.get(boolean[].class);
        final TypeSerializer<boolean[]> booleanArraySerializer = this.serializer(booleanArrayType);

        final boolean[] testArray = new boolean[] {true, false, true, true, false};
        final BasicConfigurationNode serializeTo = BasicConfigurationNode.root();
        booleanArraySerializer.serialize(booleanArrayType.getType(), testArray, serializeTo);
        assertEquals(Arrays.asList(true, false, true, true, false), serializeTo.raw());
        assertArrayEquals(testArray, booleanArraySerializer.deserialize(booleanArrayType.getType(), serializeTo));
    }

    @Test
    void testArraySerializerBytePrimitive() throws SerializationException {
        final TypeToken<byte[]> byteArrayType = TypeToken.get(byte[].class);
        final TypeSerializer<byte[]> byteArraySerializer = this.serializer(byteArrayType);

        final byte[] testArray = new byte[] {1, 5, 3, -7, 9, 0};
        final BasicConfigurationNode serializeTo = BasicConfigurationNode.root();
        byteArraySerializer.serialize(byteArrayType.getType(), testArray, serializeTo);
        assertEquals(Arrays.asList((byte) 1, (byte) 5, (byte) 3, (byte) -7, (byte) 9, (byte) 0), serializeTo.raw());
        assertArrayEquals(testArray, byteArraySerializer.deserialize(byteArrayType.getType(), serializeTo));
    }

    @Test
    void testArraySerializerCharPrimitive() throws SerializationException {
        final Class<char[]> charArrayType = char[].class;
        final TypeSerializer<char[]> charArraySerializer = this.serializer(charArrayType);

        final char[] testArray = new char[] {'s', 'l', 'e', 'e', 'p'};
        final BasicConfigurationNode serializeTo = BasicConfigurationNode.root();
        charArraySerializer.serialize(charArrayType, testArray, serializeTo);
        assertEquals(Arrays.asList('s', 'l', 'e', 'e', 'p'), serializeTo.raw());
        assertArrayEquals(testArray, charArraySerializer.deserialize(charArrayType, serializeTo));
    }

    @Test
    void testArraySerializerShortPrimitive() throws SerializationException {
        final Class<short[]> shortArrayType = short[].class;
        final TypeSerializer<short[]> shortArraySerializer = this.serializer(shortArrayType);

        final short[] testArray = new short[] {1, 5, 3, 7, 9};
        final BasicConfigurationNode serializeTo = BasicConfigurationNode.root();
        shortArraySerializer.serialize(shortArrayType, testArray, serializeTo);
        assertEquals(Arrays.asList((short) 1, (short) 5, (short) 3, (short) 7, (short) 9), serializeTo.raw());
        assertArrayEquals(testArray, shortArraySerializer.deserialize(shortArrayType, serializeTo));
    }

    @Test
    void testArraySerializerIntPrimitive() throws SerializationException {
        final Class<int[]> intArrayType = int[].class;
        final TypeSerializer<int[]> intArraySerializer = this.serializer(intArrayType);

        final int[] testArray = new int[] {1, 5, 3, 7, 9};
        final BasicConfigurationNode serializeTo = BasicConfigurationNode.root();
        intArraySerializer.serialize(intArrayType, testArray, serializeTo);
        assertEquals(Arrays.asList(1, 5, 3, 7, 9), serializeTo.raw());
        assertArrayEquals(testArray, intArraySerializer.deserialize(intArrayType, serializeTo));
    }

    @Test
    void testArraySerializerLongPrimitive() throws SerializationException {
        final Class<long[]> longArrayType = long[].class;
        final TypeSerializer<long[]> longArraySerializer = this.serializer(longArrayType);

        final long[] testArray = new long[] {1, 5, 3, 7, 9};
        final BasicConfigurationNode serializeTo = BasicConfigurationNode.root();
        longArraySerializer.serialize(longArrayType, testArray, serializeTo);
        assertEquals(Arrays.asList(1L, 5L, 3L, 7L, 9L), serializeTo.raw());
        assertArrayEquals(testArray, longArraySerializer.deserialize(longArrayType, serializeTo));
    }

    @Test
    void testArraySerializerFloatPrimitive() throws SerializationException {
        final Class<float[]> floatArrayType = float[].class;
        final TypeSerializer<float[]> floatArraySerializer = this.serializer(floatArrayType);

        final float[] testArray = new float[] {1.02f, 5.66f, 3.2f, 7.9f, 9f};
        final BasicConfigurationNode serializeTo = BasicConfigurationNode.root();
        floatArraySerializer.serialize(floatArrayType, testArray, serializeTo);
        assertEquals(Arrays.asList(1.02f, 5.66f, 3.2f, 7.9f, 9f), serializeTo.raw());
        assertArrayEquals(testArray, floatArraySerializer.deserialize(floatArrayType, serializeTo));
    }

    @Test
    void testArraySerializerDoublePrimitive() throws SerializationException {
        final Class<double[]> doubleArrayType = double[].class;
        final TypeSerializer<double[]> doubleArraySerializer = this.serializer(doubleArrayType);

        final double[] testArray = new double[] {1.02d, 5.66d, 3.2d, 7.9d, 9d};
        final BasicConfigurationNode serializeTo = BasicConfigurationNode.root();
        doubleArraySerializer.serialize(doubleArrayType, testArray, serializeTo);
        assertEquals(Arrays.asList(1.02d, 5.66d, 3.2d, 7.9d, 9d), serializeTo.raw());
        assertArrayEquals(testArray, doubleArraySerializer.deserialize(doubleArrayType, serializeTo));
    }

    @Test
    void testConfigurationNodeSerializer() throws SerializationException {
        final Class<ConfigurationNode> nodeType = ConfigurationNode.class;
        final TypeSerializer<ConfigurationNode> nodeSerializer = this.serializer(nodeType);
        assertNotNull(nodeSerializer);

        final BasicConfigurationNode sourceNode = BasicConfigurationNode.root(n -> {
            n.node("hello").raw("world");
            n.node("lorg").act(c -> {
                c.appendListNode().raw("doggo");
                c.appendListNode().raw("pupper");
            });
        });

        final ConfigurationNode ret = nodeSerializer.deserialize(nodeType, sourceNode);
        assertEquals(sourceNode, ret);

        final BasicConfigurationNode dest = BasicConfigurationNode.root();
        nodeSerializer.serialize(nodeType, ret, dest);

        assertEquals(sourceNode, dest);
    }

    @Test
    void testPathSerializer() throws SerializationException {
        final TypeSerializer<Path> pathSerializer = this.serializer(Path.class);
        assertNotNull(pathSerializer);

        final BasicConfigurationNode source = BasicConfigurationNode.root().set("test" + FileSystems.getDefault().getSeparator() + "file.txt");
        final Path ret = pathSerializer.deserialize(Path.class, source);
        assertEquals(Paths.get("test", "file.txt"), ret.normalize());

        final BasicConfigurationNode writeTo = BasicConfigurationNode.root(ConfigurationOptions.defaults()
                .nativeTypes(ImmutableSet.of(String.class, Byte.class)));
        pathSerializer.serialize(Path.class, ret, writeTo);
        assertEquals(source, writeTo);
    }

    @Test
    void testPathSerializerFromList() throws SerializationException {
        final TypeSerializer<Path> pathSerializer = this.serializer(Path.class);
        assertNotNull(pathSerializer);

        final BasicConfigurationNode source = BasicConfigurationNode.root(n -> {
            n.appendListNode().raw("test");
            n.appendListNode().raw("file.txt");
        });
        final Path ret = pathSerializer.deserialize(Path.class, source);
        assertEquals(Paths.get("test", "file.txt"), ret);
    }

    @Test
    void testFileSerializer() throws SerializationException {
        final TypeSerializer<File> fileSerializer = this.serializer(File.class);
        assertNotNull(fileSerializer);

        final BasicConfigurationNode source = BasicConfigurationNode.root().set("hello/world.png");

        assertEquals(new File("hello/world.png"), fileSerializer.deserialize(File.class, source));
    }

    @Test
    void testMapSerializerWriteToEmptyNodeWithIntegerKeys() throws SerializationException {
        final TypeToken<Map<Integer, String>> type = new TypeToken<Map<Integer, String>>() {};
        final TypeSerializer<Map<Integer, String>> serializer = this.serializer(type);

        final Map<Integer, String> source = new HashMap<>();
        source.put(1, "yoink");
        source.put(5, "hah");

        final BasicConfigurationNode destination = BasicConfigurationNode.root();

        serializer.serialize(type.getType(), source, destination);

        assertTrue(destination.isMap());
        assertEquals("yoink", destination.node(1).raw());
        assertEquals("hah", destination.node(5).raw());
    }

    @Test
    void testDeserializeEnumResultsInEnumSet() throws SerializationException {
        final TypeToken<Set<TestEnum>> type = new TypeToken<Set<TestEnum>>() {};
        final TypeSerializer<Set<TestEnum>> serializer = this.serializer(type);

        final ConfigurationNode out = BasicConfigurationNode.root(n -> {
            n.appendListNode().set("first");
            n.appendListNode().set("second");
        });

        assertTrue(serializer.deserialize(type.getType(), out) instanceof EnumSet<?>);

        // Then with specifically an enum set
        assertNotNull(TypeSerializerCollection.defaults().get(new TypeToken<EnumSet<TestEnum>>() {}));
    }

    @Test
    void testAnnotatedSerializers() throws SerializationException {
        final TypeSerializerCollection collection = TypeSerializerCollection.defaults().childBuilder()
            .registerAnnotated(UppercaseStringTypeSerializer::applicable, UppercaseStringTypeSerializer.INSTANCE)
            .build();
        final TypeToken<@UpperCase String> type = new TypeToken<@UpperCase String>() {};
        final TypeSerializer<@UpperCase String> serializer = collection.get(type);
        assertNotNull(serializer);
        assertInstanceOf(UppercaseStringTypeSerializer.class, serializer);

        final ConfigurationNode contents = BasicConfigurationNode.root().set("hello");

        assertEquals("HELLO", serializer.deserialize(type.getAnnotatedType(), contents));
    }

    @Test
    void testAnnotatedSerializersInMap() throws SerializationException {
        final TypeSerializerCollection collection = TypeSerializerCollection.defaults().childBuilder()
            .registerAnnotated(UppercaseStringTypeSerializer::applicable, UppercaseStringTypeSerializer.INSTANCE)
            .build();
        final TypeToken<Map<String, @UpperCase String>> type = new TypeToken<Map<String, @UpperCase String>>() {};
        final TypeSerializer<Map<String, @UpperCase String>> serializer = collection.get(type);
        assertNotNull(serializer);

        final ConfigurationNode contents = BasicConfigurationNode.root(
            ConfigurationOptions.defaults().serializers(collection),
            n -> {
                n.node("hello").set("world");
                n.node("one").set("two");
            }
        );

        final Map<String, String> value = serializer.deserialize(type.getAnnotatedType(), contents);
        assertEquals("WORLD", value.get("hello"));
        assertEquals("TWO", value.get("one"));
    }

    @Test
    void testAnnotatedSerializersInList() throws SerializationException {
        final TypeSerializerCollection collection = TypeSerializerCollection.defaults().childBuilder()
            .registerAnnotated(UppercaseStringTypeSerializer::applicable, UppercaseStringTypeSerializer.INSTANCE)
            .build();
        final TypeToken<List<@UpperCase String>> type = new TypeToken<List<@UpperCase String>>() {};
        final TypeSerializer<List<@UpperCase String>> serializer = collection.get(type);
        assertNotNull(serializer);

        final ConfigurationNode contents = BasicConfigurationNode.root(
            ConfigurationOptions.defaults().serializers(collection),
            n -> {
                n.appendListNode().set("one");
                n.appendListNode().set("two");
            }
        );

        final List<String> value = serializer.deserialize(type.getAnnotatedType(), contents);
        assertEquals(ImmutableList.of("ONE", "TWO"), value);
    }

    @Test
    void testPatternFlags() throws SerializationException {
        final TypeToken<Pattern> type = new TypeToken<@PatternFlags(Pattern.CASE_INSENSITIVE) Pattern>() {};
        final TypeSerializer<Pattern> serializer = this.serializer(type);

        final Pattern pattern = serializer.deserialize(type.getAnnotatedType(), BasicConfigurationNode.root(n -> n.set("test")));

        assertThat(pattern.flags())
            .inBinary()
            .isEqualTo(Pattern.CASE_INSENSITIVE);
    }

    @Test
    void testOptional() throws SerializationException {
        final TypeToken<Optional<String>> type = new TypeToken<Optional<String>>() {};
        final TypeSerializer<Optional<String>> serializer = this.serializer(type);

        final Optional<String> shouldBeEmpty = serializer.deserialize(type.getAnnotatedType(), BasicConfigurationNode.root());
        assertThat(shouldBeEmpty)
            .isEmpty();

        final Optional<String> present = serializer.deserialize(type.getAnnotatedType(), BasicConfigurationNode.root().raw("hello world"));
        assertThat(present)
            .isNotEmpty()
            .hasValue("hello world");
    }

    @Test
    void testOptionalInt() throws SerializationException {
        final TypeSerializer<OptionalInt> serializer = this.serializer(OptionalInt.class);

        final OptionalInt shouldBeEmpty = serializer.deserialize(OptionalInt.class, BasicConfigurationNode.root());
        assertThat(shouldBeEmpty)
            .isEmpty();

        final OptionalInt present = serializer.deserialize(OptionalInt.class, BasicConfigurationNode.root().raw(13));
        assertThat(present)
            .isNotEmpty()
            .hasValue(13);
    }

    @Test
    void testOptionalLong() throws SerializationException {
        final TypeSerializer<OptionalLong> serializer = this.serializer(OptionalLong.class);

        final OptionalLong shouldBeEmpty = serializer.deserialize(OptionalLong.class, BasicConfigurationNode.root());
        assertThat(shouldBeEmpty)
            .isEmpty();

        final OptionalLong present = serializer.deserialize(OptionalLong.class, BasicConfigurationNode.root().raw(450000));
        assertThat(present)
            .isNotEmpty()
            .hasValue(450000L);
    }

    @Test
    void testOptionalDouble() throws SerializationException {
        final TypeSerializer<OptionalDouble> serializer = this.serializer(OptionalDouble.class);

        final OptionalDouble shouldBeEmpty = serializer.deserialize(OptionalDouble.class, BasicConfigurationNode.root());
        assertThat(shouldBeEmpty)
            .isEmpty();

        final OptionalDouble present = serializer.deserialize(OptionalDouble.class, BasicConfigurationNode.root().raw(5.83e10));
        assertThat(present)
            .isNotEmpty()
            .hasValue(5.83e10d);
    }

}
