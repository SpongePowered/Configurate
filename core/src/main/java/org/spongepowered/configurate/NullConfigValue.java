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

import org.checkerframework.checker.nullness.qual.NonNull;
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
        return (NullConfigValue<N, A>) INSTANCE;
    }

    @SuppressWarnings({"ConstantConditions"})
    private NullConfigValue() {
        super(null);
    }

    @Nullable
    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public void setValue(final @Nullable Object value) {
    }

    @Nullable
    @Override
    T putChild(final @NonNull Object key, final @Nullable T value) {
        return null;
    }

    @Nullable
    @Override
    T putChildIfAbsent(final @NonNull Object key, final @Nullable T value) {
        return null;
    }

    @Nullable
    @Override
    public T getChild(final @Nullable Object key) {
        return null;
    }

    @NonNull
    @Override
    public Iterable<T> iterateChildren() {
        return Collections.emptySet();
    }

    @NonNull
    @Override
    NullConfigValue<N, T> copy(final @NonNull T holder) {
        return instance();
    }

    @Override
    boolean isEmpty() {
        return true;
    }

    @Override
    public void clear() {
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
