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
package org.spongepowered.configurate.commented;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.AbstractConfigurationNode;
import org.spongepowered.configurate.SimpleConfigurationNode;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Basic implementation of {@link CommentedConfigurationNode}.
 */
public class SimpleCommentedConfigurationNode extends AbstractConfigurationNode<SimpleCommentedConfigurationNode> implements CommentedConfigurationNode<SimpleCommentedConfigurationNode> {
    private String comment = null;

    @NonNull
    public static SimpleCommentedConfigurationNode root() {
        return root(ConfigurationOptions.defaults());
    }

    @NonNull
    public static SimpleCommentedConfigurationNode root(@NonNull ConfigurationOptions options) {
        return new SimpleCommentedConfigurationNode(null, null, options);
    }

    protected SimpleCommentedConfigurationNode(@Nullable Object path, @Nullable SimpleCommentedConfigurationNode parent, @NonNull ConfigurationOptions options) {
        super(path, parent, options);
    }

    protected SimpleCommentedConfigurationNode(@Nullable SimpleCommentedConfigurationNode parent, @NonNull SimpleCommentedConfigurationNode copyOf) {
        super(parent, copyOf);
    }

    @NonNull
    @Override
    public Optional<String> getComment() {
        return Optional.ofNullable(comment);
    }

    @NonNull
    @Override
    public SimpleCommentedConfigurationNode setComment(@Nullable String comment) {
        attachIfNecessary();
        this.comment = comment;
        return this;
    }

    // Methods from superclass overridden to have correct return types

    @Override
    protected SimpleCommentedConfigurationNode createNode(Object path) {
        return new SimpleCommentedConfigurationNode(path, this, getOptions());
    }

    @NonNull
    @Override
    public SimpleCommentedConfigurationNode setValue(@Nullable Object value) {
        if (value instanceof CommentedConfigurationNode && ((CommentedConfigurationNode<?>) value).getComment().isPresent()) {
            setComment(((CommentedConfigurationNode<?>) value).getComment().get());
        }
        return super.setValue(value);
    }

    @NonNull
    @Override
    public SimpleCommentedConfigurationNode mergeValuesFrom(@NonNull ConfigurationNode<?> other) {
        if (other instanceof CommentedConfigurationNode) {
            Optional<String> otherComment = ((CommentedConfigurationNode<?>) other).getComment();
            if (comment == null && otherComment.isPresent()) {
                comment = otherComment.get();
            }
        }
        return super.mergeValuesFrom(other);
    }
    @NonNull
    @Override
    protected SimpleCommentedConfigurationNode copy(@Nullable SimpleCommentedConfigurationNode parent) {
        SimpleCommentedConfigurationNode copy = new SimpleCommentedConfigurationNode(parent, this);
        copy.comment = this.comment;
        return copy;
    }

    @Override
    public SimpleCommentedConfigurationNode self() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleCommentedConfigurationNode)) return false;
        if (!super.equals(o)) return false;

        SimpleCommentedConfigurationNode that = (SimpleCommentedConfigurationNode) o;
        if (!Objects.equals(comment, that.comment)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(comment);
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
