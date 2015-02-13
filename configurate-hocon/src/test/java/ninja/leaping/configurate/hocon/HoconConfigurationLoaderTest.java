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
package ninja.leaping.configurate.hocon;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.*;
import static ninja.leaping.configurate.loader.FileConfigurationLoader.UTF8_CHARSET;

/**
 * Basic sanity checks for the loader
 */
public class HoconConfigurationLoaderTest {
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();
    @Test
    public void testSimpleLoading() throws IOException {
        URL url = getClass().getResource("/example.conf");
        final File saveTest = folder.newFile();

        HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                .setSource(Resources.asCharSource(url, UTF8_CHARSET))
                .setSink(Files.asCharSink(saveTest, UTF8_CHARSET)).build();
        CommentedConfigurationNode node = loader.load();
        assertEquals("unicorn", node.getNode("test", "op-level").getValue());
        assertEquals("dragon", node.getNode("other", "op-level").getValue());
        CommentedConfigurationNode testNode = node.getNode("test");
        assertEquals(" Test node", testNode.getComment().orNull());
        assertEquals("dog park", node.getNode("other", "location").getValue());
        loader.save(node);
        assertEquals(Resources.toString(getClass().getResource("/roundtrip-test.conf"), UTF8_CHARSET), Files
                .toString(saveTest, UTF8_CHARSET));
    }

    @Test
    public void testBooleansNotShared() throws IOException {
        URL url = getClass().getResource("/comments-test.conf");
        final File saveTo = folder.newFile();
        HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                .setFile(saveTo).setURL(url).build();

        CommentedConfigurationNode node = SimpleCommentedConfigurationNode.root();
        node.getNode("test", "third").setValue(false).setComment("really?");
        node.getNode("test", "apple").setComment("fruit").setValue(false);
        node.getNode("test", "donut").setValue(true).setComment("tasty");
        node.getNode("test", "guacamole").setValue(true).setComment("and chips?");

        loader.save(node);
        assertEquals(Resources.toString(url, UTF8_CHARSET), Files.toString(saveTo, UTF8_CHARSET));
    }
}
