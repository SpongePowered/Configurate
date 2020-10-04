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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.util.CheckedFunction;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * A combination of an {@link Publisher} and {@link Subscriber}.
 *
 * <p>Processors are expected to broadcast their submitted values to any
 * registered observers, though filtering or other transformations may
 * be applied.
 *
 * <p>Submitting a completion event to the processor will result in a completion
 * event being passed to every subscriber, and the rejection of further events
 * being submitted.
 *
 * @param <I> The type observed
 * @param <O> The type produced
 */
public interface Processor<I, O> extends Publisher<O>, Subscriber<I> {

    /**
     * Create a {@link Processor} instance that simply broadcasts submitted
     * values to its subscribers. Broadcasts will occur on the
     * common {@link ForkJoinPool}.
     *
     * @param <V> The type
     * @return A new processor instance
     */
    static <V> Processor.Iso<V> create() {
        return create(ForkJoinPool.commonPool());
    }

    /**
     * Create a {@link Processor} instance that simply broadcasts submitted
     * values to its subscribers.
     *
     * @param <V> The type
     * @param executor task executor
     * @return A new processor instance
     */
    static <V> Processor.Iso<V> create(Executor executor) {
        return new ProcessorImpl<>(executor);
    }

    /**
     * Create a processor instance that is aware of transactions.
     *
     * @param <V> The value type
     * @return a new transactional processor
     */
    static <V> Processor.TransactionalIso<V> createTransactional() {
        return createTransactional(ForkJoinPool.commonPool());
    }

    /**
     * Create a processor instance that is aware of transactions.
     *
     * <p>Operations will be submitted to the provided executor.</p>
     *
     * @param exec executor to run operations on
     * @param <V> The value type
     * @return a new transactional processor
     */
    static <V> Processor.TransactionalIso<V> createTransactional(final Executor exec) {
        return new TransactionalProcessorImpl<>(exec);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default <R> Processor<O, R> map(CheckedFunction<? super O, ? extends R, TransactionFailedException> mapper) {
        return new MappedProcessor<>(mapper, this);
    }

    /**
     * Submit an element of the observed type, bypassing any mapping this
     * Processor may do. If the input type of this processor equals the output
     * type, this is equivalent to {@link #submit(Object)}
     *
     * @param element The element to submit
     */
    void inject(O element);

    /**
     * Provide a {@link Subscriber} that will handle events submitted to this
     * processor, but only if no other subscription is active.
     *
     * @param subscriber The fallback subscriber to add. Provide {@code null} to
     *                   remove the handler
     */
    void setFallbackHandler(@Nullable Subscriber<O> subscriber);

    /**
     * Close this processor if there are no remaining subscriptions. Any signals
     * that have already been submitted will be processed.
     *
     * <p>Any call to this method after the {@link Processor} has been closed
     * will simply return true.
     *
     * @return true if there are no subscribers and this processor is closed
     */
    boolean closeIfUnsubscribed();

    /**
     * A Processor that has the same type for inputs and outputs.
     *
     * @param <V> The input and output type
     */
    interface Iso<V> extends Processor<V, V> {
        @Override
        default void inject(V element) {
            submit(element);
        }
    }

    /**
     * A processor that supports transactions.
     *
     * @param <I> input type
     * @param <O> output type
     */
    interface Transactional<I, O> extends Processor<I, O>, Publisher<O>, TransactionalSubscriber<I> {
    }

    /**
     * A processor that supports transactions using the same input and outputs.
     *
     * @param <V> input/output type
     */
    interface TransactionalIso<V> extends Transactional<V, V>, Iso<V> {

    }

}
