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
package ninja.leaping.configurate.objectmapping;

import com.google.common.base.Preconditions;
import ninja.leaping.configurate.ConfigurationNode;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * This is the object mapper interface. Its implementation should handle conversion between configuration nodes and
 * fields annotated with {@link Setting} in objects.
 *
 * Values in the node not used by the mapped object should be preserved.
 *
 * @param <T> The type to work with
 */
public interface ObjectMapper<T> {

    /**
     * Create a new object mapper that can work with objects of the given class using the
     * {@link DefaultObjectMapperFactory}.
     *
     * @param clazz The type of object
     * @param <T> The type
     * @return An appropriate object mapper instance. May be shared with other users.
     * @throws ObjectMappingException If invalid annotated fields are presented
     */
    @SuppressWarnings("unchecked")
    static <T> ObjectMapper<T> forClass(@NonNull Class<T> clazz) throws ObjectMappingException {
        return DefaultObjectMapperFactory.getInstance().getMapper(clazz);
    }

    /**
     * Creates a new object mapper bound to the given object.
     *
     * @param obj The object
     * @param <T> The object type
     * @return An appropriate object mapper instance.
     * @throws ObjectMappingException
     */
    @SuppressWarnings("unchecked")
    static <T> BoundInstance forObject(@NonNull T obj) throws ObjectMappingException {
        Preconditions.checkNotNull(obj);
        return forClass((Class<T>) obj.getClass()).bind(obj);
    }

    /**
     * Returns whether this object mapper can create new object instances. This may be
     * false if the provided class has no zero-argument constructors.
     *
     * @return Whether new object instances can be created
     */
    boolean canCreateInstances();

    /**
     * Return a view on this mapper that is bound to a single object instance
     *
     * @param instance The instance to bind to
     * @return A view referencing this mapper and the bound instance
     */
    BoundInstance<T> bind(T instance);

    /**
     * Returns a view on this mapper that is bound to a newly created object instance
     *
     * @see #bind(Object)
     * @return Bound mapper attached to a new object instance
     * @throws ObjectMappingException If the object could not be constructed correctly
     */
    BoundInstance<T> bindToNew() throws ObjectMappingException;

    Class<T> getMappedType();

    /**
     * Represents an object mapper bound to a certain instance of the object
     */
    interface BoundInstance<T> {

        /**
         * Populate the annotated fields in a pre-created object
         *
         * @param source The source to get data from
         * @return The object provided, for easier chaining
         * @throws ObjectMappingException If an error occurs while populating data
         */
        T populate(ConfigurationNode source) throws ObjectMappingException;

        /**
         * Serialize the data contained in annotated fields to the configuration node.
         *
         * @param target The target node to serialize to
         * @throws ObjectMappingException if serialization was not possible due to some error.
         */
        void serialize(ConfigurationNode target) throws ObjectMappingException;

        /**
         * Return the instance this mapper is bound to.
         *
         * @return The active instance
         */
        Object getInstance();
    }
}
