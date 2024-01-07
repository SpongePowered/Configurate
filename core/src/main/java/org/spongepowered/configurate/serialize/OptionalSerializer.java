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
package org.spongepowered.configurate.serialize;

import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

@SuppressWarnings("checkstyle:IllegalType") // for Optional
final class OptionalSerializer implements TypeSerializer.Annotated<Optional<?>> {

    static final TypeToken<Optional<?>> TYPE = new TypeToken<Optional<?>>() {};
    static final TypeSerializer<Optional<?>> INSTANCE = new OptionalSerializer();

    private OptionalSerializer() {
    }

    private static AnnotatedType extractParameter(final AnnotatedType optional) throws SerializationException {
        if (!(optional instanceof AnnotatedParameterizedType)) {
            throw new SerializationException(optional, "Required type parameters on annotated type");
        }

        return ((AnnotatedParameterizedType) optional).getAnnotatedActualTypeArguments()[0];
    }

    @Override
    public Optional<?> deserialize(final AnnotatedType type, final ConfigurationNode node) throws SerializationException {
        if (node.empty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(node.get(extractParameter(type)));
    }

    @Override
    @SuppressWarnings("NullableOptional") // needed for type signature
    public void serialize(final AnnotatedType type, final @Nullable Optional<?> obj, final ConfigurationNode node) throws SerializationException {
        if (obj == null || !obj.isPresent()) {
            node.set(null);
            return;
        }

        node.set(extractParameter(type), obj.get());
    }

    @Override
    public Optional<?> emptyValue(final AnnotatedType specificType, final ConfigurationOptions options) {
        return Optional.empty();
    }

    static final class OfInt implements TypeSerializer<OptionalInt> {

        static final Class<OptionalInt> TYPE = OptionalInt.class;
        static final TypeSerializer<OptionalInt> INSTANCE = new OfInt();

        private OfInt() {
        }

        @Override
        public OptionalInt deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
            if (node.empty()) {
                return OptionalInt.empty();
            }
            return OptionalInt.of(node.require(int.class));
        }

        @Override
        public void serialize(final Type type, final @Nullable OptionalInt obj, final ConfigurationNode node) throws SerializationException {
            if (obj == null || !obj.isPresent()) {
                node.set(null);
                return;
            }

            node.set(int.class, obj.getAsInt());
        }

        @Override
        public OptionalInt emptyValue(final AnnotatedType specificType, final ConfigurationOptions options) {
            return OptionalInt.empty();
        }

    }

    static final class OfLong implements TypeSerializer<OptionalLong> {

        static final Class<OptionalLong> TYPE = OptionalLong.class;
        static final TypeSerializer<OptionalLong> INSTANCE = new OfLong();

        private OfLong() {
        }

        @Override
        public OptionalLong deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
            if (node.empty()) {
                return OptionalLong.empty();
            }
            return OptionalLong.of(node.require(long.class));
        }

        @Override
        public void serialize(final Type type, final @Nullable OptionalLong obj, final ConfigurationNode node) throws SerializationException {
            if (obj == null || !obj.isPresent()) {
                node.set(null);
                return;
            }

            node.set(long.class, obj.getAsLong());
        }

        @Override
        public OptionalLong emptyValue(final AnnotatedType specificType, final ConfigurationOptions options) {
            return OptionalLong.empty();
        }

    }

    static final class OfDouble implements TypeSerializer<OptionalDouble> {

        static final Class<OptionalDouble> TYPE = OptionalDouble.class;
        static final TypeSerializer<OptionalDouble> INSTANCE = new OfDouble();

        private OfDouble() {
        }

        @Override
        public OptionalDouble deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
            if (node.empty()) {
                return OptionalDouble.empty();
            }
            return OptionalDouble.of(node.require(double.class));
        }

        @Override
        public void serialize(final Type type, final @Nullable OptionalDouble obj, final ConfigurationNode node) throws SerializationException {
            if (obj == null || !obj.isPresent()) {
                node.set(null);
                return;
            }

            node.set(double.class, obj.getAsDouble());
        }

        @Override
        public OptionalDouble emptyValue(final AnnotatedType specificType, final ConfigurationOptions options) {
            return OptionalDouble.empty();
        }

    }

}
