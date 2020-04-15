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

import java.util.Objects;
import java.util.Optional;

abstract class AbstractCommentedConfigurationNode<N extends CommentedConfigurationNodeIntermediary<N>, A extends AbstractCommentedConfigurationNode<N, A>> extends AbstractConfigurationNode<N, A> implements CommentedConfigurationNodeIntermediary<N> {
    protected String comment = null;

    protected AbstractCommentedConfigurationNode(A parent, A copyOf) {
        super(parent, copyOf);
    }

    protected AbstractCommentedConfigurationNode(@Nullable Object key, A parent, @NonNull ConfigurationOptions options) {
        super(key, parent, options);
    }

    @NonNull
    @Override
    public Optional<String> getComment() {
        return Optional.ofNullable(comment);
    }

    @Override
    public @NonNull N setComment(@Nullable String comment) {
        attachIfNecessary();
        this.comment = comment;
        return self();
    }

    @Override
    public @NonNull N setValue(@Nullable Object value) {
        if (value instanceof CommentedConfigurationNodeIntermediary<?> && ((CommentedConfigurationNodeIntermediary<?>) value).getComment().isPresent()) {
            setComment(((CommentedConfigurationNodeIntermediary<?>) value).getComment().get());
        }
        return super.setValue(value);
    }

    @Override
    public @NonNull N mergeValuesFrom(@NonNull ConfigurationNode other) {
        if (other instanceof CommentedConfigurationNodeIntermediary<?>) {
            Optional<String> otherComment = ((CommentedConfigurationNodeIntermediary<?>) other).getComment();
            if (comment == null && otherComment.isPresent()) {
                comment = otherComment.get();
            }
        }
        return super.mergeValuesFrom(other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractCommentedConfigurationNode)) return false;
        if (!super.equals(o)) return false;

        AbstractCommentedConfigurationNode<?, ?> that = (AbstractCommentedConfigurationNode<?, ?>) o;
        if (!Objects.equals(comment, that.comment)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(comment);
        return result;
    }
}
