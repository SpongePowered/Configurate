/**
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
package ninja.leaping.configurate;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A node in the configuration tree. This is more or less the main class of configurate, providing the methods to
 * navigate through the configuration tree and get values
 */
public interface ConfigurationNode {
    int NUMBER_DEF = 0;
    /**
     * The key for this node.
     * If this node is currently virtual, this method's result may be inaccurate.
     *
     * @return The key for this node
     */
    Object getKey();

    /**
     * The full path from the root to this node.
     *
     * Node implementations may keep a full path for each node, so this method may involve some object churn.
     *
     * @return An array compiled from the keys for each node up the hierarchy
     */
    Object[] getPath();

    /**
     * Returns the current parent for this node.
     * If this node is currently virtual, this method's result may be inaccurate.
     * @return The appropriate parent
     */
    ConfigurationNode getParent();

    /**
     * Return the options that currently apply to this node
     *
     * @return The ConfigurationOptions instance that governs the functionality of this node
     */
    ConfigurationOptions getOptions();

    /**
     * Get the current value associated with this node.
     * If this node has children, this method will recursively unwrap them to construct a List or a Map
     *
     * @see #getValue(Object)
     * @return This configuration's current value, or null if there is none
     */
    default Object getValue() {
        return getValue((Object) null);
    }

    /**
     * Get the current value associated with this node.
     * If this node has children, this method will recursively unwrap them to construct a List or a Map
     *
     * @param def The default value to return if this node has no set value
     * @return This configuration's current value, or {@code def} if there is none
     */
    Object getValue(Object def);

    /**
     * Get the current value associated with this node.
     * If this node has children, this method will recursively unwrap them to construct a List or a Map
     *
     * @param defSupplier The function that will be called to calculate a default value only if there is no existing
     *                    value
     * @return This configuration's current value, or {@code def} if there is none
     */

    Object getValue(Supplier<Object> defSupplier);

    /**
     * Gets the appropriately transformed typed version of this node's value from the provided transformation function
     *
     * @param transformer The transformation function
     * @param <T> The expected type
     * @return A transformed value of the correct type, or null either if no value is present or the value could not
     * be converted
     */
    default <T> T getValue(Function<Object, T> transformer) {
        return getValue(transformer, (T) null);
    }

    /**
     * Gets the appropriately transformed typed version of this node's value from the provided transformation function
     *
     * @param transformer The transformation function
     * @param def The default value to return if this node has no set value or is not of a convertable type
     * @param <T> The expected type
     * @return A transformed value of the correct type, or {@code def} either if no value is present or the value
     * could not be converted
     */
    <T> T getValue(Function<Object, T> transformer, T def);

    /**
     * Gets the appropriately transformed typed version of this node's value from the provided transformation function
     *
     * @param transformer The transformation function
     * @param defSupplier The function that will be called to calculate a default value only if there is no existing
     *                    value of the correct type
     * @param <T> The expected type
     * @return A transformed value of the correct type, or {@code def} either if no value is present or the value
     * could not be converted
     */
    <T> T getValue(Function<Object, T> transformer, Supplier<T> defSupplier);

    /**
     * If this node has list values, this function unwraps them and converts them to an appropriate type based on the
     * provided function.
     * If this node has a scalar value, this function treats it as a list with one value
     *
     * @param transformer The transformation function
     * @param <T> The expected type
     * @return An immutable copy of the values contained
     */
    <T> List<T> getList(Function<Object, T> transformer);

    /**
     * If this node has list values, this function unwraps them and converts them to an appropriate type based on the
     * provided function.
     * If this node has a scalar value, this function treats it as a list with one value
     *
     * @param transformer The transformation function
     * @param def The default value if no appropriate value is set
     * @param <T> The expected type
     * @return An immutable copy of the values contained that could be successfully converted, or {@code def} if no
     * values could be converted
     */
    <T> List<T> getList(Function<Object, T> transformer, List<T> def);

    /**
     * If this node has list values, this function unwraps them and converts them to an appropriate type based on the
     * provided function.
     * If this node has a scalar value, this function treats it as a list with one value
     *
     * @param transformer The transformation function
     * @param defSupplier The function that will be called to calculate a default value only if there is no existing
     *                    value of the correct type
     * @param <T> The expected type
     * @return An immutable copy of the values contained that could be successfully converted, or {@code def} if no
     * values could be converted
     */
    <T> List<T> getList(Function<Object, T> transformer, Supplier<List<T>> defSupplier);

    /**
     * If this node has list values, this function unwraps them and converts them to an appropriate type based on the
     * provided type.
     * If this node has a scalar value, this function treats it as a list with one value
     *
     * @param type The expected type
     * @param <T> The expected type
     * @return An immutable copy of the values contained
     */
    default <T> List<T> getList(TypeToken<T> type) throws ObjectMappingException {
        return getList(type, ImmutableList.of());
    }

