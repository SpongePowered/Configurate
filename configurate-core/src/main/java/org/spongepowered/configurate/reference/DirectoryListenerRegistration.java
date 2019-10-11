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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.reactive.Disposable;
import org.spongepowered.configurate.reactive.Processor;
import org.spongepowered.configurate.reactive.Subscriber;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Objects.requireNonNull;

/**
 * Data class holding listeners for a base directory and its children
 */
class DirectoryListenerRegistration implements Subscriber<WatchEvent<?>> {
    private final Lock lock = new ReentrantLock();
    private final WatchKey key;
    private final ConcurrentHashMap<Path, Processor<WatchEvent<?>, WatchEvent<?>>> fileListeners
        = new ConcurrentHashMap<>();
    private final Processor<WatchEvent<?>, WatchEvent<?>> dirListeners = Processor.create();

    DirectoryListenerRegistration(WatchKey key) {
        this.key = requireNonNull(key, "key");
    }

    public WatchKey getKey() {
        return key;
    }

    @Override
    public void submit(WatchEvent<?> item) {
        final Path file = (Path) item.context();
        lock.lock();
        try {
            @Nullable Processor<WatchEvent<?>, WatchEvent<?>> fileListeners
                = this.fileListeners.computeIfPresent(file,
                (key, old) -> old.closeIfUnsubscribed() ? null : old);
            dirListeners.submit(item);
            if (fileListeners != null) {
                fileListeners.submit(item);
            }

        } finally {
            lock.unlock();
        }
    }

    @Override
    public void onClose() {
        lock.lock();
        try {
            try {
                dirListeners.onClose();
            } catch (Throwable t) {
                dirListeners.onError(t);
            }

            fileListeners.forEach((k, v) -> {
                try {
                    v.onClose();
                } catch (Throwable t) {
                    v.onError(t);
                }
            });

            fileListeners.clear();
            key.cancel();
        } finally {
            lock.unlock();
        }
    }

    public Disposable subscribe(Subscriber<WatchEvent<?>> subscriber) {
        lock.lock();
        try {
            return dirListeners.subscribe(subscriber);
        } finally {
            lock.unlock();
        }
    }

    public Disposable subscribe(Path file, Subscriber<WatchEvent<?>> subscriber) {
        lock.lock();
        try {
            return fileListeners.computeIfAbsent(file, f -> Processor.create()).subscribe(subscriber);
        } finally {
            lock.unlock();
        }
    }

    public boolean hasSubscribers() {
        lock.lock();
        try {
            return dirListeners.hasSubscribers() || !fileListeners.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DirectoryListenerRegistration)) return false;
        DirectoryListenerRegistration that = (DirectoryListenerRegistration) o;
        return getKey().equals(that.getKey()) &&
            fileListeners.equals(that.fileListeners) &&
            dirListeners.equals(that.dirListeners);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey(), fileListeners, dirListeners);
    }

    public boolean closeIfEmpty() {
        lock.lock();
        try {
            if (!hasSubscribers()) {
                onClose();
                return true;
            }
        } finally {
            lock.unlock();
        }
        return false;
    }
}
