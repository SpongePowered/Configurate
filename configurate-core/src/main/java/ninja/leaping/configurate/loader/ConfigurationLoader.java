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

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;

import java.io.IOException;

/**
 * Loader for a specific configuration format
 */
public interface ConfigurationLoader<NodeType extends ConfigurationNode> {
    /**
     * Create a new configuration node populated with the appropriate data
     *
     * @return The newly constructed node
     * @throws java.io.IOException if any sort of error occurs with reading or parsing the configuration
     */
    NodeType load() throws IOException;

    /**
     * Create a new configuration node populated with the appropriate data, structured with the provided options
     *
     * @param options The options to load with
     * @return The newly constructed node
     * @throws java.io.IOException if any sort of error occurs with reading or parsing the configuration
     */
    NodeType load(ConfigurationOptions options) throws IOException;

    /**
     * Save the contents of the given node tree to
     *
     * @param node The node a save is being requested for
     * @throws java.io.IOException if any sort of error occurs with writing or generating the configuration
     */
    void save(ConfigurationNode node) throws IOException;

    /**
     * Return an empty node of the most appropriate type for this loader
     *
     * @param options The options to use with this node. Must not be null (take a look at {@link ConfigurationOptions#defaults()})
     * @return The appropriate node type
     */
    NodeType createEmptyNode(ConfigurationOptions options);
}
