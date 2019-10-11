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
package ninja.leaping.configurate.reactive;

/**
 * A registration for non-transactional processors
 * @param <V> The value type
 */
class RegistrationImpl<V> implements ProcessorAbstract.Registration<V> {
    final ProcessorImpl<V> holder;
    final Subscriber<? super V> subscriber;

    RegistrationImpl(ProcessorImpl<V> holder, Subscriber<? super V> subscriber) {
        this.holder = holder;
        this.subscriber = subscriber;
    }

    @Override
    public void dispose() {
        if (holder.registrations.remove(this)) {
            holder.subscriberCount.getAndDecrement();
        }
    }

    @Override
    public void submit(V newValue) {
        subscriber.submit(newValue);
    }

    @Override
    public void onClose() {
        subscriber.onClose();
    }

    @Override
    public void onError(Throwable e) {
        subscriber.onError(e);
    }
}
