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
package org.spongepowered.configurate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.leangen.geantyref.TypeToken;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.util.UnmodifiableCollections;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;

class AbstractConfigurationNodeTest {

    @Test
    void testUnattachedNodesTemporary() {
        final ConfigurationNode config = BasicConfigurationNode.root();
        final ConfigurationNode node = config.node("some", "node");
        assertTrue(node.virtual());
        assertNull(node.raw());
        assertFalse(node.isList());
        assertFalse(node.isMap());
        final ConfigurationNode node2 = config.node("some", "node");
        assertNotSame(node, node2);


        final ConfigurationNode node3 = config.node("some").node("node");
        assertNotSame(node, node3);
    }

    @Test
    void testNodeCreation() {
        final ConfigurationNode config = BasicConfigurationNode.root();
        final ConfigurationNode uncreatedNode = config.node("uncreated", "node");
        assertTrue(uncreatedNode.virtual()); // Just in case
        uncreatedNode.raw("test string for cool people");
        assertFalse(uncreatedNode.virtual());
        assertEquals("test string for cool people", uncreatedNode.raw());

        final ConfigurationNode fetchedAfterCreation = config.node("uncreated", "node");
        assertEquals(uncreatedNode, fetchedAfterCreation);
        assertEquals(uncreatedNode, config.node("uncreated").node("node"));
    }

    @Test
    void testTraversingNodeCreation() {
        final ConfigurationNode config = BasicConfigurationNode.root();
        final ConfigurationNode nodeOne = config.node("uncreated", "step", "node").raw("one");
        final ConfigurationNode nodeTwo = config.node("uncreated", "step", "color").raw("lilac");
        final ConfigurationNode attachedParent = config.node("uncreated", "step");
        assertEquals(attachedParent, nodeOne.parent());
        assertEquals(attachedParent, nodeTwo.parent());
    }

    @Test
    void testGetDefaultValue() throws SerializationException {
        final ConfigurationNode root = BasicConfigurationNode.root();
        final Object testObj = new Object();
        assertEquals(testObj, root.node("nonexistent").get(Object.class, testObj));
    }

    @Test
    void testGetChildrenMap() {
        final ConfigurationNode root = BasicConfigurationNode.root();
        final ConfigurationNode a = root.node("a").raw("one");
        final ConfigurationNode b = root.node("b").raw("two");
        assertEquals(UnmodifiableCollections.<Object, ConfigurationNode>buildMap(map -> {
            map.put("a", a);
            map.put("b", b);
        }), root.childrenMap());
    }

    @Test
    void testGetChildrenList() {
        final ConfigurationNode root = BasicConfigurationNode.root();
        final ConfigurationNode a = root.appendListNode().raw("one");
        final ConfigurationNode b = root.appendListNode().raw("two");
        assertEquals(Arrays.asList(a, b), root.childrenList());
    }

    private static final Map<Object, Object> TEST_MAP = new HashMap<>();
    private static final List<Object> TEST_LIST = new ArrayList<>();

    static {
        TEST_LIST.add("test1");
        TEST_LIST.add("test2");

        TEST_MAP.put("key", "value");
        TEST_MAP.put("fabulous", true);
    }

    @Test
    void testMapUnpacking() {
        final ConfigurationNode root = BasicConfigurationNode.root();
        root.raw(TEST_MAP);
        assertEquals("value", root.node("key").raw());
        assertEquals(true, root.node("fabulous").raw());
    }

    @Test
    void testMapPacking() {
        final ConfigurationNode root = BasicConfigurationNode.root();
        root.node("key").raw("value");
        root.node("fabulous").raw(true);

        assertEquals(TEST_MAP, root.raw());
    }

    @Test
    void testListUnpacking() {
        final ConfigurationNode root = BasicConfigurationNode.root();
        root.raw(TEST_LIST);
        assertEquals("test1", root.node(0).raw());
        assertEquals("test2", root.node(1).raw());
    }

    @Test
    void testLisUnpacking2() {
        final ConfigurationNode root = BasicConfigurationNode.root();
        final ConfigurationNode subnode = root.node("subnode");
        subnode.node(0).raw("test1");
        subnode.node(1).raw("test2");
        assertEquals(TEST_LIST, subnode.raw());
    }

    @Test
    void testListPacking() {
        final ConfigurationNode root = BasicConfigurationNode.root();
        root.appendListNode().raw("test1");
        root.appendListNode().raw("test2");
        assertEquals(TEST_LIST, root.raw());
    }

