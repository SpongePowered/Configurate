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
package ninja.leaping.configurate.attributed;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A configuration node that can have both comments and attributes attached to it.
 */
public interface AttributedConfigurationNode extends CommentedConfigurationNode {

    /**
     * Create a new node with no parent and the tag name {@code root}
     *
     * @return a new node
     */
    @NonNull
    static AttributedConfigurationNode root() {
        return root("root", ConfigurationOptions.defaults());
    }

    /**
     * Create a new node with no parent and the tag name {@code root}, and perform an action with it in scope
     *
     * @param action action to perform to initialize the created node
     * @return a new node
     */
    static AttributedConfigurationNode root(Consumer<? super AttributedConfigurationNode> action) {
        return root("root", action);
    }

    /**
     * Create a new node with no parent and a specified tag name.
     *
     * @param tagName The name of the tag to be used to represent this node
     * @return a new node
     */
    @NonNull
    static AttributedConfigurationNode root(@NonNull String tagName) {
        return root(tagName, ConfigurationOptions.defaults());
    }

    /**
     * Create a new node with no parent and a specified tag name, and perform an action with it in scope
     *
     * @param tagName The name of the tag to be used to represent this node
     * @param action action to perform to initialize the created node
     * @return a new node
     */
    @NonNull
    static AttributedConfigurationNode root(@NonNull String tagName, Consumer<? super AttributedConfigurationNode> action) {
        return root(tagName, ConfigurationOptions.defaults(), action);
    }

    /**
     * Create a new node with no parent, a specified tag name, and specific options.
     *
     * @param tagName The name of the tag to be used to represent this node
     * @param options The options to use within this node
     * @return a new node
     */
    @NonNull
    static AttributedConfigurationNode root(@NonNull String tagName, @NonNull ConfigurationOptions options) {
        return new SimpleAttributedConfigurationNode(tagName, null, null, options);
    }

    /**
     * Create a new node with no parent, a specified tag name, and specific options, and perform an action with it in scope
     *
     * @param tagName The name of the tag to be used to represent this node
     * @param options The options to use within this node
     * @param action action to perform to initialize the created node
     * @return a new node
     */
    @NonNull
    static AttributedConfigurationNode root(@NonNull String tagName, @NonNull ConfigurationOptions options, Consumer<? super AttributedConfigurationNode> action) {
        AttributedConfigurationNode ret = root(tagName, options);
        action.accept(ret);
        return ret;
    }

    /**
     * Gets the tag name of this node.
     *
     * @return The tag name
     */
    @NonNull
    String getTagName();

    /**
     * Sets the tag name of this node.
     *
     * <p>Will have no effect when called on nodes which are direct values of a
     * {@link #getChildrenMap() child map}, as the corresponding key is used as
     * the tag name.</p>
     *
     * @param name The name to set, cannot be null
     * @return this
     */
    @NonNull
    AttributedConfigurationNode setTagName(@NonNull String name);

    /**
     * Adds an attribute to this node.
     *
     * @param name The name of the attribute
     * @param value The value of the attribute
     * @return this
     */
    @NonNull
    AttributedConfigurationNode addAttribute(@NonNull String name, @NonNull String value);

    /**
     * Removes an attribute from this node.
     *
     * @param name The name of the attribute to remove
     * @return this
     */
    @NonNull
    AttributedConfigurationNode removeAttribute(@NonNull String name);

    /**
     * Sets the attributes of this node.
     *
     * @param attributes the attributes to set
     * @return this
     */
    @NonNull
    AttributedConfigurationNode setAttributes(@NonNull Map<String, String> attributes);

    /**
     * Gets if this node has any attributes.
     *
     * @return true if this node has any attributes
     */
    boolean hasAttributes();

    /**
     * Gets the value of an attribute, or null if this node doesn't have the
     * given attribute.
     *
     * @param name The name of the attribute to get
     * @return this
     */
    @Nullable
    String getAttribute(@NonNull String name);

    /**
     * Gets the attributes this node has.
     *
     * <p>The returned map is immutable.</p>
     *
     * @return The map of attributes
     */
    @NonNull
    Map<String, String> getAttributes();

    @Override
    default @NonNull Optional<String> getComment() {
        return Optional.empty();
    }

    // Methods from superclass overridden to have correct return types
    @Nullable @Override AttributedConfigurationNode getParent();
    @NonNull @Override List<? extends AttributedConfigurationNode> getChildrenList();
    @NonNull @Override Map<Object, ? extends AttributedConfigurationNode> getChildrenMap();
    @Override default @NonNull AttributedConfigurationNode setComment(@Nullable String value) {
        return this; // backwards compatibility
    }
    @NonNull @Override AttributedConfigurationNode setValue(@Nullable Object value);
    @NonNull @Override AttributedConfigurationNode mergeValuesFrom(@NonNull ConfigurationNode other);
    @NonNull @Override @Deprecated AttributedConfigurationNode getAppendedNode();
    @Override default @NonNull AttributedConfigurationNode appendListNode() {
        return getAppendedNode();
    }
    @NonNull @Override AttributedConfigurationNode getNode(@NonNull Object... path);
    @NonNull @Override AttributedConfigurationNode copy();

    @Override
    default AttributedConfigurationNode act(Consumer<? super ConfigurationNode> action) {
        action.accept(this);
        return this;
    }
}
