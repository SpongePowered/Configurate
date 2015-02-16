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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;

/**
 * This class implements a wrapper around file output streams that allows for an atomic write on platforms that support atomic file moves.
 * This means that programs that crash mid-write will not leave a partially-written file overwriting the actual permissions file
 */
public class AtomicFileOutputStream extends FilterOutputStream {
    private final File targetFile, writeFile, oldFile;
    public AtomicFileOutputStream(File file) throws IOException {
        super(null);
        writeFile = File.createTempFile(file.getCanonicalPath(), null);
        targetFile = file;
        oldFile = new File(targetFile.getName() + ".old");
        this.out = new FileOutputStream(writeFile);
    }

    @Override
    public void close() throws IOException {
        super.close();

        /**
         * This works on some platforms (Linux), and is the best option.
         * People on other platforms (windows?) have less guarantee of atomicness.
         * We don't know whether this is actually why the rename fails though, because jdk6 kinda sucks.
         */
        if (!writeFile.renameTo(targetFile)) {
            oldFile.delete();
            if (!targetFile.exists() || targetFile.renameTo(oldFile)) {
                if (!writeFile.renameTo(targetFile)) {
                    throw new IOException("Unable to overwrite file with temporary file! New config is at " + writeFile + ", old config at" + oldFile);
                } else {
                    if (!oldFile.delete()) {
                        throw new IOException("Unable to delete old file " + oldFile);
                    }
                }
            }
        }
    }
}
