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
 * A standard configuration node, without any additional options.
 *
 * @since 4.0.0
 */
public interface BasicConfigurationNode extends ScopedConfigurationNode<BasicConfigurationNode> {

    /**
     * Create a new factory providing {@link BasicConfigurationNode} instances.
     *
     * <p>The returned factory will create nodes with default options.</p>
     *
     * @return a new factory
     * @since 4.0.0
     */
    static ConfigurationNodeFactory<BasicConfigurationNode> factory() {
        return BasicConfigurationNode::root;
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
     * @since 4.0.0
     */
    static BasicConfigurationNode root() {
        return root(ConfigurationOptions.defaults());
    }

    /**
     * Create a new root node with the provided initializer which may throw.
     *
     * <p>This node will use the {@link ConfigurationOptions#defaults() default options}</p>
     *
     * <p>A root node is always attached, and has no parent and an
     * empty path.</p>
     *
     * @param <E> error type thrown
     * @param maker action to be applied to the newly created node
     * @return a new initialized node
     * @throws E when thrown from inner action
     * @since 4.0.0
     */
    static <E extends Exception> BasicConfigurationNode root(final CheckedConsumer<? super BasicConfigurationNode, E> maker) throws E {
        return root().act(maker);
    }

    /**
     * Create a new empty root node with the provided options.
     *
     * <p>A root node is always attached, and has no parent and an
     * empty path.</p>
     *
     * @param options options to apply.
     * @return a new empty node
     * @since 4.0.0
     */
    static BasicConfigurationNode root(final ConfigurationOptions options) {
        return new BasicConfigurationNodeImpl(null, null, options);
    }

    /**
     * Create a new root node with the provided options and initializer.
     *
     * <p>A root node is always attached, and has no parent and an
     * empty path.</p>
     *
     * @param <E> thrown type
     * @param options options to apply.
     * @param maker action to be applied to the newly created node
     * @return a new initialized node
     * @throws E when thrown from inner action
     * @since 4.0.0
     */
    static <E extends Exception> BasicConfigurationNode root(final ConfigurationOptions options,
            final CheckedConsumer<? super BasicConfigurationNode, E> maker) throws E {
        return root(options).act(maker);
    }

}
