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

/**
 * A node in the configuration tree.
 *
 * <p>All aspects of a configurations structure are represented using instances
 * of {@link ConfigurationNode}, and the links between them.</p>
 *
 * <p>{@link ConfigurationNode}s can hold different types of value. They can:</p>
 *
 * <ul>
 *     <li>Hold a single "scalar" value (accessed by {@link #getValue()}</li>
 *     <li>Represent a "list" of child {@link ConfigurationNode}s (accessed by {@link #isList()} and {@link #getChildrenList()})</li>
 *     <li>Represent a "map" of child {@link ConfigurationNode}s (accessed by {@link #isMap()} and {@link #getChildrenMap()})</li>
 *     <li>Hold no value at all (when {@link #isVirtual()} is true)</li>
 * </ul>
 *
 * <p>The overall configuration stems from a single "root" node, which is
 * provided by the {@link ConfigurationLoader}, or by other means programmatically.</p>
 *
 * <p>This is effectively the main class of Configurate.</p>
 */
public interface ConfigurationNode {

    int NUMBER_DEF = 0;

    /**
     * Gets the "key" of this node.
     *
     * <p>The key determines this {@link ConfigurationNode}s position within
     * the overall configuration structure.</p>
     *
     * <p>If this node is currently {@link #isVirtual() virtual}, this method's
     * result may be inaccurate.</p>
     *
     * <p>Note that this method only returns the nearest "link" in the
     * hierarchy, and does not return a representation of the full path. See
     * {@link #getPath()} for that.</p>
     *
     * <p>The {@link ConfigurationNode}s returned as values from
     * {@link #getChildrenMap()} will have keys derived from their pairing in
     * the map node.</p>
     *
     * <p>The {@link ConfigurationNode}s returned from
     * {@link #getChildrenList()} will have keys derived from their position
     * (index) in the list node.</p>
     *
     * @return The key of this node
     */
    @Nullable Object getKey();

    /**
     * Gets the full path of {@link #getKey() keys} from the root node to this
     * node.
     *
     * <p>Node implementations may not keep a full path for each node, so this
     * method may be somewhat complex to calculate. Most uses should not need to
     * calculate the full path unless providing debug information</p>
     *
     * @return An array compiled from the keys for each node up the hierarchy
     */
    NodePath getPath();

