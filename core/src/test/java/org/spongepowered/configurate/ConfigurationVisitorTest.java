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

import org.junit.jupiter.api.Test;

import java.util.Collections;

public class ConfigurationVisitorTest {

    private static final TestVisitor VISITOR = new TestVisitor();

    @Test
    void testTree() {
        final BasicConfigurationNode base = BasicConfigurationNode.root();

        base.node("cats").act(c -> {
            c.node("large").set("great");
            c.node("medium").set("wonderful");
            c.node("small").set("stupendous");
        });

        base.node("fish").act(c -> {
            c.appendListNode().set("one");
            c.appendListNode().act(f -> {
                f.node("number").set("two");
                f.node("type").set("blue");
            });
        });

        base.node("dog").set("woof");

        final String result = base.visit(VISITOR);
        assertEquals("b(m(-cats-m(-large-s)(-medium-s)(-small-s))(-fish-l(-0-s)(-1-m(-number-s)(-type-s)))(-dog-s))t", result);
    }

    @Test
    void testEmptyRoot() {
        final BasicConfigurationNode base = BasicConfigurationNode.root();
        final String result = base.visit(VISITOR);
        assertEquals("bt", result);
    }

    @Test
    void testSingleScalar() {
        final BasicConfigurationNode base = BasicConfigurationNode.root();
        base.set("test");
        final String result = base.visit(VISITOR);
        assertEquals("b(s)t", result);
    }

    @Test
    void testChangingValueTypeOnEnter() {
        final ConfigurationVisitor.Safe<StringBuilder, String> visitor = new TestVisitor() {
            @Override
            public void enterListNode(final ConfigurationNode node, final StringBuilder state) {
                super.enterListNode(node, state);
                node.set(null);
            }
        };
        final BasicConfigurationNode base = BasicConfigurationNode.root().set(Collections.emptyList());
        final String result = base.visit(visitor);

        assertEquals("b(l)t", result);
    }

    /**
     * A visitor that tracks events and outputs a string with the
     * following tokens.
     *
     */
    static class TestVisitor implements ConfigurationVisitor.Safe<StringBuilder, String> {
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
        public void beginVisit(final ConfigurationNode node, final StringBuilder state) {
            state.append(VISIT_BEGIN);
        }

        @Override
        public void enterNode(final ConfigurationNode node, final StringBuilder state) {
            state.append(NODE_ENTER);
            if (node.key() != null) {
                state.append("-").append(node.key()).append("-");
            }
        }

        @Override
        public void enterMappingNode(final ConfigurationNode node, final StringBuilder state) {
            state.append(NODE_MAP);
        }

        @Override
        public void enterListNode(final ConfigurationNode node, final StringBuilder state) {
            state.append(NODE_LIST);
        }

        @Override
        public void enterScalarNode(final ConfigurationNode node, final StringBuilder state) {
            state.append(NODE_SCALAR).append(NODE_EXIT);
        }

        @Override
        public void exitMappingNode(final ConfigurationNode node, final StringBuilder state) {
            state.append(NODE_EXIT);
        }

        @Override
        public void exitListNode(final ConfigurationNode node, final StringBuilder state) {
            state.append(NODE_EXIT);
        }

        @Override
        public String endVisit(final StringBuilder state) {
            state.append(VISIT_END);
            return state.toString();
        }
    }

}
