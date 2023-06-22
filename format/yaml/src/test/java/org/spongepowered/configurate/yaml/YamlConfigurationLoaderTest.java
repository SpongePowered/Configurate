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
package org.spongepowered.configurate.yaml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.leangen.geantyref.TypeToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Basic sanity checks for the loader.
 */
class YamlConfigurationLoaderTest {

    @Test
    void testSimpleLoading() throws ConfigurateException {
        final URL url = this.getClass().getResource("/example.yml");
        final ConfigurationLoader<CommentedConfigurationNode> loader = YamlConfigurationLoader.builder()
                .url(url).build();
        final ConfigurationNode node = loader.load();
        assertEquals("unicorn", node.node("test", "op-level").raw());
        assertEquals("dragon", node.node("other", "op-level").raw());
        assertEquals("dog park", node.node("other", "location").raw());


        final List<Map<String, List<String>>> fooList = new ArrayList<>(node.node("foo")
            .getList(new TypeToken<Map<String, List<String>>>() {}));
        assertEquals(0, fooList.get(0).get("bar").size());
    }

    @Test
    void testReadWithTabs() throws ConfigurateException {
        final ConfigurationNode expected = CommentedConfigurationNode.root(n -> {
            n.node("document").act(d -> {
                d.node("we").raw("support tabs");
                d.node("and").raw("literal tabs\tin strings");
                d.node("with").act(w -> {
                    w.appendListNode().raw("more levels");
                    w.appendListNode().raw("of indentation");
                });
            });
        });

        final URL url = this.getClass().getResource("/tab-example.yml");
        final ConfigurationLoader<CommentedConfigurationNode> loader = YamlConfigurationLoader.builder()
                .url(url).build();
        final ConfigurationNode node = loader.load();
        assertEquals(expected, node);
    }

    @Test
    void testWriteBasicFile(final @TempDir Path tempDir) throws ConfigurateException, IOException {
        final Path target = tempDir.resolve("write-basic.yml");
        final ConfigurationNode node = BasicConfigurationNode.root(n -> {
            n.node("mapping", "first").set("hello");
            n.node("mapping", "second").set("world");

            n.node("list").act(c -> {
                c.appendListNode().set(1);
                c.appendListNode().set(2);
                c.appendListNode().set(3);
                c.appendListNode().set(4);
            });
        });

        final YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(target)
                .nodeStyle(NodeStyle.BLOCK)
                .build();

        loader.save(node);

        assertEquals(readLines(this.getClass().getResource("write-expected.yml")), Files.readAllLines(target, StandardCharsets.UTF_8));
    }

    @Test
    void testWriteComments(final @TempDir Path tempDir) throws IOException {
        final Path target = tempDir.resolve("comments-actual.yml");
        final ConfigurationNode node = CommentedConfigurationNode.root(n ->
            n.node("pizza")
                .comment("john's")
                .act(p ->
                    p.node("pineapple")
                        .set(true)
                        .comment("who doesn't love a good pineapple\non a pizza??")));

        final YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
            .path(target)
            .nodeStyle(NodeStyle.BLOCK)
            .build();

        loader.save(node);

        assertEquals(
            readLines(this.getClass().getResource("comments-expected.yml")),
            Files.readAllLines(target, StandardCharsets.UTF_8)
        );
    }

    private static List<String> readLines(final URL source) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(source.openStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.toList());
        }
    }

}
