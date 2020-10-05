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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Arrays;

public class CopyTest {

    @Test
    void testSimpleCopy() throws SerializationException {
        final ConfigurationNode node = BasicConfigurationNode.root();
        node.node("test").set(5);
        node.node("section", "val1").set(true);
        node.node("section", "val2").set("TEST");
        final ConfigurationNode list = node.node("section2", "alist");
        list.appendListNode().set("value1");
        list.appendListNode().set("value2");

        final ConfigurationNode copy = node.copy();

        assertNotSame(node, copy);
        assertEquals(node, copy);

        assertFalse(node.virtual());
        assertFalse(copy.virtual());

        assertEquals(5, copy.node("test").raw());
        assertEquals(true, copy.node("section", "val1").raw());
        assertEquals("TEST", copy.node("section", "val2").raw());
        assertEquals(Arrays.asList("value1", "value2"), copy.node("section2", "alist").raw());

        // change value on original
        node.node("section", "val2").set("NOT TEST");

        // test it's still the same on copy
        assertEquals("TEST", copy.node("section", "val2").raw());

        // change value on copy
        copy.node("section", "val2").set("zzz");

        // test it's still the same on original
        assertEquals("NOT TEST", node.node("section", "val2").raw());
    }

    @Test
    void testCopyPaths() throws SerializationException {
        final ConfigurationNode node = BasicConfigurationNode.root();
        node.node("test").set(5);
        node.node("section", "val1").set(true);
        node.node("section", "val2").set("TEST");

        final ConfigurationNode original = node.node("section");
        final ConfigurationNode copy = original.copy();

        assertNotNull(original.parent());
        assertNull(copy.parent());

        final ConfigurationNode originalVal = original.node("val1");
        final ConfigurationNode copyVal = copy.node("val1");

        assertEquals(2, originalVal.path().size());
        assertEquals(1, copyVal.path().size());

        assertNotNull(originalVal.parent());
        assertNotNull(copyVal.parent());
        assertNotSame(originalVal.parent(), copyVal.parent());
    }

}
