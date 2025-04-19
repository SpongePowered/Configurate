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
package org.spongepowered.configurate.extra.dfu.v7;

import static java.util.Objects.requireNonNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.serialize.TypeSerializer;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

/**
 * A bridge between Configurate and DataFixerUpper serialization types.
 *
 * @since 4.3.0
 */
public final class DfuSerializers {

    private DfuSerializers() {
    }

    /**
     * Create a new serializer wrapping the provided {@link Codec}.
     *
     * @param codec codec to use for the serialization operation
     * @param <V> value type
     * @return a new serializer
     * @since 4.3.0
     */
    public static <V> TypeSerializer<V> serializer(final Codec<V> codec) {
        return new CodecSerializer<>(requireNonNull(codec, "codec"));
    }

    /**
     * Create a new codec that uses the default type serializer collection
     * to serialize an object of the provided type.
     *
     * @param type token representing a value type
     * @param <S> value type
     * @return a codec for the type, or null if an appropriate
     *      {@link TypeSerializer} could not be found.
     * @since 4.3.0
     */
    public static <S> @Nullable Codec<S> codec(final TypeToken<S> type) {
        return codec(requireNonNull(type, "type"), TypeSerializerCollection.defaults());
    }

    /**
     * Create a new codec based on a Configurate {@link TypeSerializer}.
     *
     * @param type type to serialize
     * @param collection source for values
     * @param <V> value type
     * @return a codec, or null if an appropriate {@link TypeSerializer}
     *      could not be found for the TypeToken.
     * @since 4.3.0
     */
    public static <V> @Nullable Codec<V> codec(final TypeToken<V> type, final TypeSerializerCollection collection) {
        final @Nullable TypeSerializer<V> serial = collection.get(requireNonNull(type, "type"));
        if (serial == null) {
            return null;
        }
        return new TypeSerializerCodec<>(type, serial, ConfigurateOps.forSerializers(collection)).withLifecycle(Lifecycle.stable());
    }

}
