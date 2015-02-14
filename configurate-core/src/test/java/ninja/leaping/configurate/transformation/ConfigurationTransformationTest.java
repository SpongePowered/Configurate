/**
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

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

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
            @Override
            public Object[] visitPath(ConfigurationTransformation.NodePath inputPath, ConfigurationNode valueAtPath) {
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
                p("a", "c", "c"),
                p("b", ConfigurationTransformation.WILDCARD_OBJECT, "d", ConfigurationTransformation.WILDCARD_OBJECT,
                        "f")
        ), populatedResults = new ArrayList<>(), expectedResult = Arrays.asList(
                p("a", "c", "c"),
                p("a", "b", "c"),
                p("a", "c", "c"),
                p("a", "d", "c"),
                p("b", "c", "d", "e", "f"),
                p("b", "c", "d", "f", "f"),
                p("b", "d", "d", "e", "f"),
                p("b", "d", "d", "f", "f")
        );

        final TransformAction action = new TransformAction() {
            @Override
            public Object[] visitPath(ConfigurationTransformation.NodePath path, ConfigurationNode valueAtPath) {
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
                    @Override
                    public Object[] visitPath(ConfigurationTransformation.NodePath inputPath, ConfigurationNode valueAtPath) {
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
}
