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

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.NodePath
import org.spongepowered.configurate.ScopedConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType
import kotlin.reflect.typeOf

/**
 * An implementation of `contains` that can traverse multiple levels in [path].
 *
 * @see ConfigurationNode.hasChild
 */
operator fun ConfigurationNode.contains(path: Array<Any>): Boolean {
    return hasChild(path)
}

/**
 * An implementation of `contains` that can traverse multiple levels in [path].
 *
 * @see ConfigurationNode.hasChild
 */
operator fun ConfigurationNode.contains(path: NodePath): Boolean {
    return hasChild(path)
}

/**
 * Contains for a single level.
 *
 * @param path a single path element
 * @see ConfigurationNode.hasChild
 */
operator fun ConfigurationNode.contains(path: Any): Boolean {
    return hasChild(path)
}

/**
 * Get a value from the receiver using the type parameter.
 *
 * @see ConfigurationNode.get
 */
@Throws(SerializationException::class)
inline fun <reified V> ConfigurationNode.get(): V? {
    return get(typeOf<V>().javaType) as V?
}

/**
 * Get a value from the receiver using the type parameter.
 *
 * @see ConfigurationNode.get
 */
@Throws(SerializationException::class)
inline fun <reified V> ConfigurationNode.get(default: V): V {
    return get(typeOf<V>().javaType, default) as V
}

/**
 * Get a value from the receiver using the type parameter.
 *
 * @see ConfigurationNode.get
 */
fun <T : Any> ConfigurationNode.get(type: KClass<T>): T? {
    return get(type.java)
}

/**
 * Get a value from the receiver using the type parameter.
 *
 * @see ConfigurationNode.get
 */
fun <T : Any> ConfigurationNode.get(type: KClass<T>, default: T): T {
    return get(type.java, default)
}

/**
 * Get a value from the receiver using the type parameter.
 *
 * @see ConfigurationNode.get
 */
fun <T : Any> ConfigurationNode.get(type: KClass<T>, default: () -> T): T {
    return get(type.java, default)
}

/**
 * Get a value from the receiver using the type parameter.
 *
 * @see ConfigurationNode.get
 */
fun ConfigurationNode.get(type: KType): Any? {
    return get(type.javaType)
}

/**
 * Get a value from the receiver using the type parameter.
 *
 * @see ConfigurationNode.get
 */
fun ConfigurationNode.get(type: KType, default: Any): Any {
    return get(type.javaType, default)
}

/**
 * Get a value from the receiver using the type parameter.
 *
 * @see ConfigurationNode.get
 */
fun ConfigurationNode.get(type: KType, default: () -> Any): Any {
    return get(type.javaType, default)
}

/**
 * Get a value from the receiver using the type parameter.
 *
 * @see ConfigurationNode.get
 */
@Throws(SerializationException::class)
inline fun <reified V> ConfigurationNode.get(noinline default: () -> V): V {
    return get(typeOf<V>().javaType, default) as V
}

/**
 * Set a value from the receiver using the type parameter to resolve a serializer.
 *
 * @see ConfigurationNode.set
 */
@Throws(SerializationException::class)
inline fun <reified V> ConfigurationNode.typedSet(value: V?) {
    set(typeOf<V>().javaType, value)
}

/**
 * Get a list value from the receiver using a Kotlin type.
 *
 * @see ConfigurationNode.getList
 */
fun <T : Any> ConfigurationNode.getList(type: KClass<T>): List<T>? {
    return getList(type.java)
}

/**
 * Get a list value from the receiver using a Kotlin type.
 *
 * @see ConfigurationNode.getList
 */
fun <T : Any> ConfigurationNode.getList(type: KClass<T>, default: List<T>): List<T> {
    return getList(type.java, default)
}

/**
 * Get a list value from the receiver using a Kotlin type.
 *
 * @see ConfigurationNode.getList
 */
fun ConfigurationNode.getList(type: KType): List<*>? {
    return getList(type.javaType)
}

/**
 * Get a list value from the receiver using a Kotlin type.
 *
 * @see ConfigurationNode.getList
 */
fun ConfigurationNode.getList(type: KType, default: List<*>): List<*> {
    return getList(type.javaType, default)
}

/**
 * Get a list value with element type [T] from the receiver.
 *
 * @see ConfigurationNode.getList
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified T> ConfigurationNode.getList(default: List<T>): List<T> {
    return getList(typeOf<T>().javaType, default) as List<T>
}

/**
 * Set a value on the receiver described by a kotlin class.
 *
 * @see ConfigurationNode.set
 */
fun <T : Any> ConfigurationNode.set(type: KClass<T>, value: T?): ConfigurationNode {
    return set(type.java, value)
}

/**
 * Set a value on the receiver described by a kotlin class.
 *
 * @see ConfigurationNode.set
 */
fun ConfigurationNode.set(type: KType, value: Any?): ConfigurationNode {
    return set(type.javaType, value)
}

/**
 * Set a value on the receiver described by a kotlin class.
 *
 * @see ConfigurationNode.set
 */
fun <T : Any, N : ScopedConfigurationNode<N>> N.set(type: KClass<T>, value: T?): N {
    return set(type.java, value)
}

/**
 * Set a value on the receiver described by a kotlin class.
 *
 * @see ConfigurationNode.set
 */
fun <T : Any, N : ScopedConfigurationNode<N>> N.set(type: KType, value: T?): N {
    return set(type.javaType, value)
}
