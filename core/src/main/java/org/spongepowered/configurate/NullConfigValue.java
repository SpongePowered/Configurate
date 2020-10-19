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
final class NullConfigValue<N extends ScopedConfigurationNode<N>, T extends AbstractConfigurationNode<N, T>> extends ConfigValue<N, T> {

    @SuppressWarnings("rawtypes")
    private static final NullConfigValue INSTANCE = new NullConfigValue();

    @SuppressWarnings("unchecked")
    static <N extends ScopedConfigurationNode<N>, A extends AbstractConfigurationNode<N, A>> NullConfigValue<N, A> instance() {
        return INSTANCE;
    }

    @SuppressWarnings({"ConstantConditions"})
    private NullConfigValue() {
        super(null);
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
    @Nullable T putChild(final Object key, final @Nullable T value) {
        return null;
    }

    @Override
    @Nullable T putChildIfAbsent(final Object key, final @Nullable T value) {
        return null;
    }

    @Override
    public @Nullable T child(final @Nullable Object key) {
        return null;
    }

    @Override
    public Iterable<T> iterateChildren() {
        return Collections.emptySet();
    }

    @Override
    NullConfigValue<N, T> copy(final T holder) {
        return instance();
    }

    @Override
    boolean isEmpty() {
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
