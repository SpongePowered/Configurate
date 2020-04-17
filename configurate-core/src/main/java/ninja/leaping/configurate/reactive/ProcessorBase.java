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

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

// Java 9 has reactive API... but we can't use that :(
class ProcessorBase<V> implements Processor.Iso<V> {
    private final int CLOSED_VALUE = Integer.MIN_VALUE / 2;
    private final Set<Registration> registrations = ConcurrentHashMap.newKeySet();
    private final AtomicInteger subscriberCount = new AtomicInteger();
    private volatile @Nullable Subscriber<V> fallbackHandler;

    @Override
    public Disposable subscribe(Subscriber<? super V> subscriber) {
        if (subscriberCount.get() < 0 || subscriberCount.incrementAndGet() <= 0) {
            subscriber.onError(new IllegalStateException("Processor " + this + " is already " +
                "closed!"));
            subscriberCount.set(CLOSED_VALUE);
            return NoOpDisposable.INSTANCE;
        }
        Registration reg = new Registration(subscriber);
        registrations.add(reg);
        return reg;
    }

    @Override
    public boolean hasSubscribers() {
        return subscriberCount.get() > 0;
    }

    @Override
    public void submit(V value) {
        if (this.subscriberCount.get() >= 0) {
            boolean handled = false;
            for (Iterator<Registration> it = this.registrations.iterator(); it.hasNext(); ) {
                Registration reg = it.next();
                try {
                    handled = true;
                    reg.subscriber.submit(value);
                } catch (Throwable t) {
                    it.remove();
                    subscriberCount.getAndDecrement();
                    reg.subscriber.onError(t);
                }
            }
            if (!handled) {
                @Nullable Subscriber<V> fallback = this.fallbackHandler;
                if (fallback != null) {
                    fallback.submit(value);
                }
            }
        }
    }

    @Override
    public void onError(final Throwable e) {
        Processor.Iso.super.onError(e);
        onClose();
    }

    @Override
    public void onClose() {
        subscriberCount.set(CLOSED_VALUE);
        for (Registration reg : registrations) {
            reg.subscriber.onClose();
        }
        registrations.clear();
    }

    @Override
    public void setFallbackHandler(@Nullable final Subscriber<V> subscriber) {
        this.fallbackHandler = subscriber;
    }

    @Override
    public boolean closeIfUnsubscribed() {
        if (subscriberCount.compareAndSet(0, CLOSED_VALUE)) {
            for (Registration reg : registrations) {
                reg.subscriber.onClose();
            }
            registrations.clear();
            return true;
        }

        return subscriberCount.get() < 0; // already closed
    }

    class Registration implements Disposable {
        private final Subscriber<? super V> subscriber;

        Registration(Subscriber<? super V> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void dispose() {
            if (registrations.remove(this)) {
                subscriberCount.getAndDecrement();
            }
        }
    }

    static class Mapped<I, O> implements Processor<I, O> {
        private final Processor<O, O> processor = Processor.create();
        private final AtomicReference<Disposable> disposable = new AtomicReference<>();
        private final Function<? super I, ? extends O> mapper;
        private final @Nullable Publisher<I> parent;

        Mapped(Function<? super I, ? extends O> mapper, @Nullable Publisher<I> parent) {
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
        public void submit(I item) {
            processor.submit(mapper.apply(item));
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
}
