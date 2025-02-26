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
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.events.CollectionEndEvent
import org.yaml.snakeyaml.events.CollectionStartEvent
import org.yaml.snakeyaml.parser.ParserImpl
import org.yaml.snakeyaml.reader.StreamReader
import org.yaml.snakeyaml.scanner.ScannerImpl

trait YamlTest {

    CommentedConfigurationNode parseString(final String input) {
        // Print events
        def loaderOpts = new LoaderOptions().tap {
            processComments = true
            acceptTabs = true
        }
        this.dumpEvents(new StreamReader(input), loaderOpts)

        final YamlParserComposer loader = new YamlParserComposer(new StreamReader(input), loaderOpts, Yaml11Tags.REPOSITORY)
        final CommentedConfigurationNode result = CommentedConfigurationNode.root()
        loader.singleDocumentStream(result)
        return result
    }

    CommentedConfigurationNode parseResource(final URL url) {
        // Print events
        def loaderOpts = new LoaderOptions().tap {
            processComments = true
            acceptTabs = true
        }
        url.openStream().withReader('UTF-8') {reader ->
            this.dumpEvents(new StreamReader(reader), loaderOpts)
        }

        assertNotNull(url, "Expected resource is missing")
        url.openStream().withReader('UTF-8') { reader ->
            final YamlParserComposer loader = new YamlParserComposer(new StreamReader(reader), loaderOpts, Yaml11Tags.REPOSITORY)
            final CommentedConfigurationNode result = CommentedConfigurationNode.root()
            loader.singleDocumentStream(result)
            return result
        }
    }

    private void dumpEvents(StreamReader reader, LoaderOptions loaderOpts) {
        def scanner = new ScannerImpl(reader, loaderOpts)
        def parser = new ParserImpl(scanner)
        int indentLevel = 0
        while (true) {
            if (parser.peekEvent() instanceof CollectionEndEvent) {
                indentLevel--
            }
            indentLevel.times {
                print "    "
            }
            if (parser.peekEvent() instanceof CollectionStartEvent) {
                indentLevel++
            }

            println parser.getEvent()
            if (!parser.peekEvent()) break
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
