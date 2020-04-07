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
package ninja.leaping.configurate.xml;

import com.google.common.io.Resources;
import ninja.leaping.configurate.attributed.AttributedConfigurationNode;
import ninja.leaping.configurate.loader.AtomicFiles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Basic sanity checks for the loader
 */
@ExtendWith(TempDirectory.class)
public class XMLConfigurationLoaderTest {

    @Test
    public void testSimpleLoading(@TempDirectory.TempDir Path tempDir) throws IOException {
        URL url = getClass().getResource("/example.xml");
        final Path saveTest = tempDir.resolve("text1.txt");

        XMLConfigurationLoader loader = XMLConfigurationLoader.builder()
                .setWriteExplicitType(false)
                .setIndent(4)
                .setSource(() -> new BufferedReader(new InputStreamReader(url.openStream(), UTF_8)))
                .setSink(AtomicFiles.createAtomicWriterFactory(saveTest, UTF_8)).build();

        AttributedConfigurationNode node = loader.load();

        assertEquals("messages", node.getTagName());
        assertEquals("false", node.getAttribute("secret"));
        assertTrue(node.isList());

        List<? extends AttributedConfigurationNode> notes = node.getChildrenList();
        assertEquals(2, notes.size());

        AttributedConfigurationNode firstNote = notes.get(0);
        assertEquals("501", firstNote.getAttribute("id"));
        assertTrue(firstNote.isMap());
        assertFalse(firstNote.isList());

        Map<Object, ? extends AttributedConfigurationNode> properties = firstNote.getChildrenMap();
        assertEquals("Tove", properties.get("to").getValue());
        assertEquals("Jani", properties.get("from").getValue());
        assertEquals("Don't forget me this weekend!", properties.get("body").getValue());
        assertEquals("heading", properties.get("heading").getTagName());

        AttributedConfigurationNode secondNode = notes.get(1);
        assertEquals("502", secondNode.getAttribute("id"));
        assertFalse(secondNode.isMap());
        assertTrue(secondNode.isList());

        List<? extends AttributedConfigurationNode> subNodes = secondNode.getChildrenList();
        for (AttributedConfigurationNode subNode : subNodes) {
            if (subNode.getTagName().equals("heading")) {
                assertEquals("true", subNode.getAttribute("bold"));
            }
        }

        // roundtrip!
        loader.save(node);
        assertEquals(Resources.readLines(url, UTF_8), Files.readAllLines(saveTest));
    }

    @Test
    public void testExplicitTypes(@TempDirectory.TempDir Path tempDir) throws IOException {
        URL url = getClass().getResource("/example2.xml");
        final Path saveTest = tempDir.resolve("text2.txt");

        XMLConfigurationLoader loader = XMLConfigurationLoader.builder()
                .setWriteExplicitType(true)
                .setIncludeXmlDeclaration(false)
                .setIndent(4)
                .setSource(() -> new BufferedReader(new InputStreamReader(url.openStream(), UTF_8)))
                .setSink(AtomicFiles.createAtomicWriterFactory(saveTest, UTF_8)).build();

        AttributedConfigurationNode node = loader.load();

        AttributedConfigurationNode list1 = node.getNode("list1");
        assertTrue(list1.isList());

        AttributedConfigurationNode list2 = node.getNode("list2");
        assertTrue(list2.isList());

        AttributedConfigurationNode map1 = node.getNode("map1");
        assertTrue(map1.isMap());

        AttributedConfigurationNode map2 = node.getNode("map2");
        assertTrue(map2.isMap());

        // roundtrip!
        loader.save(node);
        assertEquals(Resources.readLines(url, UTF_8), Files.readAllLines(saveTest));
    }

    @Test
    public void testComments(@TempDirectory.TempDir Path tempDir) throws IOException {
        URL url = getClass().getResource("/example3.xml");
        final Path saveTest = tempDir.resolve("text3.txt");

        XMLConfigurationLoader loader = XMLConfigurationLoader.builder()
                .setWriteExplicitType(true)
                .setIncludeXmlDeclaration(true)
                .setIndent(4)
                .setSource(() -> new BufferedReader(new InputStreamReader(url.openStream(), UTF_8)))
                .setSink(AtomicFiles.createAtomicWriterFactory(saveTest, UTF_8)).build();

        AttributedConfigurationNode node = loader.createEmptyNode(
                loader.getDefaultOptions().withHeader("test header\ndo multiple lines work\nyes they do!!")
        ).setTagName("test");

        node.getNode("test1").setValue("something");
        node.getNode("test2").setValue("I have a comment!").setComment("Hi!");

        loader.save(node);
        assertEquals(Resources.readLines(url, UTF_8), Files.readAllLines(saveTest));
    }

    @Test
    public void testCommentsRoundtrip(@TempDirectory.TempDir Path tempDir) throws IOException {
        URL original = getClass().getResource("/example3.xml");
        final Path destination = tempDir.resolve("test3-roundtrip.xml");

        XMLConfigurationLoader loader = XMLConfigurationLoader.builder()
                .setIndent(4)
                .setSource(() -> new BufferedReader(new InputStreamReader(original.openStream(), UTF_8)))
                .setSink(AtomicFiles.createAtomicWriterFactory(destination, UTF_8))
                .build();

        AttributedConfigurationNode node = loader.load();
        loader.save(node);

        assertEquals(Resources.readLines(original, UTF_8), Files.readAllLines(destination, UTF_8));
    }
}
