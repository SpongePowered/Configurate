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

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;

/**
 * Represents an object which can load and save {@link ConfigurationNode} objects in a specific
 * configuration format.
 *
 * <p>An abstract implementation is provided by {@link AbstractConfigurationLoader}.</p>
 *
 * @param <NodeType> The {@link ConfigurationNode} type produced by the loader
 */
public interface ConfigurationLoader<NodeType extends ConfigurationNode> {

    /**
     * Gets the default {@link ConfigurationOptions} used by the loader.
     *
     * <p>New nodes will be created using the default options if a specific set is not defined.</p>
     *
     * @return The default options
     */
    @NonNull
    ConfigurationOptions getDefaultOptions();

    /**
     * Attempts to load a {@link ConfigurationNode} using this loader, from the defined source.
     *
     * <p>The resultant node represents the root of the configuration being loaded.</p>
     *
     * <p>The {@link #getDefaultOptions() default options} will be used to construct the resultant
     * configuration nodes.</p>
     *
     * @return The newly constructed node
     * @throws IOException if any sort of error occurs with reading or parsing the configuration
     */
    @NonNull
    default NodeType load() throws IOException {
        return load(getDefaultOptions());
    }

    /**
     * Attempts to load a {@link ConfigurationNode} using this loader, from the defined source.
     *
     * <p>The resultant node represents the root of the configuration being loaded.</p>
     *
     * @param options The options to load with
     * @return The newly constructed node
     * @throws IOException if any sort of error occurs with reading or parsing the configuration
     */
    @NonNull
    NodeType load(@NonNull ConfigurationOptions options) throws IOException;

    /**
     * Attempts to save a {@link ConfigurationNode} using this loader, to the defined sink.
     *
     * @param node The node to save
     * @throws IOException if any sort of error occurs with writing or generating the configuration
     */
    void save(@NonNull ConfigurationNode node) throws IOException;

    /**
     * Return an empty node of the most appropriate type for this loader, using the default options.
     *
     * @return The appropriate node type
     */
    @NonNull
    default NodeType createEmptyNode() {
        return createEmptyNode(getDefaultOptions());
    }

    /**
     * Return an empty node of the most appropriate type for this loader
     *
     * @param options The options to use with this node. Must not be null (see {@link ConfigurationOptions#defaults()})
     * @return The appropriate node type
     */
    @NonNull
    NodeType createEmptyNode(@NonNull ConfigurationOptions options);

    /**
     * Gets if this loader is capable of loading configurations.
     *
     * @return If this loader can load
     */
    default boolean canLoad() {
        return true;
    }

    /**
     * Gets if this loader is capable of saving configurations.
     *
     * @return If this loader can save
     */
    default boolean canSave() {
        return true;
    }
}
