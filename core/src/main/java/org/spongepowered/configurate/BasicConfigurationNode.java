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

public interface BasicConfigurationNode extends ScopedConfigurationNode<BasicConfigurationNode> {

    @NonNull
    static BasicConfigurationNode root() {
        return root(ConfigurationOptions.defaults());
    }

    @NonNull
    static BasicConfigurationNode root(Consumer<BasicConfigurationNode> maker) {
        return root().act(maker);
    }

    @NonNull
    static BasicConfigurationNode root(@NonNull ConfigurationOptions options) {
        return new SimpleConfigurationNode(null, null, options);
    }

    @NonNull
    static BasicConfigurationNode root(ConfigurationOptions options, Consumer<BasicConfigurationNode> maker) {
        return root(options).act(maker);
    }

}
