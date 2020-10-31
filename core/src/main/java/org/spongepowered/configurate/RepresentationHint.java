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
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A flag for configuration loaders describing how a node should be serialized.
 *
 * <p>A loader may not accept every representation hint available, but any
 * understood hints should be exposed as constant fields on the loader class.
 * Any unknown hints will be ignored.
 *
 * @param <V> the value type
 * @since 4.0.0
 */
@AutoValue
public abstract class RepresentationHint<V> {

    /**
     * Create a new basic representation hint.
     *
     * <p>The created hint will be inheritable and have no default
     * value set.</p>
     *
     * @param identifier hint identifier
     * @param valueType type of value the hint will hold
     * @param <V> value type
     * @return a new hint
     * @since 4.0.0
     */
    public static <V> RepresentationHint<V> of(final String identifier, final Class<V> valueType) {
        return RepresentationHint.<V>builder().identifier(identifier).valueType(valueType).build();
    }

    /**
     * Create a new basic representation hint.
     *
     * <p>The created hint will be inheritable and have no default
     * value set.</p>
     *
     * @param identifier hint identifier
     * @param valueType type of value the hint will hold
     * @param <V> value type
     * @return a new hint
     * @since 4.0.0
     */
    public static <V> RepresentationHint<V> of(final String identifier, final TypeToken<V> valueType) {
        return RepresentationHint.<V>builder().identifier(identifier).valueType(valueType).build();
    }

    /**
     * Create a builder for a new hint.
     *
     * @param <V> value type
     * @return a new builder
     * @since 4.0.0
     */
    public static <V> Builder<V> builder() {
        return new AutoValue_RepresentationHint.Builder<>();
    }

    RepresentationHint() { }

    /**
     * An identifier used to represent this hint in error messages.
     *
     * @return the identifier
     * @since 4.0.0
     */
    public abstract String identifier();

    /**
     * The type that values of this type have to have.
     *
     * @return value type
     * @since 4.0.0
     */
    public abstract TypeToken<V> valueType();

    /**
     * If a value for a representation hint cannot be found by quering a node
     * or any of this parents, the default value will be returned.
     *
     * @return default type
     * @since 4.0.0
     */
    public abstract @Nullable V defaultValue();

    /**
     * Get whether or not this hint can draw its value from parent nodes.
     *
     * @return if inheritable
     * @since 4.0.0
     */
    public abstract boolean inheritable();

    /**
     * A builder for {@link RepresentationHint}s.
     *
     * @param <V> value type
     * @since 4.0.0
     */
    @AutoValue.Builder
    public abstract static class Builder<V> {

        Builder() {
            this.inheritable(true);
        }

        /**
         * Set the identifier to refer to this hint.
         *
         * @param identifier hint identifier
         * @return this builder
         * @since 4.0.0
         */
        public abstract Builder<V> identifier(String identifier);

        /**
         * Set the type used for this node's value.
         *
         * <p>Raw types are forbidden.</p>
         *
         * @param valueType the value type
         * @return this builder
         * @since 4.0.0
         */
        public final Builder<V> valueType(final Class<V> valueType) {
            return valueType(TypeToken.get(valueType));
        }

        /**
         * Set the type used for this node's value.
         *
         * <p>Raw types are forbidden.</p>
         *
         * @param valueType the value type
         * @return this builder
         * @since 4.0.0
         */
        public abstract Builder<V> valueType(TypeToken<V> valueType);

        /**
         * Set the default value when this hint is not present in the hierarchy.
         *
         * <p>This defaults to {@code null}.</p>
         *
         * @param defaultValue value to return on gets
         * @return this builder
         * @since 4.0.0
         */
        public abstract Builder<V> defaultValue(@Nullable V defaultValue);

        /**
         * Set whether or not the hint can be inherited.
         *
         * <p>Defaults to {@code true}.</p>
         *
         * @param inheritable if inheritable
         * @return this builder
         * @see #inheritable()
         * @since 4.0.0
         */
        public abstract Builder<V> inheritable(boolean inheritable);

        /**
         * Create a new hint from the provided options.
         *
         * <p>The {@code identifier} and {@code valueType} must have been set to
         * build a complete hint.</p>
         *
         * @return a new representation hint
         * @since 4.0.0
         */
        public abstract RepresentationHint<V> build();

    }

}
