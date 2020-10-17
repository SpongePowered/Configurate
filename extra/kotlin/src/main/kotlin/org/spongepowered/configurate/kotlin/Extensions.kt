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
package org.spongepowered.configurate.kotlin

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationNodeFactory
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.transformation.NodePath

/**
 * Multi level contains
 */
operator fun ConfigurationNode.contains(path: Array<Any>): Boolean {
    return !node(*path).virtual()
}

/**
 * Contains for a single level
 *
 * @param path a single path element
 */
operator fun ConfigurationNode.contains(path: Any): Boolean {
    return !node(path).virtual()
}

@Throws(SerializationException::class)
inline fun <reified V> ConfigurationNode.typedGet(): V? {
    return get(typeTokenOf<V>(), null as V?)
}

@Throws(SerializationException::class)
inline fun <reified V> ConfigurationNode.typedGet(default: V): V {
    return get(typeTokenOf(), default)
}

@Throws(SerializationException::class)
inline fun <reified V> ConfigurationNode.typedSet(value: V?) {
    set(typeTokenOf(), value)
}

operator fun <N : ConfigurationNode> ConfigurationNodeFactory<N>.invoke(options: ConfigurationOptions = defaultOptions()): N =
    createNode(options)

/**
 * Concatenate `this` with another [NodePath].
 */
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

/**
 * Concatenate `this` with a single child path element
 */
operator fun NodePath.plus(child: Any): NodePath = withAppendedChild(child)
