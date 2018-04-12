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

import java.util.concurrent.ConcurrentMap;

/**
 * A factory which creates {@link ConcurrentMap} instances.
 */
@FunctionalInterface
public interface MapFactory {

    /**
     * Create a new map instance for the given types
     *
     * @param <K> The key
     * @param <V> The value
     * @return A new map instance
     */
    @NonNull
    <K, V> ConcurrentMap<K, V> create();

}
