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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Basic implementation of {@link AttributedConfigurationNode}.
 */
public class SimpleAttributedConfigurationNode extends SimpleCommentedConfigurationNode implements AttributedConfigurationNode {
    private String tagName;
    private final Map<String, String> attributes = new LinkedHashMap<>();

    /**
     * Create a new node with no parent.
     *
     * @return a new node
     * @deprecated Use {@link AttributedConfigurationNode#root()} instead
     */
    @Deprecated
    @NonNull
    public static SimpleAttributedConfigurationNode root() {
        return root("root", ConfigurationOptions.defaults());
    }


    /**
     * Create a new node with no parent.
     *
     * @param tagName The name of the tag to be used to represent this node
     * @return a new node
     * @deprecated Use {@link AttributedConfigurationNode#root(String)} instead
     */
    @Deprecated
    @NonNull
    public static SimpleAttributedConfigurationNode root(@NonNull String tagName) {
        return root(tagName, ConfigurationOptions.defaults());
    }


    /**
     * Create a new node with no parent, a specified tag name, and specific options.
     *
     * @param tagName The name of the tag to be used to represent this node
     * @param options The options to use within this node
     * @return a new node
     * @deprecated Use {@link AttributedConfigurationNode#root(String, ConfigurationOptions)} instead
     */
    @Deprecated
    @NonNull
    public static SimpleAttributedConfigurationNode root(@NonNull String tagName, @NonNull ConfigurationOptions options) {
        return new SimpleAttributedConfigurationNode(tagName, null, null, options);
    }

    protected SimpleAttributedConfigurationNode(@NonNull String tagName, @Nullable Object path, @Nullable SimpleConfigurationNode parent, @NonNull ConfigurationOptions options) {
        super(path, parent, options);
        setTagName(tagName);
    }

    protected SimpleAttributedConfigurationNode(@NonNull String tagName, @Nullable SimpleConfigurationNode parent, @NonNull SimpleConfigurationNode copyOf) {
        super(parent, copyOf);
        setTagName(tagName);
    }

    @NonNull
    @Override
    public String getTagName() {
        return tagName;
    }

    @NonNull
    @Override
    public SimpleAttributedConfigurationNode setTagName(@NonNull String tagName) {
        if (Strings.isNullOrEmpty(tagName)) {
            throw new IllegalArgumentException("Tag name cannot be null/empty");
        }

        this.tagName = tagName;
        return this;
    }

    @NonNull
    @Override
    public SimpleAttributedConfigurationNode addAttribute(@NonNull String name, @NonNull String value) {
        if (Strings.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Attribute name cannot be null/empty");
        }

        attributes.put(name, value);
        return this;
    }

    @NonNull
    @Override
    public SimpleAttributedConfigurationNode removeAttribute(@NonNull String name) {
        attributes.remove(name);
        return this;
    }

    @NonNull
    @Override
    public SimpleAttributedConfigurationNode setAttributes(@NonNull Map<String, String> attributes) {
        for (String name : attributes.keySet()) {
            if (Strings.isNullOrEmpty(name)) {
                throw new IllegalArgumentException("Attribute name cannot be null/empty");
            }
        }

        this.attributes.clear();
        this.attributes.putAll(attributes);
        return this;
    }

    @Override
    public boolean hasAttributes() {
        return !attributes.isEmpty();
    }

    @Nullable
    @Override
    public String getAttribute(@NonNull String name) {
        return attributes.get(name);
    }

    @NonNull
    @Override
    public Map<String, String> getAttributes() {
        return ImmutableMap.copyOf(attributes);
    }

    // Methods from superclass overridden to have correct return types

    @Nullable
    @Override
    public SimpleAttributedConfigurationNode getParent() {
        return (SimpleAttributedConfigurationNode) super.getParent();
    }

    @Override
    protected SimpleAttributedConfigurationNode createNode(Object path) {
        return new SimpleAttributedConfigurationNode("element", path, this, getOptions());
    }

    @NonNull
    @Override
    public SimpleAttributedConfigurationNode setValue(@Nullable Object value) {
        if (value instanceof AttributedConfigurationNode) {
            AttributedConfigurationNode node = (AttributedConfigurationNode) value;
            setTagName(node.getTagName());
            setAttributes(node.getAttributes());
        }
        return (SimpleAttributedConfigurationNode) super.setValue(value);
    }

    @NonNull
    @Override
    public SimpleAttributedConfigurationNode mergeValuesFrom(@NonNull ConfigurationNode other) {
        if (other instanceof AttributedConfigurationNode) {
            AttributedConfigurationNode node = (AttributedConfigurationNode) other;
            setTagName(node.getTagName());
            for (Map.Entry<String, String> attribute : node.getAttributes().entrySet()) {
                addAttribute(attribute.getKey(), attribute.getValue());
            }
        }
        return (SimpleAttributedConfigurationNode) super.mergeValuesFrom(other);
    }

    @NonNull
    @Override
    public SimpleAttributedConfigurationNode getNode(@NonNull Object... path) {
        return (SimpleAttributedConfigurationNode) super.getNode(path);
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public List<? extends SimpleAttributedConfigurationNode> getChildrenList() {
        return (List<SimpleAttributedConfigurationNode>) super.getChildrenList();
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public Map<Object, ? extends SimpleAttributedConfigurationNode> getChildrenMap() {
        return (Map<Object, SimpleAttributedConfigurationNode>) super.getChildrenMap();
    }

    @NonNull
    @Override
    @Deprecated
    public SimpleAttributedConfigurationNode getAppendedNode() {
        return (SimpleAttributedConfigurationNode) super.getAppendedNode();
    }

    @NonNull
    @Override
    public SimpleAttributedConfigurationNode appendListNode() {
        return (SimpleAttributedConfigurationNode) super.appendListNode();
    }

    @NonNull
    @Override
    public SimpleAttributedConfigurationNode copy() {
        return copy(null);
    }

    @NonNull
    @Override
    protected SimpleAttributedConfigurationNode copy(@Nullable SimpleConfigurationNode parent) {
        SimpleAttributedConfigurationNode copy = new SimpleAttributedConfigurationNode(this.tagName, parent, this);
        copy.attributes.putAll(this.attributes);
        copy.comment.set(this.comment.get());
        return copy;
    }

    @Override
    public @NonNull SimpleAttributedConfigurationNode setComment(@Nullable String comment) {
        return (SimpleAttributedConfigurationNode) super.setComment(comment);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleAttributedConfigurationNode)) return false;
        if (!super.equals(o)) return false;
        SimpleAttributedConfigurationNode that = (SimpleAttributedConfigurationNode) o;
        return tagName.equals(that.tagName) && attributes.equals(that.attributes);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + tagName.hashCode();
        result = 31 * result + attributes.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SimpleAttributedConfigurationNode{" +
                "super=" + super.toString() + ", " +
                "tagName=" + tagName + ", " +
                "attributes=" + attributes +
                '}';
    }
}
