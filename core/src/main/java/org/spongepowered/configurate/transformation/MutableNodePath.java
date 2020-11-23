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

import com.google.errorprone.annotations.concurrent.LazyInit;
import org.spongepowered.configurate.NodePath;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Mutable implementation of {@link NodePath} for use with transform instances.
 */
final class MutableNodePath implements NodePath {

    @LazyInit Object[] arr;

    @Override
    public Object get(final int i) {
        return this.arr[i];
    }

    @Override
    public int size() {
        return this.arr.length;
    }

    @Override
    public Object[] array() {
        return Arrays.copyOf(this.arr, this.arr.length);
    }

    @Override
    public NodePath withAppendedChild(final Object childKey) {
        return copy().withAppendedChild(childKey);
    }

    @Override
    public NodePath with(final int index, final Object value) throws IndexOutOfBoundsException {
        return copy().with(index, value);
    }

    @Override
    public NodePath plus(final NodePath other) {
        requireNonNull(other, "other");
        final Object[] otherArr = other.array();
        final Object[] target = Arrays.copyOf(this.arr, this.arr.length + otherArr.length);
        System.arraycopy(otherArr, 0, target, this.arr.length, otherArr.length);

        return NodePath.of(target);
    }

    @Override
    public Iterator<Object> iterator() {
        return Arrays.asList(this.arr).iterator();
    }

    @Override
    public NodePath copy() {
        return NodePath.path(this.arr);
    }

}
