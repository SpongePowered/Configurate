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

import kotlinx.coroutines.flow.Flow
import org.spongepowered.configurate.ScopedConfigurationNode
import org.spongepowered.configurate.reference.ConfigurationReference
import org.spongepowered.configurate.reference.ValueReference
import kotlin.reflect.jvm.javaType
import kotlin.reflect.typeOf

/** Create a flow with events for every refresh of a value backing this reference */
@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any, N : ScopedConfigurationNode<N>> ConfigurationReference<N>.flowOf(
    vararg path: Any
): Flow<T> {
    return (this.referenceTo(typeOf<T>().javaType, *path) as ValueReference<T, N>).asFlow()
}

/** Get a reference to the value of type [T] at [path]. */
@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any, N : ScopedConfigurationNode<N>> ConfigurationReference<N>.referenceTo(
    vararg path: Any
): ValueReference<T, N> {
    return this.referenceTo(typeOf<T>().javaType, *path) as ValueReference<T, N>
}
