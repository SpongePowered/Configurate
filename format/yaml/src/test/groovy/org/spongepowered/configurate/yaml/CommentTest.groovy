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

import static org.assertj.core.api.Assertions.assertThat
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNull

import org.junit.jupiter.api.Test
import org.spongepowered.configurate.CommentedConfigurationNode

class CommentTest implements YamlTest {

    @Test
    void testLoadScalarComment() {
        final CommentedConfigurationNode node = parseString normalize("""\
            # Hello world
            "i'm a string"
            """)

        assertEquals("Hello world", node.comment())
        assertEquals("i'm a string", node.raw())
    }

    @Test
    void testLoadBlockMappingComment() {
        final CommentedConfigurationNode node = parseString normalize("""\
            # outer
            test:
                # meow
                cat: purrs
            """)

        assertThat(node.node('test'))
                .extracting { it.comment() }
                .isEqualTo("outer")

        assertThat(node.node('test', 'cat')).with {
            extracting { it.raw() }
                    .isEqualTo("purrs")
            extracting { it.comment() }
                .isEqualTo("meow")
        }
    }

    @Test
    void testLoadBlockSequenceComment() {
        final CommentedConfigurationNode node = parseString normalize("""\
            # first
            - one
            # second
            - two
            """)

        assertThat(node.node(0))
                .extracting { it.comment() }
                .isEqualTo("first")
        assertThat(node.node(1))
                .extracting { it.comment() }
                .isEqualTo("second")
    }

    @Test
    void testLoadBlockScalarSequenceComment() {
        final CommentedConfigurationNode test = parseString(normalize("""\
            - first
            # i matter less
            - second
            - third
            # we skipped one
            - fourth
            """))

        assertNull(test.node(0).comment())
        assertEquals("i matter less", test.node(1).comment())
        assertEquals("we skipped one", test.node(3).comment())
    }

    @Test
    void testLoadScalarCommentsInBlockMapping() {
        final CommentedConfigurationNode test = parseString """\
            # on mapping key
            blah:
            # beginning sequence
            - # first on map entry
              test: hello
            - # on second mapping
              test2: goodbye
            """.stripIndent(true)

        final CommentedConfigurationNode child = test.node("blah", 0)
        assertFalse(child.virtual())
        assertEquals("on mapping key\nbeginning sequence", test.node('blah').comment())
        assertEquals("first on map entry", test.node('blah', 0, 'test').comment())
        assertEquals("on second mapping", test.node('blah', 1, "test2").comment())
    }

    // flow collections are a bit trickier
    // we can't really do comments on one line, so these all have to have a line per element

    @Test
    void testLoadCommentInFlowMapping() {
        final CommentedConfigurationNode test = parseString(normalize("""\
            {
                # hello
                test: value,
                uncommented: thing,
                #hi there
                last: bye
            }
        """))

        assertEquals("hello", test.node("test").comment())
        assertNull(test.node("uncommented").comment())
        assertEquals("hi there", test.node("last").comment())
    }

    @Test
    void testLoadCommentInFlowSequence() {
        final CommentedConfigurationNode test = parseString(normalize("""\
            # on list
            [
                # first
                'first entry',
                # second
                'second entry'
            ]
        """))

        assertEquals("on list", test.comment())
        assertEquals("first", test.node(0).comment())
        assertEquals("second", test.node(1).comment())
    }

    @Test
    void testLoadMixedStructure() {
        final CommentedConfigurationNode test = parseResource(getClass().getResource("comments-complex.yml"))

        assertEquals("very mapping", test.node("core", "users", 0, "second").comment())
    }

    @Test
    void testWriteScalarCommented() {
        final CommentedConfigurationNode node = CommentedConfigurationNode.root()
                .raw("test")
                .comment("i have a comment")

        assertEquals(normalize("""\
        # i have a comment
        test"""), dump(node).trim())
    }

    @Test
    void testWriteBlockMappingCommented() {
        final CommentedConfigurationNode node = CommentedConfigurationNode.root {
            node("a").set("Hello").comment("I'm first")
            node("b", "one").set("World")
            node("b", "two").set("eee").comment("also me")
        }

        assertLinesEqual(
            normalize("""\
                # I'm first
                a: Hello
                b:
                  one: World
                  
                  # also me
                  two: eee
                """),
            dump(node, NodeStyle.BLOCK)
        )
    }

    @Test
    void testWriteBlockSequence() {
        final def node = CommentedConfigurationNode.root {
            appendListNode().set("Hello")
            appendListNode().set("World")
            appendListNode().with {
                node("one").set("aaa")
                node("two").set("bbb")
            }
        }

        final def expected = normalize("""\
        - Hello
        - World
        - one: aaa
          two: bbb
        """)
        assertLinesEqual(expected, this.dump(node, NodeStyle.BLOCK))
    }

    @Test
    void testWriteBlockSequenceCommented() {
        final def node = CommentedConfigurationNode.root {
            appendListNode().set("red").comment("A colour")
            appendListNode().set("orange").comment("Another colour")
            appendListNode().set("yellow").comment("What? a THIRD colour???")
        }

        final def expected = normalize("""\
        # A colour
        - red
        # Another colour
        - orange
        # What? a THIRD colour???
        - yellow
        """)
        assertLinesEqual(expected, this.dump(node, NodeStyle.BLOCK))
    }

    @Test
    void testWriteFlowMappingCommented() {
        final CommentedConfigurationNode node = CommentedConfigurationNode.root {
            node("a").set("Hello").comment("I'm first")
            node("b", "one").set("World")
            node("b", "two").set("eee").comment("also me")
        }

        final def expected = normalize("""\
            {
              # I'm first
              a: Hello,
              b: {
                one: World,
                
                # also me
                two: eee
              }
            }
            """)

        assertLinesEqual(expected, dump(node, NodeStyle.FLOW))
    }

    @Test
    void testPrettyFlowForcedWhenEmittingCommentsEvenNotFirst() {
        final CommentedConfigurationNode node = CommentedConfigurationNode.root {
            node('one').set "two"
            node("three").with {
                set "four"
                comment "hello"
            }
        }

        final def expected = normalize("""\
            {one: two,
            
              # hello
              three: four
            }
        """)

        assertLinesEqual(expected, dump(node, NodeStyle.FLOW))
    }

    @Test
    void testWriteFlowSequenceCommented() {
        final def node = CommentedConfigurationNode.root {
            appendListNode().set("red").comment("A colour")
            appendListNode().set("orange").comment("Another colour")
            appendListNode().set("yellow").comment("What? a THIRD colour???")
        }

        final def expected = normalize("""\
            [
              # A colour
              red,
              # Another colour
              orange,
              # What? a THIRD colour???
              yellow
            ]
            """)
        assertLinesEqual(expected, this.dump(node, NodeStyle.FLOW))
    }

    private static def assertLinesEqual(String expected, String actual) {
        assertThat(actual.split("\r?\n", -1).collect { it.isAllWhitespace() ? "" : it})
            .containsAll(expected.split("\r?\n", -1).collect { it.isAllWhitespace() ? "" : it})
    }

}
