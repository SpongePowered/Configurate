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
package org.spongepowered.configurate.reference;

import static java.util.Objects.requireNonNull;

import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ScopedConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.configurate.reactive.Processor;
import org.spongepowered.configurate.reactive.Publisher;
import org.spongepowered.configurate.transformation.NodePath;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * A reference to a configuration node, that may or may not be updating.
 */
class ManualConfigurationReference<N extends ScopedConfigurationNode<N>> implements ConfigurationReference<N> {

    protected volatile @MonotonicNonNull N node;
    private final ConfigurationLoader<? extends N> loader;
    protected final Processor.TransactionalIso<N> updateListener;
    protected final Processor.Iso<Map.Entry<ErrorPhase, Throwable>> errorListener;

    ManualConfigurationReference(final ConfigurationLoader<? extends N> loader, final Executor taskExecutor) {
        this.loader = loader;
        this.updateListener = Processor.createTransactional(taskExecutor);
        this.errorListener = Processor.create(taskExecutor);
        this.errorListener.fallbackHandler(it -> {
            System.err.println("Unhandled error while performing a " + it.getKey() + " for a "
                + "configuration reference: " + it.getValue());
            it.getValue().printStackTrace();
        });
    }

    @Override
    public void load() throws IOException {
        synchronized (this.loader) {
            this.updateListener.submit(this.node = this.loader.load());
        }
    }

    @Override
    public void save() throws IOException {
        save(this.node);
    }

    @Override
    public void save(final N newNode) throws IOException {
        synchronized (this.loader) {
            this.loader.save(this.node = requireNonNull(newNode));
        }
    }

    @Override
    public Publisher<N> saveAsync() {
        return Publisher.execute(() -> {
            save();
            return node();
        }, this.updateListener.executor());
    }

    @Override
    public Publisher<N> updateAsync(final Function<N, ? extends N> updater) {
        return Publisher.execute(() -> {
            final N newNode = updater.apply(node());
            save(newNode);
            return newNode;
        }, this.updateListener.executor());
    }

    @Override
    public N node() {
        return this.node;
    }

    @Override
    public ConfigurationLoader<? extends N> loader() {
        return this.loader;
    }

    @Override
    public N get(final Object... path) {
        return node().node(path);
    }

    @Override
    public <T> ValueReference<T, N> referenceTo(final TypeToken<T> type, final NodePath path, final @Nullable T def) throws ObjectMappingException {
        return new ValueReferenceImpl<>(this, path, type, def);
    }

    @Override
    public <T> ValueReference<T, N> referenceTo(final Class<T> type, final NodePath path, final @Nullable T def) throws ObjectMappingException {
        return new ValueReferenceImpl<>(this, path, type, def);
    }

    @Override
    public Publisher<N> updates() {
        return this.updateListener;
    }

    @Override
    public Publisher<Map.Entry<ErrorPhase, Throwable>> errors() {
        return this.errorListener;
    }

    @Override
    public void close() {
        this.updateListener.onClose();
    }

}
