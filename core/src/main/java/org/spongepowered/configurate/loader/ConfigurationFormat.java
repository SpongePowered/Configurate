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
package org.spongepowered.configurate.loader;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;

import java.net.URL;
import java.nio.file.Path;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * A service provider interface declaring a specific configuration format.
 *
 * <p>This is a service interface, designed to be implemented by modules
 * that provide a {@link ConfigurationLoader} implementation. Service discovery
 * follows the rules laid out in {@link ServiceLoader}.</p>
 *
 * @see AbstractConfigurationFormat
 * @since 4.2.0
 */
public interface ConfigurationFormat {

    /**
     * Get a configuration format that can handle the specified extension.
     *
     * <p>If a format fails to load, it will be ignored when p</p>
     *
     * @param extension the extension to handle
     * @return a format, or {@code null} if none is known
     * @since 4.2.0
     */
    static @Nullable ConfigurationFormat forExtension(final String extension) {
        final ConfigurationFormats.@Nullable Holder holder = ConfigurationFormats.BY_EXTENSION.get(extension);
        return holder == null ? null : holder.get();
    }

    /**
     * Get all supported configuration formats.
     *
     * <p>If any exceptions were thrown while discovering or loading
     * configuration format services, they will be rethrown on calling
     * this method.</p>
     *
     * @return all known formats
     * @since 4.2.0
     */
    static Set<ConfigurationFormat> supportedFormats() {
        return ConfigurationFormats.unwrappedFormats();
    }

    /**
     * An identifier describing this loader.
     *
     * <p>This should match the naming used in other locations, such as the
     * loader's artifact ID or class name.</p>
     *
     * @return the loader identifier
     * @since 4.2.0
     */
    String id();

    /**
     * Get the file extensions known to be supported by this format.
     *
     * @return the supported extensions
     * @since 4.2.0
     */
    Set<String> supportedExtensions();

    /**
     * Create a new loader configured to load from the provided file,
     * with default style options.
     *
     * @param file the file to load from
     * @return a newly configured loader
     * @since 4.2.0
     */
    default ConfigurationLoader<? extends @NonNull Object> create(final Path file) {
        return create(file, BasicConfigurationNode.factory().createNode());
    }

    /**
     * Create a new loader configured to load from the provided file.
     *
     * @param file the file to load from
     * @param options the options to use to configure the node
     * @return a newly configured loader
     * @since 4.2.0
     */
    ConfigurationLoader<? extends @NonNull Object> create(Path file, ConfigurationNode options);

    /**
     * Create a new loader configured to load from the provided URL,
     * with default style options.
     *
     * <p>This loader may not be able to write to the given URL</p>
     *
     * @param url the URL to load from
     * @return a newly configured loader
     * @since 4.2.0
     */
    default ConfigurationLoader<? extends @NonNull Object> create(final URL url) {
        return create(url, BasicConfigurationNode.factory().createNode());
    }

    /**
     * Create a new loader configured to load from the provided URL.
     *
     * <p>This loader may not be able to write to the given URL.</p>
     *
     * @param url the URL to load from
     * @param options the options to use to configure the node
     * @return a newly configured loader
     * @since 4.2.0
     */
    ConfigurationLoader<? extends @NonNull Object> create(URL url, ConfigurationNode options);

}
