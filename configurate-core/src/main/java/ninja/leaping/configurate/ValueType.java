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
package ninja.leaping.configurate;

/**
 * An enumeration of the types of value a {@link ConfigurationNode} can hold.
 *
 * @deprecated Use {@link ConfigurationNode#isList()} and {@link ConfigurationNode#isMap()} for the same information
 */
@Deprecated
public enum ValueType {

    /**
     * Represents a node that consists of a "single" scalar value
     */
    SCALAR,

    /**
     * Represents a node that consists of a number of child values, each mapped
     * by a unique key.
     */
    MAP,

    /**
     * Represents a node that consists of a number of child values, in a specific
     * order, each mapped by an index value.
     */
    LIST,

    /**
     * Represents a node that has no defined value.
     */
    NULL;

    /**
     * Gets if the type can hold child values.
     *
     * @return If the type can have children
     */
    public boolean canHaveChildren() {
        return this == MAP || this == LIST;
    }

}
