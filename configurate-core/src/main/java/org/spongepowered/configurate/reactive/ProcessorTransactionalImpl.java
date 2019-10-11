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

class ProcessorTransactionalImpl<V> extends ProcessorAbstract<V, RegistrationTransactional<V>> implements Processor.TransactionalIso<V> {

    protected ProcessorTransactionalImpl(Executor executor) {
        super(executor);
    }

    @Override
    public void submit(V item) {
        executor.execute(() -> {
            Processor.TransactionalIso.super.submit(item);
        });
    }

    @Override
    public void beginTransaction(final V newValue) throws TransactionFailedException {
        if (this.subscriberCount.get() >= 0) {
            boolean handled = false;
            for (Iterator<RegistrationTransactional<V>> it = this.registrations.iterator(); it.hasNext(); ) {
                RegistrationTransactional<V> reg = it.next();
                try {
                    handled = true;
                    reg.beginTransaction(newValue);
                } catch (TransactionFailedException ex) {
                    throw ex;
                } catch (Throwable t) {
                    it.remove();
                    subscriberCount.getAndDecrement();
                    reg.onError(t);
                }
            }
            if (!handled) {
                @Nullable Subscriber<V> fallback = this.fallbackHandler;
                if (fallback != null) {
                    fallback.submit(newValue);
                }
            }
        }
    }

    @Override
    public void commit() {
        forEachOrRemove(RegistrationTransactional::commit);
    }

    @Override
    public void rollback() {
        forEachOrRemove(RegistrationTransactional::rollback);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected RegistrationTransactional<V> createRegistration(Subscriber<? super V> sub) {
        if (sub instanceof SubscriberTransactional) {
            return new RegistrationTransactional.Fully<>(this, (SubscriberTransactional<V>) sub);
        } else {
            return new RegistrationTransactional.Wrapped<>(this, sub);
        }
    }
}
