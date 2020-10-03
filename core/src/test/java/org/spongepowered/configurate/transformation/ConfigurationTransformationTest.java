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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spongepowered.configurate.transformation.NodePath.path;

import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ScopedConfigurationNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigurationTransformationTest {

    private static Object[] arr(final Object... path) {
        return path;
    }

    @Test
    void testComparator() {
        doTestComparator(BasicConfigurationNode.root());
    }

    private <T extends ScopedConfigurationNode<T>> void doTestComparator(final T node) {
        final List<NodePath> unsortedKeys = Arrays.asList(
                path("a", "c", "c"),
                path("a", "b"),
                path("a", "b", "c"),
                path("a", "b", "d"),
                path("a", "b", "c", "d"),
                path("a", "c"),
                path("a", "b", "b")
        );
        final List<NodePath> autoSortedKeys = new ArrayList<>();
        final List<NodePath> expectedSortedKeys = Arrays.asList(
                path("a", "b", "b"),
                path("a", "b", "c", "d"),
                path("a", "b", "c"),
                path("a", "b", "d"),
                path("a", "b"),
                path("a", "c", "c"),
                path("a", "c")
        );

        final TransformAction<T> action = (inputPath, valueAtPath) -> {
            autoSortedKeys.add(inputPath.clone());
            return null;
        };

        final ConfigurationTransformation.Builder<T> build = ConfigurationTransformation.builder();
        for (NodePath path : unsortedKeys) {
            build.addAction(path, action);
        }
        for (NodePath path : unsortedKeys) {
            final T child = node.getNode(path);
            if (child.isVirtual()) {
                child.setValue("meaningless test value");
            }
        }
        build.build().apply(node);
        assertEquals(expectedSortedKeys, autoSortedKeys);
    }

    @Test
    void testWildcardMatching() {
        final BasicConfigurationNode node = BasicConfigurationNode.root();
        final List<NodePath> wildcardMatch = Arrays.asList(
                path("a", ConfigurationTransformation.WILDCARD_OBJECT, "c"),
                path("a", ConfigurationTransformation.WILDCARD_OBJECT, "d"),
                path("a", "c", "c"),
                path("b", ConfigurationTransformation.WILDCARD_OBJECT, "d", ConfigurationTransformation.WILDCARD_OBJECT,
                        "f")
        );
        final List<NodePath> populatedResults = new ArrayList<>();
        final List<NodePath> expectedResult = Arrays.asList(
                path("a", "c", "c"),
                path("a", "c", "c"),
                path("a", "d", "c"),
                path("a", "b", "c"),
                path("a", "c", "d"),
                path("a", "d", "d"),
                path("b", "c", "d", "e", "f"),
                path("b", "c", "d", "f", "f"),
                path("b", "d", "d", "e", "f"),
                path("b", "d", "d", "f", "f")
        );

        final TransformAction<BasicConfigurationNode> action = (path, valueAthPath) -> {
            populatedResults.add(path.clone());
            return null;
        };
        final ConfigurationTransformation.Builder<BasicConfigurationNode> build = ConfigurationTransformation.builder();
        for (NodePath path : wildcardMatch) {
            build.addAction(path, action);
        }
        for (NodePath path : expectedResult) {
            node.getNode(path).setValue("lame");
        }
        build.build().apply(node);
        assertEquals(expectedResult, populatedResults);
    }

    @Test
    void testMoveNode() {

        final BasicConfigurationNode node = BasicConfigurationNode.root();
        final Object nodeValue = new Object();
        node.getNode("old", "path").setValue(nodeValue);
        ConfigurationTransformation.<BasicConfigurationNode>builder()
                .addAction(path("old", "path"),
                    (inputPath, valueAtPath) -> arr("new", "path"))
                .build().apply(node);
        assertTrue(node.getNode("old", "path").isVirtual());
        assertEquals(nodeValue, node.getNode("new", "path").getValue());
    }

    @Test
    void testChainedTransformations() {
        final BasicConfigurationNode node = BasicConfigurationNode.root();
        node.getNode("a").setValue("something?");
        final List<String> actualOutput = new ArrayList<>();
        final List<String> expectedOutput = Arrays.asList("one", "two");
        transformChained(actualOutput, node);
        assertEquals(expectedOutput, actualOutput);
    }

    @SuppressWarnings("unchecked")
    private <T extends ScopedConfigurationNode<T>> void transformChained(final List<String> actualOutput, final T node) {
        ConfigurationTransformation.chain(ConfigurationTransformation.<T>builder().addAction(path("a"), (inputPath, valueAtPath) -> {
            actualOutput.add("one");
            return null;
        }).build(), ConfigurationTransformation.<T>builder().addAction(path("a"), (inputPath, valueAtPath) -> {
            actualOutput.add("two");
            return null;
        }).build()).apply(node);
    }

    @Test
    void testMoveToBase() {
        final BasicConfigurationNode node = BasicConfigurationNode.root();
        node.getNode("sub", "key").setValue("value");
        node.getNode("at-parent").setValue("until-change");
        transformMoveToBase(node);
        assertEquals("value", node.getNode("key").getValue());
        assertNull(node.getNode("at-parent").getValue());
    }

    private <T extends ScopedConfigurationNode<T>> void transformMoveToBase(final T node) {
        ConfigurationTransformation.<T>builder()
                .addAction(path("sub"), (inputPath, valueAtPath) -> {
                    return new Object[0];
                }).build().apply(node);
    }

    @Test
    void testMoveStrategy() {
        final ConfigurationTransformation.Builder<BasicConfigurationNode> build = ConfigurationTransformation.<BasicConfigurationNode>builder()
                .addAction(path("one"), (inputPath, valueAtPath) -> arr("two"));
        final BasicConfigurationNode overwritten = createMoveNode();
        final BasicConfigurationNode merged = createMoveNode();
        build.setMoveStrategy(MoveStrategy.OVERWRITE).build().apply(overwritten);
        build.setMoveStrategy(MoveStrategy.MERGE).build().apply(merged);

        assertEquals("always", overwritten.getNode("two", "fun").getValue());
        assertEquals("always", merged.getNode("two", "fun").getValue());
        assertNull(overwritten.getNode("two", "evil").getValue());
        assertEquals("always", merged.getNode("two", "evil").getValue());
    }

    private BasicConfigurationNode createMoveNode() {
        final BasicConfigurationNode ret = BasicConfigurationNode.root();
        ret.getNode("one", "fun").setValue("always");
        ret.getNode("two", "evil").setValue("always");
        return ret;
    }

    @Test
    void testCorrectNodePassed() {
        final BasicConfigurationNode node = BasicConfigurationNode.root();
        final BasicConfigurationNode child = node.getNode("childNode").setValue("something");
        ConfigurationTransformation.<BasicConfigurationNode>builder()
                .addAction(path("childNode"), (inputPath, valueAtPath) -> {
                    assertEquals(child, valueAtPath);
                    return null;
                }).build().apply(node);
    }

    @Test
    void testVersionedTransformation() {
        final BasicConfigurationNode target = BasicConfigurationNode.root();
        target.getNode("dummy").setValue("whatever");
        final List<Integer> updatedVersions = new ArrayList<>();
        this.<BasicConfigurationNode>buildVersionedTransformation(updatedVersions).apply(target);
        assertEquals(2, target.getNode("version").getInt());
        assertEquals(Arrays.asList(0, 1, 2), updatedVersions);
    }

    private <T extends ScopedConfigurationNode<T>> ConfigurationTransformation<T> buildVersionedTransformation(final List<Integer> updatedVersions) {
        return ConfigurationTransformation.<T>versionedBuilder()
                .addVersion(0, ConfigurationTransformation.<T>builder()
                        .addAction(path("dummy"), (inputPath, valueAtPath) -> {
                            updatedVersions.add(0);
                            return null;
                        }).build())
                .addVersion(2, ConfigurationTransformation.<T>builder()
                        .addAction(path("dummy"), (inputPath, valueAtPath) -> {
                            updatedVersions.add(2);
                            return null;
                        }).build())
                .addVersion(1, ConfigurationTransformation.<T>builder()
                        .addAction(path("dummy"), (inputPath, valueAtPath) -> {
                            updatedVersions.add(1);
                            return null;
                        }).build())
                .build();

    }

    @Test
    void testVersionedTransformationMoveChildToRoot() {
        final BasicConfigurationNode original = BasicConfigurationNode.root(b -> {
            b.getNode("test").act(t -> {
                t.getNode("calico").setValue("purr");
                t.getNode("sphynx").setValue("meow");
                t.getNode("russian-blue").setValue("mrow");
            });
        });
        final BasicConfigurationNode transformed = BasicConfigurationNode.root(b -> {
            b.getNode("calico").setValue("purr");
            b.getNode("sphynx").setValue("meow");
            b.getNode("russian-blue").setValue("mrow");
            b.getNode("version").setValue(1);
        });
        final ConfigurationTransformation.Versioned<BasicConfigurationNode> xform =
                ConfigurationTransformation.<BasicConfigurationNode>versionedBuilder()
                .makeVersion(1, version -> {
                    version.addAction(path("test"), (path, value) -> new Object[0]);
                    version.setMoveStrategy(MoveStrategy.MERGE);
                })
                .build();
        xform.apply(original);

        assertEquals(transformed, original);
    }

}
