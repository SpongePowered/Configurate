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
import static org.assertj.core.api.Assertions.assertThatThrownBy

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.loader.ParsingException

/**
 * Tests of the basic functionality of our composer implementation.
 *
 * <p>Comment-specific testing is handled in {@link CommentTest}
 */
class YamlParserComposerTest implements YamlTest {

    @Test
    void testEmptyDocument() throws IOException {
        final ConfigurationNode result = parseString("")
        assertThat(result.empty()).isTrue()
        assertThat(result.raw()).isNull()
    }

    @Test
    void testDuplicateKeysForbidden() throws IOException {
        assertThatThrownBy { parseString '{duplicated: 1, duplicated: 2}' }
                .isInstanceOf(ParsingException)
                .hasMessageContaining("Duplicate key")
    }

    // Different types of scalars (folded, block, etc)

    @Test
    void testLoadPlainScalar() {
        def result = parseString "hello world"
        assertThat(result.raw())
                .isEqualTo("hello world")

        assertThat(result.hint(YamlConfigurationLoader.SCALAR_STYLE))
                .isEqualTo(ScalarStyle.UNQUOTED)
    }

    @Test
    void testLoadDoubleQuotedScalar() {
        def result = parseString '"hello world"'
        assertThat(result.raw())
                .isEqualTo("hello world")

        assertThat(result.hint(YamlConfigurationLoader.SCALAR_STYLE))
                .isEqualTo(ScalarStyle.DOUBLE_QUOTED)
    }

    @Test
    void testLoadSingleQuotedScalar() {
        def result = parseString "'hello world'"
        assertThat(result.raw())
                .isEqualTo("hello world")

        assertThat(result.hint(YamlConfigurationLoader.SCALAR_STYLE))
                .isEqualTo(ScalarStyle.SINGLE_QUOTED)
    }

    @Test
    void testLoadFoldedScalar() {
        def result = parseString("""\
        test: >
            hello
            world\
        """.stripIndent(true).trim()).node("test")

        assertThat(result.raw())
                .isEqualTo("hello world")

        assertThat(result.hint(YamlConfigurationLoader.SCALAR_STYLE))
                .isEqualTo(ScalarStyle.FOLDED)
    }

    @Test
    void testLoadBlockScalar() {
        def result = parseString("""\
        test: |
            hello
            world
        """.stripIndent(true).trim()).node("test")

        assertThat(result.raw())
                .isEqualTo("hello\nworld")

        assertThat(result.hint(YamlConfigurationLoader.SCALAR_STYLE))
                .isEqualTo(ScalarStyle.LITERAL)

    }

    // More complex data structures

    @Test
    void testLoadMap() {
        def result = parseString """\
        hello:
            world: yup!
            two: [one, two, three]
            "yes":
                aaa: bbb
                ccc: ddd
        """.stripIndent(true)

        assertThat(result.node('hello', 'world').raw())
            .isEqualTo("yup!")

        assertThat(result.node('hello', 'two', 0).raw())
                .isEqualTo('one')

        assertThat(result.node('hello', 'two').getList(String))
                .containsExactly('one', 'two', 'three')

        assertThat(result.node('hello', 'yes', 'aaa').raw())
                .isEqualTo('bbb')

        assertThat(result.node('hello', 'yes', 'ccc').raw())
                .isEqualTo('ddd')
    }

    @Test
    void testLoadSequence() {
        def result = parseString """\
        flow: [a, b, c]
        block:
        - d
        - e
        - f
        """.stripIndent(true)

        assertThat(result.node('flow').getList(String))
                .containsExactly('a', 'b', 'c')
        assertThat(result.node('flow').hint(YamlConfigurationLoader.NODE_STYLE))
                .isEqualTo(NodeStyle.FLOW)

        assertThat(result.node('block').getList(String))
                .containsExactly('d', 'e', 'f')
        assertThat(result.node('block').hint(YamlConfigurationLoader.NODE_STYLE))
                .isEqualTo(NodeStyle.BLOCK)
    }


    @Test
    void testLoadAlias() {
        def result = parseString """\
        src: &ref [a, b, c]
        dest: *ref
        """.stripIndent(true)

        def src = result.node('src')
        def dest = result.node('dest')

        // Value transferred
        assertThat(dest.getList(String))
                .containsExactly('a', 'b', 'c')

        // Anchor information preserved
        // TODO: this may be different once proper reference nodes are implemented
        assertThat(src.hint(YamlConfigurationLoader.ANCHOR_ID))
                .isEqualTo('ref')

        assertThat(dest.hint(YamlConfigurationLoader.ANCHOR_ID))
                .isNull()
    }

    @Test
    void testCommentsOnNullValuePreserved() {
        def result = parseString """\
        # the greetings
        hello:
        # - abc
        # - def
        """

        println dump(result)

        assertThat(result.node('hello').comment())
            .isEqualTo("the greetings")
    }

    // Test that implicit tags are resolved properly

    @Test
    @Disabled("not yet implemented")
    void testMergeKey() {
        def result = parseString """\
        src: &ref 
           old: merged
        dest: 
          >>: *ref
          new: added
        """.stripIndent(true)

        def src = result.node('src')
        def dest = result.node('dest')

        // Value transferred
        assertThat(dest.childrenMap().keySet())
                .containsExactly('old', 'new')
    }

    @Test
    void testYIsBooleanForSomeReason() {
        def result = parseString """\
        asVal: y
        y: asKey
        """

        assertThat(result.node('asVal')).with {
            extracting { it.virtual() }
                .is(false)
            extracting { it.raw() }
                .isEqualTo(true)
        }
        assertThat(result.node(true)).with {
            extracting { it.virtual() }
                    .is(false)
            extracting { it.raw() }
                    .isEqualTo('asKey')
        }
    }

}
