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
package org.spongepowered.configurate.gson;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.SimpleConfigurationNode;
import org.spongepowered.configurate.loader.AtomicFiles;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.util.MapFactories;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Basic sanity checks for the loader
 */
@ExtendWith(TempDirectory.class)
public class GsonConfigurationLoaderTest {

    @Test
    public void testSimpleLoading(@TempDirectory.TempDir Path tempDir) throws IOException {
        URL url = getClass().getResource("/example.json");
        final Path tempFile = tempDir.resolve("text1.txt");
        ConfigurationLoader<SimpleConfigurationNode> loader = GsonConfigurationLoader.builder()
                .setSource(() -> new BufferedReader(new InputStreamReader(url.openStream())))
                .setSink(AtomicFiles.createAtomicWriterFactory(tempFile, StandardCharsets.UTF_8)).setLenient(true).build();
        ConfigurationNode<?> node = loader.load(loader.getDefaultOptions().withMapFactory(MapFactories.sortedNatural()));
        assertEquals("unicorn", node.getNode("test", "op-level").getValue());
        assertEquals("dragon", node.getNode("other", "op-level").getValue());
        assertEquals("dog park", node.getNode("other", "location").getValue());
        assertTrue(node.getNode("int-val").getValue() instanceof Integer);
        assertTrue(node.getNode("double-val").getValue() instanceof Double);
        loader.save(node);
        assertEquals(Resources.readLines(url, StandardCharsets.UTF_8), Files.readAllLines(tempFile, StandardCharsets.UTF_8));
    }

    @Test
    public void testSavingEmptyFile(@TempDirectory.TempDir Path tempDir) throws IOException {
        final File tempFile = tempDir.resolve("text2.txt").toFile();
        tempFile.createNewFile();

        ConfigurationLoader<SimpleConfigurationNode> loader = GsonConfigurationLoader.builder()
                .setFile(tempFile)
                .build();

        ConfigurationNode<?> n = SimpleConfigurationNode.root();
        loader.save(n);
    }

    @Test
    public void testLoadingEmptyFile(@TempDirectory.TempDir Path tempDir) throws IOException {
        final File tempFile = tempDir.resolve("text3.txt").toFile();
        tempFile.createNewFile();

        ConfigurationLoader<SimpleConfigurationNode> loader = GsonConfigurationLoader.builder()
                .setFile(tempFile)
                .build();

        loader.load();
    }

    @Test
    public void testLoadingFileWithEmptyObject(@TempDirectory.TempDir Path tempDir) throws IOException {
        URL url = getClass().getResource("/emptyObject.json");
        final Path tempFile = tempDir.resolve("text4.txt");
        ConfigurationLoader<SimpleConfigurationNode> loader = GsonConfigurationLoader.builder()
                .setSource(() -> new BufferedReader(new InputStreamReader(url.openStream())))
                .setSink(AtomicFiles.createAtomicWriterFactory(tempFile, StandardCharsets.UTF_8)).setLenient(true).build();

        ConfigurationNode<?> node = loader.load(loader.getDefaultOptions().withMapFactory(MapFactories.sortedNatural()));
        assertEquals(ImmutableMap.of(), node.getValue());
        assertTrue(node.hasMapChildren());
    }

    private static final long TEST_LONG_VAL = 584895858588588888l;

    @Test
    @Disabled("Gson currently makes it rather difficult to get the correct number type")
    public void testRoundtrippingLong(@TempDirectory.TempDir Path tempDir) throws IOException {
        final Path tempFile = tempDir.resolve("text5.txt");
        ConfigurationLoader<SimpleConfigurationNode> loader = GsonConfigurationLoader.builder().setPath(tempFile).build();
        ConfigurationNode<?> start = loader.createEmptyNode();
        start.getNode("long-num").setValue(TEST_LONG_VAL);
        loader.save(start);
        System.out.println(Files.readAllLines(tempFile));

        ConfigurationNode<?> ret = loader.load();
        System.out.println(ret.getNode("long-num").getValue().getClass());
        assertEquals(TEST_LONG_VAL, ret.getNode("long-num").getValue());
    }
}
