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
package ninja.leaping.configurate.reference;

import com.google.common.collect.Maps;
import ninja.leaping.configurate.ConfigurationNode;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.reactive.Disposable;
import ninja.leaping.configurate.reactive.Subscriber;

import java.io.IOException;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.concurrent.Executor;

/**
 * A reference to a configuration node, that may or may not be updating
 */
class WatchingConfigurationReference<N extends ConfigurationNode> extends ManualConfigurationReference<N> implements Subscriber<WatchEvent<?>> {
    private volatile boolean saveSuppressed = false;
    private @MonotonicNonNull Disposable disposable;

    WatchingConfigurationReference(ConfigurationLoader<? extends N> loader, Executor taskExecutor) {
        super(loader, taskExecutor);
    }

    @Override
    public void save(N newNode) throws IOException {
        synchronized (getLoader()) {
            try {
                saveSuppressed = true;
                super.save(newNode);
            } finally {
                saveSuppressed = false;
            }
        }
    }

    @Override
    public void close() {
        super.close();
        if (disposable != null) {
            disposable.dispose();
        }
    }

    @Override
    public void submit(WatchEvent<?> item) {
        if (!saveSuppressed || item.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
            try {
                load();
            } catch (Exception e) {
                errorListener.submit(Maps.immutableEntry(ErrorPhase.LOADING, e));
            }
        }
    }

    @Override
    public void onError(Throwable e) {
        errorListener.submit(Maps.immutableEntry(ErrorPhase.UNKNOWN, e));
    }

    @Override
    public void onClose() {
        close();
    }

    void setDisposable(Disposable disposable) {
        this.disposable = disposable;
    }
}
