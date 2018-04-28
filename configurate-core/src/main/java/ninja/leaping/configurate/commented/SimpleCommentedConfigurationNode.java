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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Basic implementation of {@link CommentedConfigurationNode}.
 */
public class SimpleCommentedConfigurationNode extends SimpleConfigurationNode implements CommentedConfigurationNode {
    private final AtomicReference<String> comment = new AtomicReference<>();

    @NonNull
    public static SimpleCommentedConfigurationNode root() {
        return root(ConfigurationOptions.defaults());
    }

    @NonNull
    public static SimpleCommentedConfigurationNode root(@NonNull ConfigurationOptions options) {
        return new SimpleCommentedConfigurationNode(null, null, options);
    }

    protected SimpleCommentedConfigurationNode(@Nullable Object path, @Nullable SimpleConfigurationNode parent, @NonNull ConfigurationOptions options) {
        super(path, parent, options);
    }

    @NonNull
    @Override
    public Optional<String> getComment() {
        return Optional.ofNullable(comment.get());
    }

    @NonNull
    @Override
    public SimpleCommentedConfigurationNode setComment(@Nullable String comment) {
        attachIfNecessary();
        this.comment.set(comment);
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
        if (value instanceof CommentedConfigurationNode && ((CommentedConfigurationNode) value).getComment().isPresent()) {
            setComment(((CommentedConfigurationNode) value).getComment().get());
        }
        return (SimpleCommentedConfigurationNode) super.setValue(value);
    }

    @NonNull
    @Override
    public SimpleCommentedConfigurationNode mergeValuesFrom(@NonNull ConfigurationNode other) {
        if (other instanceof CommentedConfigurationNode) {
            Optional<String> otherComment = ((CommentedConfigurationNode) other).getComment();
            if (otherComment.isPresent()) {
                comment.compareAndSet(null, otherComment.get());
            }
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
    public SimpleCommentedConfigurationNode getAppendedNode() {
        return (SimpleCommentedConfigurationNode) super.getAppendedNode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleCommentedConfigurationNode)) return false;
        if (!super.equals(o)) return false;

        SimpleCommentedConfigurationNode that = (SimpleCommentedConfigurationNode) o;
        if (!comment.equals(that.comment)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + comment.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SimpleCommentedConfigurationNode{" +
                "super=" + super.toString() +
                ", comment=" + comment +
                '}';
    }
}
