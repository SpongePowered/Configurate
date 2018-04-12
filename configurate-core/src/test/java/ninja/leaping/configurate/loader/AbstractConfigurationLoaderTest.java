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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class AbstractConfigurationLoaderTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testLoadNonexistantPath() throws IOException {
        Path tempPath = tempFolder.getRoot().toPath().resolve("does-not-exist-dont-edit-testdir");
        TestConfigurationLoader loader = TestConfigurationLoader.builder().setPath(tempPath).build();
        loader.load();
    }

    @Test
    public void testLoadNonexistantFile() throws IOException {
        File tempFile = new File(tempFolder.getRoot(), "does-not-exist-dont-edit-testdir");
        TestConfigurationLoader loader = TestConfigurationLoader.builder().setFile(tempFile).build();
        loader.load();
    }
}
