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
package org.spongepowered.configurate.loader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class AbstractConfigurationLoaderTest {

    @Test
    void testLoadNonexistentPath(final @TempDir Path tempDir) throws ConfigurateException {
        final Path tempPath = tempDir.resolve("text5.txt").getRoot().resolve("does-not-exist-dont-edit-testdir");
        final TestConfigurationLoader loader = TestConfigurationLoader.builder().path(tempPath).build();
        loader.load();
    }

    @Test
    void testLoadNonexistentFile(final @TempDir Path tempDir) throws ConfigurateException {
        final File tempFile = new File(tempDir.resolve("text5.txt").getRoot().toFile(), "does-not-exist-dont-edit-testdir");
        final TestConfigurationLoader loader = TestConfigurationLoader.builder().file(tempFile).build();
        loader.load();
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS, disabledReason = "windows FS has transient permissions issues")
    void testSaveFollowsSymbolicLinks(final @TempDir Path tempDir) throws IOException {
        final Path actualFile = tempDir.resolve(Paths.get("first", "second", "third.json"));
        Files.createDirectories(actualFile.getParent());
        final Path layerOne = tempDir.resolve("general.json");
        final Path layerTwo = tempDir.resolve("general2.json");

        Files.createFile(actualFile);
        Files.createSymbolicLink(layerOne, actualFile);
        Files.createSymbolicLink(layerTwo, layerOne);

        try (BufferedWriter writer = AtomicFiles.atomicBufferedWriter(layerTwo, StandardCharsets.UTF_8)) {
            writer.write("I should follow symlinks!\n");
        }

        // We expect links are preserved, and the underlying file is written to
        assertTrue(Files.isSymbolicLink(layerTwo));
        assertTrue(Files.isSymbolicLink(layerOne));
        assertEquals(layerOne, Files.readSymbolicLink(layerTwo));
        assertEquals(actualFile, Files.readSymbolicLink(layerOne));
        assertEquals("I should follow symlinks!\n", readToString(layerTwo));
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS, disabledReason = "Symlink permissions on windows are inconsistent")
    void testWriteFileInSymlinkedDirectory(final @TempDir Path tempDir) throws IOException {
        final Path realDirectory = tempDir.resolve("real");
        Files.createDirectories(realDirectory);
        final Path linked = tempDir.resolve("link");
        Files.createSymbolicLink(linked, realDirectory);

        final Path configFile = linked.resolve("config.yaml");

        final String contents = "helo friends\n";
        try (BufferedWriter writer = AtomicFiles.atomicBufferedWriter(configFile, StandardCharsets.UTF_8)) {
            writer.write(contents);
        }

        assertEquals(contents, readToString(configFile));
        assertEquals(contents, readToString(realDirectory.resolve("config.yaml")));
    }

    private static String readToString(final Path file) throws IOException {
        final StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            final char[] buffer = new char[4096];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                builder.append(buffer, 0, read);
            }
        }
        return builder.toString();
    }

    @Test
    void testLoadFromString() throws ConfigurateException {
        final ConfigurationNode expected = TestConfigurationLoader.builder().buildAndLoadString("hi there!");

        assertEquals("hi there!", expected.getString());
    }

    @Test
    void testSaveToString() throws ConfigurateException {
        final String expected = TestConfigurationLoader.builder().buildAndSaveString(BasicConfigurationNode.root().raw("i'm a shortcut!"));

        assertEquals("i'm a shortcut!", expected);
    }

}
