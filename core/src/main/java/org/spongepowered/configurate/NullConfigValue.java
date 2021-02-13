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
package org.spongepowered.configurate;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;

/**
 * A {@link ConfigValue} which holds no value.
 */
final class NullConfigValue<N extends @Nullable ScopedConfigurationNode<N>,
        A extends @Nullable AbstractConfigurationNode<N, A>> implements ConfigValue<N, A> {

    @SuppressWarnings("rawtypes")
    private static final NullConfigValue INSTANCE = new NullConfigValue();

    @SuppressWarnings("unchecked")
    static <N extends ScopedConfigurationNode<N>, A extends AbstractConfigurationNode<N, A>> NullConfigValue<N, A> instance() {
        return INSTANCE;
    }

    private NullConfigValue() {
    }

    @Override
    public @Nullable Object get() {
        return null;
    }

    @Override
    public void set(final @Nullable Object value) {
        throw new UnsupportedOperationException("Value should be changed from null type before setting value");
    }

    @Override
    public @Nullable A putChild(final Object key, final @Nullable A value) {
        return null;
    }

    @Override
    public @Nullable A putChildIfAbsent(final Object key, final @Nullable A value) {
        return null;
    }

    @Override
    public @Nullable A child(final @Nullable Object key) {
        return null;
    }

    @Override
    public Iterable<A> iterateChildren() {
        return Collections.emptySet();
    }

    @Override
    public NullConfigValue<N, A> copy(final A holder) {
        return instance();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void clear() {
        // empty
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof NullConfigValue;
    }

    @Override
    public int hashCode() {
        return 1009;
    }

    @Override
    public String toString() {
        return "NullConfigValue{}";
    }

}
