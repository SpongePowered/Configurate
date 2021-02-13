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

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * A {@link ConfigValue} which holds a single ("scalar") value.
 */
final class ScalarConfigValue<N extends ScopedConfigurationNode<N>, A extends AbstractConfigurationNode<N, A>> implements ConfigValue<N, A> {

    private final A holder;
    private volatile @Nullable Object value;

    ScalarConfigValue(final A holder) {
        this.holder = holder;
    }

    @Override
    public @Nullable Object get() {
        return this.value;
    }

    @Override
    public void set(final @Nullable Object value) {
        if (value != null && !this.holder.options().acceptsType(value.getClass())) {
            throw new IllegalArgumentException("Configuration does not accept objects of type " + value.getClass());
        }
        this.value = value;
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
    public ScalarConfigValue<N, A> copy(final A holder) {
        final ScalarConfigValue<N, A> copy = new ScalarConfigValue<>(holder);
        copy.value = this.value;
        return copy;
    }

    @Override
    public boolean isEmpty() {
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
