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
import org.spongepowered.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.configurate.transformation.NodePath;
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

public class AbstractConfigurationNodeTest {

    @Test
    void testUnattachedNodesTemporary() {
        final ConfigurationNode config = BasicConfigurationNode.root();
        final ConfigurationNode node = config.node("some", "node");
        assertTrue(node.virtual());
        assertNull(node.get());
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
        uncreatedNode.set("test string for cool people");
        assertFalse(uncreatedNode.virtual());
        assertEquals("test string for cool people", uncreatedNode.get());

        final ConfigurationNode fetchedAfterCreation = config.node("uncreated", "node");
        assertEquals(uncreatedNode, fetchedAfterCreation);
        assertEquals(uncreatedNode, config.node("uncreated").node("node"));
    }

    @Test
    void testTraversingNodeCreation() {
        final ConfigurationNode config = BasicConfigurationNode.root();
        final ConfigurationNode nodeOne = config.node("uncreated", "step", "node").set("one");
        final ConfigurationNode nodeTwo = config.node("uncreated", "step", "color").set("lilac");
        final ConfigurationNode attachedParent = config.node("uncreated", "step");
        assertEquals(attachedParent, nodeOne.parent());
        assertEquals(attachedParent, nodeTwo.parent());
    }

    @Test
    void testGetDefaultValue() {
        final ConfigurationNode root = BasicConfigurationNode.root();
        final Object testObj = new Object();
        assertEquals(testObj, root.node("nonexistent").get(testObj));
    }

    @Test
    void testGetChildrenMap() {
        final ConfigurationNode root = BasicConfigurationNode.root();
        final ConfigurationNode a = root.node("a").set("one");
        final ConfigurationNode b = root.node("b").set("two");
        assertEquals(UnmodifiableCollections.<Object, ConfigurationNode>buildMap(map -> {
            map.put("a", a);
            map.put("b", b);
        }), root.childrenMap());
    }

    @Test
    void testGetChildrenList() {
        final ConfigurationNode root = BasicConfigurationNode.root();
        final ConfigurationNode a = root.appendListNode().set("one");
        final ConfigurationNode b = root.appendListNode().set("two");
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
        root.set(TEST_MAP);
        assertEquals("value", root.node("key").get());
        assertEquals(true, root.node("fabulous").get());
    }

    @Test
    void testMapPacking() {
        final ConfigurationNode root = BasicConfigurationNode.root();
        root.node("key").set("value");
        root.node("fabulous").set(true);

        assertEquals(TEST_MAP, root.get());
    }

    @Test
    void testListUnpacking() {
        final ConfigurationNode root = BasicConfigurationNode.root();
        root.set(TEST_LIST);
        assertEquals("test1", root.node(0).get());
        assertEquals("test2", root.node(1).get());
    }

    @Test
    void testListPacking() {
        final ConfigurationNode root = BasicConfigurationNode.root();
        root.appendListNode().set("test1");
        root.appendListNode().set("test2");
        assertEquals(TEST_LIST, root.get());
    }

    @Test
    void testSingleListConversion() {
        final ConfigurationNode config = BasicConfigurationNode.root();
        final ConfigurationNode node = config.node("test", "value");
        node.set("test");
        final ConfigurationNode secondChild = node.appendListNode();
        secondChild.set("test2");
        assertEquals(Arrays.asList("test", "test2"), node.get());
    }

    @Test
    void testSettingNullRemoves() {
        final ConfigurationNode root = BasicConfigurationNode.root();
        final ConfigurationNode child = root.node("child").set("a");
        assertFalse(child.virtual());
        assertSame(child, root.node("child"));
        child.set(null);
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
        first.node("scalar").set("one");
        first.node("absent").set("butmerged");
        second.node("scalar").set("two");

        final ConfigurationNode firstAbsentMap = first.node("absent-map");
        firstAbsentMap.node("a").set("one");
        firstAbsentMap.node("b").set("two");

        final ConfigurationNode firstMergedMap = first.node("merged-map");
        final ConfigurationNode secondMergedMap = second.node("merged-map");
        firstMergedMap.node("source").set("first");
        secondMergedMap.node("source").set("second");
        firstMergedMap.node("first-only").set("yeah");
        secondMergedMap.node("second-only").set("yeah");

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
        subject.set(UnmodifiableCollections.buildMap(build -> {
            build.put("a", "b");
            build.put("b", "c");
            build.put("c", "d");
        }));
        assertTrue(subject.isMap());
        subject.set(UnmodifiableCollections.buildMap(build -> {
            build.put("na", "na");
            build.put("eh", "eh");
            build.put("bleugh", "bleugh");
        }));
        assertTrue(subject.isMap());
    }

