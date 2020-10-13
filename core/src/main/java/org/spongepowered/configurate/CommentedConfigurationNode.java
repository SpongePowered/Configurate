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

import org.spongepowered.configurate.util.CheckedConsumer;

/**
 * A configuration node that can have a comment attached to it.
 *
 * <p>All other standard data is supported.</p>
 */
public interface CommentedConfigurationNode extends CommentedConfigurationNodeIntermediary<CommentedConfigurationNode> {

    /**
     * Create a new factory providing {@link CommentedConfigurationNode} instances.
     *
     * <p>The returned factory will create nodes with default options.</p>
     *
     * @return a new factory
     */
    static ConfigurationNodeFactory<CommentedConfigurationNode> factory() {
        return CommentedConfigurationNode::root;
    }

    /**
     * Create a new empty root node.
     *
     * <p>This node will use the {@link ConfigurationOptions#defaults() default options}</p>
     *
     * <p>A root node is always attached, and has no parent and an
     * empty path.</p>
     *
     * @return a new empty node
     */
    static CommentedConfigurationNode root() {
        return root(ConfigurationOptions.defaults());
    }

    /**
     * Create a new root node with the provided initializer.
     *
     * <p>This node will use the {@link ConfigurationOptions#defaults() default options}</p>
     *
     * <p>A root node is always attached, and has no parent and an
     * empty path.</p>
     *
     * @param <E> thrown type
     * @param action action to be applied to the newly created node
     * @return a new initialized node
     * @throws E when thrown from inner action
     */
    static <E extends Exception> CommentedConfigurationNode root(final CheckedConsumer<? super CommentedConfigurationNode, E> action) throws E {
        return root().act(action);
    }

    /**
     * Create a new empty root node with the provided options.
     *
     * <p>A root node is always attached, and has no parent and an
     * empty path.</p>
     *
     * @param options options to apply
     * @return a new empty node
     */
    static CommentedConfigurationNode root(final ConfigurationOptions options) {
        return new CommentedConfigurationNodeImpl(null, null, options);
    }

    /**
     * Create a new root node with the provided options and initializer.
     *
     * <p>A root node is always attached, and has no parent and an
     * empty path.</p>
     *
     * @param <E> thrown type
     * @param options options to apply
     * @param action action to be applied to the newly created node
     * @return a new initialized node
     * @throws E when thrown from inner action
     */
    static <E extends Exception> CommentedConfigurationNode root(final ConfigurationOptions options,
            final CheckedConsumer<? super CommentedConfigurationNode, E> action) throws E {
        return root(options).act(action);
    }

}
