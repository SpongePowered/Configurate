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
package ninja.leaping.configurate.gson;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.loader.AtomicFiles;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.util.MapFactories;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.*;
import static ninja.leaping.configurate.loader.AbstractConfigurationLoader.UTF8_CHARSET;

/**
 * Basic sanity checks for the loader
 */
public class GsonConfigurationLoaderTest {
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testSimpleLoading() throws IOException {
        URL url = getClass().getResource("/example.json");
        final File tempFile = folder.newFile();
        ConfigurationLoader loader = GsonConfigurationLoader.builder()
                .setSource(Resources.asCharSource(url, UTF8_CHARSET))
                .setSink(AtomicFiles.asCharSink(tempFile, UTF8_CHARSET)).setLenient(true).build();
        ConfigurationNode node = loader.load(ConfigurationOptions.defaults().setMapFactory(MapFactories.sortedNatural()));
        assertEquals("unicorn", node.getNode("test", "op-level").getValue());
        assertEquals("dragon", node.getNode("other", "op-level").getValue());
        assertEquals("dog park", node.getNode("other", "location").getValue());
        assertTrue(node.getNode("int-val").getValue() instanceof Integer);
        assertTrue(node.getNode("double-val").getValue() instanceof Double);
        loader.save(node);
        assertEquals(Resources.toString(url, UTF8_CHARSET), Files
                .toString(tempFile, UTF8_CHARSET));
    }

    @Test
    public void testLoadingEmptyFile() throws IOException {
        final File tempFile = folder.newFile();
        tempFile.createNewFile();

        ConfigurationLoader<ConfigurationNode> loader = GsonConfigurationLoader.builder()
                .setFile(tempFile)
                .build();

        loader.load();
    }
}
