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
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.util.CheckedFunction;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.util.function.Supplier;

/**
 * Interface that gathers metadata from classes.
 *
 * <p>Any type of data object can be added this way.</p>
 *
 * @param <I> intermediate data type
 * @since 4.0.0
 */
@FunctionalInterface
public interface FieldDiscoverer<I> {

    /**
     * Create a new field discoverer that will handle record classes.
     *
     * <p>This discoverer will use the record's canonical constructor to create
     * new instances, passing {@code null} for any missing parameters. The
     * accessor methods for each record component will be used to read
     * values from the record.</p>
     *
     * @implNote To avoid requiring preview features to run on Java 14 and 15,
     *     the record discoverer accesses methods reflectively, and can safely
     *     be created and applied to object mappers running on any Java version.
     *     Continued support of preview features once they have been released in
     *     a stable Java version is not guaranteed.
     *
     * @return new discoverer
     * @since 4.0.0
     */
    static FieldDiscoverer<?> record() {
        return RecordFieldDiscoverer.INSTANCE;
    }

    /**
     * Create a new discoverer for object instance fields.
     *
     * <p>This discoverer will process any non-static and non-transient field
     * in the object. Modifying {@code final} fields is unsupported and may stop
     * working with newer Java versions.</p>
     *
     * @param instanceFactory a factory for instance providers
     * @return new discoverer
     * @since 4.0.0
     */
    static FieldDiscoverer<?> object(final CheckedFunction<AnnotatedType, @Nullable Supplier<Object>, SerializationException> instanceFactory) {
        return new ObjectFieldDiscoverer(requireNonNull(instanceFactory, "instanceFactory"), null, false);
    }

    /**
     * Create a new discoverer for object instance fields.
     *
     * <p>This discoverer will process any non-static and non-transient field
     * in the object. Modifying {@code final} fields is unsupported and may stop
     * working with newer Java versions.</p>
     *
     * @param instanceFactory a factory for instance providers
     * @param instanceUnavailableErrorMessage a message that will be part of the
     *     exception thrown when trying to create instances for an
     *     unsupported type
     * @return new discoverer
     * @since 4.1.0
     */
    static FieldDiscoverer<?> object(final CheckedFunction<AnnotatedType, @Nullable Supplier<Object>, SerializationException> instanceFactory,
            final String instanceUnavailableErrorMessage) {
        requireNonNull(instanceUnavailableErrorMessage, "instanceUnavailableErrorMessage");
        return new ObjectFieldDiscoverer(requireNonNull(instanceFactory, "instanceFactory"), instanceUnavailableErrorMessage, false);
    }

    /**
     * Create a new discoverer for object instance fields.
     *
     * <p>This discoverer will process any non-static and non-transient field
     * in the object. Modifying {@code final} fields is unsupported and may stop
     * working with newer Java versions.</p>
     *
     * <p>This discoverer will only match objects that it can create an instance
     * of (i. e. where {@code instanceFactory} returns a
     * non-{@code null} supplier).</p>
     *
     * @param instanceFactory a factory for instance providers
     * @return new discoverer
     * @since 4.2.0
     */
    static FieldDiscoverer<?> instantiableObject(
        final CheckedFunction<AnnotatedType, @Nullable Supplier<Object>, SerializationException> instanceFactory
    ) {
        return new ObjectFieldDiscoverer(requireNonNull(instanceFactory, "instanceFactory"), null, true);
    }

    /**
     * Create a new discoverer for object instance fields.
     *
     * <p>Only objects with empty constructors can be created.</p>
     *
     * @return new discoverer
     * @see #object(CheckedFunction) for more details on which fields will
     *      be discovered.
     * @since 4.0.0
     */
    static FieldDiscoverer<?> emptyConstructorObject() {
        return ObjectFieldDiscoverer.EMPTY_CONSTRUCTOR_INSTANCE;
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
     * @throws SerializationException if any fields have invalid data
     * @since 4.0.0
     */
    <V> @Nullable InstanceFactory<I> discover(AnnotatedType target, FieldCollector<I, V> collector) throws SerializationException;

    /**
     * A handler that controls the deserialization process for an object.
     *
     * @param <I> intermediate type
     * @since 4.0.0
     */
    interface InstanceFactory<I> {

        /**
         * Return a new instance of the intermediary type to be populated.
         *
         * @return new intermediate container
         * @since 4.0.0
         */
        I begin();

        /**
         * Return a finalized object based on the provided intermediate.
         *
         * @param intermediate intermediate container to hold values
         * @return final value
         * @throws SerializationException if unable to construct a
         * @since 4.0.0
         */
        Object complete(I intermediate) throws SerializationException;

        /**
         * Get whether or not new object instances can be created.
         *
         * @return new instance creation
         * @since 4.0.0
         */
        boolean canCreateInstances();
    }

    /**
     * A handler for working with mutable objects in the object mapper.
     *
     * @param <I> intermediate type
     * @since 4.0.0
     */
    interface MutableInstanceFactory<I> extends InstanceFactory<I> {

        /**
         * Apply the intermediate data to an existing object.
         *
         * @param instance instance to write to
         * @param intermediate intermediate container
         * @throws SerializationException if unable to apply info
         * @since 4.0.0
         */
        void complete(Object instance, I intermediate) throws SerializationException;
    }

    /**
     * A collector for the necessary metadata for fields.
     *
     * @param <I> intermediate type
     * @param <V> container type
     * @since 4.0.0
     */
    @FunctionalInterface
    interface FieldCollector<I, V> {

        /**
         * Accept metadata that defines a specific field.
         *
         * @param name name
         * @param type declared field type, as resolved as possible
         * @param annotations combined element containing all annotations
         *                    applicable to the field
         * @param deserializer a function to populate the intermediate state
         *                     with a single deserialized field value.
         * @param serializer a function to extract a value from a completed
         *                   object instance.
         * @since 4.0.0
         */
        void accept(String name, AnnotatedType type, AnnotatedElement annotations, FieldData.Deserializer<I> deserializer,
                CheckedFunction<V, @Nullable Object, Exception> serializer);
    }

}
