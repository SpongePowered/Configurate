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
package ninja.leaping.configurate.yaml;

import ninja.leaping.configurate.ConfigurationLoader;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.Charset;


/**
 * A loader for YAML-formatted configurations, using the snakeyaml library for parsing
 */
public class YAMLConfigurationLoader implements ConfigurationLoader {
    private final File file;

    private final Yaml yaml;

    public YAMLConfigurationLoader(File file) {
        this.file = file;
        this.yaml = new Yaml();
    }

    @Override
    public ConfigurationNode load() throws IOException {
        final SimpleConfigurationNode node = SimpleConfigurationNode.root();
        node.setValue(yaml.load(new InputStreamReader(new FileInputStream(file), Charset.forName("utf-8"))));
        return node;
    }

    @Override
    public void save(ConfigurationNode node) throws IOException {
        yaml.dump(node.getValue(), new OutputStreamWriter(new FileOutputStream(file), Charset.forName("utf-8")));
    }
}
