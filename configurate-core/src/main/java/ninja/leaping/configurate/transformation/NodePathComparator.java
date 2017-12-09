/*
 * This file is part of Configurate, licensed under the Apache-2.0 License.
 *
 * Copyright (C) zml
 * Copyright (C) IchorPowered
 * Copyright (C) Contributors
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

package ninja.leaping.configurate.transformation;

import java.util.Comparator;

import static ninja.leaping.configurate.transformation.ConfigurationTransformation.WILDCARD_OBJECT;

class NodePathComparator implements Comparator<Object[]> {
    @Override
    public int compare(Object[] a, Object[] b) {
        for (int i = 0; i < Math.min(a.length, b.length); ++i) {
            if (a[i] == WILDCARD_OBJECT || b[i] == WILDCARD_OBJECT) {
                if (a[i] != WILDCARD_OBJECT || b[i] != WILDCARD_OBJECT) {
                    return a[i] == WILDCARD_OBJECT ? 1 : -1;
                }

            } else if (a[i] instanceof Comparable) {
                @SuppressWarnings("unchecked")
                final int comp = ((Comparable) a[i]).compareTo(b[i]);
                switch (comp) {
                    case 0:
                        break;
                    default:
                        return comp;
                }
            } else {
                return a[i].equals(b[i]) ? 0 : Integer.valueOf(a[i].hashCode()).compareTo(b[i].hashCode());
            }
        }
        if (a.length > b.length) {
            return -1;
        } else if (b.length > a.length) {
            return 1;
        } else {
            return 0;
        }

    }
}