    @Test
    void testSingleListConversion() {
        final ConfigurationNode config = BasicConfigurationNode.root();
        final ConfigurationNode node = config.node("test", "value");
        node.raw("test");
        final ConfigurationNode secondChild = node.appendListNode();
        secondChild.raw("test2");
        assertEquals(Arrays.asList("test", "test2"), node.raw());
    }

    @Test
    void testSettingNullRemoves() {
        final ConfigurationNode root = BasicConfigurationNode.root();
        final ConfigurationNode child = root.node("child").raw("a");
        assertFalse(child.virtual());
        assertSame(child, root.node("child"));
        child.raw(null);
        assertTrue(child.virtual());
        assertNotSame(child, root.node("child"));
    }

    @Test
    void testGetPath() {
        final ConfigurationNode root = BasicConfigurationNode.root();
        assertArrayEquals(new Object[]{"a", "b", "c"}, root.node("a", "b", "c").path().array());
    }

    @Test
    void testMergeValues() {
        final ConfigurationNode first = BasicConfigurationNode.root();
        final ConfigurationNode second = BasicConfigurationNode.root();
        first.node("scalar").raw("one");
        first.node("absent").raw("butmerged");
        second.node("scalar").raw("two");

        final ConfigurationNode firstAbsentMap = first.node("absent-map");
        firstAbsentMap.node("a").raw("one");
        firstAbsentMap.node("b").raw("two");

        final ConfigurationNode firstMergedMap = first.node("merged-map");
        final ConfigurationNode secondMergedMap = second.node("merged-map");
        firstMergedMap.node("source").raw("first");
        secondMergedMap.node("source").raw("second");
        firstMergedMap.node("first-only").raw("yeah");
        secondMergedMap.node("second-only").raw("yeah");

        second.mergeFrom(first);
        assertEquals("two", second.node("scalar").getString());
        assertEquals("butmerged", second.node("absent").getString());
        assertEquals("one", second.node("absent-map", "a").getString());
        assertEquals("two", second.node("absent-map", "b").getString());
        assertEquals("second", second.node("merged-map", "source").getString());
        assertEquals("yeah", second.node("merged-map", "first-only").getString());
        assertEquals("yeah", second.node("merged-map", "second-only").getString());
    }

    @Test
    void testSettingMultipleTimesWorks() {
        final ConfigurationNode subject = BasicConfigurationNode.root();
        subject.raw(UnmodifiableCollections.buildMap(build -> {
            build.put("a", "b");
            build.put("b", "c");
            build.put("c", "d");
        }));
        assertTrue(subject.isMap());
        subject.raw(UnmodifiableCollections.buildMap(build -> {
            build.put("na", "na");
            build.put("eh", "eh");
            build.put("bleugh", "bleugh");
        }));
        assertTrue(subject.isMap());
    }

    @Test
    void testGetSetValueSerialized() throws SerializationException {
        final ConfigurationNode subject = BasicConfigurationNode.root(ConfigurationOptions.defaults()
                .nativeTypes(UnmodifiableCollections.toSet(String.class, Integer.class)));
        subject.set("48");
        assertEquals(Integer.valueOf(48), subject.get(Integer.class));
        final UUID testId = UUID.randomUUID();
        subject.set(UUID.class, testId);
        assertEquals(testId.toString(), subject.raw());
    }

    @Test
    void testDefaultsCopied() {
        final ConfigurationNode subject = BasicConfigurationNode.root(ConfigurationOptions.defaults().shouldCopyDefaults(true));
        assertNull(subject.raw());
        assertEquals("default value", subject.getString("default value"));
        assertEquals("default value", subject.getString());
    }

    @Test
    @SuppressWarnings("rawtypes")
    void testRawTypeFails() {
        final ConfigurationNode subject = BasicConfigurationNode.root(b -> {
            b.node("test1").raw(2);
            b.node("test2").raw(3);
        });
        assertThrows(SerializationException.class, () -> subject.get(Map.class));
        assertThrows(SerializationException.class, () -> subject.get((Type) Map.class));
        // expected raw type
        assertThrows(SerializationException.class, () -> subject.get(new TypeToken<Map>() {}));

    }

