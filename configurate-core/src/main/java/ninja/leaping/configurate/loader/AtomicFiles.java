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
package ninja.leaping.configurate.loader;

import com.google.common.base.Preconditions;

import java.io.BufferedWriter;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.concurrent.Callable;

public class AtomicFiles {
    private static final FileAttribute<?>[] EMPTY_ATTRIBUTE_ARR = new FileAttribute[0];
    private AtomicFiles() {}

    public static Callable<BufferedWriter> createAtomicWriterFactory(Path path, Charset charset) {
        Preconditions.checkNotNull(path, "path");
        return () -> createAtomicBufferedWriter(path, charset);
    }

    private static Path getTemporaryPath(Path parent, String key) {
        String fileName = System.currentTimeMillis() + Preconditions.checkNotNull(key, "key").replaceAll("\\\\|/|:",
                "-") + ".tmp";
        return parent.resolve(fileName);
    }

    public static BufferedWriter createAtomicBufferedWriter(Path path, Charset charset) throws IOException {
        path = path.toAbsolutePath();

        Path writePath = getTemporaryPath(path.getParent(), path.getFileName().toString());
        if (Files.exists(path)) {
            Files.copy(path, writePath, StandardCopyOption.COPY_ATTRIBUTES);
        }

        BufferedWriter output = Files.newBufferedWriter(writePath, charset);
        return new BufferedWriter(new AtomicFileWriter(writePath, path, output));
    }

    private static class AtomicFileWriter extends FilterWriter {
        private final Path targetPath, writePath;

        protected AtomicFileWriter(Path writePath, Path targetPath, Writer wrapping) throws IOException {
            super(wrapping);
            this.writePath = writePath;
            this.targetPath = targetPath;
        }

        @Override
        public void close() throws IOException {
            super.close();
            Files.move(writePath, targetPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
