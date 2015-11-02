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
package ninja.leaping.configurate.json;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;

/**
 * Basic sanity checks for the loader
 */
public class JSONConfigurationLoaderTest {
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testSimpleLoading() throws IOException {
        URL url = getClass().getResource("/example.json");
        final File tempFile = folder.newFile();
        ConfigurationLoader loader = JSONConfigurationLoader.builder()
                .setSource(() -> new BufferedReader(new InputStreamReader(url.openStream(), UTF_8)))
                        .setSink(AtomicFiles.createAtomicWriterFactory(tempFile.toPath(), UTF_8)).build();
        ConfigurationNode node = loader.load(ConfigurationOptions.defaults().setMapFactory(MapFactories.sortedNatural()));
        assertEquals("unicorn", node.getNode("test", "op-level").getValue());
        assertEquals("dragon", node.getNode("other", "op-level").getValue());
        assertEquals("dog park", node.getNode("other", "location").getValue());
        /*CommentedConfigurationNode commentNode = SimpleCommentedConfigurationNode.root();
        commentNode.getNode("childOne").setValue("a").setComment("Test comment");
        commentNode.getNode("childTwo", "something").setValue("b").setComment("Test comment 2");
        commentNode.getNode("childTwo", "another").setValue("b").setComment("Test comment 3");
        */
        loader.save(node);
        assertEquals(Resources.toString(url, UTF_8), Files
                .toString(tempFile, UTF_8));

    }
}
