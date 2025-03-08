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

/**
 * A listener to events that may be called by an {@link Publisher}.
 *
 * <p>For every publisher this subscriber is subscribed to, the subscriber will
 * only process one event at a time -- effectively, within a single publisher
 * this subscriber does not have to be aware of concurrent effects.
 *
 * @param <V> the value that will be received
 * @since 4.0.0
 */
@FunctionalInterface
public interface Subscriber<V> {

    /**
     * Called to submit a new item.
     *
     * @param item the item available
     * @since 4.0.0
     */
    void submit(V item);

    /**
     * When an error occurs while subscribing to an {@link Publisher}, or is
     * thrown during the execution of {@link #submit(Object)} that is not
     * otherwise handled, this method will be called with the error. The
     * associated {@link Publisher} will not send further update signals after
     * an error is thrown.
     *
     * @param thrown the exception thrown
     * @since 4.0.0
     */
    default void onError(final Throwable thrown) {
        final Thread t = Thread.currentThread();
        t.getUncaughtExceptionHandler().uncaughtException(t, thrown);
    }

    /**
     * When the {@link Publisher} this is subscribed to closes without error,
     * this method will be called.
     *
     * @since 4.0.0
     */
    default void onClose() {
        // no-op
    }

}
