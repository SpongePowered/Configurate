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
package ninja.leaping.configurate;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;


public class SimpleConfigurationNodeTest {
    @Test
    public void testUnattachedNodesTemporary() {
        ConfigurationNode config = SimpleConfigurationNode.root();
        ConfigurationNode node = config.getNode("some", "node");
        assertTrue(node.isVirtual());
        assertEquals(null, node.getValue());
        assertFalse(node.hasListChildren());
        assertFalse(node.hasMapChildren());
        ConfigurationNode node2 = config.getNode("some", "node");
        assertNotSame(node, node2);


        ConfigurationNode node3 = config.getChild("some").getChild("node");
        assertNotSame(node, node3);
    }

    @Test
    public void testNodeCreation() {
        ConfigurationNode config = SimpleConfigurationNode.root();
        ConfigurationNode uncreatedNode = config.getNode("uncreated", "node");
        assertTrue(uncreatedNode.isVirtual()); // Just in case
        uncreatedNode.setValue("test string for cool people");
        assertFalse(uncreatedNode.isVirtual());
        assertEquals("test string for cool people", uncreatedNode.getValue());

        ConfigurationNode fetchedAfterCreation = config.getNode("uncreated", "node");
        assertEquals(uncreatedNode, fetchedAfterCreation);
        assertEquals(uncreatedNode, config.getChild("uncreated").getChild("node"));
    }

    @Test
    public void testGetDefaultValue() {
        ConfigurationNode root = SimpleConfigurationNode.root();
        final Object testObj = new Object();
        assertEquals(testObj, root.getChild("nonexistent").getValue(testObj));
    }

    private static final Map<Object, Object> TEST_MAP = new HashMap<Object, Object>();
    private static final List<Object> TEST_LIST = new ArrayList<Object>();
    static {
        TEST_LIST.add("test1");
        TEST_LIST.add("test2");

        TEST_MAP.put("key", "value");
        TEST_MAP.put("fabulous", true);
    }
    @Test
    public void testMapUnpacking() {
        ConfigurationNode root = SimpleConfigurationNode.root();
        root.setValue(TEST_MAP);
        assertEquals("value", root.getChild("key").getValue());
        assertEquals(true, root.getChild("fabulous").getValue());
    }

    @Test
    public void testMapPacking() {
        ConfigurationNode root = SimpleConfigurationNode.root();
        root.getChild("key").setValue("value");
        root.getChild("fabulous").setValue(true);

        assertEquals(TEST_MAP, root.getValue());
    }

    @Test
    public void testListUnpacking() {
        ConfigurationNode root = SimpleConfigurationNode.root();
        root.setValue(TEST_LIST);
        assertEquals("test1", root.getChild(0).getValue());
        assertEquals("test2", root.getChild(1).getValue());
    }

    @Test
    public void testListPacking() {
        ConfigurationNode root = SimpleConfigurationNode.root();
        root.getAppendedChild().setValue("test1");
        root.getAppendedChild().setValue("test2");
        assertEquals(TEST_LIST, root.getValue());
    }

    @Test
    public void testSingleListConversion() {
        ConfigurationNode config = SimpleConfigurationNode.root();
        ConfigurationNode node = config.getNode("test", "value");
        node.setValue("test");
        ConfigurationNode secondChild = node.getAppendedChild();
        secondChild.setValue("test2");
        assertEquals(Arrays.asList("test", "test2"), node.getValue());
    }

}
