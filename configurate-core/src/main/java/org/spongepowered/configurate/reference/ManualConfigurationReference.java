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

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ScopedConfigurationNode;
import com.google.common.reflect.TypeToken;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.configurate.transformation.NodePath;
import org.spongepowered.configurate.reactive.Publisher;
import org.spongepowered.configurate.reactive.Processor;

import java.io.IOException;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * A reference to a configuration node, that may or may not be updating
 */
class ManualConfigurationReference<N extends ScopedConfigurationNode<N>> implements ConfigurationReference<N> {
    protected volatile @MonotonicNonNull N node;
    private final ConfigurationLoader<N> loader;
    protected final Processor.TransactionalIso<N> updateListener = Processor.createTransactional();
    protected final Processor.Iso<Map.Entry<ErrorPhase, Throwable>> errorListener =
        Processor.create();

    ManualConfigurationReference(ConfigurationLoader<N> loader) {
        this.loader = loader;
        errorListener.setFallbackHandler(it -> {
            System.out.println("Unhandled error while performing a " + it.getKey() + " for a " +
                "configuration reference: " + it.getValue());
            it.getValue().printStackTrace();
        });
    }

    @Override
    public void load() throws IOException {
        synchronized (this.loader) {
            updateListener.submit(node = loader.load());
        }
    }

    @Override
    public void save() throws IOException {
        save(this.node);
    }

    @Override
    public void save(N newNode) throws IOException {
        synchronized (this.loader) {
            loader.save(this.node = requireNonNull(newNode));
        }
    }

    @Override
    public N getNode() {
        return this.node;
    }

    @Override
    public ConfigurationLoader<N> getLoader() {
        return this.loader;
    }

    @Override
    public N get(Object... path) {
        return getNode().getNode(path);
    }

    @Override
    public <T> ValueReference<T, N> referenceTo(TypeToken<T> type, NodePath path, @Nullable T def) throws ObjectMappingException {
        return new ValueReferenceImpl<>(this, path, type, def);
    }

    @Override
    public <T> ValueReference<T, N> referenceTo(Class<T> type, NodePath path, @Nullable T def) throws ObjectMappingException {
        return new ValueReferenceImpl<>(this, path, type, def);
    }

    @Override
    public Publisher<N> updates() {
        return updateListener;
    }

    @Override
    public Publisher<Map.Entry<ErrorPhase, Throwable>> errors() {
        return errorListener;
    }

    @Override
    public void close() {
        updateListener.onClose();
    }

}
