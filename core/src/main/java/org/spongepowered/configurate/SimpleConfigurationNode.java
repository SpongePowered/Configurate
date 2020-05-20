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

class SimpleConfigurationNode extends AbstractConfigurationNode<BasicConfigurationNode, SimpleConfigurationNode> implements BasicConfigurationNode {
    protected SimpleConfigurationNode(@Nullable Object key, @Nullable SimpleConfigurationNode parent, @NonNull ConfigurationOptions options) {
        super(key, parent, options);
    }

    protected SimpleConfigurationNode(SimpleConfigurationNode parent, SimpleConfigurationNode copyOf) {
        super(parent, copyOf);
    }

    @NonNull
    @Override
    protected SimpleConfigurationNode copy(@Nullable SimpleConfigurationNode parent) {
        return new SimpleConfigurationNode(parent, this);
    }

    @Override
    @NonNull
    public SimpleConfigurationNode self() {
        return this;
    }

    @Override
    protected SimpleConfigurationNode implSelf() {
        return this;
    }

    @Override
    protected SimpleConfigurationNode createNode(Object path) {
        return new SimpleConfigurationNode(path, this, getOptions());
    }
}
