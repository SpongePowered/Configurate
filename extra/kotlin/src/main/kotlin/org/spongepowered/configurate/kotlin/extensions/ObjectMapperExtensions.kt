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

import kotlin.reflect.KClass
import org.spongepowered.configurate.kotlin.typeTokenOf
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.objectmapping.ObjectMapper.Factory
import org.spongepowered.configurate.objectmapping.ObjectMapper.Factory.Builder
import org.spongepowered.configurate.objectmapping.meta.Constraint
import org.spongepowered.configurate.objectmapping.meta.Processor
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection

/**
 * Create an object mapper with the given [Factory] for objects of type [T], accepting parameterized
 * types.
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
 * Get the appropriate [TypeSerializer] for the provided type [T], or null if none is applicable.
 */
inline fun <reified T> TypeSerializerCollection.get(): TypeSerializer<T>? {
    return get(typeTokenOf())
}

/**
 * Get the appropriate [TypeSerializer] for the provided type [type], or null if none is applicable.
 */
fun <T : Any> TypeSerializerCollection.get(type: KClass<T>): TypeSerializer<T>? {
    return get(type.java)
}

// Processors
/**
 * Register a [Processor] that will process fields after write, accepting Kotlin types.
 *
 * @see Builder.addProcessor
 */
fun <A : Annotation> Builder.addProcessor(
    definition: KClass<A>,
    factory: Processor.Factory<A, Any?>,
): Builder = addProcessor(definition.java, factory)

/**
 * Register a [Processor] that will process fields after write, accepting Kotlin types.
 *
 * @see Builder.addProcessor
 */
fun <A : Annotation, T : Any> Builder.addProcessor(
    definition: KClass<A>,
    valueType: KClass<T>,
    factory: Processor.Factory<A, T?>,
): Builder = addProcessor(definition.java, valueType.java, factory)

/**
 * Register a [Processor] that will process fields after write, accepting parameterized types.
 *
 * @see Builder.addProcessor
 */
inline fun <reified A : Annotation> Builder.addProcessor(factory: Processor.Factory<A, Any?>) =
    addProcessor(A::class, factory)

/**
 * Register a [Processor] that will process fields after write, accepting parameterized types.
 *
 * @see Builder.addProcessor
 */
@JvmName("addProcessorFull")
inline fun <reified A : Annotation, reified T : Any> Builder.addProcessor(
    factory: Processor.Factory<A, T?>
) = addProcessor(A::class, T::class, factory)

// Constraints
/**
 * Register a [Constraint] that will be used to validate fields, accepting Kotlin types.
 *
 * @see Builder.addConstraint
 */
fun <A : Annotation> Builder.addConstraint(
    definition: KClass<A>,
    factory: Constraint.Factory<A, Any?>,
): Builder = addConstraint(definition.java, factory)

/**
 * Register a [Constraint] that will be used to validate fields, accepting Kotlin types.
 *
 * @see Builder.addConstraint
 */
fun <A : Annotation, T : Any> Builder.addConstraint(
    definition: KClass<A>,
    valueType: KClass<T>,
    factory: Constraint.Factory<A, T?>,
): Builder = addConstraint(definition.java, valueType.java, factory)

/**
 * Register a [Constraint] that will be used to validate fields, accepting parameterized types.
 *
 * @see Builder.addConstraint
 */
inline fun <reified A : Annotation> Builder.addConstraint(factory: Constraint.Factory<A, Any?>) =
    addConstraint(A::class, factory)

/**
 * Register a [Constraint] that will be used to validate fields, accepting parameterized types.
 *
 * @see Builder.addConstraint
 */
@JvmName("addConstraintFull")
inline fun <reified A : Annotation, reified T : Any> Builder.addConstraint(
    factory: Constraint.Factory<A, T?>
) = addConstraint(A::class, T::class, factory)
