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

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Intermediate interface for different types of commented configuration nodes.
 *
 * @param <N> self type
 * @since 4.0.0
 */
public interface CommentedConfigurationNodeIntermediary<N extends CommentedConfigurationNodeIntermediary<N>> extends ScopedConfigurationNode<N> {

    /**
     * Gets the current value for the comment.
     *
     * <p>If the comment contains multiple lines, the lines will be split
     * by \n</p>
     *
     * @return the configuration's current comment
     * @since 4.0.0
     */
    @Nullable String comment();

    /**
     * Sets the comment for this configuration node.
     *
     * @param comment the comment to set. Line breaks should be represented as
     *                LFs (\n)
     * @return this node
     * @since 4.0.0
     */
    N comment(@Nullable String comment);

    /**
     * Set a comment on this node if it does not presently have a comment.
     *
     * <p>The provided comment must not be null, because setting a null comment
     * would be a no-op</p>
     *
     * @param comment the comment to set. Line breaks should be represented as
     *                LFs (\n)
     * @return this node
     * @since 4.0.0
     */
    N commentIfAbsent(String comment);

}
