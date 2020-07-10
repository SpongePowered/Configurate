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
package org.spongepowered.configurate.extra.dfu.v3;

import static java.util.Objects.requireNonNull;

import com.google.common.reflect.TypeToken;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ScopedConfigurationNode;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

/**
 * TypeSerializer implementation wrapping around codecs.
 */
final class CodecSerializer<V> implements TypeSerializer<V> {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final ConfigurateOps DEFAULT_OPS = ConfigurateOps.builder().readWriteProtection(ConfigurateOps.Protection.NONE).build();

    static DynamicOps<ConfigurationNode> opsFor(final ConfigurationNode node) {
        if (node.getOptions().getSerializers().equals(TypeSerializerCollection.defaults())) {
            return DEFAULT_OPS;
        } else {
            return ConfigurateOps.builder()
                    .factoryFromNode(node)
                    .readWriteProtection(ConfigurateOps.Protection.NONE)
                    .build();
        }
    }

    private final Codec<V> codec;

    CodecSerializer(final Codec<V> codec) {
        this.codec = requireNonNull(codec, "codec");
    }

    @Override
    public <N extends ScopedConfigurationNode<N>> @Nullable V deserialize(@NonNull final TypeToken<?> type,
                                                                          @NonNull final N value) throws ObjectMappingException {
        final DataResult<Pair<V, ConfigurationNode>> result = this.codec.decode(opsFor(value), value);
        final DataResult.PartialResult<Pair<V, ConfigurationNode>> error = result.error().orElse(null);
        if (error != null) {
            LOGGER.debug("Unable to decode value using {} due to {}", this.codec, error.message());
            throw new ObjectMappingException(error.message());
        }
        return result.result().orElseThrow(() -> new ObjectMappingException("Neither a result or error was present")).getFirst();
    }

    @Override
    public <N extends ScopedConfigurationNode<N>> void serialize(@NonNull final TypeToken<?> type, @Nullable final V obj, @NonNull final N value)
            throws ObjectMappingException {
        final DataResult<ConfigurationNode> result = this.codec.encode(obj, opsFor(value), value);
        final DataResult.PartialResult<ConfigurationNode> error = result.error().orElse(null);
        if (error != null) {
            LOGGER.debug("Unable to encode value using {} due to {}", this.codec, error.message());
            throw new ObjectMappingException(error.message());
        }

        value.setValue(result.result().orElseThrow(() -> new ObjectMappingException("Neither a result or error was present")));
    }

}
