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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.loader.AtomicFiles;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.loader.ParsingException;
import org.spongepowered.configurate.util.MapFactories;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Basic sanity checks for the loader.
 */
class GsonConfigurationLoaderTest {

    @Test
    void testSimpleLoading(final @TempDir Path tempDir) throws IOException, ConfigurateException {
        final URL url = this.getClass().getResource("/example.json");
        final Path tempFile = tempDir.resolve("text1.txt");
        final ConfigurationLoader<BasicConfigurationNode> loader = GsonConfigurationLoader.builder()
                .source(() -> new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)))
                .sink(AtomicFiles.atomicWriterFactory(tempFile, StandardCharsets.UTF_8)).lenient(true).build();
        final ConfigurationNode node = loader.load(loader.defaultOptions().mapFactory(MapFactories.sortedNatural()));
        assertEquals("unicorn", node.node("test", "op-level").raw());
        assertEquals("dragon", node.node("other", "op-level").raw());
        assertEquals("dog park", node.node("other", "location").raw());
        assertTrue(node.node("int-val").raw() instanceof Integer);
        assertTrue(node.node("double-val").raw() instanceof Double);
        loader.save(node);
        assertEquals(Resources.readLines(url, StandardCharsets.UTF_8), Files.readAllLines(tempFile, StandardCharsets.UTF_8));
    }

    @Test
    void testSavingEmptyFile(final @TempDir Path tempDir) throws IOException, ConfigurateException {
        final File tempFile = tempDir.resolve("text2.txt").toFile();
        tempFile.createNewFile();

        final ConfigurationLoader<BasicConfigurationNode> loader = GsonConfigurationLoader.builder()
                .file(tempFile)
                .build();

        final BasicConfigurationNode n = BasicConfigurationNode.root();
        loader.save(n);
    }

    @Test
    void testLoadingEmptyFile(final @TempDir Path tempDir) throws IOException, ConfigurateException {
        final File tempFile = tempDir.resolve("text3.txt").toFile();
        tempFile.createNewFile();

        final ConfigurationLoader<BasicConfigurationNode> loader = GsonConfigurationLoader.builder()
                .file(tempFile)
                .build();

        loader.load();
    }

    @Test
    void testLoadingFileWithEmptyObject(final @TempDir Path tempDir) throws ConfigurateException {
        final URL url = this.getClass().getResource("/emptyObject.json");
        final Path tempFile = tempDir.resolve("text4.txt");
        final ConfigurationLoader<BasicConfigurationNode> loader = GsonConfigurationLoader.builder()
                .source(() -> new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)))
                .sink(AtomicFiles.atomicWriterFactory(tempFile, StandardCharsets.UTF_8)).lenient(true).build();

        final ConfigurationNode node = loader.load(loader.defaultOptions().mapFactory(MapFactories.sortedNatural()));
        assertEquals(ImmutableMap.of(), node.raw());
        assertTrue(node.isMap());
    }

    private static final long TEST_LONG_VAL = 584895858588588888L;

    @Test
    void testRoundtrippingLong(final @TempDir Path tempDir) throws ConfigurateException {
        final Path tempFile = tempDir.resolve("text5.txt");
        final ConfigurationLoader<BasicConfigurationNode> loader = GsonConfigurationLoader.builder().path(tempFile).build();
        final BasicConfigurationNode start = loader.createNode();
        start.node("long-num").raw(TEST_LONG_VAL);
        loader.save(start);

        final BasicConfigurationNode ret = loader.load();
        assertEquals(TEST_LONG_VAL, ret.node("long-num").raw());
    }

    @Test
    void testPrimitiveTypes(final @TempDir Path tempDir) throws ConfigurateException {
        final Path tempFile = tempDir.resolve("text6.txt");
        final GsonConfigurationLoader loader = GsonConfigurationLoader.builder().path(tempFile).build();
        final ConfigurationNode start = loader.createNode();

        final int ival = 452252;
        final long lval = 584895858588588888L;
        final float fval = 432.2234F;
        final double dval = 243.333333239413D;
        final boolean blval = true;
        final String stval = "Sphinx of black quartz, judge my vow";

        start.node("int").raw(ival);
        start.node("long").raw(lval);
        start.node("float").raw(fval);
        start.node("double").raw(dval);
        start.node("boolean").raw(blval);
        start.node("string").raw(stval);

        loader.save(start);

        final ConfigurationNode ret = loader.load();
        assertEquals(ival, ret.node("int").raw());
        assertEquals(lval, ret.node("long").raw());
        assertEquals(fval, (double) ret.node("float").raw(), 0.05);
        assertEquals(dval, ret.node("double").raw());
        assertEquals(blval, ret.node("boolean").raw());
        assertEquals(stval, ret.node("string").raw());
    }

    @Test
    void testWriteNonRootNode() throws ConfigurateException {
        // https://github.com/SpongePowered/Configurate/issues/163
        final ConfigurationNode source = BasicConfigurationNode.root(n -> {
            n.node("GriefPrevention", "claim-name", "text")
                    .raw("§4§9The §4T§6h§ea§2r§9o§5w §4Estate");
        });

        // Code from GriefDefender's ComponentConfigSerializer
        // https://github.com/bloodmc/GriefDefender/blob/26efaf2b7386f05c74566c4715dc7068b6c806d8/sponge/src/main/java/com/griefdefender/configuration/serializer/ComponentConfigSerializer.java#L58
        final StringWriter writer = new StringWriter();

        final GsonConfigurationLoader gsonLoader = GsonConfigurationLoader.builder()
                .indent(0)
                .sink(() -> new BufferedWriter(writer))
                .headerMode(HeaderMode.NONE)
                .build();

        gsonLoader.save(source.node("GriefPrevention", "claim-name"));

        assertEquals("{\"text\":\"§4§9The §4T§6h§ea§2r§9o§5w §4Estate\"}", writer.toString().trim());
    }

    @Test
    void testExceptionContainsInformation() {
        final GsonConfigurationLoader loader = GsonConfigurationLoader.builder()
                .source(() -> new BufferedReader(new StringReader("{\n\n\"hello\": \"wo}")))
                .build();

        final ParsingException ex = assertThrows(ParsingException.class, loader::load);
        assertEquals(3, ex.line());
        assertEquals(14, ex.column());
        assertEquals(NodePath.path("hello"), ex.path());
        assertTrue(ex.rawMessage().contains("Unterminated string"));
    }

}