    @Test
    void testHasChildArray() {
        final ConfigurationNode node = BasicConfigurationNode.root();
        assertFalse(node.hasChild("ball"));
        assertTrue(node.node("ball").virtual());

        // still shouldn't change
        assertFalse(node.hasChild("ball"));

        node.node("ball").raw("yarn");
        assertTrue(node.hasChild("ball"));

        // but still doesn't have child
        assertFalse(node.hasChild("ball", "another"));

        node.node("ball", "another").raw(48);
        assertTrue(node.hasChild("ball", "another"));
    }

    @Test
    void testNullElementsForbiddenHasChild() {
        assertThrows(NullPointerException.class, () -> {
            BasicConfigurationNode.root(n -> n.node("test").raw("blah"))
                .hasChild("test", null);
        });
    }

    @Test
    void testHasChildIterable() {
        final ConfigurationNode node = BasicConfigurationNode.root();
        assertFalse(node.hasChild(NodePath.path("ball")));
        assertTrue(node.node("ball").virtual());

        // still shouldn't change
        assertFalse(node.hasChild(NodePath.path("ball")));

        node.node("ball").raw("yarn");
        assertTrue(node.hasChild(NodePath.path("ball")));

        // but still doesn't have child
        assertFalse(node.hasChild(NodePath.path("ball", "another")));

        node.node("ball", "another").raw(48);
        assertTrue(node.hasChild(NodePath.path("ball", "another")));
    }

    @Test
    void testNullOutListValue() {
        BasicConfigurationNode.root(n -> {
            n.appendListNode().raw("blah");
            n.appendListNode().raw(null);
        });
    }

    /**
     * A test representation hint which indicates to a serializer that the node
     * should be represented evilly.
     */
    private static final RepresentationHint<Boolean> IS_EVIL = RepresentationHint.of("evil", Boolean.class);

    /**
     * A representation hint for indentation
     */
    public static final RepresentationHint<Integer> INDENT = RepresentationHint.of("indent", Integer.class);

    public static final RepresentationHint<String> NAME =
            RepresentationHint.<String>builder()
                    .identifier("name")
                    .valueType(String.class)
                    .inheritable(false)
                    .build();

    @Test
    void testHintsReadWrite() {
        final ConfigurationNode node = BasicConfigurationNode.root();
        node.raw("I hold hints!");
        assertNull(node.hint(IS_EVIL));
        node.hint(IS_EVIL, true);
        assertEquals(true, node.hint(IS_EVIL));

    }

    @Test
    void testHintSetToNull() {
        final ConfigurationNode node = BasicConfigurationNode.root();
        node.hint(IS_EVIL, null);
        assertNull(node.hint(IS_EVIL));
    }

    @Test
    void testGetHintInherited() {
        final ConfigurationNode root = BasicConfigurationNode.root();
        root.hint(IS_EVIL, false);

        final ConfigurationNode child = root.node("blah");
        assertEquals(false, child.hint(IS_EVIL));

        child.hint(IS_EVIL, true);
        assertEquals(true, child.hint(IS_EVIL));

        // check that parent was not changed
        assertEquals(false, root.hint(IS_EVIL));
    }

    @Test
    void testNonInheritableHints() {
        final ConfigurationNode root = BasicConfigurationNode.root();
        root.hint(NAME, "secondary");

        final ConfigurationNode child = root.node("other");
        assertNull(child.hint(NAME));
    }

    @Test
    void testHintsCopied() {
        final ConfigurationNode original = BasicConfigurationNode.root();
        original.raw("1234").hint(IS_EVIL, true);

        final ConfigurationNode copy = original.copy();
        assertEquals(true, copy.hint(IS_EVIL));

        final ConfigurationNode copiedSet = BasicConfigurationNode.root();
        copiedSet.from(original);
        assertEquals(true, copiedSet.hint(IS_EVIL));
    }

    @Test
    void testHintsMerged() {
        final ConfigurationNode hintHolder = BasicConfigurationNode.root()
                .raw('o')
                .hint(IS_EVIL, true);
        final ConfigurationNode mergeTarget = BasicConfigurationNode.root()
                .raw('o')
                .hint(INDENT, 34);

        mergeTarget.mergeFrom(hintHolder);

        assertEquals(34, mergeTarget.hint(INDENT));
        assertEquals(true, mergeTarget.hint(IS_EVIL));
    }

