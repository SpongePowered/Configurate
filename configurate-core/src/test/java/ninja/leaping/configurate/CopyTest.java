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
package ninja.leaping.configurate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

public class CopyTest {

    @Test
    public void testSimpleCopy() {
        ConfigurationNode node = SimpleConfigurationNode.root();
        node.getNode("test").setValue(5);
        node.getNode("section", "val1").setValue(true);
        node.getNode("section", "val2").setValue("TEST");
        ConfigurationNode list = node.getNode("section2", "alist");
        list.getAppendedNode().setValue("value1");
        list.getAppendedNode().setValue("value2");

        ConfigurationNode copy = node.copy();

        assertNotSame(node, copy);
        assertEquals(node, copy);

        assertFalse(node.isVirtual());
        assertFalse(copy.isVirtual());

        assertEquals(5, copy.getNode("test").getValue());
        assertEquals(true, copy.getNode("section", "val1").getValue());
        assertEquals("TEST", copy.getNode("section", "val2").getValue());
        assertEquals(ImmutableList.of("value1", "value2"), copy.getNode("section2", "alist").getValue());

        // change value on original
        node.getNode("section", "val2").setValue("NOT TEST");

        // test it's still the same on copy
        assertEquals("TEST", copy.getNode("section", "val2").getValue());

        // change value on copy
        copy.getNode("section", "val2").setValue("zzz");

        // test it's still the same on original
        assertEquals("NOT TEST", node.getNode("section", "val2").getValue());
    }

    @Test
    public void testCopyPaths() {
        ConfigurationNode node = SimpleConfigurationNode.root();
        node.getNode("test").setValue(5);
        node.getNode("section", "val1").setValue(true);
        node.getNode("section", "val2").setValue("TEST");

        ConfigurationNode original = node.getNode("section");
        ConfigurationNode copy = original.copy();

        assertNotNull(original.getParent());
        assertNull(copy.getParent());

        ConfigurationNode originalVal = original.getNode("val1");
        ConfigurationNode copyVal = copy.getNode("val1");

        assertEquals(2, originalVal.getPath().length);
        assertEquals(1, copyVal.getPath().length);

        assertNotNull(originalVal.getParent());
        assertNotNull(copyVal.getParent());
        assertNotSame(originalVal.getParent(), copyVal.getParent());
    }

}
