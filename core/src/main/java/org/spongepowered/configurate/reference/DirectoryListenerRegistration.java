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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.reactive.Disposable;
import org.spongepowered.configurate.reactive.Processor;
import org.spongepowered.configurate.reactive.Subscriber;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Data class holding listeners for a base directory and its children.
 */
class DirectoryListenerRegistration implements Subscriber<WatchEvent<?>> {

    private final Lock lock = new ReentrantLock();
    private final AtomicBoolean acceptingRegistrations = new AtomicBoolean(true);
    private final WatchKey key;
    private final ConcurrentMap<Path, Processor<WatchEvent<?>, WatchEvent<?>>> fileListeners
        = new ConcurrentHashMap<>();
    private final Executor executor;
    private final Processor<WatchEvent<?>, WatchEvent<?>> dirListeners;

    DirectoryListenerRegistration(final WatchKey key, final Executor executor) {
        this.key = requireNonNull(key, "key");
        this.executor = requireNonNull(executor, "executor");
        this.dirListeners = Processor.create(executor);
    }

    public WatchKey key() {
        return this.key;
    }

    @Override
    public void submit(final WatchEvent<?> item) {
        if (!this.acceptingRegistrations.get()) {
            return;
        }

        final Path file = (Path) item.context();
        final @Nullable Processor<WatchEvent<?>, WatchEvent<?>> fileListeners =
                this.fileListeners.computeIfPresent(file, (key, old) -> old.closeIfUnsubscribed() ? null : old);
        this.dirListeners.submit(item);
        if (fileListeners != null) {
            fileListeners.submit(item);
        }
    }

    @Override
    public void onClose() {
        this.lock.lock();
        try {
            try {
                this.dirListeners.onClose();
            } catch (final Exception t) {
                this.dirListeners.onError(t);
            }

            this.fileListeners.forEach((k, v) -> {
                try {
                    v.onClose();
                } catch (final Exception t) {
                    v.onError(t);
                }
            });

            this.fileListeners.clear();
            this.key.cancel();
        } finally {
            this.lock.unlock();
        }
    }

    public Disposable subscribe(final Subscriber<WatchEvent<?>> subscriber) {
        if (!this.acceptingRegistrations.get()) {
            return () -> {};
        }

        this.lock.lock();
        try {
            return this.dirListeners.subscribe(subscriber);
        } finally {
            this.lock.unlock();
        }
    }

    public Disposable subscribe(final Path file, final Subscriber<WatchEvent<?>> subscriber) {
        if (!this.acceptingRegistrations.get()) {
            return () -> {};
        }

        this.lock.lock();
        try {
            return this.fileListeners.computeIfAbsent(file, f -> Processor.create(this.executor)).subscribe(subscriber);
        } finally {
            this.lock.unlock();
        }
    }

    public boolean hasSubscribers() {
        this.lock.lock();
        try {
            return this.dirListeners.hasSubscribers() || !this.fileListeners.isEmpty();
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof DirectoryListenerRegistration)) {
            return false;
        }

        final DirectoryListenerRegistration that = (DirectoryListenerRegistration) o;
        return key().equals(that.key())
            && this.fileListeners.equals(that.fileListeners)
            && this.dirListeners.equals(that.dirListeners);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key(), this.fileListeners, this.dirListeners);
    }

    public boolean closeIfEmpty() {
        this.lock.lock();
        try {
            if (!hasSubscribers()) {
                onClose();
                return true;
            }
        } finally {
            this.lock.unlock();
        }
        return false;
    }

}
