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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Basic implementation of {@link AttributedConfigurationNode}.
 */
class SimpleAttributedConfigurationNode extends AbstractCommentedConfigurationNode<AttributedConfigurationNode, SimpleAttributedConfigurationNode> implements AttributedConfigurationNode {
    private String tagName;
    private final Map<String, String> attributes = new LinkedHashMap<>();

    protected SimpleAttributedConfigurationNode(String tagName, @Nullable Object path, @Nullable SimpleAttributedConfigurationNode parent, ConfigurationOptions options) {
        super(path, parent, options);
        this.tagName = tagName;
    }

    protected SimpleAttributedConfigurationNode(String tagName, @Nullable SimpleAttributedConfigurationNode parent, SimpleAttributedConfigurationNode copyOf) {
        super(parent, copyOf);
        this.tagName = tagName;
    }

    @Override
    public String getTagName() {
        return tagName;
    }

    @Override
    public SimpleAttributedConfigurationNode setTagName(String tagName) {
        if (Strings.isNullOrEmpty(tagName)) {
            throw new IllegalArgumentException("Tag name cannot be null/empty");
        }

        this.tagName = tagName;
        return this;
    }

    @Override
    public SimpleAttributedConfigurationNode addAttribute(String name, String value) {
        if (Strings.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Attribute name cannot be null/empty");
        }

        attributes.put(name, value);
        return this;
    }

    @Override
    public SimpleAttributedConfigurationNode removeAttribute(String name) {
        attributes.remove(name);
        return this;
    }

    @Override
    public SimpleAttributedConfigurationNode setAttributes(Map<String, String> attributes) {
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

    @Override
    public @Nullable String getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Map<String, String> getAttributes() {
        return ImmutableMap.copyOf(attributes);
    }

    // Methods from superclass overridden to have correct return types

    @Override
    protected SimpleAttributedConfigurationNode createNode(Object path) {
        return new SimpleAttributedConfigurationNode("element", path, this, getOptions());
    }

    @Override
    public AttributedConfigurationNode setValue(@Nullable Object value) {
        if (value instanceof AttributedConfigurationNode) {
            AttributedConfigurationNode node = (AttributedConfigurationNode) value;
            setTagName(node.getTagName());
            setAttributes(node.getAttributes());
        }
        return super.setValue(value);
    }

    @Override
    public AttributedConfigurationNode mergeValuesFrom(ConfigurationNode other) {
        if (other instanceof AttributedConfigurationNode) {
            AttributedConfigurationNode node = (AttributedConfigurationNode) other;
            setTagName(node.getTagName());
            for (Map.Entry<String, String> attribute : node.getAttributes().entrySet()) {
                addAttribute(attribute.getKey(), attribute.getValue());
            }
        }
        return super.mergeValuesFrom(other);
    }

    @Override
    protected SimpleAttributedConfigurationNode copy(@Nullable SimpleAttributedConfigurationNode parent) {
        SimpleAttributedConfigurationNode copy = new SimpleAttributedConfigurationNode(this.tagName, parent, this);
        copy.attributes.putAll(this.attributes);
        copy.comment = this.comment;
        return copy;
    }

    @Override
    public SimpleAttributedConfigurationNode self() {
        return this;
    }

    @Override
    protected SimpleAttributedConfigurationNode implSelf() {
        return this;
    }

    @Override
    public boolean equals(@Nullable Object o) {
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
                "comment=" + comment + ", " +
                "tagName=" + tagName + ", " +
                "attributes=" + attributes +
                '}';
    }
}
