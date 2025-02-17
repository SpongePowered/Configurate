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
package org.spongepowered.configurate.yaml

import static org.junit.jupiter.api.Assertions.assertEquals

import org.junit.jupiter.api.Test
import org.spongepowered.configurate.CommentedConfigurationNode

/**
 * Testing YAML emmission.
 */
class YamlVisitorTest implements YamlTest {

    @Test
    void testPlainScalar() {
        def node = CommentedConfigurationNode.root().set("test")

        assertEquals("test\n", dump(node))
    }

    @Test
    void testNumbersAreNonPlainScalar() {
        def node = CommentedConfigurationNode.root().set("1234")

        assertEquals("\"1234\"\n", dump(node))
    }

    @Test
    void testBlockSequence() {
        final def node = CommentedConfigurationNode.root {
            appendListNode().set("Hello")
            appendListNode().set("World")
            appendListNode().act {
                it.node("one").set("aaa")
                it.node("two").set("bbb")
            }
        }

        final def expected = normalize("""\
        - Hello
        - World
        - one: aaa
          two: bbb
        """)
        assertEquals(expected, this.dump(node, NodeStyle.BLOCK))
    }

    @Test
    void testBlockMapping() {
        final def node = CommentedConfigurationNode.root {
            node("meow").set("purr")
            node("eight").set(1234)
            node("fun").set(true)
        }

        final def expected = normalize("""\
        meow: purr
        eight: 1234
        fun: true
        """)
        assertEquals(expected, this.dump(node, NodeStyle.BLOCK))
    }

    @Test
    void testFlowSequence() {
        final def node = CommentedConfigurationNode.root {
            appendListNode().set("Hello")
            appendListNode().set("World")
            appendListNode().act {
                it.node("one").set("aaa")
                it.node("two").set("bbb")
            }
        }

        final def expected = "[Hello, World, {one: aaa, two: bbb}]\n"
        assertEquals(expected, this.dump(node, NodeStyle.FLOW))
    }

    @Test
    void testFlowMapping() {
        final def node = CommentedConfigurationNode.root {
            node("meow").set("purr")
            node("eight").set(1234)
            node("fun").set(true)
        }

        final def expected = "{meow: purr, eight: 1234, fun: true}\n"
        assertEquals(expected, this.dump(node, NodeStyle.FLOW))
    }

    @Test
    void testComplex() {

    }

}
