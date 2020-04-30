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

import java.util.function.Function;

/**
 * Something that can publish events.
 * <p>
 * Each subscriber is responsible for removing itself from this stream, by using the Disposable
 * returned upon subscription
 *
 * @param <V> The type of notification received by subscribers
 */
public interface Publisher<V> {

    /**
     * Subscribe to updates from this Publisher. If this is already closed, the Subscriber will
     * receive an error event with an IllegalStateException, and the returned {@link Disposable}
     * will be a no-op.
     *
     * @param subscriber The listener to register
     * @return A disposable that can be used to cancel this subscription
     */
    Disposable subscribe(Subscriber<? super V> subscriber);

    /**
     * Return whether or not this Publisher has any subscribers.
     * <p>
     * In a concurrent environment, this value could change from the time of calling.
     *
     * @return if there are subscribers
     */
    boolean hasSubscribers();

    default <R> Publisher<R> map(Function<? super V, ? extends R> mapper) {
        return new ProcessorBase.Mapped<>(mapper, this);
    }
    
    default <R> Cached<R> cache() {
        return cache(null);
    }
    
    default <R> Cached<R> cache(R initialValue) {
        throw new UnsupportedOperationException();
    }

    /**
     * A publisher that caches the last value received
     * @param <V> value type
     */
    interface Cached<V> extends Publisher<V> {
       V get();
    }
}
