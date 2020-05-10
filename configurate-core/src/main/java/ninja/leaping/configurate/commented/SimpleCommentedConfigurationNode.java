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
package ninja.leaping.configurate.commented;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.SimpleConfigurationNode;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Basic implementation of {@link CommentedConfigurationNode}.
 */
public class SimpleCommentedConfigurationNode extends SimpleConfigurationNode implements CommentedConfigurationNode {
    private final AtomicReference<String> comment = new AtomicReference<>();

    /**
     * Create a new node with no parent.
     *
     * @return The newly created node
     * @deprecated Use {@link CommentedConfigurationNode#root()} instead
     */
    @Deprecated
    @NonNull
    public static SimpleCommentedConfigurationNode root() {
        return root(ConfigurationOptions.defaults());
    }

    /**
     * Create a new node with no parent and defined options
     *
     * @param options The options to use in this node.
     * @return The newly created node
     * @deprecated Use {@link CommentedConfigurationNode#root(ConfigurationOptions)} instead
     */
    @Deprecated
    @NonNull
    public static SimpleCommentedConfigurationNode root(@NonNull ConfigurationOptions options) {
        return new SimpleCommentedConfigurationNode(null, null, options);
    }

    protected SimpleCommentedConfigurationNode(@Nullable Object path, @Nullable SimpleConfigurationNode parent, @NonNull ConfigurationOptions options) {
        super(path, parent, options);
    }

    protected SimpleCommentedConfigurationNode(@Nullable SimpleConfigurationNode parent, @NonNull SimpleConfigurationNode copyOf) {
        super(parent, copyOf);
    }

    @NonNull
    @Override
    public Optional<String> getComment() {
        return Optional.ofNullable(comment.get());
    }

    @NonNull
    @Override
    public SimpleCommentedConfigurationNode setComment(@Nullable String comment) {
        if (!Objects.equals(this.comment.getAndSet(comment), comment)) {
            attachIfNecessary();
        }
        return this;
    }

    @Override
    public @NonNull CommentedConfigurationNode setCommentIfAbsent(String comment) {
        if (this.comment.compareAndSet(null, comment)) {
            attachIfNecessary();
        }
        return this;
    }

    // Methods from superclass overridden to have correct return types

    @Nullable
    @Override
    public SimpleCommentedConfigurationNode getParent() {
        return (SimpleCommentedConfigurationNode) super.getParent();
    }

    @Override
    protected SimpleCommentedConfigurationNode createNode(Object path) {
        return new SimpleCommentedConfigurationNode(path, this, getOptions());
    }

    @NonNull
    @Override
    public SimpleCommentedConfigurationNode setValue(@Nullable Object value) {
        if (value instanceof CommentedConfigurationNode) {
            ((CommentedConfigurationNode) value).getComment().ifPresent(this::setComment);
        }
        return (SimpleCommentedConfigurationNode) super.setValue(value);
    }

    @NonNull
    @Override
    public SimpleCommentedConfigurationNode mergeValuesFrom(@NonNull ConfigurationNode other) {
        if (other instanceof CommentedConfigurationNode) {
            Optional<String> otherComment = ((CommentedConfigurationNode) other).getComment();
            otherComment.ifPresent(this::setCommentIfAbsent);
        }
        return (SimpleCommentedConfigurationNode) super.mergeValuesFrom(other);
    }

    @NonNull
    @Override
    public SimpleCommentedConfigurationNode getNode(@NonNull Object... path) {
        return (SimpleCommentedConfigurationNode) super.getNode(path);
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public List<? extends SimpleCommentedConfigurationNode> getChildrenList() {
        return (List<SimpleCommentedConfigurationNode>) super.getChildrenList();
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public Map<Object, ? extends SimpleCommentedConfigurationNode> getChildrenMap() {
        return (Map<Object, SimpleCommentedConfigurationNode>) super.getChildrenMap();
    }

    @NonNull
    @Override
    @Deprecated
    public SimpleCommentedConfigurationNode getAppendedNode() {
        return (SimpleCommentedConfigurationNode) super.getAppendedNode();
    }

    @NonNull
    @Override
    public SimpleCommentedConfigurationNode appendListNode() {
        return (SimpleCommentedConfigurationNode) super.appendListNode();
    }

    @NonNull
    @Override
    public SimpleCommentedConfigurationNode copy() {
        return copy(null);
    }

    @NonNull
    @Override
    protected SimpleCommentedConfigurationNode copy(@Nullable SimpleConfigurationNode parent) {
        SimpleCommentedConfigurationNode copy = new SimpleCommentedConfigurationNode(parent, this);
        copy.comment.set(this.comment.get());
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleCommentedConfigurationNode)) return false;
        if (!super.equals(o)) return false;

        SimpleCommentedConfigurationNode that = (SimpleCommentedConfigurationNode) o;
        if (!Objects.equals(comment.get(), that.comment.get())) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(comment.get());
        return result;
    }

    @Override
    public String toString() {
        return "SimpleCommentedConfigurationNode{" +
                "super=" + super.toString() +
                ", comment=" + comment.get() +
                '}';
    }
}
