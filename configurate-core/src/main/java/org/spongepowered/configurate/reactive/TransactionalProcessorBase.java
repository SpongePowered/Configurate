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

class TransactionalProcessorBase<V> extends ProcessorBase<V> implements Processor.TransactionalIso<V> {

    @Override
    public void beginTransaction(final V newValue) throws TransactionFailedException {
        if (this.subscriberCount.get() >= 0) {
            boolean handled = false;
            for (Iterator<Registration> it = this.registrations.iterator(); it.hasNext(); ) {
                Registration reg = it.next();
                try {
                    handled = true;
                    reg.subscriber.submit(newValue);
                } catch (Throwable t) {
                    it.remove();
                    subscriberCount.getAndDecrement();
                    reg.subscriber.onError(t);
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
        this.registrations.forEach(r -> {
            if (r.subscriber instanceof TransactionalSubscriber<?>) {
                ((TransactionalSubscriber<?>) r.subscriber).commit();
            }
        });

    }

    @Override
    public void rollback() {
        this.registrations.forEach(r -> {
            /*if (r instanceof TransactionalWrapperRegistration) {
                ((TransactionalSubscriber<?>) r.subscriber).rollback();
            }*/
        });
    }
}
