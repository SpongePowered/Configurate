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

import java.util.function.Consumer;

/**
 * A configuration node that can have a comment attached to it.
 */
public interface CommentedConfigurationNode extends CommentedConfigurationNodeIntermediary<CommentedConfigurationNode> {

    @NonNull
    static CommentedConfigurationNode root() {
        return root(ConfigurationOptions.defaults());
    }

    @NonNull
    static CommentedConfigurationNode root(Consumer<? super CommentedConfigurationNode> action) {
        return root().act(action);
    }

    @NonNull
    static CommentedConfigurationNode root(@NonNull ConfigurationOptions options) {
        return new SimpleCommentedConfigurationNode(null, null, options);
    }

    @NonNull
    static CommentedConfigurationNode root(@NonNull ConfigurationOptions options, Consumer<? super CommentedConfigurationNode> action) {
        return root(options).act(action);
    }

}
