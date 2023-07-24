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

import static java.util.Objects.requireNonNull;
import static org.spongepowered.configurate.AbstractConfigurationNode.storeDefault;
import static org.spongepowered.configurate.util.Types.makeListType;

import io.leangen.geantyref.TypeFactory;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.Scalars;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * A node in the configuration tree.
 *
 * <p>All aspects of a configurations structure are represented using instances
 * of {@link ConfigurationNode}, and the links between them.</p>
 *
 * <p>{@link ConfigurationNode}s can hold different types of value. They can:</p>
 *
 * <ul>
 *     <li>Hold a single "scalar" value (accessed by {@link #rawScalar()}</li>
 *     <li>Represent a "list" of child {@link ConfigurationNode}s (accessed by {@link #isList()} and {@link #childrenList()})</li>
 *     <li>Represent a "map" of child {@link ConfigurationNode}s (accessed by {@link #isMap()} and {@link #childrenMap()})</li>
 *     <li>Hold no value at all (when {@link #virtual()} is true)</li>
 * </ul>
 *
 * <p>The overall configuration stems from a single "root" node, which is
 * provided by the {@link ConfigurationLoader}, or by other means programmatically.</p>
 *
 * @since 4.0.0
 */
public interface ConfigurationNode {

    /**
     * Default value for unknown number results.
     *
     * @since 4.0.0
     */
    int NUMBER_DEF = 0;

    /**
     * Gets the "key" of this node.
     *
     * <p>The key determines this {@link ConfigurationNode}s position within
     * the overall configuration structure.</p>
     *
     * <p>If this node is currently {@link #virtual() virtual}, this method's
     * result may be inaccurate.</p>
     *
     * <p>Note that this method only returns the nearest "link" in the
     * hierarchy, and does not return a representation of the full path. See
     * {@link #path()} for that.</p>
     *
     * <p>The {@link ConfigurationNode}s returned as values from
     * {@link #childrenMap()} will have keys derived from their pairing in
     * the map node.</p>
     *
     * <p>The {@link ConfigurationNode}s returned from
     * {@link #childrenList()} will have keys derived from their position
     * (index) in the list node.</p>
     *
     * @return the key of this node
     * @since 4.0.0
     */
    @Nullable Object key();

    /**
     * Gets the full path of {@link #key() keys} from the root node to this
     * node.
     *
     * <p>Node implementations may not keep a full path for each node, so this
     * method may be somewhat complex to calculate. Most uses should not need to
     * calculate the full path unless providing debug information</p>
     *
     * @return an array compiled from the keys for each node up the hierarchy
     * @since 4.0.0
     */
    NodePath path();

    /**
     * Gets the parent of this node.
     *
     * <p>If this node is currently {@link #virtual() virtual}, this method's
     * result may be inaccurate.</p>
     *
     * @return the nodes parent
     * @since 4.0.0
     */
    @Nullable ConfigurationNode parent();

    /**
     * Gets the node at the given (relative) path, possibly traversing multiple
     * levels of nodes.
     *
     * <p>This is the main method used to navigate through
     * the configuration.</p>
     *
     * <p>The path parameter effectively consumes an array of keys, which locate
     * the unique position of a given node within the structure. Each element
     * will navigate one level down in the configuration hierarchy</p>
     *
     * <p>A node is <b>always</b> returned by this method. If the given node
     * does not exist in the structure, a {@link #virtual() virtual} node will
     * be returned which represents the position.</p>
     *
     * @param path the path to fetch the node at
     * @return the node at the given path, possibly virtual
     * @since 4.0.0
     */
    ConfigurationNode node(Object... path);

    /**
     * Gets the node at the given (relative) path, possibly traversing multiple
     * levels of nodes.
     *
     * <p>This is the main method used to navigate through
     * the configuration.</p>
     *
     * <p>The path parameter effectively consumes an array of keys, which locate
     * the unique position of a given node within the structure.</p>
     *
     * <p>A node is <b>always</b> returned by this method. If the given node
     * does not exist in the structure, a {@link #virtual() virtual} node will
     * be returned which represents the position.</p>
     *
     * @param path the path to fetch the node at
     * @return the node at the given path, possibly virtual
     * @since 4.0.0
     */
    ConfigurationNode node(Iterable<?> path);

    /**
     * Checks whether or not a non-virtual node is present at the relative
     * path {@code path}.
     *
     * <p>This allows checking for more remote nodes in the configuration
     * hierarchy without having to instantiate new unattached node objects.</p>
     *
     * @param path path to search at
     * @return if a non-virtual child is present
     * @since 4.0.0
     */
    boolean hasChild(Object... path);

    /**
     * Checks whether or not a non-virtual node is present at the relative
     * path {@code path}.
     *
     * <p>This allows checking for more remote nodes in the configuration
     * hierarchy without having to instantiate new unattached node objects.</p>
     *
     * @param path path to search at
     * @return if a non-virtual child is present
     * @since 4.0.0
     */
    boolean hasChild(Iterable<?> path);

    /**
     * Gets if this node is virtual.
     *
     * <p>Virtual nodes are nodes which are not attached to a wider
     * configuration structure.</p>
     *
     * <p>A node is primarily "virtual" when it has no set value.</p>
     *
     * @return {@code true} if this node is virtual
     * @since 4.0.0
     */
    boolean virtual();

    /**
     * Gets the options that currently apply to this node.
     *
     * @return the {@link ConfigurationOptions} instance controlling the functionality
     *          of this node.
     * @since 4.0.0
     */
    ConfigurationOptions options();

    /**
     * Get if this node has a 'null' value.
     *
     * <p>This generally overlaps with the value of {@link #virtual()}, but may
     * be distinct in situations where the node has additional metadata
     * (comment, attributes, etc).</p>
     *
     * @return whether this node
     * @since 4.1.0
     */
    boolean isNull();

    /**
     * Gets if this node has "list children".
     *
     * @return if this node has children in the form of a list
     * @since 4.0.0
     */
    boolean isList();

    /**
     * Gets if this node has "map children".
     *
     * @return if this node has children in the form of a map
     * @since 4.0.0
     */
    boolean isMap();

    /**
     * Return true when this node has a null or empty value.
     *
     * <p>Values that may result in this method returning true include:
     *
     * <ul>
     *     <li><code>null</code></li>
     *     <li>the empty string</li>
     *     <li>an empty map</li>
     *     <li>an empty list</li>
     *     <li>Any other type of empty collection</li>
     * </ul>
     *
     * <p>This is a separate value from {@link #virtual()}. Emptiness refers
     * to the value of this node itself, while virtuality refers to whether or
     * not this node is attached to a configuration structure.
     *
     * @return whether this node is empty
     * @since 4.0.0
     */
    boolean empty();

    /**
     * Gets the "list children" attached to this node, if it has any.
     *
     * <p>If this node does not {@link #isList() have list children}, an empty
     * list is returned.</p>
     *
     * @return the list children currently attached to this node
     * @since 4.0.0
     */
    List<? extends ConfigurationNode> childrenList();

    /**
     * Gets the "map children" attached to this node, if it has any.
     *
     * <p>If this node does not {@link #isMap() have map children}, an empty map
     * returned.</p>
     *
     * @return the map children currently attached to this node
     * @since 4.0.0
     */
    Map<Object, ? extends ConfigurationNode> childrenMap();

    /**
     * Create a collector that appends values to this node as map children.
     *
     * <p>This collector does not accept values in parallel.</p>
     *
     * @param valueType marker for value type
     * @param <V> value type
     * @return a new collector
     * @since 4.0.0
     */
    default <V> Collector<Map.Entry<?, V>, ? extends ConfigurationNode, ? extends ConfigurationNode> toMapCollector(final TypeToken<V> valueType) {
        return Collector.of(() -> this, (node, entry) -> {
            try {
                node.node(entry.getKey()).set(valueType, entry.getValue());
            } catch (final SerializationException e) {
                throw new IllegalArgumentException(e);
            }
        }, ConfigurationNode::mergeFrom);
    }

    /**
     * Create a collector that appends values to this node as map children.
     *
     * <p>This collector does not accept values in parallel.</p>
     *
     * @param valueType marker for value type
     * @param <V> value type
     * @return a new collector
     * @since 4.0.0
     */
    default <V> Collector<Map.Entry<?, V>, ? extends ConfigurationNode, ? extends ConfigurationNode> toMapCollector(final Class<V> valueType) {
        return Collector.of(() -> this, (node, entry) -> {
            try {
                node.node(entry.getKey()).set(valueType, entry.getValue());
            } catch (final SerializationException e) {
                throw new IllegalArgumentException(e);
            }
        }, ConfigurationNode::mergeFrom);
    }

    /**
     * Create a collector that appends values to this node as list children.
     *
     * <p>This collector does not accept values in parallel.</p>
     *
     * @param valueType marker for value type
     * @param <V> value type
     * @return a new collector
     * @since 4.0.0
     */
    default <V> Collector<V, ? extends ConfigurationNode, ? extends ConfigurationNode> toListCollector(final TypeToken<V> valueType) {
        return Collector.of(() -> this, (node, value) -> {
            try {
                node.appendListNode().set(valueType, value);
            } catch (final SerializationException e) {
                throw new IllegalArgumentException(e);
            }
        }, ConfigurationNode::mergeFrom);
    }

    /**
     * Create a collector that appends values to this node as list children.
     *
     * <p>This collector does not accept values in parallel.</p>
     *
     * @param valueType marker for value type
     * @param <V> value type
     * @return a new collector
     * @since 4.0.0
     */
    default <V> Collector<V, ? extends ConfigurationNode, ? extends ConfigurationNode> toListCollector(final Class<V> valueType) {
        return Collector.of(() -> this, (node, value) -> {
            try {
                node.appendListNode().set(valueType, value);
            } catch (final SerializationException e) {
                throw new IllegalArgumentException(e);
            }
        }, ConfigurationNode::mergeFrom);
    }

    /**
     * Get the current value associated with this node, asserting that it
     * is non-null.
     *
     * <p>This method can be used when it is known that a certain key exists, or
     * when implicit initialization is enabled for the expected {@code type}</p>
     *
     * <p>This method will perform deserialization using the appropriate
     * {@link TypeSerializer} for the given type, or attempting to cast if no
     * type serializer is found.</p>
     *
     * @param type the type to deserialize to
     * @param <V> the type to get
     * @return the value if present and of the proper type
     * @throws NoSuchElementException if the returned value is null
     * @throws SerializationException if the value fails to be converted to the
     *                                requested type
     * @since 4.1.0
     */
    default <V> V require(final TypeToken<V> type) throws SerializationException {
        final @Nullable V ret = this.get(type);
        if (ret == null) {
            throw new NoSuchElementException("Node value was null when a non-null node was require()d");
        }

        return ret;
    }

    /**
     * Get the current value associated with this node, asserting that it
     * is non-null.
     *
     * <p>This method can be used when it is known that a certain key exists, or
     * when implicit initialization is enabled for the expected {@code type}</p>
     *
     * <p>This method will also perform deserialization using the appropriate
     * {@link TypeSerializer} for the given type, or casting if no type
     * serializer is found.</p>
     *
     * @param type the type to deserialize to
     * @param <V> the type to get
     * @return the value if present and of the proper type
     * @throws NoSuchElementException if the returned value is null
     * @throws SerializationException if the value fails to be converted to the
     *                                requested type
     * @since 4.1.0
     */
    default <V> V require(final Class<V> type) throws SerializationException {
        final @Nullable V ret = this.get(type);
        if (ret == null) {
            throw new NoSuchElementException("Node value was null when a non-null node was require()d");
        }

        return ret;
    }

    /**
     * Get the current value associated with this node, asserting that it
     * is non-null.
     *
     * <p>This method can be used when it is known that a certain key exists, or
     * when implicit initialization is enabled for the expected {@code type}</p>
     *
     * <p>This method will attempt to deserialize the node's value to the
     * provided {@link Type} using a configured {@link TypeSerializer} for
     * the given type, or casting if no type serializer is found.</p>
     *
     * @param type the type to deserialize to
     * @return the value if present and of the proper type
     * @throws NoSuchElementException if the returned value is null
     * @throws SerializationException if the value fails to be converted to the
     *                                requested type
     * @since 4.1.0
     */
    default Object require(final Type type) throws SerializationException {
        final @Nullable Object ret = this.get(type);
        if (ret == null) {
            throw new NoSuchElementException("Node value was null when a non-null node was require()d");
        }

        return ret;
    }

    /**
     * Get the current value associated with this node.
     *
     * <p>This method will perform deserialization using the appropriate
     * {@link TypeSerializer} for the given type, or attempting to cast if no
     * type serializer is found.</p>
     *
     * @param type the type to deserialize to
     * @param <V> the type to get
     * @return the value if present and of the proper type, else null
     * @throws SerializationException if the value fails to be converted to the
     *                                requested type
     * @since 4.0.0
     */
    @SuppressWarnings("unchecked") // type token
    default <V> @Nullable V get(final TypeToken<V> type) throws SerializationException {
        return (V) this.get(type.getAnnotatedType());
    }

    /**
     * Get the current value associated with this node.
     *
     * <p>This method will also perform deserialization using the appropriate
     * {@link TypeSerializer} for the given type, or casting if no type
     * serializer is found.</p>
     *
     * @param type the type to deserialize as.
     * @param def value to return if {@link #virtual()} or value is not of
     *            appropriate type
     * @param <V> the type to get
     * @return the value if of the proper type, else {@code def}
     * @throws SerializationException if the value fails to be converted to the
     *                                requested type
     * @since 4.0.0
     */
    @SuppressWarnings("unchecked") // type is verified by the token
    default <V> V get(final TypeToken<V> type, final V def) throws SerializationException {
        return (V) this.get(type.getAnnotatedType(), def);
    }

    /**
     * Get the current value associated with this node.
     *
     * <p>This method will also perform deserialization using the appropriate
     * TypeSerializer for the given type, or casting if no type serializer is
     * found.</p>
     *
     * @param type the type to deserialize to
     * @param defSupplier the function that will be called to calculate a
     *                    default value only if there is no existing value of
     *                    the correct type
     * @param <V> the type to get
     * @return the value if of the proper type, else {@code def}
     * @throws SerializationException if the value fails to be converted to the
     *                                requested type
     * @since 4.0.0
     */
    @SuppressWarnings("unchecked") // type is verified by the token
    default <V> V get(final TypeToken<V> type, final Supplier<V> defSupplier) throws SerializationException {
        return (V) this.get(type.getAnnotatedType(), defSupplier);
    }

    /**
     * Get the current value associated with this node.
     *
     * <p>This method will also perform deserialization using the appropriate
     * {@link TypeSerializer} for the given type, or casting if no type
     * serializer is found.</p>
     *
     * @param type the type to deserialize to
     * @param <V> the type to get
     * @return the value if present and of the proper type, else null
     * @throws SerializationException if the value fails to be converted to the
     *                                requested type
     * @since 4.0.0
     */
    @SuppressWarnings("unchecked") // type is verified by the class parameter
    default <V> @Nullable V get(final Class<V> type) throws SerializationException {
        return (V) this.get((Type) type);
    }

    /**
     * Get the current value associated with this node.
     *
     * <p>This method will also perform deserialization using the appropriate
     * {@link TypeSerializer} for the given type, or casting if no type
     * serializer is found.</p>
     *
     * @param type the type to deserialize as.
     * @param def value to return if {@link #virtual()} or value is not of
     *            appropriate type
     * @param <V> the type to get
     * @return the value if of the proper type, else {@code def}
     * @throws SerializationException if the value fails to be converted to the
     *                                requested type
     * @since 4.0.0
     */
    @SuppressWarnings("unchecked") // type is verified by the class parameter
    default <V> V get(final Class<V> type, final V def) throws SerializationException {
        return (V) this.get((Type) type, def);
    }

    /**
     * Get the current value associated with this node.
     *
     * <p>This method will also perform deserialization using the appropriate
     * TypeSerializer for the given type, or casting if no type serializer is
     * found.</p>
     *
     * @param type the type to deserialize to
     * @param defSupplier the function that will be called to calculate a
     *                    default value only if there is no existing value of
     *                    the correct type
     * @param <V> the type to get
     * @return the value if of the proper type, else {@code def}
     * @throws SerializationException if the value fails to be converted to the
     *                                requested type
     * @since 4.0.0
     */
    @SuppressWarnings("unchecked") // type is verified by the class parameter
    default <V> V get(final Class<V> type, final Supplier<V> defSupplier) throws SerializationException {
        return (V) this.get((Type) type, defSupplier);
    }

    /**
     * Get the current value associated with this node.
     *
     * <p>This method will attempt to deserialize the node's value to the
     * provided {@link AnnotatedType} using a configured
     * {@link TypeSerializer} for the given type, or casting if no type
     * serializer is found.</p>
     *
     * @param type the type to deserialize to
     * @return the value if present and of the proper type, else null
     * @throws SerializationException if the value fails to be converted to the
     *                                requested type
     * @since 4.2.0
     */
    @Nullable Object get(AnnotatedType type) throws SerializationException;

    /**
     * Get the current value associated with this node.
     *
     * <p>This method will attempt to deserialize the node's value to the
     * provided {@link AnnotatedType} using a configured
     * {@link TypeSerializer} for the given type, or casting if no type
     * serializer is found.</p>
     *
     * @param type the type to deserialize as
     * @param def value to return if {@link #virtual()} or value is not of
     *            appropriate type
     * @return the value if of the proper type, else {@code def}
     * @throws SerializationException if the value fails to be converted to the
     *                                requested type
     * @since 4.2.0
     */
    Object get(AnnotatedType type, Object def) throws SerializationException;

    /**
     * Get the current value associated with this node.
     *
     * <p>This method will attempt to deserialize the node's value to the
     * provided {@link AnnotatedType} using a configured
     * {@link TypeSerializer} for the given type, or casting if no type
     * serializer is found.</p>
     *
     * @param type the type to deserialize to
     * @param defSupplier the function that will be called to calculate a
     *                    default value only if there is no existing value of
     *                    the correct type
     * @return the value if of the proper type, else {@code def}
     * @throws SerializationException if the value fails to be converted to the
     *                                requested type
     * @since 4.2.0
     */
    Object get(AnnotatedType type, Supplier<?> defSupplier) throws SerializationException;

    /**
     * Get the current value associated with this node.
     *
     * <p>This method will attempt to deserialize the node's value to the
     * provided {@link Type} using a configured {@link TypeSerializer} for
     * the given type, or casting if no type serializer is found.</p>
     *
     * @param type the type to deserialize to
     * @return the value if present and of the proper type, else null
     * @throws SerializationException if the value fails to be converted to the
     *                                requested type
     * @since 4.0.0
     */
    @Nullable Object get(Type type) throws SerializationException;

    /**
     * Get the current value associated with this node.
     *
     * <p>This method will attempt to deserialize the node's value to the
     * provided {@link Type} using a configured {@link TypeSerializer} for
     * the given type, or casting if no type serializer is found.</p>
     *
     * @param type the type to deserialize as
     * @param def value to return if {@link #virtual()} or value is not of
     *            appropriate type
     * @return the value if of the proper type, else {@code def}
     * @throws SerializationException if the value fails to be converted to the
     *                                requested type
     * @since 4.0.0
     */
    Object get(Type type, Object def) throws SerializationException;

    /**
     * Get the current value associated with this node.
     *
     * <p>This method will attempt to deserialize the node's value to the
     * provided {@link Type} using a configured {@link TypeSerializer} for
     * the given type, or casting if no type serializer is found.</p>
     *
     * @param type the type to deserialize to
     * @param defSupplier the function that will be called to calculate a
     *                    default value only if there is no existing value of
     *                    the correct type
     * @return the value if of the proper type, else {@code def}
     * @throws SerializationException if the value fails to be converted to the
     *                                requested type
     * @since 4.0.0
     */
    Object get(Type type, Supplier<?> defSupplier) throws SerializationException;

    /**
     * If this node has list values, this function unwraps them and converts
     * them to an appropriate type based on the provided function.
     *
     * <p>If this node has a scalar value, this function treats it as a list
     * with one value.</p>
     *
     * @param type the expected type
     * @param <V> the expected type
     * @return an immutable copy of the values contained
     * @throws SerializationException if any value fails to be converted to the
     *                                requested type
     * @since 4.0.0
     */
    default <V> @Nullable List<V> getList(final TypeToken<V> type) throws SerializationException { // @cs-: NoGetSetPrefix (not a bean method)
        return this.get(makeListType(type));
    }

    /**
     * If this node has list values, this function unwraps them and converts
     * them to an appropriate type based on the provided function.
     *
     * <p>If this node has a scalar value, this function treats it as a list
     * with one value.</p>
     *
     * @param elementType expected type
     * @param def default value if no appropriate value is set
     * @param <V> expected type
     * @return an immutable copy of the values contained that could be
     *         successfully converted, or {@code def} if no values could be
     *         converted.
     * @throws SerializationException if any value fails to be converted to the
     *                                requested type
     * @since 4.0.0
     */
    default <V> List<V> getList(// @cs-: NoGetSetPrefix (not a bean method)
        final TypeToken<V> elementType,
        final List<V> def
    ) throws SerializationException {
        final TypeToken<List<V>> type = makeListType(elementType);
        final List<V> ret = this.get(type, def);
        return ret.isEmpty() ? storeDefault(this, type.getType(), def) : ret;
    }

    /**
     * If this node has list values, this function unwraps them and converts
     * them to an appropriate type based on the provided function.
     *
     * <p>If this node has a scalar value, this function treats it as a list
     * with one value.</p>
     *
     * @param elementType expected type
     * @param defSupplier function that will be called to calculate a default
     *                    value only if there is no existing value of the
     *                    correct type
     * @param <V> expected type
     * @return an immutable copy of the values contained that could be
     *         successfully converted, or {@code def} if no values could be
     *         converted.
     * @throws SerializationException if any value fails to be converted to the
     *                                requested type
     * @since 4.0.0
     */
    // @cs-: NoGetSetPrefix (not a bean method)
    default <V> List<V> getList(final TypeToken<V> elementType, final Supplier<List<V>> defSupplier) throws SerializationException {
        final TypeToken<List<V>> type = makeListType(elementType);
        final List<V> ret = this.get(type, defSupplier);
        return ret.isEmpty() ? storeDefault(this, type.getType(), defSupplier.get()) : ret;
    }

    /**
     * If this node has list values, this function unwraps them and converts
     * them to an appropriate type based on the provided function.
     *
     * <p>If this node has a scalar value, this function treats it as a list
     * with one value.</p>
     *
     * @param elementType the expected type
     * @return an immutable copy of the values contained
     * @throws SerializationException if any value fails to be converted to the
     *                                requested type
     * @since TODO: version
     */
    default @Nullable List<?> getList(Type elementType) throws SerializationException {
        return (List<?>) this.get(TypeFactory.parameterizedClass(List.class, elementType));
    }

    /**
     * If this node has list values, this function unwraps them and converts
     * them to an appropriate type based on the provided function.
     *
     * <p>If this node has a scalar value, this function treats it as a list
     * with one value.</p>
     *
     * @param elementType expected type
     * @param def default value if no appropriate value is set
     * @return an immutable copy of the values contained that could be
     *         successfully converted, or {@code def} if no values could be
     *         converted.
     * @throws SerializationException if any value fails to be converted to the
     *                                requested type
     * @since TODO: version
     */
    default List<?> getList(Type elementType, List<?> def) throws SerializationException {
        final Type type = TypeFactory.parameterizedClass(List.class, elementType);
        final List<?> ret = (List<?>) this.get(type, def);
        return ret.isEmpty() ? storeDefault(this, type, def) : ret;
    }

    /**
     * If this node has list values, this function unwraps them and converts
     * them to an appropriate type based on the provided function.
     *
     * <p>If this node has a scalar value, this function treats it as a list
     * with one value.</p>
     *
     * @param elementType expected type
     * @param defSupplier function that will be called to calculate a default
     *                    value only if there is no existing value of the
     *                    correct type
     * @return an immutable copy of the values contained that could be
     *         successfully converted, or {@code def} if no values could be
     *         converted.
     * @throws SerializationException if any value fails to be converted to the
     *                                requested type
     * @since TODO: version
     */
    default List<?> getList(Type elementType, Supplier<List<?>> defSupplier) throws SerializationException {
        final Type type = TypeFactory.parameterizedClass(List.class, elementType);
        final List<?> ret = (List<?>) this.get(type, defSupplier);
        return ret.isEmpty() ? storeDefault(this, type, defSupplier.get()) : ret;
    }

    /**
     * If this node has list values, this function unwraps them and converts
     * them to an appropriate type based on the provided function.
     *
     * <p>If this node has a scalar value, this function treats it as a list
     * with one value.</p>
     *
     * @param type the expected type
     * @param <V> the expected type
     * @return an immutable copy of the values contained
     * @throws SerializationException if any value fails to be converted to the
     *                                requested type
     * @since 4.0.0
     */
    @SuppressWarnings("unchecked")
    default <V> @Nullable List<V> getList(final Class<V> type) throws SerializationException { // @cs-: NoGetSetPrefix (not a bean method)
        return (List<V>) this.get(TypeFactory.parameterizedClass(List.class, type));
    }

    /**
     * If this node has list values, this function unwraps them and converts
     * them to an appropriate type based on the provided function.
     *
     * <p>If this node has a scalar value, this function treats it as a list
     * with one value.</p>
     *
     * @param elementType expected type
     * @param def default value if no appropriate value is set
     * @param <V> expected type
     * @return an immutable copy of the values contained that could be
     *         successfully converted, or {@code def} if no values could be
     *         converted.
     * @throws SerializationException if any value fails to be converted to the
     *                                requested type
     * @since 4.0.0
     */
    @SuppressWarnings("unchecked")
    default <V> List<V> getList(// @cs-: NoGetSetPrefix (not a bean method)
        final Class<V> elementType,
        final List<V> def
    ) throws SerializationException {
        final Type type = TypeFactory.parameterizedClass(List.class, elementType);
        final List<V> ret = (List<V>) this.get(type, def);
        return ret.isEmpty() ? storeDefault(this, type, def) : ret;
    }

    /**
     * If this node has list values, this function unwraps them and converts
     * them to an appropriate type based on the provided function.
     *
     * <p>If this node has a scalar value, this function treats it as a list
     * with one value.</p>
     *
     * @param elementType expected type
     * @param defSupplier function that will be called to calculate a default
     *                    value only if there is no existing value of the
     *                    correct type
     * @param <V> expected type
     * @return an immutable copy of the values contained that could be
     *         successfully converted, or {@code def} if no values could be
     *         converted.
     * @throws SerializationException if any value fails to be converted to the
     *                                requested type
     * @since 4.0.0
     */
    @SuppressWarnings({"unchecked", "checkstyle:NoGetSetPrefix"})
    default <V> List<V> getList(final Class<V> elementType, final Supplier<List<V>> defSupplier) throws SerializationException {
        final Type type = TypeFactory.parameterizedClass(List.class, elementType);
        final List<V> ret = (List<V>) this.get(type, defSupplier);
        return ret.isEmpty() ? storeDefault(this, type, defSupplier.get()) : ret;
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @return the value coerced to a {@link String}, or null if no value
     * @see #raw()
     * @since 4.0.0
     */
    default @Nullable String getString() { // @cs-: NoGetSetPrefix (not a bean method)
        return Scalars.STRING.tryDeserialize(this.rawScalar());
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @param def the default value if no appropriate value is set
     * @return the value coerced to a {@link String}, or {@code def} if no value
     * @see #raw()
     * @since 4.0.0
     */
    default String getString(final String def) { // @cs-: NoGetSetPrefix (not a bean method)
        requireNonNull(def, "def");
        final @Nullable String value = this.getString();
        if (value != null) {
            return value;
        }
        if (this.options().shouldCopyDefaults()) {
            Scalars.STRING.serialize(String.class, def, this);
        }
        return def;
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @return the value coerced to a float, or {@link #NUMBER_DEF} if not a float
     * @see #raw()
     * @since 4.0.0
     */
    default float getFloat() { // @cs-: NoGetSetPrefix (not a bean method)
        return this.getFloat(NUMBER_DEF);
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @param def the default value if no appropriate value is set
     * @return the value coerced to a float, or {@code def} if not a float
     * @see #raw()
     * @since 4.0.0
     */
    default float getFloat(final float def) { // @cs-: NoGetSetPrefix (not a bean method)
        final @Nullable Float val = Scalars.FLOAT.tryDeserialize(this.rawScalar());
        if (val != null) {
            return val;
        }
        if (this.options().shouldCopyDefaults() && def != NUMBER_DEF) {
            Scalars.FLOAT.serialize(float.class, def, this);
        }
        return def;
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @return the value coerced to a double, or {@link #NUMBER_DEF} if
     *         coercion failed
     * @see #raw()
     * @since 4.0.0
     */
    default double getDouble() { // @cs-: NoGetSetPrefix (not a bean method)
        return this.getDouble(NUMBER_DEF);
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @param def the default value if no appropriate value is set
     * @return the value coerced to a double, or {@code def} if coercion failed
     * @see #raw()
     * @since 4.0.0
     */
    default double getDouble(final double def) { // @cs-: NoGetSetPrefix (not a bean method)
        final @Nullable Double val = Scalars.DOUBLE.tryDeserialize(this.rawScalar());
        if (val != null) {
            return val;
        }
        if (this.options().shouldCopyDefaults() && def != NUMBER_DEF) {
            Scalars.DOUBLE.serialize(double.class, def, this);
        }
        return def;
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @return value coerced to an integer, or {@link #NUMBER_DEF} if coercion failed.
     * @see #raw()
     * @since 4.0.0
     */
    default int getInt() { // @cs-: NoGetSetPrefix (not a bean method)
        return this.getInt(NUMBER_DEF);
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @param def the default value if no appropriate value is set
     * @return value coerced to an integer, or {@code def} if coercion failed.
     * @see #raw()
     * @since 4.0.0
     */
    default int getInt(final int def) { // @cs-: NoGetSetPrefix (not a bean method)
        final @Nullable Integer val = Scalars.INTEGER.tryDeserialize(this.rawScalar());
        if (val != null) {
            return val;
        }
        if (this.options().shouldCopyDefaults() && def != NUMBER_DEF) {
            Scalars.INTEGER.serialize(int.class, def, this);
        }
        return def;
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @return value coerced to a long, or {@link #NUMBER_DEF} if coercion failed
     * @see #raw()
     * @since 4.0.0
     */
    default long getLong() { // @cs-: NoGetSetPrefix (not a bean method)
        return this.getLong(NUMBER_DEF);
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @param def the default value if no appropriate value is set
     * @return value coerced to a long, or {@code def} if coercion failed
     * @see #raw()
     * @since 4.0.0
     */
    default long getLong(final long def) { // @cs-: NoGetSetPrefix (not a bean method)
        final @Nullable Long val = Scalars.LONG.tryDeserialize(this.rawScalar());
        if (val != null) {
            return val;
        }
        if (this.options().shouldCopyDefaults() && def != NUMBER_DEF) {
            Scalars.LONG.serialize(long.class, def, this);
        }
        return def;
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @return value coerced to a boolean, or false if coercion failed
     * @see #raw()
     * @since 4.0.0
     */
    default boolean getBoolean() { // @cs-: NoGetSetPrefix (not a bean method)
        return this.getBoolean(false);
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @param def the default value if no appropriate value is set
     * @return value coerced to a boolean, or {@code def} if coercion failed
     * @see #raw()
     * @since 4.0.0
     */
    default boolean getBoolean(final boolean def) { // @cs-: NoGetSetPrefix (not a bean method)
        final @Nullable Boolean val = Scalars.BOOLEAN.tryDeserialize(this.rawScalar());
        if (val != null) {
            return val;
        }
        if (this.options().shouldCopyDefaults()) {
            Scalars.BOOLEAN.serialize(boolean.class, def, this);
        }
        return def;
    }

    /**
     * Set this node's value to the given value.
     *
     * <p>The value type will be taken from the provided value's class and used
     * to determine a serializer. To set a value of a parameterized type, the
     * parameters must be explicitly specified.</p>
     *
     * @param value the value to set
     * @return this node
     * @since 4.0.0
     */
    ConfigurationNode set(@Nullable Object value) throws SerializationException;

    /**
     * Set this node's value to the given value.
     *
     * <p>If the provided value is a {@link Collection} or a {@link Map}, it will be unwrapped into
     * the appropriate configuration node structure.</p>
     *
     * <p>This method will also perform serialization using the appropriate
     * {@link TypeSerializer} for the given type, or casting if no type
     * serializer is found.</p>
     *
     * @param type the type to use for serialization type information
     * @param value the value to set
     * @param <V> the type to serialize to
     * @return this node
     * @throws SerializationException if the value fails to be converted to the
     *                                requested type. No change will be made to
     *                                the node.
     * @since 4.0.0
     */
    <V> ConfigurationNode set(TypeToken<V> type, @Nullable V value) throws SerializationException;

    /**
     * Set this node's value to the given value.
     *
     * <p>If the provided value is a {@link Collection} or a {@link Map}, it will be unwrapped into
     * the appropriate configuration node structure.</p>
     *
     * <p>This method will also perform serialization using the appropriate
     * {@link TypeSerializer} for the given type, or casting if no type
     * serializer is found.</p>
     *
     * <p>This method will fail if a raw type
     * (i.e. a parameterized type without its type parameters) is passed.</p>
     *
     * @param type the type to use for serialization type information
     * @param value the value to set
     * @param <V> the type to serialize to
     * @return this node
     * @throws IllegalArgumentException if a raw type is passed
     * @throws SerializationException if the value fails to be converted to the
     *                                requested type. No change will be made to
     *                                the node.
     * @since 4.0.0
     */
    <V> ConfigurationNode set(Class<V> type, @Nullable V value) throws SerializationException;

    /**
     * Set this node's value to the given value.
     *
     * <p>If the provided value is a {@link Collection} or a {@link Map}, it will be unwrapped into
     * the appropriate configuration node structure.</p>
     *
     * <p>This method will also perform serialization using the appropriate
     * {@link TypeSerializer} for the given type, or casting if no type
     * serializer is found.</p>
     *
     * <p>This method will fail if a raw type
     * (i.e. a parameterized type without its type parameters) is passed.</p>
     *
     * <p>Because this method accepts a non-parameterized {@link Type} parameter,
     * it has no compile-time type checking. The variants that take
     * {@link #set(TypeToken, Object) TypeToken} and
     * {@link #set(Class, Object)} should be preferred where possible.</p>
     *
     * @param type the type to use for serialization type information
     * @param value the value to set
     * @return this node
     * @throws IllegalArgumentException if a raw type is passed
     * @throws IllegalArgumentException if {@code value} is not either
     *                                  {@code null} or of type {@code type}
     * @throws SerializationException if the value fails to be converted to the
     *                                requested type. No change will be made to
     *                                the node.
     * @since 4.0.0
     */
    ConfigurationNode set(Type type, @Nullable Object value) throws SerializationException;

    /**
     * Set this node's value to the given value.
     *
     * <p>If the provided value is a {@link Collection} or a {@link Map}, it will be unwrapped into
     * the appropriate configuration node structure.</p>
     *
     * <p>This method will also perform serialization using the appropriate
     * {@link TypeSerializer} for the given type, or casting if no type
     * serializer is found.</p>
     *
     * <p>This method will fail if a raw type
     * (i.e. a parameterized type without its type parameters) is passed.</p>
     *
     * <p>Because this method accepts a non-parameterized {@link Type} parameter,
     * it has no compile-time type checking. The variants that take
     * {@link #set(TypeToken, Object) TypeToken} and
     * {@link #set(Class, Object)} should be preferred where possible.</p>
     *
     * @param type the annotated type to use for serialization type information
     * @param value the value to set
     * @return this node
     * @throws IllegalArgumentException if a raw type is passed
     * @throws IllegalArgumentException if {@code value} is not either
     *                                  {@code null} or of type {@code type}
     * @throws SerializationException if the value fails to be converted to the
     *                                requested type. No change will be made to
     *                                the node.
     * @since 4.2.0
     */
    ConfigurationNode set(AnnotatedType type, @Nullable Object value) throws SerializationException;

    /**
     * Set the node's value to the provided list.
     *
     * <p>This method provides a helper for constructing the appropriate
     * {@link Type} for serializing a {@link List}</p>
     *
     * @param elementType the type of the list elements. This must not be
     *         a raw type.
     * @param items the list to serializer
     * @param <V> list element type, the {@code T} in {@code List<T>}
     * @return this node
     * @throws SerializationException if the value fails to be converted to the
     *         requested type.
     * @see #set(TypeToken, Object) for details on restrictions.
     * @since 4.0.0
     */
    @SuppressWarnings("checkstyle:NoGetSetPrefix") // set prefix for type alias purposes
    default <V> ConfigurationNode setList(final Class<V> elementType, final @Nullable List<V> items) throws SerializationException {
        return this.set(TypeFactory.parameterizedClass(List.class, elementType), items);
    }

    /**
     * Set the node's value to the provided list.
     *
     * <p>This method provides a helper for constructing the appropriate
     * {@link Type} for serializing a {@link List}</p>
     *
     * @param elementType the type of the list elements. This must not be
     *         a raw type.
     * @param items the list to serializer
     * @param <V> list element type, the {@code T} in {@code List<T>}
     * @return this node
     * @throws SerializationException if the value fails to be converted to the
     *         requested type.
     * @see #set(TypeToken, Object) for details on restrictions.
     * @since 4.0.0
     */
    @SuppressWarnings("checkstyle:NoGetSetPrefix") // set prefix for type alias purposes
    default <V> ConfigurationNode setList(final TypeToken<V> elementType, final @Nullable List<V> items) throws SerializationException {
        return this.set(TypeFactory.parameterizedClass(List.class, elementType.getType()), items);
    }

    /**
     * Get the raw value of this node.
     *
     * <p>The raw value is the plain value that will be passed to the loaders,
     * without serialization except for unwrapping of maps and collections.</p>
     *
     * @return this configuration's current value
     * @see #raw(Object)
     * @since 4.0.0
     */
    @Nullable Object raw();

    /**
     * Set the raw value of this node.
     *
     * <p>The provided value must be of a type accepted by
     * {@link ConfigurationOptions#acceptsType(Class)}. No other serialization
     * will be performed.</p>
     *
     * @param value the value to set on this node
     * @return this node
     * @since 4.0.0
     */
    ConfigurationNode raw(@Nullable Object value);

    /**
     * Get the raw value of this node if the node is a scalar.
     *
     * <p>The raw value is the plain value that will be passed to the loaders,
     * without serialization.</p>
     *
     * <p>Map and list values will not be unboxed.</p>
     *
     * @return this configuration's current value if it is a scalar,
     *          or else null.
     * @see #raw()
     * @since 4.0.0
     */
    @Nullable Object rawScalar();

    /**
     * Apply all data from {@code other} to this node, overwriting any
     * existing data.
     *
     * @param other source node
     * @return this node
     * @since 4.0.0
     */
    ConfigurationNode from(ConfigurationNode other);

    /**
     * Set all the values from the given node that are not present in this node
     * to their values in the provided node.
     *
     * <p>Map keys will be merged. Lists and scalar values will be replaced.</p>
     *
     * @param other the node to merge values from
     * @return this node
     * @since 4.0.0
     */
    ConfigurationNode mergeFrom(ConfigurationNode other);

    /**
     * Removes a direct child of this node.
     *
     * @param key the key of the node to remove
     * @return if a node was removed
     * @since 4.0.0
     */
    boolean removeChild(Object key);

    /**
     * Gets a new child node created as the next entry in the list.
     *
     * @return a new child created as the next entry in the list when it is
     *         attached
     * @since 4.0.0
     */
    ConfigurationNode appendListNode();

    /**
     * Creates a deep copy of this node.
     *
     * <p>If this node has child nodes (is a list or map), the child nodes will
     * also be copied. This action is performed recursively.</p>
     *
     * <p>The resultant node will (initially) contain the same value(s) as this
     * node, and will therefore be {@link Object#equals(Object) equal}, however,
     * changes made to the original will not be reflected in the copy,
     * and vice versa.</p>
     *
     * <p>The actual scalar values that back the configuration will
     * <strong>not</strong> be copied - only the node structure that forms the
     * configuration. This is not a problem in most cases, as the scalar values
     * stored in configurations are usually immutable. (e.g. strings,
     * numbers, booleans).</p>
     *
     * @return a copy of this node
     * @since 4.0.0
     */
    ConfigurationNode copy();

    /**
     * Visit this node hierarchy as described in {@link ConfigurationVisitor}.
     *
     * @param visitor the visitor
     * @param <S> the state type
     * @param <T> the terminal type
     * @param <E> exception type that may be thrown
     * @return returned terminal from the visitor
     * @throws E when throw by visitor implementation
     * @since 4.0.0
     */
    default <S, T, E extends Exception> T visit(final ConfigurationVisitor<S, T, E> visitor) throws E {
        return this.visit(visitor, visitor.newState());
    }

    /**
     * Visit this node hierarchy as described in {@link ConfigurationVisitor}.
     *
     * @param visitor the visitor
     * @param state the state to start with
     * @param <T> the terminal type
     * @param <S> the state type
     * @param <E> exception type that may be thrown
     * @return returned terminal from the visitor
     * @throws E when throw by visitor implementation
     * @since 4.0.0
     */
    <S, T, E extends Exception> T visit(ConfigurationVisitor<S, T, E> visitor, S state) throws E;

    /**
     * Visit this node hierarchy as described in {@link ConfigurationVisitor}.
     *
     * <p>This overload will remove the need for exception handling for visitors
     * that do not have any checked exceptions.</p>
     *
     * @param visitor the visitor
     * @param <S> the state type
     * @param <T> the terminal type
     * @return the returned terminal from the visitor
     * @since 4.0.0
     */
    default <S, T> T visit(final ConfigurationVisitor.Safe<S, T> visitor) {
        return this.visit(visitor, visitor.newState());
    }

    /**
     * Visit this node hierarchy as described in {@link ConfigurationVisitor}.
     *
     * <p>This overload will remove the need for exception handling for visitors
     * that do not have any checked exceptions.</p>
     *
     * @param visitor the visitor
     * @param state the state to start with
     * @param <T> the terminal type
     * @param <S> the state type
     * @return the returned terminal from the visitor
     * @since 4.0.0
     */
    <S, T> T visit(ConfigurationVisitor.Safe<S, T> visitor, S state);

    /**
     * Set a representation hint on this node.
     *
     * <p>Removing a hint from this node means the hint's value will be
     * delegated to the node's parent. To explicitly revert to a hint's default,
     * apply that default value.</p>
     *
     * @param hint the hint to set a value for
     * @param value value to set, or null to unset for self
     * @param <V> hint value type
     * @return this node
     * @since 4.0.0
     */
    <V> ConfigurationNode hint(RepresentationHint<V> hint, @Nullable V value);

    /**
     * Query a representation hint from this node.
     *
     * <p>If the hint is not set on this node, its parents will be recursively
     * checked for a value.</p>
     *
     * @param hint the hint to get
     * @param <V> value type
     * @return value of the hint, or {@link RepresentationHint#defaultValue()}
     * @since 4.0.0
     */
    <V> @Nullable V hint(RepresentationHint<V> hint);

    /**
     * Query a representation hint from this node.
     *
     * <p>This will only check the current node, and return null rather than
     * any default value.</p>
     *
     * @param hint the hint to get
     * @param <V> value type
     * @return value of the hint, or {@code null}
     * @since 4.0.0
     */
    <V> @Nullable V ownHint(RepresentationHint<V> hint);

    /**
     * Get an unmodifiable copy of representation hints stored on this node.
     *
     * <p>This does not include inherited hints.</p>
     *
     * @return copy of hints this node has set.
     * @since 4.0.0
     */
    Map<RepresentationHint<?>, ?> ownHints();

}
