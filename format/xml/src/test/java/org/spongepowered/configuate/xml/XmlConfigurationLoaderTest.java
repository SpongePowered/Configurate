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
package org.spongepowered.configuate.xml;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.spongepowered.configurate.AttributedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.loader.AtomicFiles;
import org.spongepowered.configurate.xml.XmlConfigurationLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Basic sanity checks for the loader.
 */
class XmlConfigurationLoaderTest {

    @Test
    void testSimpleLoading(final @TempDir Path tempDir) throws IOException, ConfigurateException {
        final URL url = this.getClass().getResource("/example.xml");
        final Path saveTest = tempDir.resolve("text1.txt");

        final XmlConfigurationLoader loader = XmlConfigurationLoader.builder()
                .writesExplicitType(false)
                .indent(4)
                .source(() -> new BufferedReader(new InputStreamReader(url.openStream(), UTF_8)))
                .sink(AtomicFiles.atomicWriterFactory(saveTest, UTF_8)).build();

        final AttributedConfigurationNode node = loader.load();

        assertEquals("messages", node.tagName());
        assertEquals("false", node.attribute("secret"));
        assertTrue(node.isList());

        final List<AttributedConfigurationNode> notes = node.childrenList();
        assertEquals(2, notes.size());

        final AttributedConfigurationNode firstNote = notes.get(0);
        assertEquals("501", firstNote.attribute("id"));
        assertTrue(firstNote.isMap());
        assertFalse(firstNote.isList());

        final Map<Object, AttributedConfigurationNode> properties = firstNote.childrenMap();
        assertEquals("Tove", properties.get("to").raw());
        assertEquals("Jani", properties.get("from").raw());
        assertEquals("Don't forget me this weekend!", properties.get("body").raw());
        assertEquals("heading", properties.get("heading").tagName());

        final AttributedConfigurationNode secondNode = notes.get(1);
        assertEquals("502", secondNode.attribute("id"));
        assertFalse(secondNode.isMap());
        assertTrue(secondNode.isList());

        final List<AttributedConfigurationNode> subNodes = secondNode.childrenList();
        for (final AttributedConfigurationNode subNode : subNodes) {
            if (subNode.tagName().equals("heading")) {
                assertEquals("true", subNode.attribute("bold"));
            }
        }

        // roundtrip!
        loader.save(node);
        assertEquals(Resources.readLines(url, UTF_8), Files.readAllLines(saveTest));
    }

    @Test
    void testExplicitTypes(final @TempDir Path tempDir) throws IOException, ConfigurateException {
        final URL url = this.getClass().getResource("/example2.xml");
        final Path saveTest = tempDir.resolve("text2.txt");

        final XmlConfigurationLoader loader = XmlConfigurationLoader.builder()
                .writesExplicitType(true)
                .includesXmlDeclaration(false)
                .indent(4)
                .source(() -> new BufferedReader(new InputStreamReader(url.openStream(), UTF_8)))
                .sink(AtomicFiles.atomicWriterFactory(saveTest, UTF_8)).build();

        final AttributedConfigurationNode node = loader.load();

        final AttributedConfigurationNode list1 = node.node("list1");
        assertTrue(list1.isList());

        final AttributedConfigurationNode list2 = node.node("list2");
        assertTrue(list2.isList());

        final AttributedConfigurationNode map1 = node.node("map1");
        assertTrue(map1.isMap());

        final AttributedConfigurationNode map2 = node.node("map2");
        assertTrue(map2.isMap());

        // roundtrip!
        loader.save(node);
        assertEquals(Resources.readLines(url, UTF_8), Files.readAllLines(saveTest));
    }

    @Test
    void testComments(final @TempDir Path tempDir) throws IOException, ConfigurateException {
        final URL url = this.getClass().getResource("/example3.xml");
        final Path saveTest = tempDir.resolve("text3.txt");

        final XmlConfigurationLoader loader = XmlConfigurationLoader.builder()
                .writesExplicitType(true)
                .includesXmlDeclaration(true)
                .indent(4)
                .source(() -> new BufferedReader(new InputStreamReader(url.openStream(), UTF_8)))
                .sink(AtomicFiles.atomicWriterFactory(saveTest, UTF_8)).build();

        final AttributedConfigurationNode node = loader.createNode(
                loader.defaultOptions().header("test header\ndo multiple lines work\nyes they do!!")
        ).tagName("test");

        node.node("test1").raw("something");
        node.node("test2").raw("I have a comment!").comment("Hi!");

        loader.save(node);
        assertEquals(Resources.readLines(url, UTF_8), Files.readAllLines(saveTest));
    }

    @Test
    void testCommentsRoundtrip(final @TempDir Path tempDir) throws IOException, ConfigurateException {
        final URL original = this.getClass().getResource("/example3.xml");
        final Path destination = tempDir.resolve("test3-roundtrip.xml");

        final XmlConfigurationLoader loader = XmlConfigurationLoader.builder()
                .indent(4)
                .source(() -> new BufferedReader(new InputStreamReader(original.openStream(), UTF_8)))
                .sink(AtomicFiles.atomicWriterFactory(destination, UTF_8))
                .build();

        final AttributedConfigurationNode node = loader.load();
        loader.save(node);

        assertEquals(Resources.readLines(original, UTF_8), Files.readAllLines(destination, UTF_8));
    }

}
