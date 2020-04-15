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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;

/**
 * A configuration node that can have both comments and attributes attached to it.
 */
public interface AttributedConfigurationNode extends CommentedConfigurationNodeIntermediary<AttributedConfigurationNode> {

    @NonNull
    static AttributedConfigurationNode root() {
        return root("root", ConfigurationOptions.defaults());
    }

    @NonNull
    static AttributedConfigurationNode root(@NonNull String tagName) {
        return root(tagName, ConfigurationOptions.defaults());
    }

    @NonNull
    static AttributedConfigurationNode root(@NonNull String tagName, @NonNull ConfigurationOptions options) {
        return new SimpleAttributedConfigurationNode(tagName, null, null, options);
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
}
