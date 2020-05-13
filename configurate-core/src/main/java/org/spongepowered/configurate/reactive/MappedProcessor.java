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

    MappedProcessor(CheckedFunction<? super I, ? extends O, TransactionFailedException> mapper, @Nullable Publisher<I> parent) {
        this.processor = Processor.createTransactional(parent.getExecutor());
        this.mapper = mapper;
        this.parent = parent;
    }

    @Override
    public Disposable subscribe(Subscriber<? super O> subscriber) {
        Disposable ret = this.processor.subscribe(subscriber);
        if (ret != NoOpDisposable.INSTANCE) { // if the processor isn't already closed
            Disposable ours = this.disposable.updateAndGet(it ->  // register with our parent if present
                it == null && parent != null ? parent.subscribe(this) : it);
            if (ours == NoOpDisposable.INSTANCE) {
                processor.onClose();
                return NoOpDisposable.INSTANCE;
            }
            return () -> {
                ret.dispose();
                if (!hasSubscribers()) {
                    Disposable disposable = this.disposable.getAndSet(null);
                    disposable.dispose();
                }
            };
        }
        return ret;
    }

    @Override
    public boolean hasSubscribers() {
        return processor.hasSubscribers();
    }

    @Override
    public Executor getExecutor() {
        return processor.getExecutor();
    }

    @Override
    public void beginTransaction(I newValue) throws TransactionFailedException {
        processor.beginTransaction(mapper.apply(newValue));
    }

    @Override
    public void commit() {
        processor.commit();
    }

    @Override
    public void rollback() {
        processor.rollback();
    }

    @Override
    public void onError(final Throwable e) {
        processor.onError(e);
    }

    @Override
    public void onClose() {
        Disposable disposable = this.disposable.getAndSet(null);
        if (disposable != null) {
            disposable.dispose();
        }
        processor.onClose();
    }

    @Override
    public void inject(final O element) {
        processor.submit(element);
    }

    @Override
    public void setFallbackHandler(@Nullable final Subscriber<O> subscriber) {
        this.processor.setFallbackHandler(subscriber);
    }

    @Override
    public boolean closeIfUnsubscribed() {
        if (processor.closeIfUnsubscribed()) {
            Disposable disposable = this.disposable.getAndSet(null);
            if (disposable != null) {
                disposable.dispose();
            }
            return true;
        }
        return false;
    }
}
