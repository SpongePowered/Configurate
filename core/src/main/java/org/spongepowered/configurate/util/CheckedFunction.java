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

import static java.util.Objects.requireNonNull;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Function;

/**
 * A function with one input and one output which may throw a checked exception.
 *
 * @param <I> the input parameter type
 * @param <O> the output parameter type
 * @param <E> the type thrown
 * @since 4.0.0
 */
@FunctionalInterface
public interface CheckedFunction<I, O, E extends Exception> {

    /**
     * Perform the action.
     *
     * @param one parameter
     * @return return value
     * @throws E thrown when defined by types accepting this function
     * @since 4.0.0
     */
    O apply(I one) throws E;

    /**
     * Convert a JDK {@link Function} into its checked variant.
     *
     * @param func the function
     * @param <I> parameter type
     * @param <O> return type
     * @return the function as a checked function
     * @since 4.0.0
     */
    static <I, O> CheckedFunction<I, O, RuntimeException> from(final Function<I, @NonNull O> func) {
        return requireNonNull(func, "func")::apply;
    }

}