    @Test
    void testGetSetValueSerialized() throws ObjectMappingException {
        final ConfigurationNode subject = BasicConfigurationNode.root(ConfigurationOptions.defaults()
                .nativeTypes(UnmodifiableCollections.toSet(String.class, Integer.class)));
        subject.set("48");
        assertEquals(Integer.valueOf(48), subject.get(Integer.class));
        final UUID testId = UUID.randomUUID();
        subject.set(UUID.class, testId);
        assertEquals(testId.toString(), subject.get());
    }

    @Test
    void testDefaultsCopied() {
        final ConfigurationNode subject = BasicConfigurationNode.root(ConfigurationOptions.defaults().shouldCopyDefaults(true));
        assertNull(subject.get());
        assertEquals("default value", subject.get("default value"));
        assertEquals("default value", subject.get());
    }

    @Test
    @SuppressWarnings("rawtypes")
    void testRawTypeFails() {
        final ConfigurationNode subject = BasicConfigurationNode.root(b -> {
            b.node("test1").set(2);
            b.node("test2").set(3);
        });
        assertThrows(IllegalArgumentException.class, () -> subject.get(Map.class));
        assertThrows(IllegalArgumentException.class, () -> subject.get((Type) Map.class));
        // expected raw type
        assertThrows(IllegalArgumentException.class, () -> subject.get(new TypeToken<Map>() {}));

    }

    @Test
    void testHasChildArray() {
        final ConfigurationNode node = BasicConfigurationNode.root();
        assertFalse(node.hasChild("ball"));
        assertTrue(node.node("ball").virtual());

        // still shouldn't change
        assertFalse(node.hasChild("ball"));

        node.node("ball").set("yarn");
        assertTrue(node.hasChild("ball"));

        // but still doesn't have child
        assertFalse(node.hasChild("ball", "another"));

        node.node("ball", "another").set(48);
        assertTrue(node.hasChild("ball", "another"));
    }

    @Test
    void testNullElementsForbiddenHasChild() {
        assertThrows(NullPointerException.class, () -> {
            BasicConfigurationNode.root(n -> n.node("test").set("blah"))
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

        node.node("ball").set("yarn");
        assertTrue(node.hasChild(NodePath.path("ball")));

        // but still doesn't have child
        assertFalse(node.hasChild(NodePath.path("ball", "another")));

        node.node("ball", "another").set(48);
        assertTrue(node.hasChild(NodePath.path("ball", "another")));
    }

    @Test
    void testNullOutListValue() {
        BasicConfigurationNode.root(n -> {
            n.appendListNode().set("blah");
            n.appendListNode().set(null);
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
        node.set("I hold hints!");
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
        original.set("1234").hint(IS_EVIL, true);

        final ConfigurationNode copy = original.copy();
        assertEquals(true, copy.hint(IS_EVIL));

        final ConfigurationNode copiedSet = BasicConfigurationNode.root();
        copiedSet.set(original);
        assertEquals(true, copiedSet.hint(IS_EVIL));
    }

    @Test
    void testHintsMerged() {
        final ConfigurationNode hintHolder = BasicConfigurationNode.root()
                .set('o')
                .hint(IS_EVIL, true);
        final ConfigurationNode mergeTarget = BasicConfigurationNode.root()
                .set('o')
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
        assertEquals(3, target.node("one").get());
        assertEquals(14, target.node("test").get());
    }

    @Test
    void testCollectToList() throws ObjectMappingException {
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
    void testImplicitInitialization() throws ObjectMappingException {
        final BasicConfigurationNode node = BasicConfigurationNode.root(ConfigurationOptions.defaults().implicitInitialization(true));

        assertNull(node.get());
        assertEquals(Collections.emptyList(), node.get(new TypeToken<List<String>>() {}));
        assertEquals(Collections.emptyMap(), node.get(new TypeToken<Map<String, Integer>>() {}));
        assertEquals(new Empty(), node.get(Empty.class));
    }

}
