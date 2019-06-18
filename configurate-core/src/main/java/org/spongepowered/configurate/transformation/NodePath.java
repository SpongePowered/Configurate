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

import java.util.Iterator;

/**
 * Represents the path to a given node.
 */
public interface NodePath extends Iterable<Object> {

    /**
     * Gets a specific element from the path array
     *
     * @param i The index to get
     * @return Object at the index
     */
    Object get(int i);

    /**
     * Gets the length of the path
     *
     * @return Length of the path array
     */
    int size();

    /**
     * Returns a copy of the original path array
     *
     * @return the copied array
     */
    Object[] getArray();

    /**
     * Returns an iterator over the path.
     *
     * @return An iterator of the path
     */
    @NonNull
    @Override
    Iterator<Object> iterator();
}
