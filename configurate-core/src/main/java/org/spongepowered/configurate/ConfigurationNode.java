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

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.configurate.transformation.NodePath;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A node in the configuration tree.
 *
 * <p>All aspects of a configurations structure are represented using instances of
 * {@link ConfigurationNode}, and the links between them.</p>
 *
 * <p>{@link ConfigurationNode}s can hold different types of {@link ValueType values}. They can:</p>
 *
 * <ul>
 *     <li>Hold a single "scalar" value ({@link ValueType#SCALAR})</li>
 *     <li>Represent a "list" of child {@link ConfigurationNode}s ({@link ValueType#LIST})</li>
 *     <li>Represent a "map" of child {@link ConfigurationNode}s ({@link ValueType#MAP})</li>
 *     <li>Hold no value at all ({@link ValueType#NULL})</li>
 * </ul>
 *
 * <p>The overall configuration stems from a single "root" node, which is provided by the
 * {@link ConfigurationLoader}, or by other means programmatically.</p>
 *
 * <p>This is effectively the main class of configurate.</p>
 */
public interface ConfigurationNode<T extends ConfigurationNode<T>> {
    int NUMBER_DEF = 0;

    /**
     * Gets the "key" of this node.
     *
     * <p>The key determines this {@link ConfigurationNode}s position within the overall
     * configuration structure.</p>
     *
     * <p>If this node is currently {@link #isVirtual() virtual}, this method's result may be
     * inaccurate.</p>
     *
     * <p>Note that this method only returns the nearest "link" in the hierarchy, and does not
     * return a representation of the full path. See {@link #getPath()} for that.</p>
     *
     * <p>The {@link ConfigurationNode}s returned as values from {@link #getChildrenMap()} will
     * have keys derived from their pairing in the map node.</p>
     *
     * <p>The {@link ConfigurationNode}s returned from {@link #getChildrenList()} will have keys
     * derived from their position (index) in the list node.</p>
     *
     * @return The key of this node
     */
    @Nullable
    Object getKey();

    /**
     * Gets the full path of {@link #getKey() keys} from the root node to this node.
     *
     * <p>Node implementations may not keep a full path for each node, so this method may be
     * somewhat complex to calculate.</p>
     *
     * @return An array compiled from the keys for each node up the hierarchy
     */
    @NonNull
    NodePath getPath();

    /**
     * Gets the parent of this node.
     *
     * <p>If this node is currently {@link #isVirtual() virtual}, this method's result may be
     * inaccurate.</p>
     *
     * @return The nodes parent
     */
    @Nullable
    T getParent();

    /**
     * Gets the node at the given (relative) path, possibly traversing multiple levels of nodes.
     *
     * <p>This is the main method used to navigate through the configuration.</p>
     *
     * <p>The path parameter effectively consumes an array of keys, which locate the unique position
     * of a given node within the structure.</p>
     *
     * <p>A node is <b>always</b> returned by this method. If the given node does not exist in the
     * structure, a {@link #isVirtual() virtual} node will be returned which represents the
     * position.</p>
     *
     * @param path The path to fetch the node at
     * @return The node at the given path, possibly virtual
     */
    @NonNull
    T getNode(@NonNull Object... path);

    /**
     * Gets the node at the given (relative) path, possibly traversing multiple levels of nodes.
     *
     * <p>This is the main method used to navigate through the configuration.</p>
     *
     * <p>The path parameter effectively consumes an array of keys, which locate the unique position
     * of a given node within the structure.</p>
     *
     * <p>A node is <b>always</b> returned by this method. If the given node does not exist in the
     * structure, a {@link #isVirtual() virtual} node will be returned which represents the
     * position.</p>
     *
     * @param path The path to fetch the node at
     * @return The node at the given path, possibly virtual
     */
    @NonNull
    T getNode(@NonNull Iterable<Object> path);

    /**
     * Gets if this node is virtual.
     *
     * <p>Virtual nodes are nodes which are not attached to a wider configuration structure.</p>
     *
     * <p>A node is primarily "virtual" when it has no set value.</p>
     *
     * @return true if this node is virtual
     */
    boolean isVirtual();

    /**
     * Gets the options that currently apply to this node
     *
     * @return The ConfigurationOptions instance that governs the functionality of this node
     */
    @NonNull
    ConfigurationOptions getOptions();

    /**
     * Gets the value type of this node.
     *
     * @return The value type
     */
    @NonNull
    ValueType getValueType();

    /**
     * Gets if this node has "list children".
     *
     * @return if this node has children in the form of a list
     */
    default boolean isList() {
        return getValueType() == ValueType.LIST;
    }

    /**
     * Gets if this node has "map children".
     *
     * @return if this node has children in the form of a map
     */
    default boolean isMap() {
        return getValueType() == ValueType.MAP;
    }

    /**
     * Return true when this node has a null or empty value. Values that may result in this method returning true include:
     *
     * <ul>
     *     <li><code>null</code></li>
     *     <li>the empty string</li>
     *     <li>an empty map</li>
     *     <li>an empty list</li>
     *     <li>Any other type of empty collection</li>
     * </ul>
     *
     * This is a distinct value from {@link #isVirtual()}. Emptiness refers to the value of this node itself,
     * while virtuality refers to whether or not this node
     *
     * @return Whether this node is empty
     */
    boolean isEmpty();

    /**
     * Gets the "list children" attached to this node, if it has any.
     *
     * <p>If this node does not {@link #isList() have list children}, an empty list is
     * returned.</p>
     *
     * @return The list children currently attached to this node
     */
    @NonNull
    List<T> getChildrenList();

    /**
     * Gets the "map children" attached to this node, if it has any.
     *
     * <p>If this node does not {@link #isMap() have map children}, an empty map
     * returned.</p>
     *
     * @return The map children currently attached to this node
     */
    @NonNull
    Map<Object, T> getChildrenMap();

    /**
     * Get the current value associated with this node.
     *
     * <p>If this node has children, this method will recursively unwrap them to construct a List
     * or a Map.</p>
     *
     * @see #getValue(Object)
     * @return This configuration's current value, or null if there is none
     */
    @Nullable
    default Object getValue() {
        return getValue((Object) null);
    }

    /**
     * Get the current value associated with this node.
     *
     * <p>If this node has children, this method will recursively unwrap them to construct a List
     * or a Map.</p>
     *
     * @param def The default value to return if this node has no set value
     * @return This configuration's current value, or {@code def} if there is none
     */
    Object getValue(@Nullable Object def);

    /**
     * Get the current value associated with this node.
     *
     * <p>If this node has children, this method will recursively unwrap them to construct a List
     * or a Map.</p>
     *
     * @param defSupplier The function that will be called to calculate a default value only if
     *                    there is no existing value
     * @return This configuration's current value, or {@code def} if there is none
     */
    Object getValue(@NonNull Supplier<Object> defSupplier);

    /**
     * Gets the appropriately transformed typed version of this node's value from the provided
     * transformation function.
     *
     * @param transformer The transformation function
     * @param <V> The expected type
     * @return A transformed value of the correct type, or null either if no value is present or the
     * value could not be converted
     */
    @Nullable
    default <V> V getValue(@NonNull Function<Object, V> transformer) {
        return getValue(transformer, (V) null);
    }

    /**
     * Gets the appropriately transformed typed version of this node's value from the provided
     * transformation function.
     *
     * @param transformer The transformation function
     * @param def The default value to return if this node has no set value or is not of a
     *            convertible type
     * @param <V> The expected type
     * @return A transformed value of the correct type, or {@code def} either if no value is present
     * or the value could not be converted
     */
    <V> V getValue(@NonNull Function<Object, V> transformer, @Nullable V def);

    /**
     * Gets the appropriately transformed typed version of this node's value from the provided
     * transformation function.
     *
     * @param transformer The transformation function
     * @param defSupplier The function that will be called to calculate a default value only if
     *                    there is no existing value of the correct type
     * @param <V> The expected type
     * @return A transformed value of the correct type, or {@code def} either if no value is present
     * or the value could not be converted
     */
    <V> V getValue(@NonNull Function<Object, V> transformer, @NonNull Supplier<V> defSupplier);

    /**
     * If this node has list values, this function unwraps them and converts them to an appropriate
     * type based on the provided function.
     *
     * <p>If this node has a scalar value, this function treats it as a list with one value</p>
     *
     * @param transformer The transformation function
     * @param <V> The expected type
     * @return An immutable copy of the values contained
     */
    @NonNull
    <V> List<V> getList(@NonNull Function<Object, V> transformer);

    /**
     * If this node has list values, this function unwraps them and converts them to an appropriate
     * type based on the provided function.
     *
     * <p>If this node has a scalar value, this function treats it as a list with one value.</p>
     *
     * @param transformer The transformation function
     * @param def The default value if no appropriate value is set
     * @param <V> The expected type
     * @return An immutable copy of the values contained that could be successfully converted, or {@code def} if no
     * values could be converted
     */
    <V> List<V> getList(@NonNull Function<Object, V> transformer, @Nullable List<V> def);

    /**
     * If this node has list values, this function unwraps them and converts them to an appropriate
     * type based on the provided function.
     *
     * <p>If this node has a scalar value, this function treats it as a list with one value.</p>
     *
     * @param transformer The transformation function
     * @param defSupplier The function that will be called to calculate a default value only if there is no existing
     *                    value of the correct type
     * @param <V> The expected type
     * @return An immutable copy of the values contained that could be successfully converted, or {@code def} if no
     * values could be converted
     */
    <V> List<V> getList(@NonNull Function<Object, V> transformer, @NonNull Supplier<List<V>> defSupplier);

    /**
     * If this node has list values, this function unwraps them and converts them to an appropriate
     * type based on the provided function.
     *
     * <p>If this node has a scalar value, this function treats it as a list with one value.</p>
     *
     * @param type The expected type
     * @param <V> The expected type
     * @return An immutable copy of the values contained
     * @throws ObjectMappingException If any value fails to be converted to the requested type
     */
    @NonNull
    default <V> List<V> getList(@NonNull TypeToken<V> type) throws ObjectMappingException {
        return getList(type, ImmutableList.of());
    }

    /**
     * If this node has list values, this function unwraps them and converts them to an appropriate
     * type based on the provided function.
     *
     * <p>If this node has a scalar value, this function treats it as a list with one value.</p>
     *
     * @param type The expected type
     * @param def The default value if no appropriate value is set
     * @param <V> The expected type
     * @return An immutable copy of the values contained that could be successfully converted, or {@code def} if no
     * values could be converted
     * @throws ObjectMappingException If any value fails to be converted to the requested type
     */
    <V> List<V> getList(@NonNull TypeToken<V> type, @Nullable List<V> def) throws ObjectMappingException;

    /**
     * If this node has list values, this function unwraps them and converts them to an appropriate
     * type based on the provided function.
     *
     * <p>If this node has a scalar value, this function treats it as a list with one value.</p>
     *
     * @param type The expected type
     * @param defSupplier The function that will be called to calculate a default value only if there is no existing
     *                    value of the correct type
     * @param <V> The expected type
     * @return An immutable copy of the values contained that could be successfully converted, or {@code def} if no
     * values could be converted
     * @throws ObjectMappingException If any value fails to be converted to the requested type
     */
    <V> List<V> getList(@NonNull TypeToken<V> type, @NonNull Supplier<List<V>> defSupplier) throws ObjectMappingException;

    /**
     * Gets the value typed using the appropriate type conversion from {@link Types}
     *
     * @see #getValue()
     * @return The appropriate type conversion, null if no appropriate value is available
     */
    @Nullable
    default String getString() {
        return getString(null);
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Types}
     *
     * @param def The default value if no appropriate value is set
     * @see #getValue()
     * @return The appropriate type conversion, {@code def} if no appropriate value is available
     */
    default String getString(@Nullable String def) {
        return getValue(Types::asString, def);
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Types}
     *
     * @see #getValue()
     * @return The appropriate type conversion, 0 if no appropriate value is available
     */
    default float getFloat() {
        return getFloat(NUMBER_DEF);
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Types}
     *
     * @param def The default value if no appropriate value is set
     * @see #getValue()
     * @return The appropriate type conversion, {@code def} if no appropriate value is available
     */
    default float getFloat(float def) {
        return getValue(Types::asFloat, def);
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Types}
     *
     * @see #getValue()
     * @return The appropriate type conversion, 0 if no appropriate value is available
     */
    default double getDouble() {
        return getDouble(NUMBER_DEF);
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Types}
     *
     * @param def The default value if no appropriate value is set
     * @see #getValue()
     * @return The appropriate type conversion, {@code def} if no appropriate value is available
     */
    default double getDouble(double def) {
        return getValue(Types::asDouble, def);
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Types}
     *
     * @see #getValue()
     * @return The appropriate type conversion, 0 if no appropriate value is available
     */
    default int getInt() {
        return getInt(NUMBER_DEF);
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Types}
     *
     * @param def The default value if no appropriate value is set
     * @see #getValue()
     * @return The appropriate type conversion, {@code def} if no appropriate value is available
     */
    default int getInt(int def) {
        return getValue(Types::asInt, def);
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Types}
     *
     * @see #getValue()
     * @return The appropriate type conversion, 0 if no appropriate value is available
     */
    default long getLong() {
        return getLong(NUMBER_DEF);
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Types}
     *
     * @param def The default value if no appropriate value is set
     * @see #getValue()
     * @return The appropriate type conversion, {@code def} if no appropriate value is available
     */
    default long getLong(long def) {
        return getValue(Types::asLong, def);
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Types}
     *
     * @see #getValue()
     * @return The appropriate type conversion, false if no appropriate value is available
     */
    default boolean getBoolean() {
        return getBoolean(false);
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Types}
     *
     * @param def The default value if no appropriate value is set
     * @see #getValue()
     * @return The appropriate type conversion, {@code def} if no appropriate value is available
     */
    default boolean getBoolean(boolean def) {
        return getValue(Types::asBoolean, def);
    }

    /**
     * Get the current value associated with this node.
     *
     * <p>If this node has children, this method will recursively unwrap them to construct a
     * List or a Map.</p>
     *
     * <p>This method will also perform deserialization using the appropriate TypeSerializer for
     * the given type, or casting if no type serializer is found.</p>
     *
     * @param type The type to deserialize to
     * @param <V> the type to get
     * @return the value if present and of the proper type, else null
     * @throws ObjectMappingException If the value fails to be converted to the requested type
     */
    @Nullable
    default <V> V getValue(@NonNull TypeToken<V> type) throws ObjectMappingException {
        return getValue(type, (V) null);
    }

    /**
     * Get the current value associated with this node.
     *
     * <p>If this node has children, this method will recursively unwrap them to construct a
     * List or a Map.</p>
     *
     * <p>This method will also perform deserialization using the appropriate TypeSerializer for
     * the given type, or casting if no type serializer is found.</p>
     *
     * @param type The type to deserialize to
     * @param def The value to return if no value or value is not of appropriate type
     * @param <V> the type to get
     * @return the value if of the proper type, else {@code def}
     * @throws ObjectMappingException If the value fails to be converted to the requested type
     */
    <V> V getValue(@NonNull TypeToken<V> type, V def) throws ObjectMappingException;

    /**
     * Get the current value associated with this node.
     *
     * <p>If this node has children, this method will recursively unwrap them to construct a
     * List or a Map.</p>
     *
     * <p>This method will also perform deserialization using the appropriate TypeSerializer for
     * the given type, or casting if no type serializer is found.</p>
     *
     * @param type The type to deserialize to
     * @param defSupplier The function that will be called to calculate a default value only if there is no existing
     *                    value of the correct type
     * @param <V> the type to get
     * @return the value if of the proper type, else {@code def}
     * @throws ObjectMappingException If the value fails to be converted to the requested type
     */
    <V> V getValue(@NonNull TypeToken<V> type, @NonNull Supplier<V> defSupplier) throws ObjectMappingException;

    /**
     * Set this node's value to the given value.
     *
     * <p>If the provided value is a {@link Collection} or a {@link Map}, it will be unwrapped into
     * the appropriate configuration node structure.</p>
     *
     * @param value The value to set
     * @return this
     */
    @NonNull
    T setValue(@Nullable Object value);

    /**
     * Set this node's value to the given value.
     *
     * <p>If the provided value is a {@link Collection} or a {@link Map}, it will be unwrapped into
     * the appropriate configuration node structure.</p>
     *
     * <p>This method will also perform serialization using the appropriate TypeSerializer for the
     * given type, or casting if no type serializer is found.</p>
     *
     * @param type The type to use for serialization type information
     * @param value The value to set
     * @param <V> The type to serialize to
     * @return this
     * @throws ObjectMappingException If the value fails to be converted to the requested type. No change will be made to the node.
     */
    @NonNull
    default <V> T setValue(@NonNull TypeToken<V> type, @Nullable V value) throws ObjectMappingException {
        if (value == null) {
            return setValue(null);
        }

        TypeSerializer<V> serial = getOptions().getSerializers().get(type);
        if (serial != null) {
            serial.serialize(type, value, self());
        } else if (getOptions().acceptsType(value.getClass())) {
            setValue(value); // Just write if no applicable serializer exists?
        } else {
            throw new ObjectMappingException("No serializer available for type " + type);
        }
        return self();
    }

    /**
     * Set all the values from the given node that are not present in this node
     * to their values in the provided node.
     *
     * <p>Map keys will be merged. Lists and scalar values will be replaced.</p>
     *
     * @param other The node to merge values from
     * @return this
     */
    @NonNull
    T mergeValuesFrom(@NonNull ConfigurationNode<?> other);

    /**
     * Removes a direct child of this node
     *
     * @param key The key of the node to remove
     * @return If a node was removed
     */
    boolean removeChild(@NonNull Object key);

    /**
     * Gets a new child node created as the next entry in the list.
     *
     * @return A new child created as the next entry in the list when it is attached
     */
    @NonNull
    T appendListNode();

    /**
     * Creates a deep copy of this node.
     *
     * <p>If this node has child nodes (is a list or map), the child nodes will
     * also be copied. This action is performed recursively.</p>
     *
     * <p>The resultant node will (initially) contain the same value(s) as this node,
     * and will therefore be {@link Object#equals(Object) equal}, however, changes made to
     * the original will not be reflected in the copy, and vice versa.</p>
     *
     * <p>The actual scalar values that back the configuration will
     * <strong>not</strong> be copied - only the node structure that forms the
     * configuration. This is not a problem in most cases, as the scalar values
     * stored in configurations are usually immutable. (e.g. strings, numbers, booleans).</p>
     *
     * @return A copy of this node
     */
    @NonNull
    T copy();

    /**
     * Get own instance. Primarily used for type-safety in implementations
     *
     * @return this
     */
    @NonNull
    T self();
}
