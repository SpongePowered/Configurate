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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        assertEquals(ImmutableMap.<Object, ConfigurationNode>of("a", a, "b", b), root.getChildrenMap());
    }

    @Test
    public void testGetChildrenList() {
        final ConfigurationNode root = BasicConfigurationNode.root();
        final ConfigurationNode a = root.appendListNode().setValue("one");
        final ConfigurationNode b = root.appendListNode().setValue("two");
        assertEquals(ImmutableList.of(a, b), root.getChildrenList());
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
        subject.setValue(ImmutableMap.of("a", "b", "b", "c", "c", "d"));
        assertTrue(subject.isMap());
        subject.setValue(ImmutableMap.of("na", "na", "eh", "eh", "bleugh", "bleugh"));
        assertTrue(subject.isMap());
    }

    @Test
    public void testGetSetValueSerialized() throws ObjectMappingException {
        final ConfigurationNode subject = BasicConfigurationNode.root(ConfigurationOptions.defaults()
                .withNativeTypes(ImmutableSet.of(String.class, Integer.class)));
        subject.setValue("48");
        assertEquals(Integer.valueOf(48), subject.getValue(TypeToken.of(Integer.class)));
        final UUID testId = UUID.randomUUID();
        subject.setValue(TypeToken.of(UUID.class), testId);
        assertEquals(testId.toString(), subject.getValue());
    }

    @Test
    public void testDefaultsCopied() {
        final ConfigurationNode subject = BasicConfigurationNode.root(ConfigurationOptions.defaults().withShouldCopyDefaults(true));
        assertNull(subject.getValue());
        assertEquals("default value", subject.getValue("default value"));
        assertEquals("default value", subject.getValue());
    }

}
