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
package org.spongepowered.configurate.loader;

import static java.util.Objects.requireNonNull;

import net.kyori.option.Option;
import net.kyori.option.value.ValueSource;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.regex.Pattern;

final class ValueSources {

    private ValueSources() {
    }

    public static ValueSource node(final ConfigurationNode node) {
        return new ValueSources.Node(requireNonNull(node, "node"));
    }

    /**
     * A value source fetching from a node.
     *
     * <p>Option keys will be split by any of {@code :} or {@code /}.</p>
     */
    static final class Node implements ValueSource {

        private static final Pattern PATH_SPLIT = Pattern.compile("[:/]");
        private final ConfigurationNode source;

        Node(final ConfigurationNode source) {
            this.source = source;
        }

        @Override
        public <T> @Nullable T value(final Option<T> option) {
            final String[] path = PATH_SPLIT.split(option.id(), -1);
            try {
                return this.source.node((Object[]) path).get(option.valueType().type());
            } catch (final SerializationException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

    }

}
