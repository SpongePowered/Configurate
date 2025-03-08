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
package org.spongepowered.configurate.util;

import java.util.function.Consumer;

/**
 * A functional interface similar to Consumer, except allowing contained methods
 * to throw exceptions.
 *
 * @param <V> the value accepted
 * @param <E> the exception type thrown
 * @since 4.0.0
 */
@FunctionalInterface
public interface CheckedConsumer<V, E extends Throwable> {

    /**
     * Accept a value.
     *
     * @param value value excepted
     * @throws E exception thrown, described in detail at the use site.
     * @since 4.0.0
     */
    void accept(V value) throws E;

    /**
     * Create an instance from an ordinary consumer.
     *
     * @param consumer the consumer to convert
     * @param <V> the type returned by the consumer
     * @return a function that executes the provided consumer
     * @since 4.0.0
     */
    static <V> CheckedConsumer<V, RuntimeException> from(final Consumer<V> consumer) {
        return consumer::accept;
    }

}