    @Test
    void testCollectToMap() {
        final ConfigurationNode target = ImmutableMap.of("one", 3,
                "two", 28,
                "test", 14).entrySet().stream()
                .filter(ent -> ent.getKey().contains("e"))
                .collect(BasicConfigurationNode.factory().toMapCollector(Integer.class));

        assertTrue(target.node("two").virtual());
        assertEquals(3, target.node("one").raw());
        assertEquals(14, target.node("test").raw());
    }

    @Test
    void testCollectToList() throws SerializationException {
        final BasicConfigurationNode target = IntStream.of(1, 2, 3, 4, 8).boxed()
                .collect(BasicConfigurationNode.factory().toListCollector(Integer.class));

        assertEquals(ImmutableList.of(1, 2, 3, 4, 8), target.getList(TypeToken.get(Integer.class)));
    }

    @ConfigSerializable
    static class Empty {
        String ignoreMe = "hello";

        @Override
        public boolean equals(final Object that) {
            return that instanceof Empty
                    && Objects.equals(this.ignoreMe, ((Empty) that).ignoreMe);
        }

        @Override
        public int hashCode() {
            return 31 * Objects.hashCode(this.ignoreMe);
        }
    }

    @Test
    void testImplicitInitialization() throws SerializationException {
        final BasicConfigurationNode node = BasicConfigurationNode.root(ConfigurationOptions.defaults().implicitInitialization(true));

        assertNull(node.raw());
        assertEquals(Collections.emptyList(), node.get(new TypeToken<List<String>>() {}));
        assertEquals(Collections.emptyMap(), node.get(new TypeToken<Map<String, Integer>>() {}));
        assertEquals(new Empty(), node.get(Empty.class));
    }

    @Test
    void testAppendListToMap() throws SerializationException {
        final BasicConfigurationNode node = BasicConfigurationNode.root(n -> {
            n.node("one").set("yee");
            n.node("two").set("haw");
        });

        assertTrue(node.isMap());

        node.appendListNode().set("I'm a list now!");

        assertFalse(node.isMap());
        assertTrue(node.isList());
        assertEquals(Collections.singletonList("I'm a list now!"), node.getList(String.class));
    }

    @Test
    void testMergeToVirtualNode() throws SerializationException {
        final BasicConfigurationNode node = BasicConfigurationNode.root(n -> {
            n.node("source", "one").set("hi");
            n.node("source", "two").set("there");
        });

        final ConfigurationNode source = node.node("source");
        assertFalse(source.empty());

        final ConfigurationNode target = node.node("target");
        assertTrue(target.empty());
        assertTrue(target.virtual());

        target.mergeFrom(source);

        assertEquals("hi", target.node("one").raw());
        assertEquals("there", target.node("two").raw());
        assertFalse(target.virtual());
    }

    @Test
    void testSetValueOfInvalidType() {
        final BasicConfigurationNode root = BasicConfigurationNode.root(ConfigurationOptions.defaults()
            .nativeTypes(UnmodifiableCollections.toSet(String.class)));

        assertTrue(assertThrows(SerializationException.class, () -> root.set(Integer.class, "hello"))
            .getMessage().contains("Got a value of unexpected type"));
    }

    @ConfigSerializable
    static class ImplicitInitTest {
        private String name;

        ImplicitInitTest() {
        }

        ImplicitInitTest(final String name) {
            this.name = name;
        }
    }

    // https://github.com/SpongePowered/Configurate/issues/243
    @Test
    void testNoImplicitInitWhenDefaultProvided() throws SerializationException {
        final BasicConfigurationNode node = BasicConfigurationNode.root();

        final ImplicitInitTest check = node.get(ImplicitInitTest.class, new ImplicitInitTest("someone"));

        assertEquals("someone", check.name);
        assertEquals("someone", node.node("name").raw());
    }

    @Test
    void testSetPrimitive() throws SerializationException {
        final BasicConfigurationNode node = BasicConfigurationNode.root();

        node.set(int.class, 2);

        assertThat(node.raw()).isEqualTo(2);
    }

    @Test
    void testGetPrimitive() throws SerializationException {
        final BasicConfigurationNode node = BasicConfigurationNode.root();
        node.raw(42.2d);

        assertThat(node.get(double.class)).isEqualTo(42.2d);
    }

    // https://github.com/SpongePowered/Configurate/issues/300
    @Test
    void testIntKeysInVirtualMaps() {
        assertThatNoException()
            .as("attaching configuration node with an integer key")
            .isThrownBy(() -> BasicConfigurationNode.root().node("hello", 3).set("abc"));
    }

}
