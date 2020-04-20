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
class SimpleCommentedConfigurationNode extends AbstractCommentedConfigurationNode<CommentedConfigurationNode, SimpleCommentedConfigurationNode> implements CommentedConfigurationNode {

    protected SimpleCommentedConfigurationNode(@Nullable Object path, @Nullable SimpleCommentedConfigurationNode parent, ConfigurationOptions options) {
        super(path, parent, options);
    }

    protected SimpleCommentedConfigurationNode(@Nullable SimpleCommentedConfigurationNode parent, SimpleCommentedConfigurationNode copyOf) {
        super(parent, copyOf);
    }

    // Methods from superclass overridden to have correct return types

    @Override
    protected SimpleCommentedConfigurationNode createNode(Object path) {
        return new SimpleCommentedConfigurationNode(path, this, getOptions());
    }

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
    protected SimpleCommentedConfigurationNode implSelf() {
        return this;
    }

    @Override
    public String toString() {
        return "SimpleCommentedConfigurationNode{" +
                "super=" + super.toString() +
                ", comment=" + comment +
                '}';
    }
}
