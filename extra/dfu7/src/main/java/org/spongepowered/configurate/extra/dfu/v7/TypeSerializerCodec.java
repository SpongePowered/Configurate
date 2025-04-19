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

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.leangen.geantyref.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

final class TypeSerializerCodec<V> implements Codec<V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TypeSerializerCodec.class);

    private final TypeToken<V> token;
    private final TypeSerializer<V> serializer;
    private final DynamicOps<ConfigurationNode> ops;

    TypeSerializerCodec(final TypeToken<V> token, final TypeSerializer<V> serializer, final DynamicOps<ConfigurationNode> ops) {
        this.token = requireNonNull(token, "token");
        this.serializer = requireNonNull(serializer, "serializer");
        this.ops = ops;
    }

    /**
     * Deserialize a value from the provided type.
     *
     * <p>The type will be converted to a {@link ConfigurationNode}, processed,
     * and returned as a value paired to the empty value.
     *
     * @param ops operations for source type
     * @param holder source data object
     * @param <T> source data type
     * @return a result with a pair of decoded value to the node the result was
     *          extracted from
     */
    @Override
    public <T> DataResult<Pair<V, T>> decode(final DynamicOps<T> ops, final T holder) {
        final ConfigurationNode node = ops.convertTo(this.ops, holder);
        try {
            return DataResult.success(Pair.of(this.serializer.deserialize(this.token.getType(), node), holder));
        } catch (final SerializationException ex) {
            LOGGER.debug("Error decoding value of type {}", this.token, ex);
            return DataResult.error(ex::getMessage);
        }
    }

    @Override
    public <T> DataResult<T> encode(final V input, final DynamicOps<T> ops, final T container) {
        try {
            if (container instanceof ConfigurationNode) {
                this.serializer.serialize(this.token.getType(), input, (ConfigurationNode) container);
                return DataResult.success(container);
            } else {
                final ConfigurationNode dest = this.ops.empty();
                this.serializer.serialize(this.token.getType(), input, dest);
                final T result = this.ops.convertTo(ops, dest);
                if (dest.isList()) {
                    return ops.mergeToList(container, result);
                } else if (dest.isMap()) {
                    return ops.getMap(result).flatMap(map -> ops.mergeToMap(container, map));
                } else {
                    return ops.mergeToPrimitive(container, result);
                }
            }
        } catch (final SerializationException ex) {
            LOGGER.debug("Error encoding value of type {}", this.token, ex);
            return DataResult.error(ex::getMessage);
        }
    }

    @Override
    public String toString() {
        return "TypeSerializerCodec<" + this.token + '>';
    }

}
