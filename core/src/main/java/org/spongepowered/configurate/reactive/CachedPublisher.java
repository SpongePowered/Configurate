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
package org.spongepowered.configurate.reactive;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * Implementation of a caching publisher
 *
 * <p>All subscriptions are handled by the parent publisher, so transactional
 * and non-transactional subscribers can be handled appropriately by the
 * parent publisher.
 *
 * @param <V> value type
 */
class CachedPublisher<V> implements Publisher.Cached<V>, AutoCloseable {

    private final Publisher<V> parent;
    private final Set<Subscriber<? super V>> subscribers = ConcurrentHashMap.newKeySet();
    private volatile @MonotonicNonNull V value;
    private final Disposable closer;

    CachedPublisher(final Publisher<V> parent, final @Nullable V initialValue) {
        this.parent = parent;
        if (initialValue != null) {
            this.value = initialValue;
        }
        this.closer = this.parent.subscribe(next -> this.value = next);
    }

    @Override
    public Disposable subscribe(final Subscriber<? super V> subscriber) {
        final Disposable disp = this.parent.subscribe(subscriber);
        if (disp != NoOpDisposable.INSTANCE) {
            this.subscribers.add(subscriber);
            final V value = this.value;
            if (value != null) {
                subscriber.submit(value);
            }
            return () -> {
                this.subscribers.remove(subscriber);
                disp.dispose();
            };
        } else {
            return disp;
        }
    }

    @Override
    public boolean hasSubscribers() {
        return !this.subscribers.isEmpty();
    }

    @Override
    public Cached<V> cache() {
        return this;
    }

    @Override
    public Cached<V> cache(final @Nullable V initialValue) {
        if (this.value == null && initialValue != null) {
            this.value = initialValue;
        }
        return this;
    }

    @Override
    public Executor executor() {
        return this.parent.executor();
    }

    @Override
    public V get() {
        return this.value;
    }

    @Override
    public void submit(final V value) {
        this.value = value;
        this.subscribers.forEach(it -> it.submit(value));
    }

    @Override
    public void close() {
        this.closer.dispose();
    }

}
