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

    public static Object[] appendPath(Object[] path, Object next) {
        Object[] newArray = Arrays.copyOf(path, path.length + 1);
        newArray[path.length] = next;
        return newArray;
    }

    public static Object[] dropPathTail(Object[] path) {
        return Arrays.copyOf(path, path.length - 1);
    }
}
