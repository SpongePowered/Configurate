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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AbstractConfigurationNodeTest {

    @Test
    public void testUnattachedNodesTemporary() {
        ConfigurationNode config = BasicConfigurationNode.root();
        ConfigurationNode node = config.getNode("some", "node");
        assertTrue(node.isVirtual());
        assertNull(node.getValue());
        assertFalse(node.isList());
        assertFalse(node.isMap());
        ConfigurationNode node2 = config.getNode("some", "node");
        assertNotSame(node, node2);


        ConfigurationNode node3 = config.getNode("some").getNode("node");
        assertNotSame(node, node3);
    }

    @Test
    public void testNodeCreation() {
        ConfigurationNode config = BasicConfigurationNode.root();
        ConfigurationNode uncreatedNode = config.getNode("uncreated", "node");
        assertTrue(uncreatedNode.isVirtual()); // Just in case
        uncreatedNode.setValue("test string for cool people");
        assertFalse(uncreatedNode.isVirtual());
        assertEquals("test string for cool people", uncreatedNode.getValue());

        ConfigurationNode fetchedAfterCreation = config.getNode("uncreated", "node");
        assertEquals(uncreatedNode, fetchedAfterCreation);
        assertEquals(uncreatedNode, config.getNode("uncreated").getNode("node"));
    }

    @Test
    public void testTraversingNodeCreation() {
        ConfigurationNode config = BasicConfigurationNode.root();
        ConfigurationNode nodeOne = config.getNode("uncreated", "step", "node").setValue("one");
        ConfigurationNode nodeTwo = config.getNode("uncreated", "step", "color").setValue("lilac");
        ConfigurationNode attachedParent = config.getNode("uncreated", "step");
        assertEquals(attachedParent, nodeOne.getParent());
        assertEquals(attachedParent, nodeTwo.getParent());
    }

    @Test
    public void testGetDefaultValue() {
        ConfigurationNode root = BasicConfigurationNode.root();
        final Object testObj = new Object();
        assertEquals(testObj, root.getNode("nonexistent").getValue(testObj));
    }

    @Test
    public void testGetChildrenMap() {
        ConfigurationNode root = BasicConfigurationNode.root();
        ConfigurationNode a = root.getNode("a").setValue("one");
        ConfigurationNode b = root.getNode("b").setValue("two");
        assertEquals(ImmutableMap.<Object, ConfigurationNode>of("a", a, "b", b), root.getChildrenMap());
    }

    @Test
    public void testGetChildrenList() {
        ConfigurationNode root = BasicConfigurationNode.root();
        ConfigurationNode a = root.appendListNode().setValue("one");
        ConfigurationNode b = root.appendListNode().setValue("two");
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
        ConfigurationNode root = BasicConfigurationNode.root();
        root.setValue(TEST_MAP);
        assertEquals("value", root.getNode("key").getValue());
        assertEquals(true, root.getNode("fabulous").getValue());
    }

    @Test
    public void testMapPacking() {
        ConfigurationNode root = BasicConfigurationNode.root();
        root.getNode("key").setValue("value");
        root.getNode("fabulous").setValue(true);

        assertEquals(TEST_MAP, root.getValue());
    }

    @Test
    public void testListUnpacking() {
        ConfigurationNode root = BasicConfigurationNode.root();
        root.setValue(TEST_LIST);
        assertEquals("test1", root.getNode(0).getValue());
        assertEquals("test2", root.getNode(1).getValue());
    }

    @Test
    public void testListPacking() {
        ConfigurationNode root = BasicConfigurationNode.root();
        root.appendListNode().setValue("test1");
        root.appendListNode().setValue("test2");
        assertEquals(TEST_LIST, root.getValue());
    }

    @Test
    public void testSingleListConversion() {
        ConfigurationNode config = BasicConfigurationNode.root();
        ConfigurationNode node = config.getNode("test", "value");
        node.setValue("test");
        ConfigurationNode secondChild = node.appendListNode();
        secondChild.setValue("test2");
        assertEquals(Arrays.asList("test", "test2"), node.getValue());
    }

    @Test
    public void testSettingNullRemoves() {
        ConfigurationNode root = BasicConfigurationNode.root();
        ConfigurationNode child = root.getNode("child").setValue("a");
        assertFalse(child.isVirtual());
        assertSame(child, root.getNode("child"));
        child.setValue(null);
        assertTrue(child.isVirtual());
        assertNotSame(child, root.getNode("child"));
    }

    @Test
    public void testGetPath() {
        ConfigurationNode root = BasicConfigurationNode.root();
        assertArrayEquals(new Object[]{"a", "b", "c"}, root.getNode("a", "b", "c").getPath().getArray());
    }

    @Test
    public void testMergeValues() {
        ConfigurationNode first = BasicConfigurationNode.root();
        ConfigurationNode second = BasicConfigurationNode.root();
        first.getNode("scalar").setValue("one");
        first.getNode("absent").setValue("butmerged");
        second.getNode("scalar").setValue("two");

        ConfigurationNode firstAbsentMap = first.getNode("absent-map");
        firstAbsentMap.getNode("a").setValue("one");
        firstAbsentMap.getNode("b").setValue("two");

        ConfigurationNode firstMergedMap = first.getNode("merged-map");
        ConfigurationNode secondMergedMap = second.getNode("merged-map");
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
        ConfigurationNode subject = BasicConfigurationNode.root();
        subject.setValue(ImmutableMap.of("a", "b", "b", "c", "c", "d"));
        assertTrue(subject.isMap());
        subject.setValue(ImmutableMap.of("na", "na", "eh", "eh", "bleugh", "bleugh"));
        assertTrue(subject.isMap());
    }

    @Test
    public void testGetSetValueSerialized() throws ObjectMappingException {
        ConfigurationNode subject = BasicConfigurationNode.root(ConfigurationOptions.defaults().withAcceptedTypes(ImmutableSet.of(String.class, Integer.class)));
        subject.setValue("48");
        assertEquals(Integer.valueOf(48), subject.getValue(TypeToken.of(Integer.class)));
        UUID testId = UUID.randomUUID();
        subject.setValue(TypeToken.of(UUID.class), testId);
        assertEquals(testId.toString(), subject.getValue());
    }

    @Test
    public void testDefaultsCopied() {
        ConfigurationNode subject = BasicConfigurationNode.root(ConfigurationOptions.defaults().withShouldCopyDefaults(true));
        assertNull(subject.getValue());
        assertEquals("default value", subject.getValue("default value"));
        assertEquals("default value", subject.getValue());
    }

}
