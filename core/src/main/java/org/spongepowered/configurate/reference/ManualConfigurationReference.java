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
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.ScopedConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.reactive.Processor;
import org.spongepowered.configurate.reactive.Publisher;
import org.spongepowered.configurate.serialize.SerializationException;

import java.lang.reflect.Type;
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
    public final void load() throws ConfigurateException {
        synchronized (this.loader) {
            this.updateListener.submit(this.node = this.loader.load());
        }
    }

    @Override
    public final void save() throws ConfigurateException {
        save(this.node);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void save(final ConfigurationNode newNode) throws ConfigurateException {
        requireNonNull(newNode, "newNode");
        synchronized (this.loader) {
            final ConfigurationNode existing = this.node;
            if (existing.getClass().equals(newNode.getClass())) {
                // Set
                this.node = (N) newNode;
                this.loader.save(this.node);
                if (newNode != existing) {
                    this.updateListener.submit(this.node);
                }
            } else {
                this.loader.save(this.node.from(newNode));
                this.updateListener.submit(this.node);
            }
        }
    }

    @Override
    public final Publisher<N> saveAsync() {
        return Publisher.execute(() -> {
            save();
            return node();
        }, this.updateListener.executor());
    }

    @Override
    public final Publisher<N> updateAsync(final Function<N, ? extends N> updater) {
        return Publisher.execute(() -> {
            final N newNode = updater.apply(node());
            save(newNode);
            return newNode;
        }, this.updateListener.executor());
    }

    @Override
    public final N node() {
        return this.node;
    }

    @Override
    public final ConfigurationLoader<? extends N> loader() {
        return this.loader;
    }

    @Override
    public final N get(final Object... path) {
        return node().node(path);
    }

    @Override
    public final N get(final Iterable<?> path) {
        return node().node(path);
    }

    @Override
    public final ValueReference<?, N> referenceTo(final Type type,
            final NodePath path, final @Nullable Object def) throws SerializationException {
        return new ValueReferenceImpl<>(this, path, type, def);
    }

    @Override
    public final <T> ValueReference<T, N> referenceTo(final TypeToken<T> type, final NodePath path, final @Nullable T def) throws SerializationException {
        return new ValueReferenceImpl<>(this, path, type, def);
    }

    @Override
    public final <T> ValueReference<T, N> referenceTo(final Class<T> type, final NodePath path, final @Nullable T def) throws SerializationException {
        return new ValueReferenceImpl<>(this, path, type, def);
    }

    @Override
    public final Publisher<N> updates() {
        return this.updateListener;
    }

    @Override
    public final Publisher<Map.Entry<ErrorPhase, Throwable>> errors() {
        return this.errorListener;
    }

    @Override
    public void close() {
        this.updateListener.onClose();
    }

}
