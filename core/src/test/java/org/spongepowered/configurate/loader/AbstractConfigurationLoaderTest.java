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
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class AbstractConfigurationLoaderTest {

    @Test
    public void testLoadNonexistantPath(final @TempDir Path tempDir) throws IOException {
        final Path tempPath = tempDir.resolve("text5.txt").getRoot().resolve("does-not-exist-dont-edit-testdir");
        final TestConfigurationLoader loader = TestConfigurationLoader.builder().setPath(tempPath).build();
        loader.load();
    }

    @Test
    public void testLoadNonexistantFile(final @TempDir Path tempDir) throws IOException {
        final File tempFile = new File(tempDir.resolve("text5.txt").getRoot().toFile(), "does-not-exist-dont-edit-testdir");
        final TestConfigurationLoader loader = TestConfigurationLoader.builder().setFile(tempFile).build();
        loader.load();
    }

    @Test
    public void testSaveFollowsSymbolicLinks(final @TempDir Path tempDir) throws IOException {
        final Path actualFile = tempDir.resolve(Paths.get("first", "second", "third.json"));
        Files.createDirectories(actualFile.getParent());
        final Path layerOne = tempDir.resolve("general.json");
        final Path layerTwo = tempDir.resolve("general2.json");

        Files.createFile(actualFile);
        Files.createSymbolicLink(layerOne, actualFile);
        Files.createSymbolicLink(layerTwo, layerOne);

        try (BufferedWriter writer = AtomicFiles.createAtomicBufferedWriter(layerTwo, StandardCharsets.UTF_8)) {
            writer.write("I should follow symlinks!\n");
        }

        // We expect links are preserved, and the underlying file is written to
        assertTrue(Files.isSymbolicLink(layerTwo));
        assertTrue(Files.isSymbolicLink(layerOne));
        assertEquals(layerOne, Files.readSymbolicLink(layerTwo));
        assertEquals(actualFile, Files.readSymbolicLink(layerOne));
        assertEquals("I should follow symlinks!", Files.readAllLines(layerTwo, StandardCharsets.UTF_8).stream()
                .collect(Collectors.joining("\n")));
    }

}
