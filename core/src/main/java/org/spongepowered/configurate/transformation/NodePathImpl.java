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

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Iterators;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.Iterator;

final class NodePathImpl implements NodePath {

    Object[] arr;

    NodePathImpl() {

    }

    NodePathImpl(final Object[] arr, final boolean copy) {
        requireNonNull(arr);
        this.arr = copy ? Arrays.copyOf(arr, arr.length) : arr;
    }

    @Override
    public Object get(final int i) {
        return this.arr[i];
    }

    @Override
    public int size() {
        return this.arr.length;
    }

    @Override
    public NodePath withAppendedChild(final @NonNull Object childKey) {
        final Object[] arr = this.arr;
        if (arr.length == 0 || (arr.length == 1 && arr[0] == null)) {
            return new NodePathImpl(new Object[] {childKey}, false);
        }

        final Object[] childPath = Arrays.copyOf(arr, arr.length + 1);
        childPath[childPath.length - 1] = childKey;

        return new NodePathImpl(childPath, false);
    }

    @Override
    public NodePath with(final int index, final Object value) throws IndexOutOfBoundsException {
        final Object[] arr = this.arr;
        if (index < 0 || index >= arr.length) {
            throw new IndexOutOfBoundsException("Index " + index + " is not within limit of [0," + arr.length + ")");
        }
        final Object[] newPath = Arrays.copyOf(arr, arr.length);
        newPath[index] = value;
        return new NodePathImpl(newPath, false);
    }

    @Override
    public Object[] getArray() {
        return Arrays.copyOf(this.arr, this.arr.length);
    }

    @Override
    public @NonNull Iterator<Object> iterator() {
        return Iterators.forArray(this.arr);
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public NodePath clone() {
        return new NodePathImpl(this.arr, true);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final NodePathImpl that = (NodePathImpl) o;
        return Arrays.equals(this.arr, that.arr);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.arr);
    }

    @Override
    public String toString() {
        return "NodePathImpl" + Arrays.toString(this.arr);
    }

}
