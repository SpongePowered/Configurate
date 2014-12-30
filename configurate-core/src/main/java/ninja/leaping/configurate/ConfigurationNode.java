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


import com.google.common.base.Function;

import java.util.List;
import java.util.Map;

/**
 * A node in the configuration tree. This is more or less the main class of configurate, providing the methods to
 * navigate through the configuration tree and get values
 */
public interface ConfigurationNode {

    /**
     * Get the current value associated with this node.
     * If this node has children, this method will recursively unwrap them to construct a List or a Map
     *
     * @see #getValue(Object)
     * @return This configuration's current value, or null if there is none
     */
    public Object getValue();

    /**
     * Get the current value associated with this node.
     * If this node has children, this method will recursively unwrap them to construct a List or a Map
     *
     * @return This configuration's current value, or {@code def} if there is none
     */
    public Object getValue(Object def);

    /**
     * Gets the appropriately transformed typed version of this node's value from the provided transformation function
     *
     * @param transformer The transformation function
     * @param <T> The expected type
     * @return A transformed value of the correct type, or null either if no value is present or the value could not
     * be converted
     */
    public <T> T getValue(Function<Object, T> transformer);

    /**
     * Gets the appropriately transformed typed version of this node's value from the provided transformation function
     *
     * @param transformer The transformation function
     * @param def The expected type
     * @param <T>
     * @return A transformed value of the correct type, or {@code def} either if no value is present or the value
     * could not be converted
     */
    public <T> T getValue(Function<Object, T> transformer, T def);

    /**
     * If this node has list values, this function unwraps them and converts them to an appropriate type based on the
     * provided function.
     * If this node has a scalar value, this function treats it as a list with one value
     *
     * @param transformer The transformation function
     * @param <T> The expected type
     * @return An immutable copy of the values contained
     */
    public <T> List<T> getList(Function<Object, T> transformer);

    /**
     * If this node has list values, this function unwraps them and converts them to an appropriate type based on the
     * provided function.
     * If this node has a scalar value, this function treats it as a list with one value
     *
     * @param transformer The transformation function
     * @param <T> The expected type
     * @return An immutable copy of the values contained that could be successfully converted, or {@code def} if no
     * values could be converted
     */
    public <T> List<T> getList(Function<Object, T> transformer, List<T> def);

    /**
     * Set this node's value to the given value.
     * If the provided value is a {@link java.util.Collection} or a {@link java.util.Map}, it will be unwrapped into
     * the appropriate configuration node structure
     *
     * @param value The value to set
     * @return this
     */
    public ConfigurationNode setValue(Object value);

    /**
     * @return if this node has children in the form of a list
     */
    public boolean hasListChildren();

    /**
     * @return if this node has children in the form of a map
     */
    public boolean hasMapChildren();

    /**
     * Return an immutable copy of the list of children this node is aware of
     *
     * @return The children currently attached to this node
     */
    public List<? extends ConfigurationNode> getChildrenList();

    /**
     * Return an immutable copy of the mapping from key to node of every child this node is aware of
     *
     * @return Child nodes currently attached
     */
    public Map<Object, ? extends ConfigurationNode> getChildrenMap();

    /**
     * Gets a direct child node of this one.
     * If this is a list, an int passed to this method is the index into the list to get a node at
     *
     * @param key The key to look up
     * @return A child node, possibly unattached
     */
    public ConfigurationNode getChild(Object key);

    /**
     * Removes a direct child of this node
     *
     * @param key The key of the node to remove
     * @return if an actual node was removed
     */
    public boolean removeChild(Object key);

    /**
     * @return a new child created as the next entry in the list when it is attached
     */
    public ConfigurationNode getAppendedChild();


    /**
     * Gets the node at the given (relative) path, possibly traversing multiple levels of nodes
     *
     * @param path The path to fetch the node at
     * @return The node at the given path, possibly virtual
     */
    public ConfigurationNode getNode(Object... path);

    /**
     * Whether this node does not currently exist in the configuration structure.
     *
     * @return true if this node is not attached (this occurs primarily when the node has no set value)
     */
    public boolean isVirtual();
}
