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
package org.spongepowered.configurate.jackson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.loader.AtomicFiles;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.loader.ParsingException;
import org.spongepowered.configurate.util.MapFactories;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
class JacksonConfigurationLoaderTest {

    @Test
    void testSimpleLoading(final @TempDir Path tempDir) throws IOException, ConfigurateException {
        final URL url = this.getClass().getResource("/example.json");
        final Path tempFile = tempDir.resolve("text1.txt");
        final ConfigurationLoader<? extends ConfigurationNode> loader = JacksonConfigurationLoader.builder()
                .source(() -> new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)))
                        .sink(AtomicFiles.atomicWriterFactory(tempFile, StandardCharsets.UTF_8)).build();
        final ConfigurationNode node = loader.load(ConfigurationOptions.defaults().mapFactory(MapFactories.sortedNatural()));
        assertEquals("unicorn", node.node("test", "op-level").raw());
        assertEquals("dragon", node.node("other", "op-level").raw());
        assertEquals("dog park", node.node("other", "location").raw());

        loader.save(node);
        assertEquals(Resources.readLines(url, StandardCharsets.UTF_8), Files
                .readAllLines(tempFile, StandardCharsets.UTF_8));

    }

    private static final long TEST_LONG_VAL = 584895858588588888L;
    private static final double TEST_DOUBLE_VAL = 5.95859682984429e53d;

    private void testRoundtripValue(final Path tempDir, final Object value) throws ConfigurateException {
        final Path tempFile = tempDir.resolve("text2.txt");
        final ConfigurationLoader<? extends ConfigurationNode> loader = JacksonConfigurationLoader.builder().path(tempFile).build();
        final ConfigurationNode start = loader.createNode();
        start.node("value").raw(value);
        loader.save(start);

        final ConfigurationNode ret = loader.load();
        assertEquals(value, ret.node("value").raw());
    }

    @Test
    void testRoundtrippingLong(final @TempDir Path tempDir) throws ConfigurateException {
        this.testRoundtripValue(tempDir, TEST_LONG_VAL);
    }

    @Test
    void testRoundtripDouble(final @TempDir Path tempDir) throws ConfigurateException {
        this.testRoundtripValue(tempDir, TEST_DOUBLE_VAL);
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

        final JacksonConfigurationLoader jacksonLoader = JacksonConfigurationLoader.builder()
                .indent(0)
                .fieldValueSeparatorStyle(FieldValueSeparatorStyle.NO_SPACE)
                .sink(() -> new BufferedWriter(writer))
                .headerMode(HeaderMode.NONE)
                .build();

        jacksonLoader.save(source.node("GriefPrevention", "claim-name"));

        assertEquals("{\"text\":\"§4§9The §4T§6h§ea§2r§9o§5w §4Estate\"}", writer.toString().trim());
    }

    @Test
    void testExceptionContainsInformation() {
        final JacksonConfigurationLoader loader = JacksonConfigurationLoader.builder()
                .source(() -> new BufferedReader(new StringReader("{\n\n\"hello\": \"wo}")))
                .build();

        final ParsingException ex = assertThrows(ParsingException.class, loader::load);
        assertEquals(3, ex.line());
        assertEquals(14, ex.column());
        assertEquals(NodePath.path("hello"), ex.path());
        assertTrue(ex.rawMessage().contains("Unexpected end-of-input"));
    }

}
