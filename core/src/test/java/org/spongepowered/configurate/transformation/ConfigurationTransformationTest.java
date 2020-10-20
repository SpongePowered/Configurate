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
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigurationTransformationTest {

    private static Object[] arr(final Object... path) {
        return path;
    }

    @Test
    void testComparator() throws ConfigurateException {
        doTestComparator(BasicConfigurationNode.root());
    }

    private void doTestComparator(final ConfigurationNode node) throws ConfigurateException {
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

        final TransformAction action = (inputPath, valueAtPath) -> {
            autoSortedKeys.add(inputPath.copy());
            return null;
        };

        final ConfigurationTransformation.Builder build = ConfigurationTransformation.builder();
        for (NodePath path : unsortedKeys) {
            build.addAction(path, action);
        }
        for (NodePath path : unsortedKeys) {
            final ConfigurationNode child = node.node(path);
            if (child.virtual()) {
                child.raw("meaningless test value");
            }
        }
        build.build().apply(node);
        assertEquals(expectedSortedKeys, autoSortedKeys);
    }

    @Test
    void testWildcardMatching() throws ConfigurateException {
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

        final TransformAction action = (path, valueAthPath) -> {
            populatedResults.add(path.copy());
            return null;
        };
        final ConfigurationTransformation.Builder build = ConfigurationTransformation.builder();
        for (NodePath path : wildcardMatch) {
            build.addAction(path, action);
        }
        for (NodePath path : expectedResult) {
            node.node(path).raw("lame");
        }
        build.build().apply(node);
        assertEquals(expectedResult, populatedResults);
    }

    @Test
    void testMoveNode() throws ConfigurateException {

        final BasicConfigurationNode node = BasicConfigurationNode.root();
        final Object nodeValue = new Object();
        node.node("old", "path").raw(nodeValue);
        ConfigurationTransformation.builder()
                .addAction(path("old", "path"),
                    (inputPath, valueAtPath) -> arr("new", "path"))
                .build().apply(node);
        assertTrue(node.node("old", "path").virtual());
        assertEquals(nodeValue, node.node("new", "path").raw());
    }

    @Test
    void testChainedTransformations() throws ConfigurateException {
        final BasicConfigurationNode node = BasicConfigurationNode.root();
        node.node("a").raw("something?");
        final List<String> actualOutput = new ArrayList<>();
        final List<String> expectedOutput = Arrays.asList("one", "two");
        transformChained(actualOutput, node);
        assertEquals(expectedOutput, actualOutput);
    }

    private void transformChained(final List<String> actualOutput, final ConfigurationNode node)
            throws ConfigurateException {
        ConfigurationTransformation.chain(ConfigurationTransformation.builder().addAction(path("a"), (inputPath, valueAtPath) -> {
            actualOutput.add("one");
            return null;
        }).build(), ConfigurationTransformation.builder().addAction(path("a"), (inputPath, valueAtPath) -> {
            actualOutput.add("two");
            return null;
        }).build()).apply(node);
    }

    @Test
    void testMoveToBase() throws ConfigurateException {
        final BasicConfigurationNode node = BasicConfigurationNode.root();
        node.node("sub", "key").raw("value");
        node.node("at-parent").raw("until-change");
        transformMoveToBase(node);
        assertEquals("value", node.node("key").raw());
        assertNull(node.node("at-parent").raw());
    }

    private void transformMoveToBase(final ConfigurationNode node) throws ConfigurateException {
        ConfigurationTransformation.builder()
                .addAction(path("sub"), (inputPath, valueAtPath) -> {
                    return new Object[0];
                }).build().apply(node);
    }

    @Test
    void testMoveStrategy() throws ConfigurateException {
        final ConfigurationTransformation.Builder build = ConfigurationTransformation.builder()
                .addAction(path("one"), (inputPath, valueAtPath) -> arr("two"));
        final BasicConfigurationNode overwritten = createMoveNode();
        final BasicConfigurationNode merged = createMoveNode();
        build.moveStrategy(MoveStrategy.OVERWRITE).build().apply(overwritten);
        build.moveStrategy(MoveStrategy.MERGE).build().apply(merged);

        assertEquals("always", overwritten.node("two", "fun").raw());
        assertEquals("always", merged.node("two", "fun").raw());
        assertNull(overwritten.node("two", "evil").raw());
        assertEquals("always", merged.node("two", "evil").raw());
    }

    private BasicConfigurationNode createMoveNode() {
        final BasicConfigurationNode ret = BasicConfigurationNode.root();
        ret.node("one", "fun").raw("always");
        ret.node("two", "evil").raw("always");
        return ret;
    }

    @Test
    void testCorrectNodePassed() throws ConfigurateException {
        final BasicConfigurationNode node = BasicConfigurationNode.root();
        final BasicConfigurationNode child = node.node("childNode").raw("something");
        ConfigurationTransformation.builder()
                .addAction(path("childNode"), (inputPath, valueAtPath) -> {
                    assertEquals(child, valueAtPath);
                    return null;
                }).build().apply(node);
    }

    @Test
    void testVersionedTransformation() throws ConfigurateException {
        final BasicConfigurationNode target = BasicConfigurationNode.root();
        target.node("dummy").raw("whatever");
        final List<Integer> updatedVersions = new ArrayList<>();
        buildVersionedTransformation(updatedVersions).apply(target);
        assertEquals(2, target.node("version").getInt());
        assertEquals(Arrays.asList(0, 1, 2), updatedVersions);
    }

    private ConfigurationTransformation buildVersionedTransformation(final List<Integer> updatedVersions) {
        return ConfigurationTransformation.versionedBuilder()
                .addVersion(0, ConfigurationTransformation.builder()
                        .addAction(path("dummy"), (inputPath, valueAtPath) -> {
                            updatedVersions.add(0);
                            return null;
                        }).build())
                .addVersion(2, ConfigurationTransformation.builder()
                        .addAction(path("dummy"), (inputPath, valueAtPath) -> {
                            updatedVersions.add(2);
                            return null;
                        }).build())
                .addVersion(1, ConfigurationTransformation.builder()
                        .addAction(path("dummy"), (inputPath, valueAtPath) -> {
                            updatedVersions.add(1);
                            return null;
                        }).build())
                .build();

    }

    @Test
    void testVersionedTransformationMoveChildToRoot() throws ConfigurateException {
        final BasicConfigurationNode original = BasicConfigurationNode.root(b -> {
            b.node("test").act(t -> {
                t.node("calico").raw("purr");
                t.node("sphynx").raw("meow");
                t.node("russian-blue").raw("mrow");
            });
        });
        final BasicConfigurationNode transformed = BasicConfigurationNode.root(b -> {
            b.node("calico").raw("purr");
            b.node("sphynx").raw("meow");
            b.node("russian-blue").raw("mrow");
            b.node("version").raw(1);
        });
        final ConfigurationTransformation.Versioned xform =
                ConfigurationTransformation.versionedBuilder()
                .makeVersion(1, version -> {
                    version.addAction(path("test"), (path, value) -> new Object[0]);
                    version.moveStrategy(MoveStrategy.MERGE);
                })
                .build();
        xform.apply(original);

        assertEquals(transformed, original);
    }

}
