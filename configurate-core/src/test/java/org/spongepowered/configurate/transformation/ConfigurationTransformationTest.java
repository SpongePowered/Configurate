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
package org.spongepowered.configurate.transformation;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.SimpleConfigurationNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigurationTransformationTest {
    private static Object[] p(Object... path) {
        return path;
    }

    @Test
    public void testComparator() {
        doTestComparator(ConfigurationNode.root());
    }

    private <T extends ConfigurationNode<T>> void doTestComparator(T node) {
        final List<Object[]> unsortedKeys = Arrays.asList(
                p("a", "c", "c"),
                p("a", "b"),
                p("a", "b", "c"),
                p("a", "b", "d"),
                p("a", "b", "c", "d"),
                p("a", "c"),
                p("a", "b", "b")
        ), autoSortedKeys = new ArrayList<>(), expectedSortedKeys
                = Arrays.asList(
                p("a", "b", "b"),
                p("a", "b", "c", "d"),
                p("a", "b", "c"),
                p("a", "b", "d"),
                p("a", "b"),
                p("a", "c", "c"),
                p("a", "c")
        );

        final TransformAction<T> action = (inputPath, valueAtPath) -> {
            autoSortedKeys.add(inputPath.getArray());
            return null;
        };

        final ConfigurationTransformation.Builder<T> build = ConfigurationTransformation.builder();
        for (Object[] path : unsortedKeys) {
            build.addAction(path, action);
        }
        for (Object[] path : unsortedKeys) {
            T child = node.getNode(path);
            if (child.isVirtual()) {
                child.setValue("meaningless test value");
            }
        }
        build.build().apply(node);
        assertListOfArrayEquals(expectedSortedKeys, autoSortedKeys);

    }

    public static void assertListOfArrayEquals(List<Object[]> expected, List<Object[]> tested) {
        assertEquals(expected.size(), tested.size());
        for (int i = 0; i < expected.size(); ++i) {
            System.out.println("Comparing lists " + Arrays.toString(expected.get(i)) + " and " + Arrays.toString(tested.get(i)));
            assertArrayEquals(expected.get(i), tested.get(i));
        }
    }

    @Test
    public void testWildcardMatching() {
        doTestWildcardMatching(ConfigurationNode.root());
    }

    private <T extends ConfigurationNode<T>> void doTestWildcardMatching(T node) {
        final List<Object[]> wildcardMatch = Arrays.asList(
                p("a", ConfigurationTransformation.WILDCARD_OBJECT, "c"),
                p("a", ConfigurationTransformation.WILDCARD_OBJECT, "d"),
                p("a", "c", "c"),
                p("b", ConfigurationTransformation.WILDCARD_OBJECT, "d", ConfigurationTransformation.WILDCARD_OBJECT,
                        "f")
        ), populatedResults = new ArrayList<>(), expectedResult = Arrays.asList(
                p("a", "c", "c"),
                p("a", "c", "c"),
                p("a", "d", "c"),
                p("a", "b", "c"),
                p("a", "c", "d"),
                p("a", "d", "d"),
                p("b", "c", "d", "e", "f"),
                p("b", "c", "d", "f", "f"),
                p("b", "d", "d", "e", "f"),
                p("b", "d", "d", "f", "f")
        );

        final TransformAction<T> action = (path, valueAthPath) -> {
            populatedResults.add(path.getArray());
            return null;
        };
        final ConfigurationTransformation.Builder<T> build = ConfigurationTransformation.builder();
        for (Object[] path : wildcardMatch) {
            build.addAction(path, action);
        }
        for (Object[] path : expectedResult) {
            node.getNode(path).setValue("lame");
        }
        build.build().apply(node);
        assertListOfArrayEquals(expectedResult, populatedResults);
    }

    @Test
    public void testMoveNode() {

        SimpleConfigurationNode node = ConfigurationNode.root();
        final Object nodeValue = new Object();
        node.getNode("old", "path").setValue(nodeValue);
        transformMoveNode(node);
        assertTrue(node.getNode("old", "path").isVirtual());
        assertEquals(nodeValue, node.getNode("new", "path").getValue());
    }

    private <T extends ConfigurationNode<T>> void transformMoveNode(T node) {
        ConfigurationTransformation.<T>builder()
                .addAction(p("old", "path"),
                        (inputPath, valueAtPath) -> p("new", "path"))
                .build().apply(node);
    }

    @Test
    public void testChainedTransformations() {
        SimpleConfigurationNode node = ConfigurationNode.root();
        node.getNode("a").setValue("something?");
        final List<String> actualOutput = new ArrayList<>(), expectedOutput = ImmutableList.of("one", "two");
        transformChained(actualOutput, node);
        assertEquals(expectedOutput, actualOutput);
    }

    @SuppressWarnings("unchecked")
    private <T extends ConfigurationNode<T>> void transformChained(List<String> actualOutput, T node) {
        ConfigurationTransformation.chain(ConfigurationTransformation.<T>builder().addAction(p("a"), (inputPath, valueAtPath) -> {
            actualOutput.add("one");
            return null;
        }).build(), ConfigurationTransformation.<T>builder().addAction(p("a"), (inputPath, valueAtPath) -> {
            actualOutput.add("two");
            return null;
        }).build()).apply(node);
    }

    @Test
    public void testMoveToBase() {
        SimpleConfigurationNode node = ConfigurationNode.root();
        node.getNode("sub", "key").setValue("value");
        node.getNode("at-parent").setValue("until-change");
        transformMoveToBase(node);
        assertEquals("value", node.getNode("key").getValue());
        assertEquals(null, node.getNode("at-parent").getValue());
    }

    private <T extends ConfigurationNode<T>> void transformMoveToBase(T node) {
        ConfigurationTransformation.<T>builder()
                .addAction(p("sub"), (inputPath, valueAtPath) -> {
                    return new Object[0];
                }).build().apply(node);
    }

    @Test
    public void testMoveStrategy() {
        final ConfigurationTransformation.Builder<SimpleConfigurationNode> build = ConfigurationTransformation.<SimpleConfigurationNode>builder()
                .addAction(p("one"), (inputPath, valueAtPath) -> p("two"));
        SimpleConfigurationNode overwritten = createMoveNode(), merged = createMoveNode();
        build.setMoveStrategy(MoveStrategy.OVERWRITE).build().apply(overwritten);
        build.setMoveStrategy(MoveStrategy.MERGE).build().apply(merged);

        assertEquals("always", overwritten.getNode("two", "fun").getValue());
        assertEquals("always", merged.getNode("two", "fun").getValue());
        assertNull(overwritten.getNode("two", "evil").getValue());
        assertEquals("always", merged.getNode("two", "evil").getValue());
    }

    private SimpleConfigurationNode createMoveNode() {
        SimpleConfigurationNode ret = ConfigurationNode.root();
        ret.getNode("one", "fun").setValue("always");
        ret.getNode("two", "evil").setValue("always");
        return ret;
    }

    @Test
    public void testCorrectNodePassed() {
        final SimpleConfigurationNode node = ConfigurationNode.root();
        final SimpleConfigurationNode child = node.getNode("childNode").setValue("something");
        transformTestCorrectNodePassed(node, child);
    }

    private <T extends ConfigurationNode<T>> void transformTestCorrectNodePassed(T node, T child) {
        ConfigurationTransformation.<T>builder()
                .addAction(p("childNode"), (inputPath, valueAtPath) -> {
                    assertEquals(child, valueAtPath);
                    return null;
                }).build().apply(node);
    }

    @Test
    public void testVersionedTransformation() {
        final SimpleConfigurationNode target = ConfigurationNode.root();
        target.getNode("dummy").setValue("whatever");
        final List<Integer> updatedVersions = new ArrayList<>();
        this.<SimpleConfigurationNode>buildVersionedTransformation(updatedVersions).apply(target);
        assertEquals(2, target.getNode("version").getInt());
        assertEquals(ImmutableList.of(0, 1, 2), updatedVersions);
    }

    private <T extends ConfigurationNode<T>> ConfigurationTransformation<T> buildVersionedTransformation(List<Integer> updatedVersions) {
        return ConfigurationTransformation.<T>versionedBuilder()
                .addVersion(0, ConfigurationTransformation.<T>builder()
                        .addAction(p("dummy"), (inputPath, valueAtPath) -> {
                            updatedVersions.add(0);
                            return null;
                        }).build())
                .addVersion(2, ConfigurationTransformation.<T>builder()
                        .addAction(p("dummy"), (inputPath, valueAtPath) ->  {
                            updatedVersions.add(2);
                            return null;
                        }).build())
                .addVersion(1, ConfigurationTransformation.<T>builder()
                        .addAction(p("dummy"), (inputPath, valueAtPath) -> {
                            updatedVersions.add(1);
                            return null;
                        }).build())
                .build();

    }
}
