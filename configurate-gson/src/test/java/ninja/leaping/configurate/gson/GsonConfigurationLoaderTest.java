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
package ninja.leaping.configurate.gson;

import com.google.common.io.Resources;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.AtomicFiles;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.loader.HeaderMode;
import ninja.leaping.configurate.util.MapFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        ConfigurationLoader<ConfigurationNode> loader = GsonConfigurationLoader.builder()
                .setSource(() -> new BufferedReader(new InputStreamReader(url.openStream())))
                .setSink(AtomicFiles.createAtomicWriterFactory(tempFile, UTF_8)).setLenient(true).build();
        ConfigurationNode node = loader.load(loader.getDefaultOptions().withMapFactory(MapFactories.sortedNatural()));
        assertEquals("unicorn", node.getNode("test", "op-level").getValue());
        assertEquals("dragon", node.getNode("other", "op-level").getValue());
        assertEquals("dog park", node.getNode("other", "location").getValue());
        assertTrue(node.getNode("int-val").getValue() instanceof Integer);
        assertTrue(node.getNode("double-val").getValue() instanceof Double);
        loader.save(node);
        assertEquals(Resources.readLines(url, UTF_8), Files.readAllLines(tempFile, UTF_8));
    }

    @Test
    public void testSavingEmptyFile(@TempDirectory.TempDir Path tempDir) throws IOException {
        final File tempFile = tempDir.resolve("text2.txt").toFile();
        tempFile.createNewFile();

        ConfigurationLoader<ConfigurationNode> loader = GsonConfigurationLoader.builder()
                .setFile(tempFile)
                .build();

        ConfigurationNode n = ConfigurationNode.root();
        loader.save(n);
    }

    @Test
    public void testLoadingEmptyFile(@TempDirectory.TempDir Path tempDir) throws IOException {
        final File tempFile = tempDir.resolve("text3.txt").toFile();
        tempFile.createNewFile();

        ConfigurationLoader<ConfigurationNode> loader = GsonConfigurationLoader.builder()
                .setFile(tempFile)
                .build();

        loader.load();
    }

    @Test
    public void testLoadingFileWithEmptyObject(@TempDirectory.TempDir Path tempDir) throws IOException {
        URL url = getClass().getResource("/emptyObject.json");
        final Path tempFile = tempDir.resolve("text4.txt");
        ConfigurationLoader<ConfigurationNode> loader = GsonConfigurationLoader.builder()
                .setSource(() -> new BufferedReader(new InputStreamReader(url.openStream())))
                .setSink(AtomicFiles.createAtomicWriterFactory(tempFile, UTF_8)).setLenient(true).build();

        ConfigurationNode node = loader.load(loader.getDefaultOptions().withMapFactory(MapFactories.sortedNatural()));
        assertNull(node.getValue());
        assertFalse(node.isMap());
    }

    private static final long TEST_LONG_VAL = 584895858588588888l;

    @Test
    public void testRoundtrippingLong(@TempDirectory.TempDir Path tempDir) throws IOException {
        final Path tempFile = tempDir.resolve("text5.txt");
        ConfigurationLoader<ConfigurationNode> loader = GsonConfigurationLoader.builder().setPath(tempFile).build();
        ConfigurationNode start = loader.createEmptyNode();
        start.getNode("long-num").setValue(TEST_LONG_VAL);
        loader.save(start);

        ConfigurationNode ret = loader.load();
        assertEquals(TEST_LONG_VAL, ret.getNode("long-num").getValue());
    }

    @Test
    public void testPrimitiveTypes(@TempDirectory.TempDir Path tempDir) throws IOException {
        final Path tempFile = tempDir.resolve("text6.txt");
        GsonConfigurationLoader loader = GsonConfigurationLoader.builder().setPath(tempFile).build();
        ConfigurationNode start = loader.createEmptyNode();

        int ival = 452252;
        long lval = 584895858588588888L;
        float fval = 432.2234F;
        double dval = 243.333333239413D;
        boolean blval = true;
        String stval = "Sphinx of black quartz, judge my vow";

        start.getNode("int").setValue(ival);
        start.getNode("long").setValue(lval);
        start.getNode("float").setValue(fval);
        start.getNode("double").setValue(dval);
        start.getNode("boolean").setValue(blval);
        start.getNode("string").setValue(stval);

        loader.save(start);

        ConfigurationNode ret = loader.load();
        assertEquals(ival, ret.getNode("int").getValue());
        assertEquals(lval, ret.getNode("long").getValue());
        assertEquals(fval, (double)ret.getNode("float").getValue(), 0.05);
        assertEquals(dval, ret.getNode("double").getValue());
        assertEquals(blval, ret.getNode("boolean").getValue());
        assertEquals(stval, ret.getNode("string").getValue());
    }

    @Test
    void testWriteNonRootNode() throws IOException {
        // https://github.com/SpongePowered/Configurate/issues/163
        final ConfigurationNode source = ConfigurationNode.root(n -> {
            n.getNode("GriefPrevention", "claim-name", "text")
                    .setValue("§4§9The §4T§6h§ea§2r§9o§5w §4Estate");
        });

        // Code from GriefDefender's ComponentConfigSerializer
        // https://github.com/bloodmc/GriefDefender/blob/26efaf2b7386f05c74566c4715dc7068b6c806d8/sponge/src/main/java/com/griefdefender/configuration/serializer/ComponentConfigSerializer.java#L58
        StringWriter writer = new StringWriter();

        GsonConfigurationLoader gsonLoader = GsonConfigurationLoader.builder()
                .setIndent(0)
                .setSink(() -> new BufferedWriter(writer))
                .setHeaderMode(HeaderMode.NONE)
                .build();

        gsonLoader.save(source.getNode("GriefPrevention", "claim-name"));
    }
}
