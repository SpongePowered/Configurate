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
package org.spongepowered.configurate.yaml;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.composer.Composer;
import org.yaml.snakeyaml.parser.ParserImpl;
import org.yaml.snakeyaml.reader.StreamReader;

import java.io.Reader;

final class ConfigurateYaml extends Yaml {

    ConfigurateYaml(final DumperOptions options) {
        super(options);
    }

    public Object loadConfigurate(final Reader yaml) {
        // Match the superclass implementation, except we substitute our own scanner implementation
        final StreamReader reader = new StreamReader(yaml);
        final ParserImpl parser = new ParserImpl(new ConfigurateScanner(reader));
        final Composer compose = new Composer(parser, this.resolver, this.loadingConfig);
        this.constructor.setComposer(compose);
        return this.constructor.getSingleData(Object.class);
    }

}
