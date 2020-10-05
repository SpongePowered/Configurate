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
package org.spongepowered.configurate.hocon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.AtomicFiles;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic sanity checks for the loader.
 */
@SuppressWarnings("UnusedVariable")
public class HoconConfigurationLoaderTest {

    @Test
    void testSimpleLoading(final @TempDir Path tempDir) throws ConfigurateException, IOException {
        final URL url = getClass().getResource("/example.conf");
        final Path saveTest = tempDir.resolve("text1.txt");

        final HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                .source(() -> new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)))
                .sink(AtomicFiles.atomicWriterFactory(saveTest, StandardCharsets.UTF_8)).build();
        final CommentedConfigurationNode node = loader.load();
        assertEquals("unicorn", node.node("test", "op-level").raw());
        assertEquals("dragon", node.node("other", "op-level").raw());
        final CommentedConfigurationNode testNode = node.node("test");
        assertEquals(" Test node", testNode.comment());
        assertEquals("dog park", node.node("other", "location").raw());
        loader.save(node);
        assertEquals(Resources.readLines(getClass().getResource("/roundtrip-test.conf"), StandardCharsets.UTF_8), Files
                .readAllLines(saveTest, StandardCharsets.UTF_8));
    }

    @Test
    void testSplitLineCommentInput(final @TempDir Path tempDir) throws ConfigurateException, IOException {
        final Path saveTo = tempDir.resolve("text2.txt");
        final HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                .path(saveTo)
                .url(getClass().getResource("/splitline-comment-input.conf"))
                .build();
        final CommentedConfigurationNode node = loader.load();
        loader.save(node);

        assertEquals(Resources.readLines(getClass().getResource("/splitline-comment-output.conf"), StandardCharsets.UTF_8),
                Files.readAllLines(saveTo, StandardCharsets.UTF_8));
    }

    @Test
    void testHeaderSaved(final @TempDir Path tempDir) throws IOException, ConfigurateException {
        final Path saveTo = tempDir.resolve("text3.txt");
        final HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                .path(saveTo)
                .build();
        final CommentedConfigurationNode node = loader.createNode(ConfigurationOptions.defaults().header("Hi! I am a header!\n"
                        + "Look at meeeeeee!!!"));
        node.node("node").comment("I have a comment").node("party").set("now");

        loader.save(node);
        assertEquals(Resources.readLines(getClass().getResource("/header.conf"), StandardCharsets.UTF_8),
                Files.readAllLines(saveTo, StandardCharsets.UTF_8));

    }

    @Test
    void testBooleansNotShared(final @TempDir Path tempDir) throws IOException, ConfigurateException {
        final URL url = getClass().getResource("/comments-test.conf");
        final Path saveTo = tempDir.resolve("text4.txt");
        final HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                .path(saveTo).url(url).build();

        final CommentedConfigurationNode node = loader.createNode(ConfigurationOptions.defaults());
        node.node("test", "third").set(false).comment("really?");
        node.node("test", "apple").comment("fruit").set(false);
        node.node("test", "donut").set(true).comment("tasty");
        node.node("test", "guacamole").set(true).comment("and chips?");

        loader.save(node);
        assertEquals(Resources.readLines(url, StandardCharsets.UTF_8), Files.readAllLines(saveTo, StandardCharsets.UTF_8));
    }

    @Test
    void testNewConfigObject() {
        final Map<String, ConfigValue> entries = ImmutableMap.of("a", ConfigValueFactory.fromAnyRef("hi"), "b", ConfigValueFactory.fromAnyRef("bye"));
        HoconConfigurationLoader.newConfigObject(entries);
    }

    @Test
    void testNewConfigList() {
        final List<ConfigValue> entries = Arrays.asList(ConfigValueFactory.fromAnyRef("hello"), ConfigValueFactory.fromAnyRef("goodbye"));
        HoconConfigurationLoader.newConfigList(entries);
    }

    @Test
    void testRoundtripAndMergeEmpty(final @TempDir Path tempDir) throws IOException, ConfigurateException {
        // https://github.com/SpongePowered/Configurate/issues/44
        final URL rsrc = getClass().getResource("/empty-values.conf");
        final Path output = tempDir.resolve("load-merge-empty.conf");
        final HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                .path(output)
                .url(rsrc).build();

        final CommentedConfigurationNode source = loader.load();
        final CommentedConfigurationNode destination = loader.createNode();
        destination.mergeFrom(source);
        loader.save(source);
        assertLinesMatch(Resources.readLines(rsrc, StandardCharsets.UTF_8), Files.readAllLines(output, StandardCharsets.UTF_8));
        loader.save(destination);
        assertLinesMatch(Resources.readLines(rsrc, StandardCharsets.UTF_8), Files.readAllLines(output, StandardCharsets.UTF_8));
    }

    static class OuterConfig {
        static Class<OuterConfig> TYPE = OuterConfig.class;
        @Setting
        private Section section = new Section();
    }

    @ConfigSerializable
    static class Section {
        @Setting
        private Map<String, String> aliases = new HashMap<>();
    }

    @Test
    void testCreateEmptyObjectmappingSection(final @TempDir Path tempDir) throws IOException, ConfigurateException {
        // https://github.com/SpongePowered/Configurate/issues/40
        final URL rsrc = getClass().getResource("/empty-section.conf");
        final Path output = tempDir.resolve("empty-section.conf");
        final HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                .defaultOptions(o -> o.shouldCopyDefaults(true))
                .path(output)
                .url(rsrc).build();

        final CommentedConfigurationNode source = loader.createNode();
        ObjectMapper.factory().get(OuterConfig.TYPE).load(source);
        loader.save(source);
        assertLinesMatch(Resources.readLines(rsrc, StandardCharsets.UTF_8), Files.readAllLines(output, StandardCharsets.UTF_8));
    }

}
