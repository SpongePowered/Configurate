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
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.loader.AtomicFiles;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.util.MapFactories;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
        ConfigurationNode node = loader.load(loader.getDefaultOptions().setMapFactory(MapFactories.sortedNatural()));
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

        ConfigurationNode n = SimpleConfigurationNode.root();
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

        ConfigurationNode node = loader.load(loader.getDefaultOptions().setMapFactory(MapFactories.sortedNatural()));
        assertNull(node.getValue());
        assertFalse(node.hasMapChildren());
    }

    @Test
    public void testHugeLong(@TempDirectory.TempDir Path tempDir) throws IOException {
        final Path tempFile = tempDir.resolve("text5.txt");
        ConfigurationLoader<ConfigurationNode> loader = GsonConfigurationLoader.builder().setPath(tempFile).build();
        ConfigurationNode start = loader.createEmptyNode();
        start.getNode("long-num").setValue(Long.MAX_VALUE);
        loader.save(start);
        System.out.println(Files.readAllLines(tempFile));

        ConfigurationNode ret = loader.load();
        System.out.println(ret.getNode("long-num").getValue().getClass());
        assertEquals(Long.MAX_VALUE, ret.getNode("long-num").getValue());
    }

    @Test
    public void testPrimitiveTypes(@TempDirectory.TempDir Path tempDir) throws IOException {
        final Path tempFile = tempDir.resolve("text6.txt");
        ConfigurationLoader<ConfigurationNode> loader = GsonConfigurationLoader.builder().setPath(tempFile).build();
        ConfigurationNode start = loader.createEmptyNode();

        byte btval = 56;
        short shval = 1552;
        int ival = 452252;
        long lval = 584895858588588888L;
        float fval = 432.2234F;
        double dval = 243.333333239413D;
        boolean blval = true;
        String stval = "Sphinx of black quartz, judge my vow";

        start.getNode("byte").setValue(btval);
        start.getNode("short").setValue(shval);
        start.getNode("int").setValue(ival);
        start.getNode("long").setValue(lval);
        start.getNode("float").setValue(fval);
        start.getNode("double").setValue(dval);
        start.getNode("boolean").setValue(blval);
        start.getNode("string").setValue(stval);

        loader.save(start);

        ConfigurationNode ret = loader.load();
        assertEquals(btval, ret.getNode("byte").getValue());
        assertEquals(shval, ret.getNode("short").getValue());
        assertEquals(ival, ret.getNode("int").getValue());
        assertEquals(lval, ret.getNode("long").getValue());
        assertEquals(fval, (double)ret.getNode("float").getValue(), 0.05);
        assertEquals(dval, ret.getNode("double").getValue());
        assertEquals(blval, ret.getNode("boolean").getValue());
        assertEquals(stval, ret.getNode("string").getValue());
    }
}
