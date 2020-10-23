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

import net.kyori.coffee.function.Function0E;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A publisher that handles a single value submitted through
 * a CompletableFuture.
 *
 * <p>When subscribed after the original action is complete, the original result
 * of the future will be submitted.
 *
 * @param <V> value type
 */
class ExecutePublisher<V> implements Publisher<V> {

    private final CompletableFuture<V> actor;
    private final Executor executor;

    ExecutePublisher(final Function0E<V, ? extends Exception> action, final Executor exec) {
        this.actor = new CompletableFuture<>();
        exec.execute(() -> {
            try {
                this.actor.complete(action.apply());
            } catch (final Exception ex) {
                this.actor.completeExceptionally(ex);
            }
        });
        this.executor = exec;
    }

    @Override
    public Disposable subscribe(final Subscriber<? super V> subscriber) {
        final AtomicBoolean subscribed = new AtomicBoolean();
        this.actor.whenCompleteAsync((value, err) -> {
            if (subscribed.compareAndSet(true, false)) { // guard against multiple values
                if (err != null) {
                    subscriber.onError(err);
                } else {
                    try {
                        subscriber.submit(value);
                        subscriber.onClose();
                    } catch (final Exception t) {
                        subscriber.onError(t);
                    }
                }
            }
        }, this.executor).exceptionally(ex -> {
            throw new Error(ex);
        });
        return () -> subscribed.set(false);
    }

    @Override
    public boolean hasSubscribers() {
        return !this.actor.isDone() && this.actor.getNumberOfDependents() > 0;
    }

    @Override
    public Executor executor() {
        return this.executor;
    }

}
