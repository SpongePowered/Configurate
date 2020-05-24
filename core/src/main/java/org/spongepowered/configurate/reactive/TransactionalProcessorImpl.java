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
import java.util.concurrent.Executor;

class TransactionalProcessorImpl<V> extends AbstractProcessor<V, TransactionalRegistration<V>> implements Processor.TransactionalIso<V> {

    protected TransactionalProcessorImpl(final Executor executor) {
        super(executor);
    }

    @Override
    public void submit(final V item) {
        executor.execute(() -> TransactionalIso.super.submit(item));
    }

    @Override
    public void beginTransaction(final V newValue) throws TransactionFailedException {
        if (this.subscriberCount.get() >= 0) {
            boolean handled = false;
            for (Iterator<TransactionalRegistration<V>> it = this.registrations.iterator(); it.hasNext(); ) {
                final TransactionalRegistration<V> reg = it.next();
                try {
                    handled = true;
                    reg.beginTransaction(newValue);
                } catch (final TransactionFailedException ex) {
                    throw ex;
                } catch (final Exception t) {
                    it.remove();
                    subscriberCount.getAndDecrement();
                    reg.onError(t);
                }
            }
            if (!handled) {
                final @Nullable Subscriber<V> fallback = this.fallbackHandler;
                if (fallback != null) {
                    fallback.submit(newValue);
                }
            }
        }
    }

    @Override
    public void commit() {
        forEachOrRemove(TransactionalRegistration::commit);
    }

    @Override
    public void rollback() {
        forEachOrRemove(TransactionalRegistration::rollback);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected TransactionalRegistration<V> createRegistration(final Subscriber<? super V> sub) {
        if (sub instanceof TransactionalSubscriber) {
            return new TransactionalRegistration.Fully<>(this, (TransactionalSubscriber<V>) sub);
        } else {
            return new TransactionalRegistration.Wrapped<>(this, sub);
        }
    }

}
