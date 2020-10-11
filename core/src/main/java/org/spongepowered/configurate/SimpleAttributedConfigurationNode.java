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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Basic implementation of {@link AttributedConfigurationNode}.
 */
class SimpleAttributedConfigurationNode extends AbstractCommentedConfigurationNode<AttributedConfigurationNode, SimpleAttributedConfigurationNode>
        implements AttributedConfigurationNode {

    private String tagName;
    private final Map<String, String> attributes = new LinkedHashMap<>();

    protected SimpleAttributedConfigurationNode(final @NonNull String tagName, final @Nullable Object path,
            final @Nullable SimpleAttributedConfigurationNode parent, final @NonNull ConfigurationOptions options) {
        super(path, parent, options);
        tagName(tagName);
    }

    protected SimpleAttributedConfigurationNode(final @NonNull String tagName, final @Nullable SimpleAttributedConfigurationNode parent,
            final @NonNull SimpleAttributedConfigurationNode copyOf) {
        super(parent, copyOf);
        tagName(tagName);
    }

    @NonNull
    @Override
    public String tagName() {
        return this.tagName;
    }

    @NonNull
    @Override
    public SimpleAttributedConfigurationNode tagName(final @NonNull String tagName) {
        if (requireNonNull(tagName, "tag name").isEmpty()) {
            throw new IllegalArgumentException("Tag name cannot be null/empty");
        }

        this.tagName = tagName;
        return this;
    }

    @NonNull
    @Override
    public SimpleAttributedConfigurationNode addAttribute(final @NonNull String name, final @NonNull String value) {
        if (requireNonNull(name, "name").isEmpty()) {
            throw new IllegalArgumentException("Attribute name cannot be null/empty");
        }
        attachIfNecessary();
        this.attributes.put(name, value);
        return this;
    }

    @NonNull
    @Override
    public SimpleAttributedConfigurationNode removeAttribute(final @NonNull String name) {
        this.attributes.remove(name);
        return this;
    }

    @NonNull
    @Override
    public SimpleAttributedConfigurationNode attributes(final @NonNull Map<String, String> attributes) {
        for (String name : attributes.keySet()) {
            if (requireNonNull(name, "name").isEmpty()) {
                throw new IllegalArgumentException("Attribute name cannot be null/empty");
            }
        }
        this.attributes.clear();
        if (!attributes.isEmpty()) {
            attachIfNecessary();
            this.attributes.putAll(attributes);
        }
        return this;
    }

    @NonNull
    @Override
    public Map<String, String> attributes() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(this.attributes));
    }

    @Override
    public boolean hasAttributes() {
        return !this.attributes.isEmpty();
    }

    @Nullable
    @Override
    public String attribute(final @NonNull String name) {
        return this.attributes.get(name);
    }

    @Override
    public boolean empty() {
        return super.empty() && this.attributes.isEmpty();
    }

    // Typing overrides

    @Override
    protected SimpleAttributedConfigurationNode createNode(final Object path) {
        return new SimpleAttributedConfigurationNode("element", path, this, options());
    }

    @NonNull
    @Override
    public AttributedConfigurationNode set(final @Nullable Object value) {
        if (value instanceof AttributedConfigurationNode) {
            final AttributedConfigurationNode node = (AttributedConfigurationNode) value;
            tagName(node.tagName());
            attributes(node.attributes());
        }
        return super.set(value);
    }

    @NonNull
    @Override
    public AttributedConfigurationNode mergeFrom(final @NonNull ConfigurationNode other) {
        if (other instanceof AttributedConfigurationNode) {
            final AttributedConfigurationNode node = (AttributedConfigurationNode) other;
            tagName(node.tagName());
            for (Map.Entry<String, String> attribute : node.attributes().entrySet()) {
                addAttribute(attribute.getKey(), attribute.getValue());
            }
        }
        return super.mergeFrom(other);
    }

    @NonNull
    @Override
    protected SimpleAttributedConfigurationNode copy(final @Nullable SimpleAttributedConfigurationNode parent) {
        final SimpleAttributedConfigurationNode copy = new SimpleAttributedConfigurationNode(this.tagName, parent, this);
        copy.attributes.putAll(this.attributes);
        copy.comment.set(this.comment.get());
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
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof SimpleAttributedConfigurationNode)) {
            return false;
        }

        if (!super.equals(o)) {
            return false;
        }

        final SimpleAttributedConfigurationNode that = (SimpleAttributedConfigurationNode) o;
        return this.tagName.equals(that.tagName) && this.attributes.equals(that.attributes);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.tagName.hashCode();
        result = 31 * result + this.attributes.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SimpleAttributedConfigurationNode{"
                + "super=" + super.toString() + ", "
                + "comment=" + this.comment + ", "
                + "tagName=" + this.tagName + ", "
                + "attributes=" + this.attributes
                + '}';
    }

}
