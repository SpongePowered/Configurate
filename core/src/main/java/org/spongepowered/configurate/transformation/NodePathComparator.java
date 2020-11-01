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

import static org.spongepowered.configurate.transformation.ConfigurationTransformation.WILDCARD_OBJECT;

import java.util.Comparator;

final class NodePathComparator implements Comparator<NodePath> {

    static final NodePathComparator INSTANCE = new NodePathComparator();

    private NodePathComparator() {
    }

    @Override
    public int compare(final NodePath pathA, final NodePath pathB) {
        for (int i = 0; i < Math.min(pathA.size(), pathB.size()); ++i) {
            final Object a = pathA.get(i);
            final Object b = pathB.get(i);

            if (a == WILDCARD_OBJECT || b == WILDCARD_OBJECT) {
                if (a != WILDCARD_OBJECT || b != WILDCARD_OBJECT) {
                    return a == WILDCARD_OBJECT ? 1 : -1;
                }

            } else if (a instanceof Comparable) {
                @SuppressWarnings({"unchecked", "rawtypes"})
                final int comp = ((Comparable) a).compareTo(b);
                if (comp != 0) {
                    return comp;
                }
            } else {
                return a.equals(b) ? 0 : Integer.compare(a.hashCode(), b.hashCode());
            }
        }

        return Integer.compare(pathB.size(), pathA.size());
    }

}
