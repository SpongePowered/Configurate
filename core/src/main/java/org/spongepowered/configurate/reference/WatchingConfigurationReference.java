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
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ScopedConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.reactive.Disposable;
import org.spongepowered.configurate.reactive.Subscriber;
import org.spongepowered.configurate.util.UnmodifiableCollections;

import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.concurrent.Executor;

/**
 * A reference to a configuration node, that may or may not be updating.
 */
class WatchingConfigurationReference<N extends ScopedConfigurationNode<N>>
        extends ManualConfigurationReference<N> implements Subscriber<WatchEvent<?>> {

    private volatile boolean saveSuppressed;
    private @MonotonicNonNull Disposable disposable;

    WatchingConfigurationReference(final ConfigurationLoader<? extends N> loader, final Executor taskExecutor) {
        super(loader, taskExecutor);
    }

    @Override
    public void save(final N newNode) throws ConfigurateException {
        synchronized (loader()) {
            try {
                this.saveSuppressed = true;
                super.save(newNode);
            } finally {
                this.saveSuppressed = false;
            }
        }
    }

    @Override
    public void close() {
        super.close();
        if (this.disposable != null) {
            this.disposable.dispose();
        }
    }

    @Override
    public void submit(final WatchEvent<?> item) {
        if (!this.saveSuppressed || item.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
            try {
                load();
            } catch (final Exception e) {
                this.errorListener.submit(UnmodifiableCollections.immutableMapEntry(ErrorPhase.LOADING, e));
            }
        }
    }

    @Override
    public void onError(final Throwable thrown) {
        this.errorListener.submit(UnmodifiableCollections.immutableMapEntry(ErrorPhase.UNKNOWN, thrown));
    }

    @Override
    public void onClose() {
        close();
    }

    void disposable(final Disposable disposable) {
        this.disposable = disposable;
    }

}
