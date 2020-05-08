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
package ninja.leaping.configurate.util;

import java.util.function.Supplier;

/**
 * A functional interface similar to Supplier, except allowing contained methods to throw exceptions
 *
 * @param <V> The value returned
 * @param <E> The exception type thrown
 */
@FunctionalInterface
public interface CheckedSupplier<V, E extends Throwable> {
    V get() throws E;

    /**
     * Create an instance from an ordinary supplier
     *
     * @param consumer The supplier to convert
     * @param <V> The type returned by the consumer
     * @return A function that executes the provided consumer
     */
    static <V> CheckedSupplier<V, RuntimeException> fromSupplier(Supplier<V> consumer) {
        return consumer::get;
    }
}
