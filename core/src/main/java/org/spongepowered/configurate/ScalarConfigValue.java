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

import static java.util.Objects.requireNonNull;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * A {@link ConfigValue} which holds a single ("scalar") value.
 */
final class ScalarConfigValue<N extends ScopedConfigurationNode<N>, T extends AbstractConfigurationNode<N, T>> extends ConfigValue<N, T> {

    private volatile @Nullable Object value;

    ScalarConfigValue(final T holder) {
        super(holder);
    }

    @Nullable
    @Override
    public Object getValue() {
        return this.value;
    }

    @Override
    public void setValue(final @Nullable Object value) {
        if (!holder.getOptions().acceptsType(requireNonNull(value).getClass())) {
            throw new IllegalArgumentException("Configuration does not accept objects of type " + value.getClass());
        }
        this.value = value;
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
    ScalarConfigValue<N, T> copy(final @NonNull T holder) {
        final ScalarConfigValue<N, T> copy = new ScalarConfigValue<>(holder);
        copy.value = this.value;
        return copy;
    }

    @Override
    boolean isEmpty() {
        final @Nullable Object value = this.value;
        return (value instanceof String && ((String) value).isEmpty())
                || (value instanceof Collection<?> && ((Collection<?>) value).isEmpty());
    }

    @Override
    public void clear() {
        this.value = null;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ScalarConfigValue<?, ?>)) {
            return false;
        }
        final ScalarConfigValue<?, ?> that = (ScalarConfigValue<?, ?>) other;
        return Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return 7 + Objects.hashCode(this.value);
    }

    @Override
    public String toString() {
        return "ScalarConfigValue{value=" + this.value + '}';
    }

}
