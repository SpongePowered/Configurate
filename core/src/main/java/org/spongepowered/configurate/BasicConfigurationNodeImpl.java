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

class BasicConfigurationNodeImpl
        extends AbstractConfigurationNode<BasicConfigurationNode, BasicConfigurationNodeImpl> implements BasicConfigurationNode {

    protected BasicConfigurationNodeImpl(final @Nullable Object key, final @Nullable BasicConfigurationNodeImpl parent,
            final @NonNull ConfigurationOptions options) {
        super(key, parent, options);
    }

    protected BasicConfigurationNodeImpl(final @Nullable BasicConfigurationNodeImpl parent, final BasicConfigurationNodeImpl copyOf) {
        super(parent, copyOf);
    }

    @NonNull
    @Override
    protected BasicConfigurationNodeImpl copy(final @Nullable BasicConfigurationNodeImpl parent) {
        return new BasicConfigurationNodeImpl(parent, this);
    }

    @Override
    @NonNull
    public BasicConfigurationNodeImpl self() {
        return this;
    }

    @Override
    protected BasicConfigurationNodeImpl implSelf() {
        return this;
    }

    @Override
    protected BasicConfigurationNodeImpl createNode(final Object path) {
        return new BasicConfigurationNodeImpl(path, this, options());
    }

}
