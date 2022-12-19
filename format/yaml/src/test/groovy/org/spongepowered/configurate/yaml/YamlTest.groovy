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
package org.spongepowered.configurate.yaml

import static org.junit.jupiter.api.Assertions.assertNotNull

import org.spongepowered.configurate.CommentedConfigurationNode
import org.yaml.snakeyaml.parser.ParserImpl
import org.yaml.snakeyaml.reader.StreamReader
import org.yaml.snakeyaml.scanner.ScannerImpl

trait YamlTest {

    CommentedConfigurationNode parseString(final String input) {
        // Print events
        def scanner = new ScannerImpl(new StreamReader(input))
        scanner.parseComments = true
        scanner.acceptTabs = true
        def parser = new ParserImpl(scanner)

        while (true) {
            println parser.getEvent()
            if (!parser.peekEvent()) break
        }

        final YamlParserComposer loader = new YamlParserComposer(new StreamReader(input), Yaml11Tags.REPOSITORY, true)
        final CommentedConfigurationNode result = CommentedConfigurationNode.root()
        loader.singleDocumentStream(result)
        return result
    }

    CommentedConfigurationNode parseResource(final URL url) {
        // Print events
        url.openStream().withReader('UTF-8') {reader ->
            def scanner = new ScannerImpl(new StreamReader(reader))
            scanner.parseComments = true
            scanner.acceptTabs = true
            def parser = new ParserImpl(scanner)
            while (true) {
                println parser.getEvent()
                if (!parser.peekEvent()) break
            }
        }

        assertNotNull(url, "Expected resource is missing")
        url.openStream().withReader('UTF-8') { reader ->
            final YamlParserComposer loader = new YamlParserComposer(new StreamReader(reader), Yaml11Tags.REPOSITORY, true)
            final CommentedConfigurationNode result = CommentedConfigurationNode.root()
            loader.singleDocumentStream(result)
            return result
        }
    }

    String dump(final CommentedConfigurationNode input) {
        return dump(input, null)
    }

    String dump(final CommentedConfigurationNode input, final NodeStyle preferredStyle) {
        return YamlConfigurationLoader.builder()
            .nodeStyle(preferredStyle)
            .indent(2)
            .commentsEnabled(true)
            .buildAndSaveString(input)
    }

    String normalize(final String input) {
        return input.stripIndent(true)
    }

}
