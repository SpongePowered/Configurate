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
package org.spongepowered.configurate.objectmapping;

import static java.util.Objects.requireNonNull;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.util.CheckedFunction;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Interface that gathers metadata from classes.
 *
 * <p>Any type of data object can be added this way.</p>
 *
 * @param <I> intermediate data type
 */
public interface FieldDiscoverer<I> {

    /**
     * Create a new field discoverer that will handle record classes.
     *
     * @return new discoverer
     */
    static FieldDiscoverer<?> ofRecord() {
        return RecordFieldDiscoverer.INSTANCE;
    }

    /**
     * Create a new discoverer for fields in POJOs.
     *
     * <p>This discoverer will process any non-static and non-transient field
     * in the object.</p>
     *
     * @param instanceFactory A factory for instance providers
     * @return new discoverer
     */
    static FieldDiscoverer<?> ofPojo(final CheckedFunction<AnnotatedType, @Nullable Supplier<Object>, ObjectMappingException> instanceFactory) {
        return new PojoFieldDiscoverer(requireNonNull(instanceFactory, "instanceFactory"));
    }

    /**
     * Create a new discoverer for fields in POJOs.
     *
     * <p>Only objects with empty constructors can be created.</p>
     *
     * @return new discoverer
     * @see #ofPojo(CheckedFunction) for more details on which fields will
     *      be discovered.
     */
    static FieldDiscoverer<?> ofEmptyConstructorPojo() {
        return PojoFieldDiscoverer.EMPTY_CONSTRUCTOR_INSTANCE;
    }

    /**
     * Inspect the {@code target} type for fields to be supplied to
     * the {@code collector}.
     *
     * <p>If the target type is handleable, a non-null value must be returned.
     * Fields can only be collected from one source at the moment, so if the
     * instance factory is null any discovered fields will be discarded.</p>
     *
     * @param target type to inspect
     * @param collector collector for discovered fields.
     * @param <V> object type
     * @return a factory for handling the construction of object instances, or
     *      {@code null} if {@code target} is not of a handleable type.
     * @throws ObjectMappingException if any fields have invalid data
     */
    <V> @Nullable InstanceFactory<I> discover(AnnotatedType target, FieldCollector<I, V> collector) throws ObjectMappingException;

    /**
     * A handler for controlling the deserialization process for an object.
     *
     * @param <I> intermediate type
     */
    interface InstanceFactory<I> {

        /**
         * Return a new instance of the intermediary type to be populated.
         *
         * @return new intermediate container
         */
        I begin();

        /**
         * Return a finalized object based on the provided intermediate.
         *
         * @param intermediate intermediate container to hold values
         * @return final value
         * @throws ObjectMappingException if unable to construct a
         */
        Object complete(I intermediate) throws ObjectMappingException;

        /**
         * Get whether or not new object instances can be created.
         *
         * @return new instance creation
         */
        boolean canCreateInstances();
    }

    /**
     * A handler for working with mutable objects in the object mapper.
     *
     * @param <I> intermediate type
     */
    interface MutableInstanceFactory<I> extends InstanceFactory<I> {

        /**
         * Apply the intermediate data to an existing object.
         *
         * @param instance instance to write to
         * @param intermediate intermediate container
         * @throws ObjectMappingException if unable to apply info
         */
        void complete(Object instance, I intermediate) throws ObjectMappingException;
    }

    /**
     * A collector for the necessary metadata for fields.
     *
     * @param <I> intermediate type
     * @param <V> container type
     */
    @FunctionalInterface
    interface FieldCollector<I, V> {

        /**
         * Accept metadata that defines a specific field.
         *
         * @param name name
         * @param type declared field type, as resolved as possible
         * @param enclosing The element containing the field
         * @param deserializer a function to populate the intermediate state
         *                     with a single deserialized field value.
         * @param serializer a function to extract a value from a completed
         *                   object instance.
         */
        void accept(String name, AnnotatedType type, AnnotatedElement enclosing, BiConsumer<I, Object> deserializer,
                CheckedFunction<V, Object, Exception> serializer);
    }

}
