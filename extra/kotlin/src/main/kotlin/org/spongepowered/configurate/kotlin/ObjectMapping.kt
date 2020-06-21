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

import io.leangen.geantyref.GenericTypeReflector
import io.leangen.geantyref.GenericTypeReflector.erase
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.objectmapping.FieldDiscoverer
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.objectmapping.ObjectMapper.Factory
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.util.Typing.combinedAnnotations
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.AnnotatedType
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaMethod

private val dataClassMapperFactory = ObjectMapper.factoryBuilder().addDiscoverer(DataClassFieldDiscoverer).build()

fun objectMapperFactory(): Factory {
    return dataClassMapperFactory
}

/**
 * Get an object mapper for the type [T] using the default object mapper factory
 */
inline fun <reified T> objectMapper(): ObjectMapper<T> {
    return objectMapperFactory()[typeTokenOf()]
}

/**
 * Get an object mapper bound to the instance of [T], resolving type parameters
 */
inline fun <reified T> T.toNode(target: ConfigurationNode) {
    return objectMapperFactory()[typeTokenOf<T>()].save(this, target)
}

/**
 * Create an object mapper with the given [Factory] for objects of type [T],
 * accepting parameterized types.
 */
inline fun <reified T> Factory.get(): ObjectMapper<T> {
    return get(typeTokenOf())
}

/**
 * Get the appropriate [TypeSerializer] for the provided type [T], or null if
 * none is applicable.
 */
inline fun <reified T> TypeSerializerCollection.get(): TypeSerializer<T>? {
    return get(typeTokenOf())
}

@PublishedApi
internal inline fun <reified T> typeTokenOf() = object : TypeToken<T>() {}

annotation class Fancy(val value: String, val message: String = "")

/**
 * A field discoverer that gathers definitions from kotlin `data` classes.
 *
 * Note: Type use annotations are not handled correctly in Kotlin at the moment.
 * To use these annotations, the `-Xemit-jvm-type-annotations` compiler option
 * should be used (available Kotlin 1.4+ only).
 *
 * See [KT-39369](https://youtrack.jetbrains.com/issue/KT-39369) for details.
 */
object DataClassFieldDiscoverer : FieldDiscoverer<MutableMap<KParameter, Any>> {
    override fun <V> discover(
        target: AnnotatedType,
        collector: FieldDiscoverer.FieldCollector<MutableMap<KParameter, Any>, V>
    ): FieldDiscoverer.InstanceFactory<MutableMap<KParameter, Any>>? {
        val klass = erase(target.type).kotlin
        if (!klass.isData) {
            return null
        }

        val constructor = klass.primaryConstructor ?: return null
        constructor.javaMethod

        val annotatedTypes = constructor.javaConstructor!!.annotatedParameterTypes
        val properties = klass.memberProperties
        constructor.parameters.asSequence().zip(annotatedTypes.asSequence()).forEach { (param, type) ->
            val resolvedType = GenericTypeReflector.resolveType(type, target)
            val field = properties.first { it.name == param.name }

            @Suppress("UNCHECKED_CAST")
            collector.accept(
                param.name,
                resolvedType,
                combinedAnnotations(param.type.javaElement, param.javaElement, field.javaField), // type, backing field, etc
                { intermediate, arg -> intermediate[param] = arg },
                { (field as KProperty1<V, *>).get(it) }
            )
        }

        return object : FieldDiscoverer.InstanceFactory<MutableMap<KParameter, Any>> {
            override fun begin(): MutableMap<KParameter, Any> {
                return mutableMapOf()
            }

            override fun complete(intermediate: MutableMap<KParameter, Any>): Any {
                return constructor.callBy(intermediate)
            }

            override fun canCreateInstances(): Boolean = true
        }
    }
}

// thanks kotlin :(

/**
 * Get a kotlin annotated element as a Java one
 */
private val KAnnotatedElement.javaElement: AnnotatedElement get() {
    if (this is KProperty<*>) {
        val javaType = this.javaField ?: this.javaGetter
        if (javaType != null) {
            return javaType
        }
    } else if (this is KFunction<*>) {
        val javaType = this.javaMethod ?: this.javaConstructor
        if (javaType != null) {
            return javaType
        }
    }

    return WrappedElement(this)
}

private class WrappedElement(private val backing: KAnnotatedElement) : AnnotatedElement {
    @Suppress("UNCHECKED_CAST")
    override fun <T : Annotation> getAnnotation(annotationClass: Class<T>): T? {
        return backing.annotations.firstOrNull { it.annotationClass.java == annotationClass } as T?
    }

    override fun getAnnotations(): Array<Annotation> {
        return backing.annotations.toTypedArray()
    }

    override fun getDeclaredAnnotations(): Array<Annotation> = this.annotations
}
