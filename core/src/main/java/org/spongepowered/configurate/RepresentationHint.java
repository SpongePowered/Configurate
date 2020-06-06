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

import com.google.auto.value.AutoValue;
import com.google.common.reflect.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A flag for configuration loaders describing how a node should be serialized.
 *
 * <p>A loader may not accept every representation hint available, but any
 * understood hints should be exposed as constant fields on the loader class.
 * Any unknown hints will be ignored.
 *
 * @param <V> The value type
 */
@AutoValue
public abstract class RepresentationHint<V> {

    public static final RepresentationHint<Integer> INDENT = of("indent", Integer.class);

    public static <V> RepresentationHint<V> of(final String identifier, final Class<V> valueType) {
        return new AutoValue_RepresentationHint<>(identifier, TypeToken.of(valueType), null);
    }

    public static <V> RepresentationHint<V> of(final String identifier, final TypeToken<V> valueType) {
        return new AutoValue_RepresentationHint<>(identifier, valueType, null);
    }

    public static <V> RepresentationHint<V> of(final String identifier, final Class<V> valueType, final V defaultValue) {
        return new AutoValue_RepresentationHint<>(identifier, TypeToken.of(valueType), defaultValue);
    }

    public static <V> RepresentationHint<V> of(final String identifier, final TypeToken<V> valueType, final V defaultValue) {
        return new AutoValue_RepresentationHint<>(identifier, valueType, defaultValue);
    }

    RepresentationHint() { }

    /**
     * An identifier used to represent this hint in error messages.
     *
     * @return the identifier
     */
    public abstract String getIdentifier();

    /**
     * The type that values of this type have to have.
     *
     * @return value type
     */
    public abstract TypeToken<V> getValueType();

    /**
     * If a value for a representation hint cannot be found by quering a node
     * or any of this parents, the default value will be returned.
     *
     * @return default type
     */
    public abstract @Nullable V getDefaultValue();

}