    /**
     * If this node has list values, this function unwraps them and converts them to an appropriate type based on the
     * provided type.
     * If this node has a scalar value, this function treats it as a list with one value
     *
     * @param type The expected type
     * @param def The default value if no appropriate value is set
     * @param <T> The expected type
     * @return An immutable copy of the values contained that could be successfully converted, or {@code def} if no
     * values could be converted
     */
    <T> List<T> getList(TypeToken<T> type, List<T> def) throws ObjectMappingException;

    /**
     * If this node has list values, this function unwraps them and converts them to an appropriate type based on the
     * provided type.
     * If this node has a scalar value, this function treats it as a list with one value
     *
     * @param type The expected type
     * @param defSupplier The function that will be called to calculate a default value only if there is no existing
     *                    value of the correct type
     * @param <T> The expected type
     * @return An immutable copy of the values contained that could be successfully converted, or {@code def} if no
     * values could be converted
     */
    <T> List<T> getList(TypeToken<T> type, Supplier<List<T>> defSupplier) throws ObjectMappingException;

    /**
     * Gets the value typed using the appropriate type conversion from {@link Types}
     *
     * @see #getValue()
     * @return The appropriate type conversion, null if no appropriate value is available
     */
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
    default String getString(String def) {
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
     * Set this node's value to the given value.
     * If the provided value is a {@link java.util.Collection} or a {@link java.util.Map}, it will be unwrapped into
     * the appropriate configuration node structure
     *
     * @param value The value to set
     * @return this
     */
    ConfigurationNode setValue(Object value);

    /**
     * Get the current value associated with this node.
     * If this node has children, this method will recursively unwrap them to construct a List or a Map.
     * This method will also perform deserialization using the appropriate TypeSerializer for the given type, or casting if no type serializer is found.
     *
     * @param type The type to deserialize to
     * @param <T> the type to get
     * @return the value if present and of the proper type, else null
     */
    default <T> T getValue(TypeToken<T> type) throws ObjectMappingException {
        return getValue(type, (T) null);
    }

    /**
     * Get the current value associated with this node.
     * If this node has children, this method will recursively unwrap them to construct a List or a Map.
     * This method will also perform deserialization using the appropriate TypeSerializer for the given type, or casting if no type serializer is found.
     *
     * @param type The type to deserialize to
     * @param def The value to return if no value or value is not of appropriate type
     * @param <T> the type to get
     * @return the value if of the proper type, else {@code def}
     */
    <T> T getValue(TypeToken<T> type, T def) throws ObjectMappingException;

    /**
     * Get the current value associated with this node.
     * If this node has children, this method will recursively unwrap them to construct a List or a Map.
     * This method will also perform deserialization using the appropriate TypeSerializer for the given type, or casting if no type serializer is found.
     *
     * @param type The type to deserialize to
     * @param defSupplier The function that will be called to calculate a default value only if there is no existing
     *                    value of the correct type
     * @param <T> the type to get
     * @return the value if of the proper type, else {@code def}
     */
    <T> T getValue(TypeToken<T> type, Supplier<T> defSupplier) throws ObjectMappingException;

    /**
     * Set this node's value to the given value.
     * If the provided value is a {@link java.util.Collection} or a {@link java.util.Map}, it will be unwrapped into
     * the appropriate configuration node structure.
     * This method will also perform serialization using the appropriate TypeSerializer for the given type, or casting if no type serializer is found.
     *
     * @param type The type to use for serialization type information
     * @param value The value to set
     * @param <T> The type to serialize to
     * @return this
     */
    <T> ConfigurationNode setValue(TypeToken<T> type, T value) throws ObjectMappingException;

    /**
     * Set all the values from the given node that are not present in this node
     * to their values in the provided node.
     *
     * Map keys will be merged. Lists and scalar values will be replaced.
     *
     * @param other The node to merge values from
     * @return this
     */
    ConfigurationNode mergeValuesFrom(ConfigurationNode other);

    /**
     * @return if this node has children in the form of a list
     */
    boolean hasListChildren();

    /**
     * @return if this node has children in the form of a map
     */
    boolean hasMapChildren();

    /**
     * Return an immutable copy of the list of children this node is aware of
     *
     * @return The children currently attached to this node
     */
    List<? extends ConfigurationNode> getChildrenList();

    /**
     * Return an immutable copy of the mapping from key to node of every child this node is aware of
     *
     * @return Child nodes currently attached
     */
    Map<Object, ? extends ConfigurationNode> getChildrenMap();

    /**
     * Removes a direct child of this node
     *
     * @param key The key of the node to remove
     * @return if an actual node was removed
     */
    boolean removeChild(Object key);

    /**
     * @return a new child created as the next entry in the list when it is attached
     */
    ConfigurationNode getAppendedNode();


    /**
     * Gets the node at the given (relative) path, possibly traversing multiple levels of nodes
     *
     * @param path The path to fetch the node at
     * @return The node at the given path, possibly virtual
     */
    ConfigurationNode getNode(Object... path);

    /**
     * Whether this node does not currently exist in the configuration structure.
     *
     * @return true if this node is not attached (this occurs primarily when the node has no set value)
     */
    boolean isVirtual();
}
