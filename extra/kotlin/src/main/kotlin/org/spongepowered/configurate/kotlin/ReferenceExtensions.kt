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

import kotlinx.coroutines.flow.Flow
import org.spongepowered.configurate.ScopedConfigurationNode
import org.spongepowered.configurate.reference.ConfigurationReference
import org.spongepowered.configurate.reference.ValueReference

inline fun <reified T : Any, N : ScopedConfigurationNode<N>> ConfigurationReference<N>.flowOf(vararg path: Any): Flow<T> {
    return this.referenceTo<T>(typeTokenOf(), *path).asFlow()
}

inline fun <reified T : Any, N : ScopedConfigurationNode<N>> ConfigurationReference<N>.referenceTo(vararg path: Any): ValueReference<T, N> {
    return this.referenceTo(typeTokenOf(), *path)
}

fun ConfigurationReference<*>.set(vararg path: Any, value: Any?) {
    node().node(*path).set(value)
}

@JvmName("set\$serialized")
inline fun <reified V> ConfigurationReference<*>.set(vararg path: Any, value: V?) {
    node().node(*path).set(typeTokenOf(), value)
}
