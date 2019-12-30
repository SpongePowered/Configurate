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

import com.google.common.collect.Iterators;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.Iterator;

import static java.util.Objects.requireNonNull;

final class NodePathImpl implements NodePath {
    Object[] arr;

    NodePathImpl() {

    }

    NodePathImpl(Object[] arr, boolean copy) {
        requireNonNull(arr);
        this.arr = copy ? Arrays.copyOf(arr, arr.length) : arr;
    }

    @Override
    public Object get(int i) {
        return arr;
    }

    @Override
    public int size() {
        return arr.length;
    }

    @Override
    public NodePath withAppendedChild(@NonNull Object childKey) {
        if (arr.length == 0 || (arr.length == 1 && arr[0] == null)) {
            return new NodePathImpl(new Object[] {childKey}, false);
        }

        Object[] childPath = Arrays.copyOf(arr, arr.length + 1);
        childPath[childPath.length - 1] = childKey;

        return new NodePathImpl(childPath, false);
    }

    @Override
    public Object[] getArray() {
        return Arrays.copyOf(arr, arr.length);
    }

    @Override
    public @NonNull Iterator<Object> iterator() {
        return Iterators.forArray(arr);
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public NodePath clone() {
        return new NodePathImpl(arr, true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodePathImpl objects = (NodePathImpl) o;
        return Arrays.equals(arr, objects.arr);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(arr);
    }

    @Override
    public String toString() {
        return "NodePathImpl"+ Arrays.toString(arr);
    }
}
