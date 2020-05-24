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
import java.util.concurrent.atomic.AtomicReference;

abstract class AbstractCommentedConfigurationNode<N extends CommentedConfigurationNodeIntermediary<N>, A extends
        AbstractCommentedConfigurationNode<N, A>> extends AbstractConfigurationNode<N, A> implements CommentedConfigurationNodeIntermediary<N> {

    protected final AtomicReference<String> comment = new AtomicReference<>();

    protected AbstractCommentedConfigurationNode(final @Nullable A parent, final A copyOf) {
        super(parent, copyOf);
    }

    protected AbstractCommentedConfigurationNode(final @Nullable Object key, final @Nullable A parent, final @NonNull ConfigurationOptions options) {
        super(key, parent, options);
    }

    @Override
    public @Nullable String getComment() {
        return this.comment.get();
    }

    @Override
    public @NonNull N setComment(final @Nullable String comment) {
        if (!Objects.equals(this.comment.getAndSet(comment), comment)) {
            attachIfNecessary();
        }
        return self();
    }

    @Override
    public @NonNull N setCommentIfAbsent(final String comment) {
        if (this.comment.compareAndSet(null, comment)) {
            attachIfNecessary();
        }
        return self();
    }

    @Override
    public @NonNull N setValue(final @Nullable Object value) {
        if (value instanceof CommentedConfigurationNodeIntermediary<?>) {
            final @Nullable String otherComment = ((CommentedConfigurationNodeIntermediary<?>) value).getComment();
            if (otherComment != null) {
                setComment(otherComment);
            }
        }
        return super.setValue(value);
    }

    @Override
    public @NonNull N mergeValuesFrom(final @NonNull ConfigurationNode other) {
        if (other instanceof CommentedConfigurationNodeIntermediary<?>) {
            final @Nullable String otherComment = ((CommentedConfigurationNodeIntermediary<?>) other).getComment();
            if (otherComment != null) {
                setCommentIfAbsent(otherComment);
            }
        }
        return super.mergeValuesFrom(other);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof AbstractCommentedConfigurationNode)) {
            return false;
        }

        if (!super.equals(o)) {
            return false;
        }

        final AbstractCommentedConfigurationNode<?, ?> that = (AbstractCommentedConfigurationNode<?, ?>) o;
        return Objects.equals(this.comment.get(), that.comment.get());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(this.comment.get());
        return result;
    }

}
