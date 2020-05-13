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

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A registration that is transaction-aware
 * @param <V>
 */
interface TransactionalRegistration<V> extends AbstractProcessor.Registration<V> {
    TransactionalProcessorImpl<V> getHolder();

    @Override
    default void dispose() {
        if (getHolder().registrations.remove(this)) {
            getHolder().subscriberCount.getAndDecrement();
        }
    }

    @Override
    default void submit(V value) {
        try {
            beginTransaction(value);
            commit();
        } catch (TransactionFailedException ex) {
            rollback();
        }
    }

    void beginTransaction(V value) throws TransactionFailedException;
    void commit();
    void rollback();

    /**
     * Wrapper to allow non-transactional subscribers to function within a transactional environment
     *
     * @param <V> The value type
     */
    class Wrapped<V> implements TransactionalRegistration<V> {
        private final AtomicReference<V> active = new AtomicReference<>();
        private final TransactionalProcessorImpl<V> holder;
        private final Subscriber<? super V> sub;

        Wrapped(TransactionalProcessorImpl<V> holder, Subscriber <? super V> sub) {
            this.holder = holder;
            this.sub = sub;

        }

        @Override
        public TransactionalProcessorImpl<V> getHolder() {
            return this.holder;
        }

        @Override
        public void beginTransaction(V value) {
            active.set(value);
        }

        public void commit() {
            V active = this.active.getAndSet(null);
            if (active != null) {
                this.sub.submit(active);
            }
        }

        public void rollback() {
            active.set(null);
        }


        @Override
        public void onClose() {
            sub.onClose();
        }

        @Override
        public void onError(Throwable e) {
            sub.onError(e);
        }
    }

    /**
     * A fully transactional registration. To ensure the integrity of values, a lock will be acquired before beginning the transaction, and only released upon a {@link #commit()} or {@link #rollback()}
     * @param <V>
     */
    class Fully<V> implements TransactionalRegistration<V> {

        private final TransactionalProcessorImpl<V> holder;
        private final TransactionalSubscriber<? super V> sub;
        private final Lock lock = new ReentrantLock();

        Fully(final TransactionalProcessorImpl<V> holder, final TransactionalSubscriber<? super V> sub) {
            this.holder = holder;
            this.sub = sub;
        }

        @Override
        public TransactionalProcessorImpl<V> getHolder() {
            return this.holder;
        }

        @Override
        public void beginTransaction(final V value) throws TransactionFailedException {
            lock.lock();
            this.sub.beginTransaction(value);
        }

        @Override
        public void commit() {
            try {
                this.sub.commit();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void rollback() {
            try {
                this.sub.rollback();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void onClose() {
            this.sub.onClose();
        }

        @Override
        public void onError(Throwable e) {
            this.sub.onError(e);
        }
    }
}