    /**
     * Gets the parent of this node.
     *
     * <p>If this node is currently {@link #isVirtual() virtual}, this method's
     * result may be inaccurate.</p>
     *
     * @return The nodes parent
     */
    @Nullable ConfigurationNode getParent();

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
     * does not exist in the structure, a {@link #isVirtual() virtual} node will
     * be returned which represents the position.</p>
     *
     * @param path The path to fetch the node at
     * @return The node at the given path, possibly virtual
     */
    ConfigurationNode getNode(Object... path);

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
     * does not exist in the structure, a {@link #isVirtual() virtual} node will
     * be returned which represents the position.</p>
     *
     * @param path The path to fetch the node at
     * @return The node at the given path, possibly virtual
     */
    ConfigurationNode getNode(Iterable<?> path);

    /**
     * Checks whether or not a non-virtual node is present at the relative
     * path {@code path}.
     *
     * <p>This allows checking for more remote nodes in the configuration
     * hierarchy without having to instantiate new unattached node objects.</p>
     *
     * @param path Path to search at
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
     * @param path Path to search at
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
     * @return true if this node is virtual
     */
    boolean isVirtual();

    /**
     * Gets the options that currently apply to this node.
     *
     * @return The ConfigurationOptions instance that governs the functionality
     *          of this node.
     */
    ConfigurationOptions getOptions();

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
     * <p>This is a separate value from {@link #isVirtual()}. Emptiness refers
     * to the value of this node itself, while virtuality refers to whether or
     * not this node is attached to a configuration structure.
     *
     * @return Whether this node is empty
     */
    boolean isEmpty();

    /**
     * Gets the "list children" attached to this node, if it has any.
     *
     * <p>If this node does not {@link #isList() have list children}, an empty
     * list is returned.</p>
     *
     * @return The list children currently attached to this node
     */
    List<? extends ConfigurationNode> getChildrenList();

    /**
     * Gets the "map children" attached to this node, if it has any.
     *
     * <p>If this node does not {@link #isMap() have map children}, an empty map
     * returned.</p>
     *
     * @return The map children currently attached to this node
     */
    Map<Object, ? extends ConfigurationNode> getChildrenMap();

    /**
     * Get the current value associated with this node.
     *
     * <p>If this node has children, this method will recursively unwrap them to
     * construct a List or a Map.</p>
     *
     * @return This configuration's current value, or null if there is none
     * @see #getValue(Object)
     */
    @Nullable Object getValue();

    /**
     * Get the current value associated with this node.
     *
     * <p>If this node has children, this method will recursively unwrap them to
     * construct a List or a Map.</p>
     *
     * @param def The default value to return if this node has no set value
     * @return This configuration's current value, or {@code def} if none.
     */
    default Object getValue(Object def) {
        final @Nullable Object value = getValue();
        return value == null ? storeDefault(this, def) : value;
    }

    /**
     * Get the current value associated with this node.
     *
     * <p>If this node has children, this method will recursively unwrap them to
     * construct a List or a Map.</p>
     *
     * @param defSupplier The function that will be called to calculate a
     *                    default value only if there is no existing value
     * @return This configuration's current value, or {@code def} if none
     */
    default Object getValue(Supplier<Object> defSupplier) {
        final @Nullable Object value = getValue();
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
     * @param type The type to deserialize to
     * @param <V> the type to get
     * @return the value if present and of the proper type, else null
     * @throws ObjectMappingException If the value fails to be converted to the
     *                                requested type
     */
    @SuppressWarnings("unchecked") // type token
    default <V> @Nullable V getValue(TypeToken<V> type) throws ObjectMappingException {
        return (V) getValue(type.getType());
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
     * @param type The type to deserialize as.
     * @param def value to return if {@link #isVirtual()} or value is not of
     *            appropriate type
     * @param <V> the type to get
     * @return the value if of the proper type, else {@code def}
     * @throws ObjectMappingException If the value fails to be converted to the
     *                                requested type
     */
    @SuppressWarnings("unchecked") // type is verified by the token
    default <V> V getValue(TypeToken<V> type, V def) throws ObjectMappingException {
        return (V) getValue(type.getType(), def);
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
     * @param type The type to deserialize to
     * @param defSupplier The function that will be called to calculate a
     *                    default value only if there is no existing value of
     *                    the correct type
     * @param <V> the type to get
     * @return the value if of the proper type, else {@code def}
     * @throws ObjectMappingException If the value fails to be converted to the
     *                                requested type
     */
    @SuppressWarnings("unchecked") // type is verified by the token
    default <V> V getValue(TypeToken<V> type, Supplier<V> defSupplier) throws ObjectMappingException {
        return (V) getValue(type.getType(), defSupplier);
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
     * @param type The type to deserialize to
     * @param <V> the type to get
     * @return the value if present and of the proper type, else null
     * @throws ObjectMappingException If the value fails to be converted to the
     *                                requested type
     */
    @SuppressWarnings("unchecked") // type is verified by the class parameter
    default <V> @Nullable V getValue(Class<V> type) throws ObjectMappingException {
        return (V) getValue((Type) type);
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
     * @param type The type to deserialize as.
     * @param def value to return if {@link #isVirtual()} or value is not of
     *            appropriate type
     * @param <V> the type to get
     * @return the value if of the proper type, else {@code def}
     * @throws ObjectMappingException If the value fails to be converted to the
     *                                requested type
     */
    @SuppressWarnings("unchecked") // type is verified by the class parameter
    default <V> V getValue(Class<V> type, V def) throws ObjectMappingException {
        return (V) getValue((Type) type, def);
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
     * @param type The type to deserialize to
     * @param defSupplier The function that will be called to calculate a
     *                    default value only if there is no existing value of
     *                    the correct type
     * @param <V> the type to get
     * @return the value if of the proper type, else {@code def}
     * @throws ObjectMappingException If the value fails to be converted to the
     *                                requested type
     */
    @SuppressWarnings("unchecked") // type is verified by the class parameter
    default <V> V getValue(Class<V> type, Supplier<V> defSupplier) throws ObjectMappingException {
        return (V) getValue((Type) type, defSupplier);
    }

    /**
     * Get the current value associated with this node.
     *
     * <p>This method will attempt to deserialize the node's value to the
     * provided {@link Type} using a configured {@link TypeSerializer} for
     * the given type, or casting if no type serializer is found.</p>
     *
     * @param type The type to deserialize to
     * @return the value if present and of the proper type, else null
     * @throws ObjectMappingException If the value fails to be converted to the
     *                                requested type
     */
    @Nullable Object getValue(Type type) throws ObjectMappingException;

    /**
     * Get the current value associated with this node.
     *
     * <p>This method will attempt to deserialize the node's value to the
     * provided {@link Type} using a configured {@link TypeSerializer} for
     * the given type, or casting if no type serializer is found.</p>
     *
     * @param type The type to deserialize as
     * @param def value to return if {@link #isVirtual()} or value is not of
     *            appropriate type
     * @return the value if of the proper type, else {@code def}
     * @throws ObjectMappingException If the value fails to be converted to the
     *                                requested type
     */
    default Object getValue(Type type, Object def) throws ObjectMappingException {
        final @Nullable Object value = getValue(type);
        return value == null ? storeDefault(this, type, def) : value;
    }

    /**
     * Get the current value associated with this node.
     *
     * <p>This method will attempt to deserialize the node's value to the
     * provided {@link Type} using a configured {@link TypeSerializer} for
     * the given type, or casting if no type serializer is found.</p>
     *
     * @param type The type to deserialize to
     * @param defSupplier The function that will be called to calculate a
     *                    default value only if there is no existing value of
     *                    the correct type
     * @return the value if of the proper type, else {@code def}
     * @throws ObjectMappingException If the value fails to be converted to the
     *                                requested type
     */
    default Object getValue(Type type, Supplier<?> defSupplier) throws ObjectMappingException {
        final @Nullable Object value = getValue(type);
        return value == null ? storeDefault(this, type, defSupplier.get()) : value;
    }

    /**
     * If this node has list values, this function unwraps them and converts
     * them to an appropriate type based on the provided function.
     *
     * <p>If this node has a scalar value, this function treats it as a list
     * with one value.</p>
     *
     * @param type The expected type
     * @param <V> The expected type
     * @return An immutable copy of the values contained
     * @throws ObjectMappingException If any value fails to be converted to the
     *                                requested type
     */
    default <V> List<V> getList(TypeToken<V> type) throws ObjectMappingException {
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
     * @return An immutable copy of the values contained that could be
     *         successfully converted, or {@code def} if no values could be
     *         converted.
     * @throws ObjectMappingException If any value fails to be converted to the
     *                                requested type
     */
    default <V> List<V> getList(TypeToken<V> type, List<V> def) throws ObjectMappingException {
        final List<V> ret = getValue(makeListType(type), def);
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
     * @return An immutable copy of the values contained that could be
     *         successfully converted, or {@code def} if no values could be
     *         converted.
     * @throws ObjectMappingException if any value fails to be converted to the
     *                                requested type
     */
    default <V> List<V> getList(TypeToken<V> type, Supplier<List<V>> defSupplier) throws ObjectMappingException {
        final List<V> ret = getValue(makeListType(type), defSupplier);
        return ret.isEmpty() ? storeDefault(this, defSupplier.get()) : ret;
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @return The value coerced to a {@link String}, or null if no value
     * @see #getValue()
     */
    default @Nullable String getString() {
        return Scalars.STRING.tryDeserialize(getValue());
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @param def The default value if no appropriate value is set
     * @return The value coerced to a {@link String}, or {@code def} if no value
     * @see #getValue()
     */
    default String getString(final String def) {
        requireNonNull(def, "def");
        final @Nullable String value = getString();
        if (value != null) {
            return value;
        }
        if (getOptions().shouldCopyDefaults()) {
            setValue(def);
        }
        return def;
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @return The value coerced to a float, or 0 if not a float
     * @see #getValue()
     */
    default float getFloat() {
        return getFloat(NUMBER_DEF);
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @param def The default value if no appropriate value is set
     * @return The value coerced to a float, or {@code def} if not a float
     * @see #getValue()
     */
    default float getFloat(float def) {
        final @Nullable Float val = Scalars.FLOAT.tryDeserialize(getValue());
        if (val != null) {
            return val;
        }
        if (getOptions().shouldCopyDefaults() && def != NUMBER_DEF) {
            setValue(def);
        }
        return def;
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @return The value coerced to a double, or 0 if coercion failed
     * @see #getValue()
     */
    default double getDouble() {
        return getDouble(NUMBER_DEF);
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @param def The default value if no appropriate value is set
     * @return The value coerced to a double, or {@code def} if coercion failed
     * @see #getValue()
     */
    default double getDouble(double def) {
        final @Nullable Double val = Scalars.DOUBLE.tryDeserialize(getValue());
        if (val != null) {
            return val;
        }
        if (getOptions().shouldCopyDefaults() && def != NUMBER_DEF) {
            setValue(def);
        }
        return def;
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @return value coerced to an integer, or 0 if coercion failed.
     * @see #getValue()
     */
    default int getInt() {
        return getInt(NUMBER_DEF);
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @param def The default value if no appropriate value is set
     * @return value coerced to an integer, or {@code def} if coercion failed.
     * @see #getValue()
     */
    default int getInt(int def) {
        final @Nullable Integer val = Scalars.INTEGER.tryDeserialize(getValue());
        if (val != null) {
            return val;
        }
        if (getOptions().shouldCopyDefaults() && def != NUMBER_DEF) {
            setValue(def);
        }
        return def;
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @return value coerced to a long, or 0 if coercion failed
     * @see #getValue()
     */
    default long getLong() {
        return getLong(NUMBER_DEF);
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @param def The default value if no appropriate value is set
     * @return value coerced to a long, or {@code def} if coercion failed
     * @see #getValue()
     */
    default long getLong(long def) {
        final @Nullable Long val = Scalars.LONG.tryDeserialize(getValue());
        if (val != null) {
            return val;
        }
        if (getOptions().shouldCopyDefaults() && def != NUMBER_DEF) {
            setValue(def);
        }
        return def;
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @return value coerced to a boolean, or 0 if coercion failed
     * @see #getValue()
     */
    default boolean getBoolean() {
        return getBoolean(false);
    }

    /**
     * Gets the value typed using the appropriate type conversion from {@link Scalars}.
     *
     * @param def The default value if no appropriate value is set
     * @return value coerced to a boolean, or {@code def} if coercion failed
     * @see #getValue()
     */
    default boolean getBoolean(boolean def) {
        final @Nullable Boolean val = Scalars.BOOLEAN.tryDeserialize(getValue());
        if (val != null) {
            return val;
        }
        if (getOptions().shouldCopyDefaults()) {
            setValue(def);
        }
        return def;
    }

    /**
     * Set this node's value to the given value.
     *
     * <p>If the provided value is a {@link Collection} or a {@link Map}, it will be unwrapped into
     * the appropriate configuration node structure.</p>
     *
     * @param value The value to set
     * @return this
     */
    ConfigurationNode setValue(@Nullable Object value);

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
     * @param type The type to use for serialization type information
     * @param value The value to set
     * @param <V> The type to serialize to
     * @return this
     * @throws ObjectMappingException If the value fails to be converted to the
     *                                requested type. No change will be made to
     *                                the node.
     */
    <V> ConfigurationNode setValue(TypeToken<V> type, @Nullable V value) throws ObjectMappingException;

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
     * @param type The type to use for serialization type information
     * @param value The value to set
     * @param <V> The type to serialize to
     * @return this
     * @throws IllegalArgumentException if a raw type is passed
     * @throws ObjectMappingException If the value fails to be converted to the
     *                                requested type. No change will be made to
     *                                the node.
     */
    <V> ConfigurationNode setValue(Class<V> type, @Nullable V value) throws ObjectMappingException;

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
     * {@link #setValue(TypeToken, Object) TypeToken} and
     * {@link #setValue(Class, Object)} should be preferred where possible.</p>
     *
     * @param type The type to use for serialization type information
     * @param value The value to set
     * @return this
     * @throws IllegalArgumentException if a raw type is passed
     * @throws IllegalArgumentException if {@code value} is not either
     *                                  {@code null} or of type {@code type}
     * @throws ObjectMappingException If the value fails to be converted to the
     *                                requested type. No change will be made to
     *                                the node.
     */
    ConfigurationNode setValue(Type type, @Nullable Object value) throws ObjectMappingException;

    /**
     * Set all the values from the given node that are not present in this node
     * to their values in the provided node.
     *
     * <p>Map keys will be merged. Lists and scalar values will be replaced.</p>
     *
     * @param other The node to merge values from
     * @return this
     */
    ConfigurationNode mergeValuesFrom(ConfigurationNode other);

    /**
     * Removes a direct child of this node.
     *
     * @param key The key of the node to remove
     * @return If a node was removed
     */
    boolean removeChild(Object key);

    /**
     * Gets a new child node created as the next entry in the list.
     *
     * @return A new child created as the next entry in the list when it is
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
     * @return A copy of this node
     */
    ConfigurationNode copy();

    /**
     * Set a representation hint on this node.
     *
     * <p>Removing a hint from this node means the hint's value will be
     * delegated to the node's parent. To explicitly revert to a hint's default,
     * apply that default value.</p>
     *
     * @param hint The hint to set a value for
     * @param value Value to set, or null to unset for self
     * @param <V> Hint value type
     * @return this
     */
    <V> ConfigurationNode setHint(RepresentationHint<V> hint, @Nullable V value);

    /**
     * Query a representation hint from this node.
     * @param hint The hint to get
     * @param <V> value type
     * @return value of the hint, or {@link RepresentationHint#getDefaultValue()}
     */
    <V> @Nullable V getHint(RepresentationHint<V> hint);

}
