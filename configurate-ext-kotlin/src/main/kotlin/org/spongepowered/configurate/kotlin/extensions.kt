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

import com.google.common.reflect.TypeToken
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ScopedConfigurationNode
import org.spongepowered.configurate.objectmapping.ObjectMappingException

operator fun <T: ScopedConfigurationNode<T>> T.get(path: Any): T {
    return getNode(path)
}

operator fun <T: ScopedConfigurationNode<T>> T.get(vararg path: Any): T {
    return getNode(*path)
}

operator fun ConfigurationNode.set(vararg path: Any, value: Any?) {
    getNode(*path).value = value
}

/**
 * Multi level contains
 */
operator fun ConfigurationNode.contains(path: Array<Any>): Boolean {
    return !getNode(*path).isVirtual()
}

/**
 * Contains for a single level
 *
 * @param path a single path element
 */
operator fun ConfigurationNode.contains(path: Any): Boolean {
    return !getNode(path).isVirtual()
}

@Throws(ObjectMappingException::class)
inline fun <reified V> ConfigurationNode.get(default: V? = null): V? {
    return getValue(object : TypeToken<V>() {}, default)
}

@Throws(ObjectMappingException::class)
inline fun <reified V> ConfigurationNode.set(value: V?) {
    setValue(object : TypeToken<V>() {}, value)
}
