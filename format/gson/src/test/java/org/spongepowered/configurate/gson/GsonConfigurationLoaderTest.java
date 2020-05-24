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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
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

/**
 * Basic sanity checks for the loader
 */
@ExtendWith(TempDirectory.class)
public class GsonConfigurationLoaderTest {

    @Test
    public void testSimpleLoading(final @TempDirectory.TempDir Path tempDir) throws IOException {
        final URL url = getClass().getResource("/example.json");
        final Path tempFile = tempDir.resolve("text1.txt");
        final ConfigurationLoader<BasicConfigurationNode> loader = GsonConfigurationLoader.builder()
                .setSource(() -> new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)))
                .setSink(AtomicFiles.createAtomicWriterFactory(tempFile, StandardCharsets.UTF_8)).setLenient(true).build();
        final ConfigurationNode node = loader.load(loader.defaultOptions().withMapFactory(MapFactories.sortedNatural()));
        assertEquals("unicorn", node.getNode("test", "op-level").getValue());
        assertEquals("dragon", node.getNode("other", "op-level").getValue());
        assertEquals("dog park", node.getNode("other", "location").getValue());
        assertTrue(node.getNode("int-val").getValue() instanceof Integer);
        assertTrue(node.getNode("double-val").getValue() instanceof Double);
        loader.save(node);
        assertEquals(Resources.readLines(url, StandardCharsets.UTF_8), Files.readAllLines(tempFile, StandardCharsets.UTF_8));
    }

    @Test
    public void testSavingEmptyFile(final @TempDirectory.TempDir Path tempDir) throws IOException {
        final File tempFile = tempDir.resolve("text2.txt").toFile();
        tempFile.createNewFile();

        final ConfigurationLoader<BasicConfigurationNode> loader = GsonConfigurationLoader.builder()
                .setFile(tempFile)
                .build();

        final BasicConfigurationNode n = BasicConfigurationNode.root();
        loader.save(n);
    }

    @Test
    public void testLoadingEmptyFile(final @TempDirectory.TempDir Path tempDir) throws IOException {
        final File tempFile = tempDir.resolve("text3.txt").toFile();
        tempFile.createNewFile();

        final ConfigurationLoader<BasicConfigurationNode> loader = GsonConfigurationLoader.builder()
                .setFile(tempFile)
                .build();

        loader.load();
    }

    @Test
    public void testLoadingFileWithEmptyObject(final @TempDirectory.TempDir Path tempDir) throws IOException {
        final URL url = getClass().getResource("/emptyObject.json");
        final Path tempFile = tempDir.resolve("text4.txt");
        final ConfigurationLoader<BasicConfigurationNode> loader = GsonConfigurationLoader.builder()
                .setSource(() -> new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)))
                .setSink(AtomicFiles.createAtomicWriterFactory(tempFile, StandardCharsets.UTF_8)).setLenient(true).build();

        final ConfigurationNode node = loader.load(loader.defaultOptions().withMapFactory(MapFactories.sortedNatural()));
        assertEquals(ImmutableMap.of(), node.getValue());
        assertTrue(node.isMap());
    }

    private static final long TEST_LONG_VAL = 584895858588588888L;

    @Test
    public void testRoundtrippingLong(final @TempDirectory.TempDir Path tempDir) throws IOException {
        final Path tempFile = tempDir.resolve("text5.txt");
        final ConfigurationLoader<BasicConfigurationNode> loader = GsonConfigurationLoader.builder().setPath(tempFile).build();
        final BasicConfigurationNode start = loader.createNode();
        start.getNode("long-num").setValue(TEST_LONG_VAL);
        loader.save(start);

        final BasicConfigurationNode ret = loader.load();
        assertEquals(TEST_LONG_VAL, ret.getNode("long-num").getValue());
    }

    @Test
    public void testPrimitiveTypes(final @TempDirectory.TempDir Path tempDir) throws IOException {
        final Path tempFile = tempDir.resolve("text6.txt");
        final GsonConfigurationLoader loader = GsonConfigurationLoader.builder().setPath(tempFile).build();
        final ConfigurationNode start = loader.createNode();

        final int ival = 452252;
        final long lval = 584895858588588888L;
        final float fval = 432.2234F;
        final double dval = 243.333333239413D;
        final boolean blval = true;
        final String stval = "Sphinx of black quartz, judge my vow";

        start.getNode("int").setValue(ival);
        start.getNode("long").setValue(lval);
        start.getNode("float").setValue(fval);
        start.getNode("double").setValue(dval);
        start.getNode("boolean").setValue(blval);
        start.getNode("string").setValue(stval);

        loader.save(start);

        final ConfigurationNode ret = loader.load();
        assertEquals(ival, ret.getNode("int").getValue());
        assertEquals(lval, ret.getNode("long").getValue());
        assertEquals(fval, (double) ret.getNode("float").getValue(), 0.05);
        assertEquals(dval, ret.getNode("double").getValue());
        assertEquals(blval, ret.getNode("boolean").getValue());
        assertEquals(stval, ret.getNode("string").getValue());
    }

}
