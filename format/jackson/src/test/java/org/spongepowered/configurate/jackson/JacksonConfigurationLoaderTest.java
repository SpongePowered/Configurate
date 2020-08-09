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

import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.AtomicFiles;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.util.MapFactories;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Basic sanity checks for the loader
 */
public class JacksonConfigurationLoaderTest {

    @Test
    public void testSimpleLoading(final @TempDir Path tempDir) throws IOException {
        final URL url = getClass().getResource("/example.json");
        final Path tempFile = tempDir.resolve("text1.txt");
        final ConfigurationLoader<? extends ConfigurationNode> loader = JacksonConfigurationLoader.builder()
                .setSource(() -> new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)))
                        .setSink(AtomicFiles.createAtomicWriterFactory(tempFile, StandardCharsets.UTF_8)).build();
        final ConfigurationNode node = loader.load(ConfigurationOptions.defaults().withMapFactory(MapFactories.sortedNatural()));
        assertEquals("unicorn", node.getNode("test", "op-level").getValue());
        assertEquals("dragon", node.getNode("other", "op-level").getValue());
        assertEquals("dog park", node.getNode("other", "location").getValue());

        loader.save(node);
        assertEquals(Resources.readLines(url, StandardCharsets.UTF_8), Files
                .readAllLines(tempFile, StandardCharsets.UTF_8));

    }

    private static final long TEST_LONG_VAL = 584895858588588888L;
    private static final double TEST_DOUBLE_VAL = 5.95859682984429e53d;

    private void testRoundtripValue(final Path tempDir, final Object value) throws IOException {
        final Path tempFile = tempDir.resolve("text2.txt");
        final ConfigurationLoader<? extends ConfigurationNode> loader = JacksonConfigurationLoader.builder().setPath(tempFile).build();
        final ConfigurationNode start = loader.createNode();
        start.getNode("value").setValue(value);
        loader.save(start);

        final ConfigurationNode ret = loader.load();
        assertEquals(value, ret.getNode("value").getValue());
    }

    @Test
    public void testRoundtrippingLong(final @TempDir Path tempDir) throws IOException {
        testRoundtripValue(tempDir, TEST_LONG_VAL);
    }

    @Test
    public void testRoundtripDouble(final @TempDir Path tempDir) throws IOException {
        testRoundtripValue(tempDir, TEST_DOUBLE_VAL);
    }

    @Test
    void testWriteNonRootNode() throws IOException {
        // https://github.com/SpongePowered/Configurate/issues/163
        final ConfigurationNode source = BasicConfigurationNode.root(n -> {
            n.getNode("GriefPrevention", "claim-name", "text")
                    .setValue("§4§9The §4T§6h§ea§2r§9o§5w §4Estate");
        });

        // Code from GriefDefender's ComponentConfigSerializer
        // https://github.com/bloodmc/GriefDefender/blob/26efaf2b7386f05c74566c4715dc7068b6c806d8/sponge/src/main/java/com/griefdefender/configuration/serializer/ComponentConfigSerializer.java#L58
        final StringWriter writer = new StringWriter();

        final JacksonConfigurationLoader jacksonLoader = JacksonConfigurationLoader.builder()
                .setIndent(0)
                .setFieldValueSeparatorStyle(FieldValueSeparatorStyle.NO_SPACE)
                .setSink(() -> new BufferedWriter(writer))
                .setHeaderMode(HeaderMode.NONE)
                .build();

        jacksonLoader.save(source.getNode("GriefPrevention", "claim-name"));

        assertEquals("{\"text\":\"§4§9The §4T§6h§ea§2r§9o§5w §4Estate\"}", writer.toString().trim());
    }

}
