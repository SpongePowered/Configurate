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
import static org.spongepowered.configurate.util.Typing.makeListType;

import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.configurate.serialize.Scalars;
import org.spongepowered.configurate.serialize.TypeSerializer;
import org.spongepowered.configurate.transformation.NodePath;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
 *     <li>Hold a single "scalar" value (accessed by {@link #get()}</li>
 *     <li>Represent a "list" of child {@link ConfigurationNode}s (accessed by {@link #isList()} and {@link #childrenList()})</li>
 *     <li>Represent a "map" of child {@link ConfigurationNode}s (accessed by {@link #isMap()} and {@link #childrenMap()})</li>
 *     <li>Hold no value at all (when {@link #virtual()} is true)</li>
 * </ul>
 *
 * <p>The overall configuration stems from a single "root" node, which is
 * provided by the {@link ConfigurationLoader}, or by other means programmatically.</p>
 *
 * <p>This is effectively the main class of Configurate.</p>
 */
public interface ConfigurationNode {

    /**
     * Default value for unknown number results.
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
     */
    NodePath path();

    /**
     * Gets the parent of this node.
     *
     * <p>If this node is currently {@link #virtual() virtual}, this method's
     * result may be inaccurate.</p>
     *
     * @return the nodes parent
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
     */
    boolean virtual();

    /**
     * Gets the options that currently apply to this node.
     *
     * @return the {@link ConfigurationOptions} instance controlling the functionality
     *          of this node.
     */
    ConfigurationOptions options();

    /**
     * Gets if this node has "list children".
     *
     * @return if this node has children in the form of a list
     */
    boolean isList();

    /**
     * Gets if this node has "map children".
     *
     * @return if this node has children in the form of a map
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
     */
    boolean isEmpty();

    /**
     * Gets the "list children" attached to this node, if it has any.
     *
     * <p>If this node does not {@link #isList() have list children}, an empty
     * list is returned.</p>
     *
     * @return the list children currently attached to this node
     */
    List<? extends ConfigurationNode> childrenList();

    /**
     * Gets the "map children" attached to this node, if it has any.
     *
     * <p>If this node does not {@link #isMap() have map children}, an empty map
     * returned.</p>
     *
     * @return the map children currently attached to this node
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
     */
    default <V> Collector<Map.Entry<?, V>, ? extends ConfigurationNode, ? extends ConfigurationNode> toMapCollector(final TypeToken<V> valueType) {
        return Collector.of(() -> this, (node, entry) -> {
            try {
                node.node(entry.getKey()).set(valueType, entry.getValue());
            } catch (ObjectMappingException e) {
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
     */
    default <V> Collector<Map.Entry<?, V>, ? extends ConfigurationNode, ? extends ConfigurationNode> toMapCollector(final Class<V> valueType) {
        return Collector.of(() -> this, (node, entry) -> {
            try {
                node.node(entry.getKey()).set(valueType, entry.getValue());
            } catch (ObjectMappingException e) {
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
     */
    default <V> Collector<V, ? extends ConfigurationNode, ? extends ConfigurationNode> toListCollector(final TypeToken<V> valueType) {
        return Collector.of(() -> this, (node, value) -> {
            try {
                node.appendListNode().set(valueType, value);
            } catch (ObjectMappingException e) {
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
     */
    default <V> Collector<V, ? extends ConfigurationNode, ? extends ConfigurationNode> toListCollector(final Class<V> valueType) {
        return Collector.of(() -> this, (node, value) -> {
            try {
                node.appendListNode().set(valueType, value);
            } catch (ObjectMappingException e) {
                throw new IllegalArgumentException(e);
            }
        }, ConfigurationNode::mergeFrom);
    }

    /**
     * Get the current value associated with this node.
     *
     * <p>If this node has children, this method will recursively unwrap them to
     * construct a List or a Map.</p>
     *
     * @return this configuration's current value, or null if there is none
     * @see #get(Object)
     */
    @Nullable Object get();

    /**
     * Get the current value associated with this node.
     *
     * <p>If this node has children, this method will recursively unwrap them to
     * construct a List or a Map.</p>
     *
     * @param def the default value to return if this node has no set value
     * @return this configuration's current value, or {@code def} if none.
     */
    default Object get(Object def) {
        final @Nullable Object value = get();
        return value == null ? storeDefault(this, def) : value;
    }

    /**
     * Get the current value associated with this node.
     *
     * <p>If this node has children, this method will recursively unwrap them to
     * construct a List or a Map.</p>
     *
     * @param defSupplier the function that will be called to calculate a
     *                    default value only if there is no existing value
     * @return this configuration's current value, or {@code def} if none
     */
    default Object get(Supplier<Object> defSupplier) {
        final @Nullable Object value = get();
        return value == null ? storeDefault(this, defSupplier.get()) : value;
    }

    /**
     * Get the current value associated with this node.
     *
     * <p>If this node has children, this method will recursively unwrap them to
     * construct a List or a Map.</p>
     *
     * <p>This method will also perform deserialization using the appropriate
     * {@link TypeSerializer} for the given type, or casting if no type
     * serializer is found.</p>
     *
     * @param type the type to deserialize to
     * @param <V> the type to get
     * @return the value if present and of the proper type, else null
     * @throws ObjectMappingException if the value fails to be converted to the
     *                                requested type
     */
    @SuppressWarnings("unchecked") // type token
    default <V> @Nullable V get(TypeToken<V> type) throws ObjectMappingException {
        return (V) get(type.getType());
    }

    /**
     * Get the current value associated with this node.
     *
     * <p>If this node has children, this method will recursively unwrap them to
     * construct a List or a Map.</p>
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
     * @throws ObjectMappingException if the value fails to be converted to the
     *                                requested type
     */
    @SuppressWarnings("unchecked") // type is verified by the token
    default <V> V get(TypeToken<V> type, V def) throws ObjectMappingException {
        return (V) get(type.getType(), def);
    }

    /**
     * Get the current value associated with this node.
     *
     * <p>If this node has children, this method will recursively unwrap them to
     * construct a List or a Map.</p>
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
     * @throws ObjectMappingException if the value fails to be converted to the
     *                                requested type
     */
    @SuppressWarnings("unchecked") // type is verified by the token
    default <V> V get(TypeToken<V> type, Supplier<V> defSupplier) throws ObjectMappingException {
        return (V) get(type.getType(), defSupplier);
    }

    /**
     * Get the current value associated with this node.
     *
     * <p>If this node has children, this method will recursively unwrap them to
     * construct a List or a Map.</p>
     *
     * <p>This method will also perform deserialization using the appropriate
     * {@link TypeSerializer} for the given type, or casting if no type
     * serializer is found.</p>
     *
     * @param type the type to deserialize to
     * @param <V> the type to get
     * @return the value if present and of the proper type, else null
     * @throws ObjectMappingException if the value fails to be converted to the
     *                                requested type
     */
    @SuppressWarnings("unchecked") // type is verified by the class parameter
    default <V> @Nullable V get(Class<V> type) throws ObjectMappingException {
        return (V) get((Type) type);
    }

    /**
     * Get the current value associated with this node.
     *
     * <p>If this node has children, this method will recursively unwrap them to
     * construct a List or a Map.</p>
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
     * @throws ObjectMappingException if the value fails to be converted to the
     *                                requested type
     */
    @SuppressWarnings("unchecked") // type is verified by the class parameter
    default <V> V get(Class<V> type, V def) throws ObjectMappingException {
        return (V) get((Type) type, def);
    }

    /**
     * Get the current value associated with this node.
     *
     * <p>If this node has children, this method will recursively unwrap them to
     * construct a List or a Map.</p>
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
     * @throws ObjectMappingException if the value fails to be converted to the
     *                                requested type
     */
    @SuppressWarnings("unchecked") // type is verified by the class parameter
    default <V> V get(Class<V> type, Supplier<V> defSupplier) throws ObjectMappingException {
        return (V) get((Type) type, defSupplier);
    }

    /**
     * Get the current value associated with this node.
     *
     * <p>This method will attempt to deserialize the node's value to the
     * provided {@link Type} using a configured {@link TypeSerializer} for
     * the given type, or casting if no type serializer is found.</p>
     *
     * @param type the type to deserialize to
     * @return the value if present and of the proper type, else null
     * @throws ObjectMappingException if the value fails to be converted to the
     *                                requested type
     */
    @Nullable Object get(Type type) throws ObjectMappingException;

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
     * @throws ObjectMappingException if the value fails to be converted to the
     *                                requested type
     */
    default Object get(Type type, Object def) throws ObjectMappingException {
        final @Nullable Object value = get(type);
        return value == null ? storeDefault(this, type, def) : value;
    }

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
     * @throws ObjectMappingException if the value fails to be converted to the
     *                                requested type
     */
    default Object get(Type type, Supplier<?> defSupplier) throws ObjectMappingException {
        final @Nullable Object value = get(type);
        return value == null ? storeDefault(this, type, defSupplier.get()) : value;
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
     * @throws ObjectMappingException if any value fails to be converted to the
     *                                requested type
     */
    default <V> List<V> getList(TypeToken<V> type) throws ObjectMappingException { // @cs-: NoGetSetPrefix (not a bean method)
        return getList(type, Collections.emptyList());
    }

    /**
     * If this node has list values, this function unwraps them and converts
     * them to an appropriate type based on the provided function.
     *
     * <p>If this node has a scalar value, this function treats it as a list
     * with one value.</p>
     *
     * @param type expected type
     * @param def default value if no appropriate value is set
     * @param <V> expected type
     * @return an immutable copy of the values contained that could be
     *         successfully converted, or {@code def} if no values could be
     *         converted.
     * @throws ObjectMappingException if any value fails to be converted to the
     *                                requested type
     */
    default <V> List<V> getList(TypeToken<V> type, List<V> def) throws ObjectMappingException { // @cs-: NoGetSetPrefix (not a bean method)
        final List<V> ret = get(makeListType(type), def);
        return ret.isEmpty() ? storeDefault(this, def) : ret;
    }

    /**
     * If this node has list values, this function unwraps them and converts
     * them to an appropriate type based on the provided function.
     *
     * <p>If this node has a scalar value, this function treats it as a list
     * with one value.</p>
     *
     * @param type expected type
     * @param defSupplier function that will be called to calculate a default
     *                    value only if there is no existing value of the
     *                    correct type
     * @param <V> expected type
     * @return an immutable copy of the values contained that could be
     *         successfully converted, or {@code def} if no values could be
     *         converted.
     * @throws ObjectMappingException if any value fails to be converted to the
     *                                requested type
     */
    // @cs-: NoGetSetPrefix (not a bean method)
    default <V> List<V> getList(TypeToken<V> type, Supplier<List<V>> defSupplier) throws ObjectMappingException {
        final List<V> ret = get(makeListType(type), defSupplier);
        return ret.isEmpty() ? storeDefault(this, defSupplier.get()) : ret;
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @return the value coerced to a {@link String}, or null if no value
     * @see #get()
     */
    default @Nullable String getString() { // @cs-: NoGetSetPrefix (not a bean method)
        return Scalars.STRING.tryDeserialize(get());
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @param def the default value if no appropriate value is set
     * @return the value coerced to a {@link String}, or {@code def} if no value
     * @see #get()
     */
    default String getString(final String def) { // @cs-: NoGetSetPrefix (not a bean method)
        requireNonNull(def, "def");
        final @Nullable String value = getString();
        if (value != null) {
            return value;
        }
        if (options().shouldCopyDefaults()) {
            set(def);
        }
        return def;
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @return the value coerced to a float, or {@link #NUMBER_DEF} if not a float
     * @see #get()
     */
    default float getFloat() { // @cs-: NoGetSetPrefix (not a bean method)
        return getFloat(NUMBER_DEF);
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @param def the default value if no appropriate value is set
     * @return the value coerced to a float, or {@code def} if not a float
     * @see #get()
     */
    default float getFloat(float def) { // @cs-: NoGetSetPrefix (not a bean method)
        final @Nullable Float val = Scalars.FLOAT.tryDeserialize(get());
        if (val != null) {
            return val;
        }
        if (options().shouldCopyDefaults() && def != NUMBER_DEF) {
            set(def);
        }
        return def;
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @return the value coerced to a double, or {@link #NUMBER_DEF} if
     *         coercion failed
     * @see #get()
     */
    default double getDouble() { // @cs-: NoGetSetPrefix (not a bean method)
        return getDouble(NUMBER_DEF);
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @param def the default value if no appropriate value is set
     * @return the value coerced to a double, or {@code def} if coercion failed
     * @see #get()
     */
    default double getDouble(double def) { // @cs-: NoGetSetPrefix (not a bean method)
        final @Nullable Double val = Scalars.DOUBLE.tryDeserialize(get());
        if (val != null) {
            return val;
        }
        if (options().shouldCopyDefaults() && def != NUMBER_DEF) {
            set(def);
        }
        return def;
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @return value coerced to an integer, or 0 if coercion failed.
     * @see #get()
     */
    default int getInt() { // @cs-: NoGetSetPrefix (not a bean method)
        return getInt(NUMBER_DEF);
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @param def the default value if no appropriate value is set
     * @return value coerced to an integer, or {@code def} if coercion failed.
     * @see #get()
     */
    default int getInt(int def) { // @cs-: NoGetSetPrefix (not a bean method)
        final @Nullable Integer val = Scalars.INTEGER.tryDeserialize(get());
        if (val != null) {
            return val;
        }
        if (options().shouldCopyDefaults() && def != NUMBER_DEF) {
            set(def);
        }
        return def;
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @return value coerced to a long, or 0 if coercion failed
     * @see #get()
     */
    default long getLong() { // @cs-: NoGetSetPrefix (not a bean method)
        return getLong(NUMBER_DEF);
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @param def the default value if no appropriate value is set
     * @return value coerced to a long, or {@code def} if coercion failed
     * @see #get()
     */
    default long getLong(long def) { // @cs-: NoGetSetPrefix (not a bean method)
        final @Nullable Long val = Scalars.LONG.tryDeserialize(get());
        if (val != null) {
            return val;
        }
        if (options().shouldCopyDefaults() && def != NUMBER_DEF) {
            set(def);
        }
        return def;
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @return value coerced to a boolean, or 0 if coercion failed
     * @see #get()
     */
    default boolean getBoolean() { // @cs-: NoGetSetPrefix (not a bean method)
        return getBoolean(false);
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @param def the default value if no appropriate value is set
     * @return value coerced to a boolean, or {@code def} if coercion failed
     * @see #get()
     */
    default boolean getBoolean(boolean def) { // @cs-: NoGetSetPrefix (not a bean method)
        final @Nullable Boolean val = Scalars.BOOLEAN.tryDeserialize(get());
        if (val != null) {
            return val;
        }
        if (options().shouldCopyDefaults()) {
            set(def);
        }
        return def;
    }

    /**
     * Set this node's value to the given value.
     *
     * <p>If the provided value is a {@link Collection} or a {@link Map}, it will be unwrapped into
     * the appropriate configuration node structure.</p>
     *
     * @param value the value to set
     * @return this node
     */
    ConfigurationNode set(@Nullable Object value);

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
     * @throws ObjectMappingException if the value fails to be converted to the
     *                                requested type. No change will be made to
     *                                the node.
     */
    <V> ConfigurationNode set(TypeToken<V> type, @Nullable V value) throws ObjectMappingException;

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
     * @throws ObjectMappingException if the value fails to be converted to the
     *                                requested type. No change will be made to
     *                                the node.
     */
    <V> ConfigurationNode set(Class<V> type, @Nullable V value) throws ObjectMappingException;

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
     * @throws ObjectMappingException if the value fails to be converted to the
     *                                requested type. No change will be made to
     *                                the node.
     */
    ConfigurationNode set(Type type, @Nullable Object value) throws ObjectMappingException;

    /**
     * Set all the values from the given node that are not present in this node
     * to their values in the provided node.
     *
     * <p>Map keys will be merged. Lists and scalar values will be replaced.</p>
     *
     * @param other the node to merge values from
     * @return this node
     */
    ConfigurationNode mergeFrom(ConfigurationNode other);

    /**
     * Removes a direct child of this node.
     *
     * @param key the key of the node to remove
     * @return if a node was removed
     */
    boolean removeChild(Object key);

    /**
     * Gets a new child node created as the next entry in the list.
     *
     * @return a new child created as the next entry in the list when it is
     *         attached
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
     */
    ConfigurationNode copy();

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
     */
    <V> @Nullable V ownHint(RepresentationHint<V> hint);

    /**
     * Get an unmodifiable copy of representation hints stored on this node.
     *
     * <p>This does not include inherited hints.</p>
     *
     * @return copy of hints this node has set.
     */
    Map<RepresentationHint<?>, ?> ownHints();

}
