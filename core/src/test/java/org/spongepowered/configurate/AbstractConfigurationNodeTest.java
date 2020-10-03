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
    public void testUnattachedNodesTemporary() {
        final ConfigurationNode config = BasicConfigurationNode.root();
        final ConfigurationNode node = config.getNode("some", "node");
        assertTrue(node.isVirtual());
        assertNull(node.getValue());
        assertFalse(node.isList());
        assertFalse(node.isMap());
        final ConfigurationNode node2 = config.getNode("some", "node");
        assertNotSame(node, node2);


        final ConfigurationNode node3 = config.getNode("some").getNode("node");
        assertNotSame(node, node3);
    }

    @Test
    public void testNodeCreation() {
        final ConfigurationNode config = BasicConfigurationNode.root();
        final ConfigurationNode uncreatedNode = config.getNode("uncreated", "node");
        assertTrue(uncreatedNode.isVirtual()); // Just in case
        uncreatedNode.setValue("test string for cool people");
        assertFalse(uncreatedNode.isVirtual());
        assertEquals("test string for cool people", uncreatedNode.getValue());

        final ConfigurationNode fetchedAfterCreation = config.getNode("uncreated", "node");
        assertEquals(uncreatedNode, fetchedAfterCreation);
        assertEquals(uncreatedNode, config.getNode("uncreated").getNode("node"));
    }

    @Test
    public void testTraversingNodeCreation() {
        final ConfigurationNode config = BasicConfigurationNode.root();
        final ConfigurationNode nodeOne = config.getNode("uncreated", "step", "node").setValue("one");
        final ConfigurationNode nodeTwo = config.getNode("uncreated", "step", "color").setValue("lilac");
        final ConfigurationNode attachedParent = config.getNode("uncreated", "step");
        assertEquals(attachedParent, nodeOne.getParent());
        assertEquals(attachedParent, nodeTwo.getParent());
    }

    @Test
    public void testGetDefaultValue() {
        final ConfigurationNode root = BasicConfigurationNode.root();
        final Object testObj = new Object();
        assertEquals(testObj, root.getNode("nonexistent").getValue(testObj));
    }

    @Test
    public void testGetChildrenMap() {
        final ConfigurationNode root = BasicConfigurationNode.root();
        final ConfigurationNode a = root.getNode("a").setValue("one");
        final ConfigurationNode b = root.getNode("b").setValue("two");
        assertEquals(UnmodifiableCollections.<Object, ConfigurationNode>buildMap(map -> {
            map.put("a", a);
            map.put("b", b);
        }), root.getChildrenMap());
    }

    @Test
    public void testGetChildrenList() {
        final ConfigurationNode root = BasicConfigurationNode.root();
        final ConfigurationNode a = root.appendListNode().setValue("one");
        final ConfigurationNode b = root.appendListNode().setValue("two");
        assertEquals(Arrays.asList(a, b), root.getChildrenList());
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
    public void testMapUnpacking() {
        final ConfigurationNode root = BasicConfigurationNode.root();
        root.setValue(TEST_MAP);
        assertEquals("value", root.getNode("key").getValue());
        assertEquals(true, root.getNode("fabulous").getValue());
    }

    @Test
    public void testMapPacking() {
        final ConfigurationNode root = BasicConfigurationNode.root();
        root.getNode("key").setValue("value");
        root.getNode("fabulous").setValue(true);

        assertEquals(TEST_MAP, root.getValue());
    }

    @Test
    public void testListUnpacking() {
        final ConfigurationNode root = BasicConfigurationNode.root();
        root.setValue(TEST_LIST);
        assertEquals("test1", root.getNode(0).getValue());
        assertEquals("test2", root.getNode(1).getValue());
    }

    @Test
    public void testListPacking() {
        final ConfigurationNode root = BasicConfigurationNode.root();
        root.appendListNode().setValue("test1");
        root.appendListNode().setValue("test2");
        assertEquals(TEST_LIST, root.getValue());
    }

    @Test
    public void testSingleListConversion() {
        final ConfigurationNode config = BasicConfigurationNode.root();
        final ConfigurationNode node = config.getNode("test", "value");
        node.setValue("test");
        final ConfigurationNode secondChild = node.appendListNode();
        secondChild.setValue("test2");
        assertEquals(Arrays.asList("test", "test2"), node.getValue());
    }

    @Test
    public void testSettingNullRemoves() {
        final ConfigurationNode root = BasicConfigurationNode.root();
        final ConfigurationNode child = root.getNode("child").setValue("a");
        assertFalse(child.isVirtual());
        assertSame(child, root.getNode("child"));
        child.setValue(null);
        assertTrue(child.isVirtual());
        assertNotSame(child, root.getNode("child"));
    }

    @Test
    public void testGetPath() {
        final ConfigurationNode root = BasicConfigurationNode.root();
        assertArrayEquals(new Object[]{"a", "b", "c"}, root.getNode("a", "b", "c").getPath().getArray());
    }

    @Test
    public void testMergeValues() {
        final ConfigurationNode first = BasicConfigurationNode.root();
        final ConfigurationNode second = BasicConfigurationNode.root();
        first.getNode("scalar").setValue("one");
        first.getNode("absent").setValue("butmerged");
        second.getNode("scalar").setValue("two");

        final ConfigurationNode firstAbsentMap = first.getNode("absent-map");
        firstAbsentMap.getNode("a").setValue("one");
        firstAbsentMap.getNode("b").setValue("two");

        final ConfigurationNode firstMergedMap = first.getNode("merged-map");
        final ConfigurationNode secondMergedMap = second.getNode("merged-map");
        firstMergedMap.getNode("source").setValue("first");
        secondMergedMap.getNode("source").setValue("second");
        firstMergedMap.getNode("first-only").setValue("yeah");
        secondMergedMap.getNode("second-only").setValue("yeah");

        second.mergeValuesFrom(first);
        assertEquals("two", second.getNode("scalar").getString());
        assertEquals("butmerged", second.getNode("absent").getString());
        assertEquals("one", second.getNode("absent-map", "a").getString());
        assertEquals("two", second.getNode("absent-map", "b").getString());
        assertEquals("second", second.getNode("merged-map", "source").getString());
        assertEquals("yeah", second.getNode("merged-map", "first-only").getString());
        assertEquals("yeah", second.getNode("merged-map", "second-only").getString());
    }

    @Test
    public void testSettingMultipleTimesWorks() {
        final ConfigurationNode subject = BasicConfigurationNode.root();
        subject.setValue(UnmodifiableCollections.buildMap(build -> {
            build.put("a", "b");
            build.put("b", "c");
            build.put("c", "d");
        }));
        assertTrue(subject.isMap());
        subject.setValue(UnmodifiableCollections.buildMap(build -> {
            build.put("na", "na");
            build.put("eh", "eh");
            build.put("bleugh", "bleugh");
        }));
        assertTrue(subject.isMap());
    }

    @Test
    public void testGetSetValueSerialized() throws ObjectMappingException {
        final ConfigurationNode subject = BasicConfigurationNode.root(ConfigurationOptions.defaults()
                .withNativeTypes(UnmodifiableCollections.toSet(String.class, Integer.class)));
        subject.setValue("48");
        assertEquals(Integer.valueOf(48), subject.getValue(Integer.class));
        final UUID testId = UUID.randomUUID();
        subject.setValue(UUID.class, testId);
        assertEquals(testId.toString(), subject.getValue());
    }

    @Test
    public void testDefaultsCopied() {
        final ConfigurationNode subject = BasicConfigurationNode.root(ConfigurationOptions.defaults().withShouldCopyDefaults(true));
        assertNull(subject.getValue());
        assertEquals("default value", subject.getValue("default value"));
        assertEquals("default value", subject.getValue());
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testRawTypeFails() {
        final ConfigurationNode subject = BasicConfigurationNode.root(b -> {
            b.getNode("test1").setValue(2);
            b.getNode("test2").setValue(3);
        });
        assertThrows(IllegalArgumentException.class, () -> subject.getValue(Map.class));
        assertThrows(IllegalArgumentException.class, () -> subject.getValue((Type) Map.class));
        // expected raw type
        assertThrows(IllegalArgumentException.class, () -> subject.getValue(new TypeToken<Map>() {}));

    }

    @Test
    public void testHasChildArray() {
        final ConfigurationNode node = BasicConfigurationNode.root();
        assertFalse(node.hasChild("ball"));
        assertTrue(node.getNode("ball").isVirtual());

        // still shouldn't change
        assertFalse(node.hasChild("ball"));

        node.getNode("ball").setValue("yarn");
        assertTrue(node.hasChild("ball"));

        // but still doesn't have child
        assertFalse(node.hasChild("ball", "another"));

        node.getNode("ball", "another").setValue(48);
        assertTrue(node.hasChild("ball", "another"));
    }

    @Test
    public void testNullElementsForbiddenHasChild() {
        assertThrows(NullPointerException.class, () -> {
            BasicConfigurationNode.root(n -> n.getNode("test").setValue("blah"))
                .hasChild("test", null);
        });
    }

    @Test
    public void testHasChildIterable() {
        final ConfigurationNode node = BasicConfigurationNode.root();
        assertFalse(node.hasChild(NodePath.path("ball")));
        assertTrue(node.getNode("ball").isVirtual());

        // still shouldn't change
        assertFalse(node.hasChild(NodePath.path("ball")));

        node.getNode("ball").setValue("yarn");
        assertTrue(node.hasChild(NodePath.path("ball")));

        // but still doesn't have child
        assertFalse(node.hasChild(NodePath.path("ball", "another")));

        node.getNode("ball", "another").setValue(48);
        assertTrue(node.hasChild(NodePath.path("ball", "another")));
    }

    @Test
    public void testNullOutListValue() {
        BasicConfigurationNode.root(n -> {
            n.appendListNode().setValue("blah");
            n.appendListNode().setValue(null);
        });
    }

    /**
     * A test representation hint which indicates to a serializer that the node
     * should be represented evilly.
     */
    private static final RepresentationHint<Boolean> IS_EVIL = RepresentationHint.of("evil", Boolean.class);

    @Test
    public void testHintsReadWrite() {
        final ConfigurationNode node = BasicConfigurationNode.root();
        node.setValue("I hold hints!");
        assertNull(node.getHint(IS_EVIL));
        node.setHint(IS_EVIL, true);
        assertEquals(true, node.getHint(IS_EVIL));

    }

    @Test
    public void testHintSetToNull() {
        final ConfigurationNode node = BasicConfigurationNode.root();
        node.setHint(IS_EVIL, null);
        assertNull(node.getHint(IS_EVIL));
    }

    @Test
    public void testGetHintInherited() {
        final ConfigurationNode root = BasicConfigurationNode.root();
        root.setHint(IS_EVIL, false);

        final ConfigurationNode child = root.getNode("blah");
        assertEquals(false, child.getHint(IS_EVIL));

        child.setHint(IS_EVIL, true);
        assertEquals(true, child.getHint(IS_EVIL));

        // check that parent was not changed
        assertEquals(false, root.getHint(IS_EVIL));
    }

    @Test
    public void testHintsCopied() {
        final ConfigurationNode original = BasicConfigurationNode.root();
        original.setValue("1234").setHint(IS_EVIL, true);

        final ConfigurationNode copy = original.copy();
        assertEquals(true, copy.getHint(IS_EVIL));

        final ConfigurationNode copiedSet = BasicConfigurationNode.root();
        copiedSet.setValue(original);
        assertEquals(true, copiedSet.getHint(IS_EVIL));
    }

    @Test
    public void testHintsMerged() {
        final ConfigurationNode hintHolder = BasicConfigurationNode.root()
                .setValue('o')
                .setHint(IS_EVIL, true);
        final ConfigurationNode mergeTarget = BasicConfigurationNode.root()
                .setValue('o')
                .setHint(RepresentationHint.INDENT, 34);

        mergeTarget.mergeValuesFrom(hintHolder);

        assertEquals(34, mergeTarget.getHint(RepresentationHint.INDENT));
        assertEquals(true, mergeTarget.getHint(IS_EVIL));
    }

    @Test
    public void testCollectToMap() throws ObjectMappingException {
        final ConfigurationNode target = ImmutableMap.of("one", 3,
                "two", 28,
                "test", 14).entrySet().stream()
                .filter(ent -> ent.getKey().contains("e"))
                .collect(BasicConfigurationNode.factory().toMapCollector(Integer.class));

        assertTrue(target.getNode("two").isVirtual());
        assertEquals(3, target.getNode("one").getValue());
        assertEquals(14, target.getNode("test").getValue());
    }

    @Test
    public void testCollectToList() throws ObjectMappingException {
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
        final BasicConfigurationNode node = BasicConfigurationNode.root(ConfigurationOptions.defaults().withImplicitInitialization(true));

        assertNull(node.getValue());
        assertEquals(Collections.emptyList(), node.getValue(new TypeToken<List<String>>() {}));
        assertEquals(Collections.emptyMap(), node.getValue(new TypeToken<Map<String, Integer>>() {}));
        assertEquals(new Empty(), node.getValue(Empty.class));
    }

}
