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

import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;

/**
 * A {@link ConfigValue} which holds no value.
 */
class NullConfigValue<N extends ScopedConfigurationNode<N>, T extends AbstractConfigurationNode<N, T>> extends ConfigValue<N, T> {
    NullConfigValue(@UnknownInitialization T holder) {
        super(holder);
    }

    @Override
    ValueType getType() {
        return ValueType.NULL;
    }

    @Override
    public @Nullable Object getValue() {
        return null;
    }

    @Override
    public void setValue(@Nullable Object value) {
    }

    @Override
    @Nullable T putChild(Object key, @Nullable T value) {
        return null;
    }

    @Override
    @Nullable T putChildIfAbsent(Object key, @Nullable T value) {
        return null;
    }

    @Override
    public @Nullable T getChild(Object key) {
        return null;
    }

    @Override
    public Iterable<T> iterateChildren() {
        return Collections.emptySet();
    }

    @Override
    NullConfigValue<N, T> copy(@NonNull T holder) {
        return new NullConfigValue<>(holder);
    }

    @Override
    boolean isEmpty() {
        return true;
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean equals(@Nullable Object o) {
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
