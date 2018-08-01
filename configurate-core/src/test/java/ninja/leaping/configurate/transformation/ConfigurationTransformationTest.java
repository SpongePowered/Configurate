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
package ninja.leaping.configurate.transformation;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.transformation.ConfigurationTransformation.NodePath;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigurationTransformationTest {
    private static Object[] p(Object... path) {
        return path;
    }

    @Test
    public void testComparator() {
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

        final TransformAction action = new TransformAction() {
            @Nullable
            @Override
            public Object[] visitPath(@NonNull NodePath inputPath, @NonNull ConfigurationNode valueAtPath) {
                autoSortedKeys.add(inputPath.getArray());
                return null;
            }
        };
        final ConfigurationTransformation.Builder build = ConfigurationTransformation.builder();
        for (Object[] path : unsortedKeys) {
            build.addAction(path, action);
        }
        ConfigurationNode node = SimpleConfigurationNode.root();
        for (Object[] path : unsortedKeys) {
            ConfigurationNode child = node.getNode(path);
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

        final TransformAction action = new TransformAction() {
            @Nullable
            @Override
            public Object[] visitPath(@NonNull NodePath path, @NonNull ConfigurationNode valueAtPath) {
                populatedResults.add(path.getArray());
                return null;
            }
        };
        final ConfigurationTransformation.Builder build = ConfigurationTransformation.builder();
        for (Object[] path : wildcardMatch) {
            build.addAction(path, action);
        }
        ConfigurationNode node = SimpleConfigurationNode.root();
        for (Object[] path : expectedResult) {
            node.getNode(path).setValue("lame");
        }
        build.build().apply(node);
        assertListOfArrayEquals(expectedResult, populatedResults);

    }

    @Test
    public void testMoveNode() {
        final ConfigurationTransformation transform = ConfigurationTransformation.builder()
                .addAction(p("old", "path"), new TransformAction() {
                    @Nullable
                    @Override
                    public Object[] visitPath(@NonNull NodePath inputPath, @NonNull ConfigurationNode valueAtPath) {
                        return p("new", "path");
                    }
                }).build();

        ConfigurationNode node = SimpleConfigurationNode.root();
        final Object nodeValue = new Object();
        node.getNode("old", "path").setValue(nodeValue);
        transform.apply(node);
        assertTrue(node.getNode("old", "path").isVirtual());
        assertEquals(nodeValue, node.getNode("new", "path").getValue());
    }

    @Test
    public void testChainedTransformations() {
        ConfigurationNode node = SimpleConfigurationNode.root();
        node.getNode("a").setValue("something?");
        final List<String> actualOutput = new ArrayList<>(), expectedOutput = ImmutableList.of("one", "two");
        ConfigurationTransformation.chain(ConfigurationTransformation.builder().addAction(p("a"), new TransformAction() {
            @Nullable
            @Override
            public Object[] visitPath(@NonNull NodePath inputPath, @NonNull ConfigurationNode valueAtPath) {
                actualOutput.add("one");
                return null;
            }
        }).build(), ConfigurationTransformation.builder().addAction(p("a"), new TransformAction() {
            @Nullable
            @Override
            public Object[] visitPath(@NonNull NodePath inputPath, @NonNull ConfigurationNode valueAtPath) {
                actualOutput.add("two");
                return null;
            }
        }).build()).apply(node);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testMoveToBase() {
        final ConfigurationTransformation transform = ConfigurationTransformation.builder()
                .addAction(p("sub"), new TransformAction() {
                    @Nullable
                    @Override
                    public Object[] visitPath(@NonNull NodePath inputPath, @NonNull ConfigurationNode valueAtPath) {
                        return new Object[0];
                    }
                }).build();

        ConfigurationNode node = SimpleConfigurationNode.root();
        node.getNode("sub", "key").setValue("value");
        node.getNode("at-parent").setValue("until-change");
        transform.apply(node);
        assertEquals("value", node.getNode("key").getValue());
        assertEquals(null, node.getNode("at-parent").getValue());
    }

    @Test
    public void testMoveStrategy() {
        final ConfigurationTransformation.Builder build = ConfigurationTransformation.builder()
                .addAction(p("one"), new TransformAction() {
                    @Nullable
                    @Override
                    public Object[] visitPath(@NonNull NodePath inputPath, @NonNull ConfigurationNode valueAtPath) {
                        return p("two");
                    }
                });
        ConfigurationNode overwritten = createMoveNode(), merged = createMoveNode();
        build.setMoveStrategy(MoveStrategy.OVERWRITE).build().apply(overwritten);
        build.setMoveStrategy(MoveStrategy.MERGE).build().apply(merged);

        assertEquals("always", overwritten.getNode("two", "fun").getValue());
        assertEquals("always", merged.getNode("two", "fun").getValue());
        assertEquals(null, overwritten.getNode("two", "evil").getValue());
        assertEquals("always", merged.getNode("two", "evil").getValue());
    }

    private ConfigurationNode createMoveNode() {
        ConfigurationNode ret = SimpleConfigurationNode.root();
        ret.getNode("one", "fun").setValue("always");
        ret.getNode("two", "evil").setValue("always");
        return ret;
    }

    @Test
    public void testCorrectNodePassed() {
        final ConfigurationNode node = SimpleConfigurationNode.root();
        final ConfigurationNode child = node.getNode("childNode").setValue("something");
        ConfigurationTransformation.builder()
                .addAction(p("childNode"), new TransformAction() {
                    @Nullable
                    @Override
                    public Object[] visitPath(@NonNull NodePath inputPath, @NonNull ConfigurationNode valueAtPath) {
                        assertEquals(child, valueAtPath);
                        return null;
                    }
                }).build().apply(node);
    }

    @Test
    public void testVersionedTransformation() {
        final ConfigurationNode target = SimpleConfigurationNode.root();
        target.getNode("dummy").setValue("whatever");
        final List<Integer> updatedVersions = new ArrayList<>();

        final ConfigurationTransformation versionTransform = ConfigurationTransformation.versionedBuilder()
                .addVersion(0, ConfigurationTransformation.builder()
                        .addAction(p("dummy"), new TransformAction() {
                            @Nullable
                            @Override
                            public Object[] visitPath(@NonNull NodePath inputPath, @NonNull ConfigurationNode valueAtPath) {
                                updatedVersions.add(0);
                                return null;
                            }
                        }).build())
                .addVersion(2, ConfigurationTransformation.builder()
                        .addAction(p("dummy"), new TransformAction() {
                            @Nullable
                            @Override
                            public Object[] visitPath(@NonNull NodePath inputPath, @NonNull ConfigurationNode valueAtPath) {
                                updatedVersions.add(2);
                                return null;
                            }
                        }).build())
                .addVersion(1, ConfigurationTransformation.builder()
                        .addAction(p("dummy"), new TransformAction() {
                            @Nullable
                            @Override
                            public Object[] visitPath(@NonNull NodePath inputPath, @NonNull ConfigurationNode valueAtPath) {
                                updatedVersions.add(1);
                                return null;
                            }
                        }).build())
                .build();
        versionTransform.apply(target);
        assertEquals(2, target.getNode("version").getInt());
        assertEquals(ImmutableList.of(0, 1, 2), updatedVersions);
    }
}
