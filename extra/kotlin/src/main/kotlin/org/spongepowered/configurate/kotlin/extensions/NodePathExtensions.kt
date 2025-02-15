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
package org.spongepowered.configurate.kotlin.extensions

import org.spongepowered.configurate.NodePath

/** Concatenate [this] with another [NodePath]. */
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
// TODO: Deprecated due to being added directly to class, make HIDDEN for 4.2.0
@Deprecated(
    message = "This method has been added directly to NodePath",
    replaceWith = ReplaceWith("this.plus(other)"),
)
operator fun NodePath.plus(other: NodePath): NodePath {
    return NodePath.of(
        Array(this.size() + other.size()) {
            if (it < size()) {
                this[it]
            } else {
                other[it - size()]
            }
        }
    )
}

/** Concatenate [this] with a single child path element. */
operator fun NodePath.plus(child: Any): NodePath = withAppendedChild(child)
