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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@ExtendWith(TempDirectory.class)
public class AbstractConfigurationLoaderTest {

    @Test
    public void testLoadNonexistantPath(@TempDirectory.TempDir Path tempDir) throws IOException {
        Path tempPath = tempDir.resolve("text5.txt").getRoot().resolve("does-not-exist-dont-edit-testdir");
        TestConfigurationLoader loader = TestConfigurationLoader.builder().setPath(tempPath).build();
        loader.load();
    }

    @Test
    public void testLoadNonexistantFile(@TempDirectory.TempDir Path tempDir) throws IOException {
        File tempFile = new File(tempDir.resolve("text5.txt").getRoot().toFile(), "does-not-exist-dont-edit-testdir");
        TestConfigurationLoader loader = TestConfigurationLoader.builder().setFile(tempFile).build();
        loader.load();
    }
}
