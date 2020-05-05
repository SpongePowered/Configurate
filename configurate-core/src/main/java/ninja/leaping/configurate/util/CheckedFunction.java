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

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * A function with one input and one output which may throw a checked exception
 *
 * @param <I> The input parameter type
 * @param <O> The output parameter type
 * @param <E> The type thrown
 */
@FunctionalInterface
public interface CheckedFunction<I, O, E extends Exception> {
    /**
     * Perform the action.
     *
     * @param one parameter
     * @return return value
     * @throws E Thrown when defined by types accepting this function
     */
    O apply(I one) throws E;

    /**
     * Convert a JDK {@link Function} into its checked variant
     *
     * @param func The function
     * @param <I> Parameter type
     * @param <O> return type
     * @return The function as a checked function
     */
    static <I, O> CheckedFunction<I, O, RuntimeException> fromFunction(Function<I, @NonNull O> func) {
        return requireNonNull(func, "func")::apply;
    }
}
