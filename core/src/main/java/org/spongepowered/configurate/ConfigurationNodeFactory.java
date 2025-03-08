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

import io.leangen.geantyref.TypeToken;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.util.CheckedConsumer;

import java.util.Map;
import java.util.stream.Collector;

/**
 * Something that can create a customized node.
 *
 * @since 4.0.0
 */
@FunctionalInterface
public interface ConfigurationNodeFactory<N extends ConfigurationNode> {

    /**
     * Default options for the types of nodes created by this factory.
     *
     * <p>All values must match what a created node will see, but some values
     * may be determined by this factory to be non user-modifiable. These should
     * be documented for any factory implementation.
     *
     * @return default options
     * @since 4.0.0
     */
    default ConfigurationOptions defaultOptions() {
        return ConfigurationOptions.defaults();
    }

    /**
     * Create an empty node with the provided options.
     *
     * <p>Node options may be overridden if the factory enforces specific
     * requirements on options.
     *
     * @param options node options
     * @return newly created empty node
     * @since 4.0.0
     */
    N createNode(ConfigurationOptions options);

    /**
     * Create a new node with default options.
     *
     * @return newly created empty node
     * @since 4.0.0
     */
    default N createNode() {
        return createNode(defaultOptions());
    }

    /**
     * Create a new node with default options and initialize it with the
     * provided action.
     *
     * @param <E> thrown type
     * @param action action to initialize node with
     * @return newly created empty node
     * @throws E when thrown from inner action
     * @since 4.0.0
     */
    default <E extends Exception> N createNode(final CheckedConsumer<N, E> action) throws E {
        final N node = createNode();
        action.accept(node);
        return node;
    }

    /**
     * Create a new node with the provided options and initialize it with the
     * provided action.
     *
     * <p>Node options may be overridden if the factory enforces specific
     * requirements on options.
     *
     * @param <E> thrown type
     * @param options node options
     * @param action action to initialize node with
     * @return newly created empty node
     * @throws E when thrown from inner action
     * @since 4.0.0
     */
    default <E extends Exception> N createNode(final ConfigurationOptions options, final CheckedConsumer<N, E> action) throws E {
        final N node = createNode(options);
        action.accept(node);
        return node;
    }

    /**
     * Create a collector that appends values to a newly created node as
     * map children.
     *
     * <p>This collector does not accept values in parallel.</p>
     *
     * @param valueType marker for value type
     * @param <V> value type
     * @return a new collector
     * @since 4.0.0
     */
    default <V> Collector<Map.Entry<?, V>, N, N> toMapCollector(final TypeToken<V> valueType) {
        return Collector.of(this::createNode, (node, entry) -> {
            try {
                node.node(entry.getKey()).set(valueType, entry.getValue());
            } catch (final SerializationException ex) {
                throw new IllegalArgumentException(ex);
            }
        }, (a, b) -> {
                a.mergeFrom(b);
                return a;
            });
    }

    /**
     * Create a collector that appends values to a newly created node as
     * map children.
     *
     * <p>This collector does not accept values in parallel.</p>
     *
     * @param valueType marker for value type
     * @param <V> value type
     * @return a new collector
     * @since 4.0.0
     */
    default <V> Collector<Map.Entry<?, V>, N, N> toMapCollector(final Class<V> valueType) {
        return Collector.of(this::createNode, (node, entry) -> {
            try {
                node.node(entry.getKey()).set(valueType, entry.getValue());
            } catch (final SerializationException ex) {
                throw new IllegalArgumentException(ex);
            }
        }, (a, b) -> {
                a.mergeFrom(b);
                return a;
            });
    }

    /**
     * Create a collector that appends values to a newly created node as
     * list children.
     *
     * <p>This collector does not accept values in parallel.</p>
     *
     * @param valueType marker for value type
     * @param <V> value type
     * @return a new collector
     * @since 4.0.0
     */
    default <V> Collector<V, N, N> toListCollector(final TypeToken<V> valueType) {
        return Collector.of(this::createNode, (node, value) -> {
            try {
                node.appendListNode().set(valueType, value);
            } catch (final SerializationException ex) {
                throw new IllegalArgumentException(ex);
            }
        }, (a, b) -> {
                a.mergeFrom(b);
                return a;
            });
    }

    /**
     * Create a collector that appends values to a newly created node as
     * list children.
     *
     * <p>This collector does not accept values in parallel.</p>
     *
     * @param valueType marker for value type
     * @param <V> value type
     * @return a new collector
     * @since 4.0.0
     */
    default <V> Collector<V, N, N> toListCollector(final Class<V> valueType) {
        return Collector.of(this::createNode, (node, value) -> {
            try {
                node.appendListNode().set(valueType, value);
            } catch (final SerializationException ex) {
                throw new IllegalArgumentException(ex);
            }
        }, (a, b) -> {
                a.mergeFrom(b);
                return a;
            });
    }

}
