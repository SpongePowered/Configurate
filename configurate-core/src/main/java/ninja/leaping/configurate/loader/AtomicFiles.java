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
package ninja.leaping.configurate.loader;

import org.checkerframework.checker.nullness.qual.NonNull;

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

import static java.util.Objects.requireNonNull;

/**
 * A utility for creating "atomic" file writers.
 *
 * <p>An atomic writer copies any existing file at the given path to a temporary location, then
 * writes to the same temporary location, before moving the file back to the desired output path
 * once the write is fully complete.</p>
 */
public final class AtomicFiles {
    private AtomicFiles() {}

    /**
     * Creates and returns an "atomic" writer factory for the given path.
     *
     * @param path The path
     * @param charset The charset to be used by the writer
     * @return The writer factory
     */
    @NonNull
    public static Callable<BufferedWriter> createAtomicWriterFactory(@NonNull Path path, @NonNull Charset charset) {
        requireNonNull(path, "path");
        return () -> createAtomicBufferedWriter(path, charset);
    }

    /**
     * Creates and returns an "atomic" writer for the given path.
     *
     * @param path The path
     * @param charset The charset to be used by the writer
     * @return The writer factory
     * @throws IOException For any underlying filesystem errors
     */
    @NonNull
    public static BufferedWriter createAtomicBufferedWriter(@NonNull Path path, @NonNull Charset charset) throws IOException {
        path = path.toAbsolutePath();

        Path writePath = getTemporaryPath(path.getParent(), path.getFileName().toString());
        if (Files.exists(path)) {
            Files.copy(path, writePath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
        }

        BufferedWriter output = Files.newBufferedWriter(writePath, charset);
        return new BufferedWriter(new AtomicFileWriter(writePath, path, output));
    }

    @NonNull
    private static Path getTemporaryPath(@NonNull Path parent, @NonNull String key) {
        String fileName = System.nanoTime() + ThreadLocalRandom.current().nextInt() + requireNonNull(key, "key").replaceAll("\\\\|/|:",
                "-") + ".tmp";
        return parent.resolve(fileName);
    }

    private static class AtomicFileWriter extends FilterWriter {
        private final Path targetPath, writePath;

        protected AtomicFileWriter(Path writePath, Path targetPath, Writer wrapping) {
            super(wrapping);
            this.writePath = writePath;
            this.targetPath = targetPath;
        }

        @Override
        public void close() throws IOException {
            super.close();
            Files.createDirectories(targetPath.getParent());
            Files.move(writePath, targetPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
