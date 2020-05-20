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
package org.spongepowered.configurate.transformation;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ScopedConfigurationNode;

/**
 * Represents an action to be performed that transforms a node in the
 * configuration tree.
 */
@FunctionalInterface
public interface TransformAction<T extends ScopedConfigurationNode<T>> {

    /**
     * Called at a certain path, with the node at that path.
     *
     * <p><strong>Caution:</strong> The state of the <code>inputPath</code> is
     * only guaranteed to be accurate during a run of the transform function.
     * Use {@link NodePath#clone()} if the path's state needs to
     * be stored.
     *
     * @param inputPath The path of the given node
     * @param valueAtPath The node at the input path. May be modified
     * @return A modified path, or null if the path is to stay the same
     */
    Object @Nullable[] visitPath(@NonNull NodePath inputPath, @NonNull T valueAtPath);

}
