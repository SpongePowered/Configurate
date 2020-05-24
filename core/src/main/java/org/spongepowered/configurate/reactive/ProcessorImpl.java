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

// Java 9 has reactive API... but we can't use that :(
class ProcessorImpl<V> extends AbstractProcessor<V, RegistrationImpl<V>> implements Processor.Iso<V> {

    ProcessorImpl(final Executor exec) {
        super(exec);
    }

    @Override
    public void submit(final V value) {
        if (this.subscriberCount.get() >= 0) {
            boolean handled = false;
            for (Iterator<RegistrationImpl<V>> it = this.registrations.iterator(); it.hasNext(); ) {
                final RegistrationImpl<V> reg = it.next();
                try {
                    handled = true;
                    reg.submit(value);
                } catch (final Exception t) {
                    it.remove();
                    this.subscriberCount.getAndDecrement();
                    reg.subscriber.onError(t);
                }
            }
            if (!handled) {
                final @Nullable Subscriber<V> fallback = this.fallbackHandler;
                if (fallback != null) {
                    fallback.submit(value);
                }
            }
        }
    }

    @Override
    protected RegistrationImpl<V> createRegistration(final Subscriber<? super V> sub) {
        return new RegistrationImpl<>(this, sub);
    }

}
