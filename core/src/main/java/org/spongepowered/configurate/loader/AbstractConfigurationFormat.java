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

import static java.util.Objects.requireNonNull;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ScopedConfigurationNode;
import org.spongepowered.configurate.util.UnmodifiableCollections;

import java.net.URL;
import java.nio.file.Path;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Supplier;

/**
 * An implementation of {@link ConfigurationFormat} designed to work
 * with {@link AbstractConfigurationLoader}.
 *
 * <p>This reduces the boilerplate that would otherwise be required to implement
 * a configuration format service.</p>
 *
 * @param <N> the node type
 * @param <L> the loader type
 * @param <B> the builder type
 * @since 4.2.0
 */
public abstract class AbstractConfigurationFormat<
    N extends ScopedConfigurationNode<N>,
    L extends AbstractConfigurationLoader<N>,
    B extends AbstractConfigurationLoader.Builder<B, L>
    > implements ConfigurationFormat {

    private final String id;
    private final Supplier<B> builderMaker;
    private final Set<String> supportedExtensions;

    /**
     * Create a new configuration format.
     *
     * <p>Subclasses should have a zero-argument constructor to fulfil the
     * requirements of {@link ServiceLoader}.</p>
     *
     * @param builderMaker a factory creating a new builder
     * @param supportedExtensions the file extensions associated with
     *     this format
     * @since 4.2.0
     */
    protected AbstractConfigurationFormat(final String id, final Supplier<B> builderMaker, final Set<String> supportedExtensions) {
        this.id = requireNonNull(id, "id");
        this.builderMaker = requireNonNull(builderMaker, "builderMaker");
        this.supportedExtensions = UnmodifiableCollections.copyOf(requireNonNull(supportedExtensions, "supportedExtensions"));
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public Set<String> supportedExtensions() {
        return this.supportedExtensions;
    }

    @Override
    public ConfigurationLoader<? extends @NonNull Object> create(final Path file) {
        return this.builderMaker.get()
            .path(file)
            .build();
    }

    @Override
    public ConfigurationLoader<? extends @NonNull Object> create(final Path file, final ConfigurationNode options) {
        return this.builderMaker.get()
            .editOptions(opts -> opts.values(ValueSources.node(options)))
            .path(file)
            .build();
    }

    @Override
    public ConfigurationLoader<? extends @NonNull Object> create(final URL url) {
        return this.builderMaker.get()
            .url(url)
            .build();
    }

    @Override
    public ConfigurationLoader<? extends @NonNull Object> create(final URL url, final ConfigurationNode options) {
        return this.builderMaker.get()
            .editOptions(opts -> opts.values(ValueSources.node(options)))
            .url(url)
            .build();
    }

}
