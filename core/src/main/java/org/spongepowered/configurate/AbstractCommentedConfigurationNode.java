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
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

abstract class AbstractCommentedConfigurationNode<N extends CommentedConfigurationNodeIntermediary<N>, A extends
        AbstractCommentedConfigurationNode<N, A>> extends AbstractConfigurationNode<N, A> implements CommentedConfigurationNodeIntermediary<N> {

    @SuppressWarnings("rawtypes")
    protected static final AtomicReferenceFieldUpdater<AbstractCommentedConfigurationNode, String> COMMENT_UPDATER =
        AtomicReferenceFieldUpdater.newUpdater(AbstractCommentedConfigurationNode.class, String.class, "comment");

    protected volatile @Nullable String comment;

    protected AbstractCommentedConfigurationNode(final @Nullable A parent, final A copyOf) {
        super(parent, copyOf);
    }

    protected AbstractCommentedConfigurationNode(final @Nullable Object key, final @Nullable A parent, final @NonNull ConfigurationOptions options) {
        super(key, parent, options);
    }

    @Override
    public @Nullable String comment() {
        return this.comment;
    }

    @Override
    public N comment(final @Nullable String comment) {
        if (!Objects.equals(COMMENT_UPDATER.getAndSet(this, comment), comment)) {
            attachIfNecessary();
        }
        return self();
    }

    @Override
    public N commentIfAbsent(final String comment) {
        if (COMMENT_UPDATER.compareAndSet(this, null, comment)) {
            attachIfNecessary();
        }
        return self();
    }

    @Override
    public N from(final ConfigurationNode that) {
        if (that instanceof CommentedConfigurationNodeIntermediary<?>) {
            final @Nullable String otherComment = ((CommentedConfigurationNodeIntermediary<?>) that).comment();
            if (otherComment != null) {
                comment(otherComment);
            }
        }
        return super.from(that);
    }

    @Override
    public N mergeFrom(final ConfigurationNode other) {
        if (other instanceof CommentedConfigurationNodeIntermediary<?>) {
            final @Nullable String otherComment = ((CommentedConfigurationNodeIntermediary<?>) other).comment();
            if (otherComment != null) {
                commentIfAbsent(otherComment);
            }
        }
        return super.mergeFrom(other);
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
        return Objects.equals(this.comment, that.comment);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(this.comment);
        return result;
    }

}
