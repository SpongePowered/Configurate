/*
 * This file is part of Configurate, licensed under the Apache-2.0 License.
 *
 * Copyright (C) zml
 * Copyright (C) IchorPowered
 * Copyright (C) Contributors
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
import java.nio.file.Files;
import java.nio.file.Path;

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
        final Path tempFile = folder.newFile().toPath();
        ConfigurationLoader loader = JSONConfigurationLoader.builder()
                .setSource(() -> new BufferedReader(new InputStreamReader(url.openStream(), UTF_8)))
                        .setSink(AtomicFiles.createAtomicWriterFactory(tempFile, UTF_8)).build();
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
        assertEquals(Resources.readLines(url, UTF_8), Files
                .readAllLines(tempFile, UTF_8));

    }

    private static final long TEST_LONG_VAL = 584895858588588888l;
    private static final double TEST_DOUBLE_VAL = 595859682984428959583045732020572045273498409257349587.85485884287387d;

    private void testRoundtripValue(Object value) throws IOException {
        final Path tempFile = folder.newFile().toPath();
        ConfigurationLoader<ConfigurationNode> loader = JSONConfigurationLoader.builder().setPath(tempFile).build();
        ConfigurationNode start = loader.createEmptyNode();
        start.getNode("value").setValue(value);
        loader.save(start);

        ConfigurationNode ret = loader.load();
        assertEquals(value, ret.getNode("value").getValue());
    }

    @Test
    public void testRoundtrippingLong() throws IOException {
        testRoundtripValue(TEST_LONG_VAL);
    }

    @Test
    public void testRoundtripDouble() throws IOException {
        testRoundtripValue(TEST_DOUBLE_VAL);
    }
}
