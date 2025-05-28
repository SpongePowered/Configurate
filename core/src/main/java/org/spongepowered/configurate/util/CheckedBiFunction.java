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

/**
 * A function with two inputs and one output which may throw a checked exception.
 *
 * @param <A> the first input parameter type
 * @param <B> the second input parameter type
 * @param <R> the output parameter type
 * @param <E> the type thrown
 * @since 4.3.0
 */
@FunctionalInterface
public interface CheckedBiFunction<A,B,R,E extends Throwable>{
    /**
     * Perform the action.
     *
     * @param a first parameter
     * @param b second parameter
     * @return return value
     * @throws E thrown when defined by types accepting this function
     * @since 4.3.0
     */
    R accept(A a,B b) throws E;
}
