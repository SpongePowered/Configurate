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
import java.util.concurrent.atomic.AtomicReference;

class MappedProcessor<I, O> implements Processor.Transactional<I, O> {

    private final Processor.TransactionalIso<O> processor;
    private final AtomicReference<Disposable> disposable = new AtomicReference<>();
    private final CheckedFunction<? super I, ? extends O, TransactionFailedException> mapper;
    private final @Nullable Publisher<I> parent;

    MappedProcessor(final CheckedFunction<? super I, ? extends O, TransactionFailedException> mapper, final @Nullable Publisher<I> parent) {
        this.processor = parent == null ? Processor.createTransactional() : Processor.createTransactional(parent.executor());
        this.mapper = mapper;
        this.parent = parent;
    }

    @Override
    public Disposable subscribe(final Subscriber<? super O> subscriber) {
        final Disposable ret = this.processor.subscribe(subscriber);
        if (ret != NoOpDisposable.INSTANCE) { // if the processor isn't already closed
            final Disposable ours = this.disposable.updateAndGet(it -> // register with our parent if present
                it == null && this.parent != null ? this.parent.subscribe(this) : it);
            if (ours == NoOpDisposable.INSTANCE) {
                this.processor.onClose();
                return NoOpDisposable.INSTANCE;
            }
            return () -> {
                ret.dispose();
                if (!this.hasSubscribers()) {
                    final Disposable disposable = this.disposable.getAndSet(null);
                    disposable.dispose();
                }
            };
        }
        return ret;
    }

    @Override
    public boolean hasSubscribers() {
        return this.processor.hasSubscribers();
    }

    @Override
    public Executor executor() {
        return this.processor.executor();
    }

    @Override
    public void beginTransaction(final I newValue) throws TransactionFailedException {
        this.processor.beginTransaction(this.mapper.apply(newValue));
    }

    @Override
    public void commit() {
        this.processor.commit();
    }

    @Override
    public void rollback() {
        this.processor.rollback();
    }

    @Override
    public void onError(final Throwable thrown) {
        this.processor.onError(thrown);
    }

    @Override
    public void onClose() {
        final Disposable disposable = this.disposable.getAndSet(null);
        if (disposable != null) {
            disposable.dispose();
        }
        this.processor.onClose();
    }

    @Override
    public void inject(final O element) {
        this.processor.submit(element);
    }

    @Override
    public void fallbackHandler(final @Nullable Subscriber<O> subscriber) {
        this.processor.fallbackHandler(subscriber);
    }

    @Override
    public boolean closeIfUnsubscribed() {
        if (this.processor.closeIfUnsubscribed()) {
            final Disposable disposable = this.disposable.getAndSet(null);
            if (disposable != null) {
                disposable.dispose();
            }
            return true;
        }
        return false;
    }

}
