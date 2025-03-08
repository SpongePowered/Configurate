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

import java.util.function.Supplier;

/**
 * A functional interface similar to Supplier, except allowing contained methods
 * to throw exceptions.
 *
 * @param <V> the value returned
 * @param <E> the exception type thrown
 * @since 4.0.0
 */
@FunctionalInterface
public interface CheckedSupplier<V, E extends Throwable> {

    /**
     * Perform an operation that returns a value.
     *
     * @return the result value
     * @throws E an implementation-dependent error
     * @since 4.0.0
     */
    V get() throws E;

    /**
     * Create an instance from an ordinary supplier.
     *
     * @param consumer the supplier to convert
     * @param <V> the type returned by the consumer
     * @return a function that executes the provided consumer
     * @since 4.0.0
     */
    static <V> CheckedSupplier<V, RuntimeException> from(final Supplier<V> consumer) {
        return consumer::get;
    }

}
