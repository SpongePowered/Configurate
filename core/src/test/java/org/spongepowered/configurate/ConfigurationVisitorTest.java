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

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

public class ConfigurationVisitorTest {

    private static final TestVisitor<BasicConfigurationNode> VISITOR = new TestVisitor<>();

    @Test
    public void testTree() {
        final BasicConfigurationNode base = BasicConfigurationNode.root();

        base.getNode("cats").act(c -> {
            c.getNode("large").setValue("great");
            c.getNode("medium").setValue("wonderful");
            c.getNode("small").setValue("stupendous");
        });

        base.getNode("fish").act(c -> {
            c.appendListNode().setValue("one");
            c.appendListNode().act(f -> {
                f.getNode("number").setValue("two");
                f.getNode("type").setValue("blue");
            });
        });

        base.getNode("dog").setValue("woof");

        final String result = base.visit(VISITOR);
        assertEquals("b(m(-cats-m(-large-s)(-medium-s)(-small-s))(-fish-l(-0-s)(-1-m(-number-s)(-type-s)))(-dog-s))t", result);
    }

    @Test
    public void testEmptyRoot() {
        final BasicConfigurationNode base = BasicConfigurationNode.root();
        final String result = base.visit(VISITOR);
        assertEquals("bt", result);
    }

    @Test
    public void testSingleScalar() {
        final BasicConfigurationNode base = BasicConfigurationNode.root();
        base.setValue("test");
        final String result = base.visit(VISITOR);
        assertEquals("b(s)t", result);
    }

    @Test
    public void testChangingValueTypeOnEnter() {
        final ConfigurationVisitor.Safe<BasicConfigurationNode, StringBuilder, String> visitor = new TestVisitor<BasicConfigurationNode>() {
            @Override
            public void enterListNode(final BasicConfigurationNode node, final StringBuilder state) {
                super.enterListNode(node, state);
                node.setValue(null);
            }
        };
        final BasicConfigurationNode base = BasicConfigurationNode.root().setValue(ImmutableList.of());
        final String result = base.visit(visitor);

        assertEquals("b(l)t", result);
    }

    /**
     * A visitor that tracks events and outputs a string with the
     * following tokens.
     *
     * @param <N> node type
     */
    static class TestVisitor<N extends ScopedConfigurationNode<N>> implements ConfigurationVisitor.Safe<N, StringBuilder, String> {
        static final String VISIT_BEGIN = "b";
        /**
         * Appended on node enter. If the node has a non-null key, will be
         * followed by {@code -<key>-}
         */
        static final String NODE_ENTER = "(";
        static final String NODE_MAP = "m";
        static final String NODE_LIST = "l";
        static final String NODE_SCALAR = "s";
        static final String NODE_EXIT = ")";
        static final String VISIT_END = "t";

        @Override
        public StringBuilder newState() {
            return new StringBuilder();
        }

        @Override
        public void beginVisit(final N node, final StringBuilder state) {
            state.append(VISIT_BEGIN);
        }

        @Override
        public void enterNode(final N node, final StringBuilder state) {
            state.append(NODE_ENTER);
            if (node.getKey() != null) {
                state.append("-").append(node.getKey()).append("-");
            }
        }

        @Override
        public void enterMappingNode(final N node, final StringBuilder state) {
            state.append(NODE_MAP);
        }

        @Override
        public void enterListNode(final N node, final StringBuilder state) {
            state.append(NODE_LIST);
        }

        @Override
        public void enterScalarNode(final N node, final StringBuilder state) {
            state.append(NODE_SCALAR).append(NODE_EXIT);
        }

        @Override
        public void exitMappingNode(final N node, final StringBuilder state) {
            state.append(NODE_EXIT);
        }

        @Override
        public void exitListNode(final N node, final StringBuilder state) {
            state.append(NODE_EXIT);
        }

        @Override
        public String endVisit(final StringBuilder state) {
            state.append(VISIT_END);
            return state.toString();
        }
    }

}
