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

import java.util.Optional;

/**
 * A configuration node that can have a comment attached to it.
 */
public interface CommentedConfigurationNode<T extends CommentedConfigurationNode<T>> extends ConfigurationNode<T> {

    @NonNull
    static SimpleCommentedConfigurationNode root() {
        return root(ConfigurationOptions.defaults());
    }

    @NonNull
    static SimpleCommentedConfigurationNode root(@NonNull ConfigurationOptions options) {
        return new SimpleCommentedConfigurationNode(null, null, options);
    }

    /**
     * Gets the current value for the comment.
     *
     * <p>If the comment contains multiple lines, the lines will be split by \n</p>
     *
     * @return The configuration's current comment
     */
    @NonNull
    Optional<String> getComment();

    /**
     * Sets the comment for this configuration node.
     *
     * @param comment The comment to set. Line breaks should be represented as LFs (\n)
     * @return this
     */
    @NonNull
    T setComment(@Nullable String comment);
}
