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

import java.util.function.Consumer;

/**
 * Various methods to create empty configuration nodes.
 */
public interface ConfigurationNodeFactory<N extends ConfigurationNode> {

    /**
     * Default options for the types of nodes created by this factory.
     *
     * <p>All values must match what a created node will see, but some values
     * may be determined by this factory to be non user-modifiable. These should
     * be documented for any factory implementation.
     *
     * @return default options
     */
    default ConfigurationOptions defaultOptions() {
        return ConfigurationOptions.defaults();
    }

    /**
     * Create an empty node with the provided options.
     *
     * <p>Node options may be overridden if the factory enforces specific
     * requirements on options.
     *
     * @param options node options
     * @return newly created empty node
     */
    N createNode(ConfigurationOptions options);

    /**
     * Create a new node with default options.
     *
     * @return newly created empty node
     */
    default N createNode() {
        return createNode(defaultOptions());
    }

    /**
     * Create a new node with default options and initialize it with the
     * provided action.
     *
     * @param action action to initialize node with
     * @return newly created empty node
     */
    default N createNode(final Consumer<N> action) {
        final N node = createNode();
        action.accept(node);
        return node;
    }

    /**
     * Create a new node with the provided options and initialize it with the
     * provided action.
     *
     * <p>Node options may be overridden if the factory enforces specific
     * requirements on options.
     *
     * @param options node options
     * @param action action to initialize node with
     * @return newly created empty node
     */
    default N createNode(final ConfigurationOptions options, final Consumer<N> action) {
        final N node = createNode(options);
        action.accept(node);
        return node;
    }

}
