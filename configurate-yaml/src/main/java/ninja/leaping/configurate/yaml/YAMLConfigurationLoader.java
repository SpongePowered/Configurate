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

import com.google.common.io.CharSink;
import com.google.common.io.CharSource;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.loader.FileConfigurationLoader;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URL;


/**
 * A loader for YAML-formatted configurations, using the snakeyaml library for parsing
 */
public class YAMLConfigurationLoader extends FileConfigurationLoader {
    private final ThreadLocal<Yaml> yaml = new ThreadLocal<Yaml>() {
        @Override
        protected Yaml initialValue() {
            return new Yaml();
        }
    };

    public YAMLConfigurationLoader(File file) {
        super(file);
    }

    public YAMLConfigurationLoader(URL url) {
        super(url);
    }

    public YAMLConfigurationLoader(CharSource source, CharSink sink) {
        super(source, sink);
    }

    @Override
    public ConfigurationNode load() throws IOException {
        if (!canLoad()) {
            throw new IOException("No source present to read from!");
        }
        final SimpleConfigurationNode node = SimpleConfigurationNode.root();
        try (Reader reader = source.openStream()) {
            node.setValue(yaml.get().load(reader));
        }
        return node;
    }

    @Override
    public void save(ConfigurationNode node) throws IOException {
        if (!canSave()) {
            throw new IOException("No sink present to write to!");
        }
        try (Writer writer = sink.openStream()) {
            yaml.get().dump(node.getValue(), writer);
        }
    }
}
