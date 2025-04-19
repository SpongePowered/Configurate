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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationNodeFactory;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Implementation of DataFixerUpper's DynamicOps.
 *
 * <p>The {@link DynamicOps} interface should be thought of essentially as a way
 * to perform operations on a type without having to directly implement that
 * interface on the type. Rather than taking an object that implements an
 * interface, DFU methods take the implementation of the DynamicOps interface
 * plus the type implemented onto.
 *
 * <p>When possible, the first node's {@link ConfigurationNode#copy()} method
 * will be used to create a new node to contain results. Otherwise, the provided
 * factory will be used. The default factory creates a
 * {@link CommentedConfigurationNode} with the default serializer collection
 * but a custom factory may be provided.
 *
 * <p>DynamicOps has the following primitive types (as determined by those
 * codecs that implement {@link com.mojang.serialization.codecs.PrimitiveCodec}):
 * <dl>
 *     <dt>boolean</dt>
 *     <dd>literal boolean, or numeric 1 for true, 0 for false</dd>
 *     <dt>byte</dt>
 *     <dd>numeric value, {@link Number#byteValue() coerced} to a byte.
 *     If {@link #compressMaps()}, a string may be parsed as a byte as well.</dd>
 *     <dt>short</dt>
 *     <dd>numeric value, {@link Number#shortValue() coerced} to a short.
 *     If {@link #compressMaps()}, a string may be parsed as a short as well.</dd>
 *     <dt>int</dt>
 *     <dd>numeric value, {@link Number#intValue() coerced} to an integer.
 *     If {@link #compressMaps()}, a string may be parsed as an integer as well.</dd>
 *     <dt>long</dt>
 *     <dd>numeric value, {@link Number#longValue() coerced} to a long.
 *     If {@link #compressMaps()}, a string may be parsed as a long as well.</dd>
 *     <dt>float</dt>
 *     <dd>numeric value, {@link Number#floatValue() coerced} to a float.
 *     If {@link #compressMaps()}, a string may be parsed as a float as well.</dd>
 *     <dt>double</dt>
 *     <dd>numeric value, {@link Number#doubleValue() coerced} to a double.
 *     If {@link #compressMaps()}, a string may be parsed as a double as well.</dd>
 *     <dt>{@link String}</dt>
 *     <dd>Any scalar value, as {@link Object#toString() a string}</dd>
 *     <dt>{@link java.nio.ByteBuffer}</dt>
 *     <dd>An array of bytes. Either a native byte array in the node,
 *         or (by default impl) a list of bytes</dd>
 *     <dt>{@link java.util.stream.IntStream}</dt>
 *     <dd>A sequence of integers. Either a native int array in the node,
 *         or (by default impl) a list of integers</dd>
 *     <dt>{@link java.util.stream.LongStream}</dt>
 *     <dd>A sequence of longs. Either a native long array in the node,
 *         or (by default impl) a list of longs</dd>
 * </dl>
 *
 * @since 4.3.0
 */
public final class ConfigurateOps implements DynamicOps<ConfigurationNode> {

    private static final ConfigurateOps UNCOMPRESSED = ConfigurateOps.builder().build();
    private static final ConfigurateOps COMPRESSED = ConfigurateOps.builder().compressed(true).build();

    private final ConfigurationNodeFactory<? extends ConfigurationNode> factory;
    private final boolean compressed;
    private final Protection readProtection;
    private final Protection writeProtection;

    /**
     * Get the shared instance of this class, which creates new nodes using
     * the default factory. The returned instance will not be compressed
     *
     * @return the shared instance
     * @since 4.3.0
     */
    public static DynamicOps<ConfigurationNode> instance() {
        return instance(false);
    }

    /**
     * Get the shared instance of this class, which creates new nodes using
     * the default factory.
     *
     * <p>See {@link #compressMaps()} for a description of what the
     * <pre>compressed</pre> parameter does.
     *
     * @param compressed whether keys should be compressed in the output of
     *     this serializer
     * @return the shared instance
     * @since 4.3.0
     */
    public static DynamicOps<ConfigurationNode> instance(final boolean compressed) {
        return compressed ? COMPRESSED : UNCOMPRESSED;
    }

    /**
     * Get an ops instance that will create nodes using the provided collection.
     *
     * @param collection collection to provide through created nodes' options
     * @return ops instance
     * @since 4.3.0
     */
    public static DynamicOps<ConfigurationNode> forSerializers(final TypeSerializerCollection collection) {
        if (requireNonNull(collection, "collection").equals(TypeSerializerCollection.defaults())) {
            return UNCOMPRESSED;
        } else {
            return builder().factoryFromSerializers(collection).build();
        }
    }

    /**
     * Wrap a ConfigurationNode in a {@link Dynamic} instance. The returned
     * Dynamic will use the same type serializer collection as the original node
     * for its operations.
     *
     * @param node the node to wrap
     * @return a wrapped node
     * @since 4.3.0
     */
    public static Dynamic<ConfigurationNode> wrap(final ConfigurationNode node) {
        if (node.options().serializers().equals(TypeSerializerCollection.defaults())) {
            return new Dynamic<>(instance(), node);
        } else {
            return builder().factoryFromNode(node).buildWrapping(node);
        }
    }

    /**
     * Configure an ops instance using the options of an existing node.
     *
     * @param value the value type
     * @return values
     * @since 4.3.0
     */
    public static DynamicOps<ConfigurationNode> fromNode(final ConfigurationNode value) {
        return builder().factoryFromNode(value).build();
    }

    /**
     * Create a new builder for an ops instance.
     *
     * @return builder
     * @since 4.3.0
     */
    public static ConfigurateOpsBuilder builder() {
        return new ConfigurateOpsBuilder();
    }

    ConfigurateOps(final ConfigurationNodeFactory<? extends ConfigurationNode> factory, final boolean compressed,
            final Protection readProtection, final Protection writeProtection) {
        this.factory = factory;
        this.compressed = compressed;
        this.readProtection = readProtection;
        this.writeProtection = writeProtection;
    }

    /**
     * Whether data passed through this ops will be compressed or not.
     *
     * <p>In the context of DFU, <pre>compressed</pre> means that in situations
     * where values are of a {@link com.mojang.serialization.Keyable} type
     * (as is with types like Minecraft Registries)
     * rather than fully encoding each value, its index into the container
     * is encoded.
     *
     * <p>While data encoded this way may take less space to store, the
     * compressed data will also require an explicit mapping of indices to
     * values. If this is not stored with the node, the indices of values must
     * be preserved to correctly deserialize compressed values.
     *
     * <p>For example, for an enum new values could only be appended, not added
     * in the middle of the constants.
     *
     * @return whether maps are compressed
     */
    @Override
    public boolean compressMaps() {
        return this.compressed;
    }

    /**
     * Extract a value from a node used as a {@code key} in DFU methods.
     *
     * <p>This currently only attempts to interpret the key as a single level
     * down. However, we may want to try to extract an array or iterable of path
     * elements to be able to traverse multiple levels.
     *
     * @param node data source
     * @return a key, asserted non-null
     */
    static Object keyFrom(final ConfigurationNode node) {
        if (node.isList() || node.isMap()) {
            throw new IllegalArgumentException("Key nodes must have scalar values");
        }
        return requireNonNull(node.raw(), "The provided key node must have a value");
    }

    /**
     * Guard source node according to ops instance's copying policy.
     *
     * @implNote currently, this will make a deep copy of the node.
     *
     * @param untrusted original node
     * @return a node with equivalent data
     */
    ConfigurationNode guardOutputRead(final ConfigurationNode untrusted) {
        switch (this.readProtection) {
            case COPY_DEEP: return untrusted.copy();
            case NONE: return untrusted;
            default: throw new IllegalArgumentException("Unexpected state");
        }
    }

    ConfigurationNode guardInputWrite(final ConfigurationNode untrusted) {
        switch (this.writeProtection) {
            case COPY_DEEP: return untrusted.copy();
            case NONE: return untrusted;
            default: throw new IllegalArgumentException("Unexpected state");
        }
    }

    /**
     * Create a new empty node using this ops instance's factory.
     *
     * @return the new node
     */
    @Override
    public ConfigurationNode empty() {
        return this.factory.createNode();
    }

    @Override
    public ConfigurationNode emptyMap() {
        return empty().raw(ImmutableMap.of());
    }

    @Override
    public ConfigurationNode emptyList() {
        return empty().raw(ImmutableList.of());
    }

    // If the destination ops is another Configurate ops instance, just directly pass the node through
    @SuppressWarnings("unchecked")
    private <U> @Nullable U convertSelf(final DynamicOps<U> outOps, final ConfigurationNode input) {
        if (outOps instanceof ConfigurateOps) {
            return (U) input;
        } else {
            return null;
        }
    }

    /**
     * Create a copy of the source node converted to a different data structure.
     *
     * <p>Value types will be preserved as much as possible, but a reverse
     * conversion will most likely be lossy
     *
     * @param targetOps output type
     * @param source source value
     * @param <U> output type
     * @return output value
     */
    @Override
    public <U> U convertTo(final DynamicOps<U> targetOps, final ConfigurationNode source) {
        final @Nullable U self = convertSelf(requireNonNull(targetOps, "targetOps"), requireNonNull(source, "source"));
        if (self != null) {
            return self;
        }

        if (source.isMap()) {
            return convertMap(targetOps, source);
        } else if (source.isList()) {
            return convertList(targetOps, source);
        } else {
            final @Nullable Object value = source.rawScalar();
            if (value == null) {
                return targetOps.empty();
            } else if (value instanceof String) {
                return targetOps.createString((String) value);
            } else if (value instanceof Boolean) {
                return targetOps.createBoolean((Boolean) value);
            } else if (value instanceof Short) {
                return targetOps.createShort((Short) value);
            } else if (value instanceof Integer) {
                return targetOps.createInt((Integer) value);
            } else if (value instanceof Long) {
                return targetOps.createLong((Long) value);
            } else if (value instanceof Float) {
                return targetOps.createFloat((Float) value);
            } else if (value instanceof Double) {
                return targetOps.createDouble((Double) value);
            } else if (value instanceof Byte) {
                return targetOps.createByte((Byte) value);
            } else if (value instanceof byte[]) {
                return targetOps.createByteList(ByteBuffer.wrap((byte[]) value));
            } else if (value instanceof int[]) {
                return targetOps.createIntList(IntStream.of((int[]) value));
            } else if (value instanceof long[]) {
                return targetOps.createLongList(LongStream.of((long[]) value));
            } else {
                throw new IllegalArgumentException("Scalar value '" + source + "' has an unknown type: " + value.getClass().getName());
            }
        }
    }

    /**
     * Get the value of the provided node if it is a number or boolean.
     *
     * <p>If {@link #compressMaps()} is true, values may be coerced from
     * another type.
     *
     * @param input data source
     * @return extracted number
     */
    @Override
    public DataResult<Number> getNumberValue(final ConfigurationNode input) {
        if (!(input.isMap() || input.isList())) {
            final @Nullable Object value = input.rawScalar();
            if (value instanceof Number) {
                return DataResult.success((Number) value);
            } else if (value instanceof Boolean) {
                return DataResult.success((boolean) value ? 1 : 0);
            }

            if (compressMaps()) {
                final int result = input.getInt(Integer.MIN_VALUE);
                if (result == Integer.MIN_VALUE) {
                    return DataResult.error(() -> "Value is not a number");
                }
                return DataResult.success(result);
            }
        }

        return DataResult.error(() -> "Not a number: " + input);
    }

    /**
     * Get the value of the provided node if it is a scalar, converted to
     * a {@link String}.
     *
     * @param input data source
     * @return string | error
     */
    @Override
    public DataResult<String> getStringValue(final ConfigurationNode input) {
        final @Nullable String value = input.getString();
        if (value != null) {
            return DataResult.success(value);
        }

        return DataResult.error(() -> "Not a string: " + input);
    }

    /**
     * Create a new node using this ops instance's node factory,
     * and set its value to the provided number.
     *
     * @param value value
     * @return new node with value
     */
    @Override
    public ConfigurationNode createNumeric(final Number value) {
        return empty().raw(requireNonNull(value, "value"));
    }

    /**
     * Create a new node using this ops instance's node factory,
     * and set its value to the provided boolean.
     *
     * @param value value
     * @return new node with value
     */
    @Override
    public ConfigurationNode createBoolean(final boolean value) {
        return empty().raw(value);
    }

    /**
     * Create a new node using this ops instance's node factory,
     * and set its value to the provided string.
     *
     * @param value value
     * @return new node with value
     */
    @Override
    public ConfigurationNode createString(final String value) {
        return empty().raw(requireNonNull(value, "value"));
    }

    /**
     * Return a result where if {@code prefix} is empty, the node is
     * {@code value}, but otherwise returns an error.
     *
     * @param prefix starting value
     * @param value to update base with
     * @return result of updated node or error
     */
    @Override
    public DataResult<ConfigurationNode> mergeToPrimitive(final ConfigurationNode prefix, final ConfigurationNode value) {
        if (!prefix.empty()) {
            return DataResult.error(() -> "Cannot merge " + value + " into non-empty node " + prefix);
        }
        return DataResult.success(guardOutputRead(value));
    }

    /**
     * Appends element {@code value} to list node {@code input}.
     *
     * @param input base node. Must be empty or of list type
     * @param value value to add as element to the list
     * @return success with modified node, or error if {@code input} contains a
     *          non-{@link ConfigurationNode#isList() list} value
     */
    @Override
    public DataResult<ConfigurationNode> mergeToList(final ConfigurationNode input, final ConfigurationNode value) {
        if (input.isList() || input.empty()) {
            final ConfigurationNode ret = guardOutputRead(input);
            ret.appendListNode().from(value);
            return DataResult.success(ret);
        }

        return DataResult.error(() -> "mergeToList called on a node which is not a list: " + input, input);
    }

    /**
     * Appends nodes in {@code values} to copy of list node {@code input}.
     *
     * @param input base node. Must be empty or of list type
     * @param values list of values to append to base node
     * @return success with modified node, or error if {@code input} contains a
     *          non-{@link ConfigurationNode#isList() list} value
     */
    @Override
    public DataResult<ConfigurationNode> mergeToList(final ConfigurationNode input, final List<ConfigurationNode> values) {
        if (input.isList() || input.empty()) {
            final ConfigurationNode ret = guardInputWrite(input);
            for (ConfigurationNode node : values) {
                ret.appendListNode().from(node);
            }
            return DataResult.success(ret);
        }

        return DataResult.error(() -> "mergeToList called on a node which is not a list: " + input, input);
    }

    /**
     * Update the child of {@code input} at {@code key} with {@code value}.
     *
     * <p>This operation will only affect the returned copy of the input node
     *
     * @param input base node. Must be empty or of map type
     * @param key key relative to base node
     * @param value value to set at empty node
     * @return success with modified node, or error if {@code input} contains a
     *          non-{@link ConfigurationNode#isList() list} value
     */
    @Override
    public DataResult<ConfigurationNode> mergeToMap(final ConfigurationNode input, final ConfigurationNode key, final ConfigurationNode value) {
        if (input.isMap() || input.empty()) {
            final ConfigurationNode copied = guardInputWrite(input);
            copied.node(keyFrom(key)).from(value);
            return DataResult.success(copied);
        }

        return DataResult.error(() -> "mergeToMap called on a node which is not a map: " + input, input);
    }

    /**
     * Return a stream of pairs of (key, value) for map data in the input node.
     *
     * <p>If the input node is non-empty and not a map, the result will
     * be a failure.
     *
     * @param input input node
     * @return result, if successful, of a stream of pairs (key, value) of
     *          entries in the input node.
     */
    @Override
    public DataResult<Stream<Pair<ConfigurationNode, ConfigurationNode>>> getMapValues(final ConfigurationNode input) {
        if (input.empty() || input.isMap()) {
            return DataResult.success(input.childrenMap().entrySet().stream()
                                              .map(entry -> Pair.of(BasicConfigurationNode.root(input.options()).raw(entry.getKey()),
                                                                    guardOutputRead(entry.getValue()))));
        }

        return DataResult.error(() -> "Not a map: " + input);
    }

    /**
     * Get a map-like view of a copy of the contents of {@code input}.
     *
     * <p>If the input node is non-empty and not a map, the result will
     * be a failure.
     *
     * @param input input node
     * @return result, if successful, of map-like view of a copy of the input
     */
    @Override
    public DataResult<MapLike<ConfigurationNode>> getMap(final ConfigurationNode input) {
        if (input.empty() || input.isMap()) {
            return DataResult.success(new NodeMaplike(this, input.options(), input.childrenMap()));
        } else {
            return DataResult.error(() -> "Input node is not a map");
        }
    }

    /**
     * Get a consumer that takes an action to perform on every element of
     * list node {@code input}.
     *
     * <p>As an example, to print out every node in a list
     *      (minus error checking):
     *     <pre>
     *         getList(listNode).result().get()
     *             .accept(element -&gt; System.out.println(element);
     *     </pre>
     *
     * @param input data source
     * @return result, that if successful will take an action to perform on
     *          every element
     */
    @Override
    public DataResult<Consumer<Consumer<ConfigurationNode>>> getList(final ConfigurationNode input) {
        if (input.isList()) {
            return DataResult.success(action -> {
                for (ConfigurationNode child : input.childrenList()) {
                    action.accept(guardOutputRead(child));
                }
            });
        } else {
            return DataResult.error(() -> "Input node is not a list");
        }
    }

    /**
     * Get the contents of list node {@code input} as a {@link Stream} of nodes.
     *
     * @param input data source
     * @return if node is empty or a list, stream of nodes
     */
    @Override
    public DataResult<Stream<ConfigurationNode>> getStream(final ConfigurationNode input) {
        if (input.empty() || input.isList()) {
            final Stream<ConfigurationNode> stream = input.childrenList().stream().map(this::guardOutputRead);
            return DataResult.success(stream);
        }

        return DataResult.error(() -> "Not a list: " + input);
    }

    /**
     * Create a new node containing the map entries from the
     * stream {@code values}.
     *
     * <p>Keys will be interpreted as a single Object, and can only
     * currently access direct children.
     *
     * @param values entries in the map
     * @return newly created node
     */
    @Override
    public ConfigurationNode createMap(final Stream<Pair<ConfigurationNode, ConfigurationNode>> values) {
        final ConfigurationNode ret = empty();

        values.forEach(p -> ret.node(keyFrom(p.getFirst())).from(p.getSecond()));

        return ret;
    }

    /**
     * Create a new node containing the map entries from the
     * map {@code values}.
     *
     * <p>Keys will be interpreted as a single Object, and can only
     * currently access direct children.
     *
     * @param values unwrapped node map
     * @return newly created node
     */
    @Override
    public ConfigurationNode createMap(final Map<ConfigurationNode, ConfigurationNode> values) {
        final ConfigurationNode ret = empty();

        for (Map.Entry<ConfigurationNode, ConfigurationNode> entry : values.entrySet()) {
            ret.node(keyFrom(entry.getKey())).from(entry.getValue());
        }

        return ret;
    }

    /**
     * Create a new node containing values emitted by {@code input} as
     * list elements.
     *
     * @param input data source
     * @return newly created node
     */
    @Override
    public ConfigurationNode createList(final Stream<ConfigurationNode> input) {
        final ConfigurationNode ret = empty();
        input.forEach(it -> ret.appendListNode().from(it));
        return ret;
    }

    /**
     * Get a copy of {@code input} without the value at node {@code key}.
     *
     * <p>If the input node is not a map, the input node will be returned.
     *
     * @param input data source
     * @param key key to the node to be removed
     * @return if node removed, a copy of the input without node,
     *          otherwise input
     */
    @Override
    public ConfigurationNode remove(final ConfigurationNode input, final String key) {
        if (input.isMap()) {
            final ConfigurationNode ret = guardInputWrite(input);
            ret.node(key).raw(null);
            return ret;
        }

        return input;
    }

    /**
     * Attempt to get the child of {@code input} at {@code key}.
     *
     * @param input data source
     * @param key child key
     * @return success containing child if child is non-virtual,
     *          otherwise failure
     */
    @Override
    public DataResult<ConfigurationNode> get(final ConfigurationNode input, final String key) {
        final ConfigurationNode ret = input.node(key);
        return ret.virtual() ? DataResult.error(() -> "No element " + key + " in the map " + input) : DataResult.success(guardOutputRead(ret));
    }

    /**
     * Get a child of the provided node at {@code key}.
     *
     * <p>Keys will be interpreted as a single Object, and can only
     * currently access direct children.
     *
     * @param input parent node
     * @param key wrapped key of child
     * @return success containing child if child is non-virtual,
     *          otherwise failure
     */
    @Override
    public DataResult<ConfigurationNode> getGeneric(final ConfigurationNode input, final ConfigurationNode key) {
        final ConfigurationNode ret = input.node(keyFrom(key));
        return ret.virtual() ? DataResult.error(() -> "No element " + key + " in the map " + input) : DataResult.success(guardOutputRead(ret));
    }

    /**
     * Update a copy of {@code input} with {@code value} at path {@code key}.
     *
     * @param input data source
     * @param key key of child node
     * @param value value for child node
     * @return updated parent node
     */
    @Override
    public ConfigurationNode set(final ConfigurationNode input, final String key, final ConfigurationNode value) {
        final ConfigurationNode ret = guardInputWrite(input);
        ret.node(key).from(value);
        return ret;
    }

    /**
     * Copies the input node and transform its child at {@code key}.
     *
     * <p>Return a copy of the input node with the child at {@code key}
     * transformed by the provided function
     *
     * <p>If there is no value at {@code key}, the input node will be
     * returned unmodified.
     *
     * @param input base value
     * @param key key to change
     * @param function function to process the node at {@code wrappedKey}
     * @return an updated copy of input node
     */
    @Override
    public ConfigurationNode update(final ConfigurationNode input, final String key, final Function<ConfigurationNode, ConfigurationNode> function) {
        if (input.node(key).virtual()) {
            return input;
        }

        final ConfigurationNode ret = guardInputWrite(input);
        final ConfigurationNode child = ret.node(key);
        child.from(function.apply(child));
        return ret;
    }

    /**
     * Copies the input node and transform the node at {@code wrappedKey}.
     *
     * <p>Return a copy of the input node with the child at {@code wrappedKey}
     * transformed by the provided function
     *
     * <p>If there is no value at {@code wrappedKey}, the input node will be
     * returned unmodified.
     *
     * <p>Keys will be interpreted as a single Object, and can only
     * currently access direct children.
     *
     * @param input base value
     * @param wrappedKey key to change
     * @param function function to process the node at {@code wrappedKey}
     * @return an updated copy of input node
     */
    @Override
    public ConfigurationNode updateGeneric(final ConfigurationNode input, final ConfigurationNode wrappedKey,
            final Function<ConfigurationNode, ConfigurationNode> function) {
        final Object key = keyFrom(wrappedKey);
        if (input.node(key).virtual()) {
            return input;
        }

        final ConfigurationNode ret = guardInputWrite(input);

        final ConfigurationNode child = ret.node(key);
        child.from(function.apply(child));
        return ret;
    }

    @Override
    public String toString() {
        return "Configurate";
    }

    /**
     * Protection level for configuration node accesses through ops instance.
     *
     * @since 4.3.0
     */
    public enum Protection {
        /**
         * When an operation is executed on the node, make a deep copy of the
         * result.
         */
        COPY_DEEP,

        /**
         * Directly pass on nodes, still attached to their original structure.
         */
        NONE

    }

}
