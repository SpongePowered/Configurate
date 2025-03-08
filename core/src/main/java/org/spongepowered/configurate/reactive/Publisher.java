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

import static java.util.Objects.requireNonNull;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.util.CheckedFunction;
import org.spongepowered.configurate.util.CheckedSupplier;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * Something that can publish events.
 *
 * <p>Each subscriber is responsible for removing itself from this stream, by
 * using the Disposable returned upon subscription.
 *
 * @param <V> the type of notification received by subscribers
 * @since 4.0.0
 */
public interface Publisher<V> {

    /**
     * Execute an action returning a single value on the common {@link ForkJoinPool},
     * and pass the result to any subscribers.
     *
     * <p>Subscribers who only begin subscribing after the operation has been
     * completed will receive the result of the operation.
     *
     * @param action the action to perform
     * @param <V> returned value type
     * @param <E> exception thrown
     * @return a publisher
     * @since 4.0.0
     */
    static <V, E extends Exception> Publisher<V> execute(final CheckedSupplier<V, E> action) {
        return execute(action, ForkJoinPool.commonPool());
    }

    /**
     * Execute an action returning a single value on the provided {@link Executor},
     * and pass the result to any subscribers.
     *
     * <p>Subscribers who only begin subscribing after the operation has been
     * completed will receive the result of the operation.
     *
     * @param action the action to perform
     * @param executor the executor to perform this operation on
     * @param <V> returned value type
     * @param <E> exception thrown
     * @return a publisher
     * @since 4.0.0
     */
    static <V, E extends Exception> Publisher<V> execute(final CheckedSupplier<V, E> action, final Executor executor) {
        return new ExecutePublisher<>(requireNonNull(action, "action"), requireNonNull(executor, "executor"));
    }

    /**
     * Subscribe to updates from this Publisher. If this is already closed, the
     * Subscriber will receive an error event with an IllegalStateException, and
     * the returned {@link Disposable} will be a no-op.
     *
     * @param subscriber the listener to register
     * @return a disposable that can be used to cancel this subscription
     * @since 4.0.0
     */
    Disposable subscribe(Subscriber<? super V> subscriber);

    /**
     * Return whether or not this Publisher has any subscribers.
     *
     * <p>In a concurrent environment, this value could change from the time
     * of calling.
     *
     * @return if there are subscribers
     * @since 4.0.0
     */
    boolean hasSubscribers();

    /**
     * Create a new publisher that will transform events published.
     *
     * @param mapper transformer function
     * @param <R> output value type
     * @return a new publisher
     * @since 4.0.0
     */
    default <R> Publisher<R> map(final CheckedFunction<? super V, ? extends R, TransactionFailedException> mapper) {
        return new MappedProcessor<>(mapper, this);
    }

    /**
     * Return a publisher that will track its most recent value. The provided
     * processor won't have a value until one is submitted to this publisher.
     *
     * @return a publisher based on this one
     * @since 4.0.0
     */
    default Cached<V> cache() {
        return cache(null);
    }

    /**
     * Create a cached publisher with an initial value.
     *
     * @param initialValue value to initialize the returned publisher with
     * @return publisher that will cache future responses
     * @since 4.0.0
     */
    default Cached<V> cache(final @Nullable V initialValue) {
        return new CachedPublisher<>(this, initialValue);
    }

    /**
     * Get the executor used to handle published events.
     *
     * @return the executor
     * @since 4.0.0
     */
    Executor executor();

    /**
     * A publisher that caches the last value received.
     *
     * @param <V> value type
     * @since 4.0.0
     */
    interface Cached<V> extends Publisher<V> {

        /**
         * Get the last cached value.
         *
         * @return latest cached value
         * @since 4.0.0
         */
        V get();

        /**
         * Directly submit a value to be the current cached value.
         *
         * <p>This will be passed on to downstream subscribers.</p>
         *
         * @param value new value
         * @since 4.0.0
         */
        void submit(V value);

    }

}
