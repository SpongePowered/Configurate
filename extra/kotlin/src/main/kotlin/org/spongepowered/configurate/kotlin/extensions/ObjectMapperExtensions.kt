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

import org.spongepowered.configurate.kotlin.typeTokenOf
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.objectmapping.ObjectMapper.Factory
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import kotlin.reflect.KClass

/**
 * Create an object mapper with the given [Factory] for objects of type [T],
 * accepting parameterized types.
 */
inline fun <reified T> Factory.get(): ObjectMapper<T> {
    return get(typeTokenOf())
}

/**
 * Get an object mapper for the kotlin class provided.
 *
 * This cannot be used for parameterized types.
 */
fun <T : Any> Factory.get(type: KClass<T>): ObjectMapper<T> {
    return get(type.java)
}

/**
 * Get the appropriate [TypeSerializer] for the provided type [T], or null if
 * none is applicable.
 */
inline fun <reified T> TypeSerializerCollection.get(): TypeSerializer<T>? {
    return get(typeTokenOf())
}

/**
 * Get the appropriate [TypeSerializer] for the provided type [type], or null if
 * none is applicable.
 */
fun <T : Any> TypeSerializerCollection.get(type: KClass<T>): TypeSerializer<T>? {
    return get(type.java)
}
