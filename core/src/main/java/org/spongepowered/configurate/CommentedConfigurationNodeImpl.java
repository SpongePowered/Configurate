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

/**
 * Basic implementation of {@link CommentedConfigurationNode}.
 */
class CommentedConfigurationNodeImpl extends AbstractCommentedConfigurationNode<CommentedConfigurationNode, CommentedConfigurationNodeImpl>
        implements CommentedConfigurationNode {

    protected CommentedConfigurationNodeImpl(final @Nullable Object path,
            final @Nullable CommentedConfigurationNodeImpl parent, final @NonNull ConfigurationOptions options) {
        super(path, parent, options);
    }

    protected CommentedConfigurationNodeImpl(final @Nullable CommentedConfigurationNodeImpl parent,
            final @NonNull CommentedConfigurationNodeImpl copyOf) {
        super(parent, copyOf);
    }

    // Methods from superclass overridden to have correct return types

    @Override
    protected CommentedConfigurationNodeImpl createNode(final Object path) {
        return new CommentedConfigurationNodeImpl(path, this, options());
    }

    @NonNull
    @Override
    protected CommentedConfigurationNodeImpl copy(final @Nullable CommentedConfigurationNodeImpl parent) {
        final CommentedConfigurationNodeImpl copy = new CommentedConfigurationNodeImpl(parent, this);
        copy.comment.set(this.comment.get());
        return copy;
    }

    @Override
    @NonNull
    public CommentedConfigurationNodeImpl self() {
        return this;
    }

    @Override
    protected CommentedConfigurationNodeImpl implSelf() {
        return this;
    }

    @Override
    public String toString() {
        return "CommentedConfigurationNodeImpl{"
                + "super=" + super.toString()
                + ", comment=" + this.comment
                + '}';
    }

}
