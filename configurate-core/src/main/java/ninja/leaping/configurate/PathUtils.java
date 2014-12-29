/**
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
package ninja.leaping.configurate;

import com.google.common.base.Objects;

import java.util.Arrays;

/**
 * Utilities for working with paths
 */
public class PathUtils {
    private PathUtils() {
        // Nevar
    }

    /**
     * Validate that the provided child path is a direct child of the provided parent path.
     * This means that the length of the child path is 1 greater than the length of the parent path, and that all
     * elements from 0 to the length of the parent path are identical in both paths.
     *
     * @param child The child path to check
     * @param parent The parent path to
     * @return Whether child path is a direct child of the parent path
     */
    public static boolean isDirectChild(Object[] child, Object[] parent) {
        if (child.length != parent.length + 1) {
            return false;
        }
        for (int i = 0; i < parent.length; ++i) {
            if (!Objects.equal(child[i], parent[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Takes a path and extends it by one element, {@code next}
     *
     * @param path The path to be appended to
     * @param next The object to be appended to the path
     * @return The new path
     */
    public static Object[] appendPath(Object[] path, Object next) {
        Object[] newArray = Arrays.copyOf(path, path.length + 1);
        newArray[path.length] = next;
        return newArray;
    }

    /**
     * Creates a new path not containing the last element of the input path
     *
     * @param path The input path
     * @return An appropriately modified path
     */
    public static Object[] dropPathTail(Object[] path) {
        return Arrays.copyOf(path, path.length - 1);
    }
}
