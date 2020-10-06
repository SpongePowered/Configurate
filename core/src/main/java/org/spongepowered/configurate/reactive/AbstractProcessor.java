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

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Base implementation for processors.
 *
 * @param <V> value type
 * @param <R> registration type
 */
abstract class AbstractProcessor<V, R extends AbstractProcessor.Registration<V>> implements Processor.Iso<V> {

    private static final int CLOSED_VALUE = Integer.MIN_VALUE / 2;
    final AtomicInteger subscriberCount = new AtomicInteger();
    volatile @Nullable Subscriber<V> fallbackHandler;
    protected final Set<R> registrations = ConcurrentHashMap.newKeySet();
    protected final Executor executor;

    protected AbstractProcessor(final Executor executor) {
        this.executor = executor;
    }

    @Override
    public Executor getExecutor() {
        return this.executor;
    }

    protected abstract R createRegistration(Subscriber<? super V> sub);

    @Override
    public Disposable subscribe(final Subscriber<? super V> subscriber) {
        if (this.subscriberCount.get() < 0 || this.subscriberCount.incrementAndGet() <= 0) {
            subscriber.onError(new IllegalStateException("Processor " + this + " is already "
                    + "closed!"));
            this.subscriberCount.set(CLOSED_VALUE);
            return NoOpDisposable.INSTANCE;
        }
        final R reg = createRegistration(subscriber);
        this.registrations.add(reg);
        return reg;
    }

    @Override
    public boolean hasSubscribers() {
        return this.subscriberCount.get() > 0;
    }

    @Override
    public void onError(final Throwable thrown) {
        Processor.Iso.super.onError(thrown);
        onClose();
    }

    @Override
    public void onClose() {
        this.executor.execute(() -> {
            this.subscriberCount.set(CLOSED_VALUE);
            for (Registration<V> reg : this.registrations) {
                try {
                    reg.onClose();
                } catch (final Exception t) {
                    // not much we can do here, maybe log?
                }
            }
            this.registrations.clear();
        });
    }

    /**
     * Perform an action on each registration, deregistering and calling {@link Registration#onError(Throwable)} to
     * notify in the event of an error.
     *
     * @param processor the processor
     */
    protected void forEachOrRemove(final Consumer<R> processor) {
        for (final Iterator<R> it = this.registrations.iterator(); it.hasNext(); ) {
            final R reg = it.next();
            try {
                processor.accept(reg);
            } catch (final Exception t) {
                it.remove();
                this.subscriberCount.getAndDecrement();
                try {
                    reg.onError(t);
                } catch (final Exception t2) { // really? how rude
                    Processor.Iso.super.onError(t2); // just use the uncaught exception handler... oh well
                }
            }
        }
    }

    @Override
    public void setFallbackHandler(final @Nullable Subscriber<V> subscriber) {
        this.fallbackHandler = subscriber;
    }

    @Override
    public boolean closeIfUnsubscribed() {
        this.executor.execute(() -> {
            if (this.subscriberCount.compareAndSet(0, CLOSED_VALUE)) {
                for (Registration<V> reg : this.registrations) {
                    reg.onClose();
                }
                this.registrations.clear();
            }
        });
        return this.subscriberCount.get() <= 0; // will close or already closed
    }

    /**
     * A registered subscriber.
     *
     * <p>methods mostly replicate those in {@link Subscriber}, delegating to
     * the underlying class
     */
    protected interface Registration<V> extends Disposable {

        void submit(V value);

        void onClose();

        void onError(Throwable e);

    }

}
