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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    /**
     * A visitor that tracks events and outputs a string with the following tokens
     * @param <N>
     */
    static class TestVisitor<N extends ScopedConfigurationNode<N>> implements ConfigurationVisitor.Safe<N, StringBuilder, String> {
        static final String VISIT_BEGIN = "b";
        /**
         * Appended on node enter. If the node has a non-null key, will be followed by {@code -<key>-}
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
        public void beginVisit(N node, StringBuilder state) {
            state.append(VISIT_BEGIN);
        }

        @Override
        public void enterNode(N node, StringBuilder state) {
            state.append(NODE_ENTER);
            if (node.getKey() != null) {
                state.append("-").append(node.getKey()).append("-");
            }
        }

        @Override
        public void enterMappingNode(N node, StringBuilder state) {
            state.append(NODE_MAP);
        }

        @Override
        public void enterListNode(N node, StringBuilder state) {
            state.append(NODE_LIST);
        }

        @Override
        public void enterScalarNode(N node, StringBuilder state) {
            state.append(NODE_SCALAR).append(NODE_EXIT);
        }

        @Override
        public void exitMappingNode(N node, StringBuilder state) {
            state.append(NODE_EXIT);
        }

        @Override
        public void exitListNode(N node, StringBuilder state) {
            state.append(NODE_EXIT);
        }

        @Override
        public String endVisit(StringBuilder state) {
            state.append(VISIT_END);
            return state.toString();
        }
    }
}
