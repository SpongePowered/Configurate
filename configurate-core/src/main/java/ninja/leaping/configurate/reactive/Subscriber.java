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
 * A listener to events that may be called by an {@link Publisher}
 *
 * @param <V> The value that will be received
 */
@FunctionalInterface
public interface Subscriber<V> {
    /**
     * Called to submit a new item
     *
     * @param item The item available
     */
    void submit(V item);

    /**
     * When an error occurs while subscribing to an {@link Publisher}, or is thrown during the
     * execution of {@link #submit(Object)} that is not otherwise handled, this method will be
     * called with the error. The associated {@link Publisher} will not send further update signals
     * after an error is thrown.
     *
     * @param e The exception thrown
     */
    default void onError(Throwable e) {
        Thread t = Thread.currentThread();
        t.getUncaughtExceptionHandler().uncaughtException(t, e);
    }

    /**
     * When the {@link Publisher} this is subscribed to closes without error, this method will be
     * called.
     */
    default void onClose() {
        // no-op
    }
}
