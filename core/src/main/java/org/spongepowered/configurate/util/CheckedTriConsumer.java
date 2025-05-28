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
 * A functional interface similar to Consumer, but allowing contained methods
 * to have three parameters and throw exceptions.
 *
 * @param <A> first value type
 * @param <B> second value type
 * @param <C> third value type
 * @param <E> the exception type thrown
 * @since 4.3.0
 */
@FunctionalInterface
public interface CheckedTriConsumer<A,B,C,E extends Throwable>{
    /**
     * Consume a values.
     *
     * @param a first value
     * @param b second value
     * @param c third value
     * @throws E exception thrown, described in detail at the use site.
     * @since 4.3.0
     */
    void consume(A a,B b,C c) throws E;
}
