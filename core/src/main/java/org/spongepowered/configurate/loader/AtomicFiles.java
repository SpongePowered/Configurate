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

import static java.util.Objects.requireNonNull;

import java.io.BufferedWriter;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A utility for creating "atomic" file writers.
 *
 * <p>An atomic writer copies any existing file at the given path to a temporary
 * location, then writes to the same temporary location, before moving the file
 * back to the desired output path once the write is fully complete.</p>
 */
public final class AtomicFiles {

    private AtomicFiles() {}

    /**
     * Creates and returns an "atomic" writer factory for the given path.
     *
     * @param path path the complete file should be written to
     * @param charset the charset to be used by the writer
     * @return a new writer factory
     */
    public static Callable<BufferedWriter> createAtomicWriterFactory(final Path path, final Charset charset) {
        requireNonNull(path, "path");
        return () -> createAtomicBufferedWriter(path, charset);
    }

    /**
     * Creates and returns an "atomic" writer for the given path.
     *
     * @param path the path
     * @param charset the charset to be used by the writer
     * @return a new writer factory
     * @throws IOException for any underlying filesystem errors
     */
    public static BufferedWriter createAtomicBufferedWriter(Path path, final Charset charset) throws IOException {
        // absolute
        path = path.toAbsolutePath();

        // unwrap any symbolic links
        try {
            while (Files.isSymbolicLink(path)) {
                path = Files.readSymbolicLink(path);
            }
        } catch (final UnsupportedOperationException | IOException ex) {
            // ignore
        }

        final Path writePath = getTemporaryPath(path.getParent(), path.getFileName().toString());
        if (Files.exists(path)) {
            Files.copy(path, writePath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
        }

        Files.createDirectories(writePath.getParent());
        final BufferedWriter output = Files.newBufferedWriter(writePath, charset);
        return new BufferedWriter(new AtomicFileWriter(writePath, path, output));
    }

    private static Path getTemporaryPath(final Path parent, final String key) {
        final String fileName = System.nanoTime() + ThreadLocalRandom.current().nextInt()
                + requireNonNull(key, "key").replaceAll("[\\\\/:]", "-") + ".tmp";
        return parent.resolve(fileName);
    }

    private static class AtomicFileWriter extends FilterWriter {

        private final Path targetPath;
        private final Path writePath;

        protected AtomicFileWriter(final Path writePath, final Path targetPath, final Writer wrapping) {
            super(wrapping);
            this.writePath = writePath;
            this.targetPath = targetPath;
        }

        @Override
        public void close() throws IOException {
            super.close();
            Files.move(this.writePath, this.targetPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        }

    }

}
