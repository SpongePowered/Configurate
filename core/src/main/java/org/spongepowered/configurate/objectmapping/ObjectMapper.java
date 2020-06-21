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

import io.leangen.geantyref.TypeToken;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.meta.Constraint;
import org.spongepowered.configurate.objectmapping.meta.NodeResolver;
import org.spongepowered.configurate.objectmapping.meta.Processor;
import org.spongepowered.configurate.util.NamingScheme;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

/**
 * A mapper that converts between configuration nodes and Java objects.
 *
 * <p>Mapped objects fall into one of several types: timmutable</p>
 * @param <V> mapped type
 */
public interface ObjectMapper<V> {

    /**
     * Get the default object mapper factory instance.
     *
     * <p>This factory has the following characteristics:</p>
     * <ul>
     *     <li>can resolve fields in empty-constructor objects and Records</li>
     *     <li>will try to resolve any field in objects</li>
     *     <li>supports {@link org.spongepowered.configurate.objectmapping.meta.NodeKey} and
     *     {@link org.spongepowered.configurate.objectmapping.meta.Setting} annotations
     *     for customizing node resolution</li>
     *     <li>uses the {@link org.spongepowered.configurate.util.NamingSchemes#LOWER_CASE_DASHED}
     *     naming scheme for other nodes</li>
     *     <li>supports unlocalized {@link org.spongepowered.configurate.objectmapping.meta.Matches},
     *     and {@link org.spongepowered.configurate.objectmapping.meta.Required}
     *     constraints</li>
     *     <li>processes {@link org.spongepowered.configurate.objectmapping.meta.Comment}
     *     annotations</li>
     * </ul>
     *
     * @return default factory
     */
    static Factory factory() {
        return ObjectMapperFactoryImpl.INSTANCE;
    }

    /**
     * Create an empty builder.
     *
     * <p>This applies none of the standard formats, processors, constraints or
     * resolvers. Unless you want to do something particularly specialized,
     * you should probably be using {@link #factoryBuilder()}.</p>
     *
     * @return new empty builder
     */
    static Factory.Builder emptyFactoryBuilder() {
        return new ObjectMapperFactoryImpl.Builder();
    }

    /**
     * Create a builder populated with default settings.
     *
     * <p>This builder is prepared to allow overriding any of the default
     * object mapper features.</p>
     *
     * @return new builder
     * @see #factory() for a description of the default settings
     */
    static Factory.Builder factoryBuilder() {
        return ObjectMapperFactoryImpl.defaultBuilder();
    }

    /**
     * Create a new object instance.
     *
     * @param source object source
     * @return new instance
     * @throws ObjectMappingException if any invalid data is present. Loading is
     *      done in stages, so any deserialization errors will occur before
     *      anything is written to objects.
     *
     */
    V load(ConfigurationNode source) throws ObjectMappingException;

    /**
     * Write data from the provided object to the target.
     *
     * @param value value type
     * @param target destination
     * @throws ObjectMappingException if unable to fully save
     */
    void save(V value, ConfigurationNode target) throws ObjectMappingException;

    /**
     * Get the parameters that will be handled by this mapper.
     *
     * @return immutable list of fields
     */
    List<? extends FieldData<?, V>> getFields();

    /**
     * The generic type of object that this mapper instance handles.
     *
     * @return object type
     */
    Type getMappedType();

    /**
     * Get whether or not this mapper is capable of creating new instances of
     * its mapped type.
     *
     * <p>If this returns {@code false}, {@link #load(ConfigurationNode)} will
     * always fail.</p>
     *
     * @return if the mapped type can be instantiated.
     */
    boolean canCreateInstances();

    /**
     * An object mapper capable of loading data into an existing object.
     *
     * @param <V> value type
     */
    interface Mutable<V> extends ObjectMapper<V> {

        /**
         * Load data from {@code node} into an existing instance.
         *
         * @param value existing instance
         * @param node node to load from
         * @throws ObjectMappingException if unable to deserialize data
         */
        void load(V value, ConfigurationNode node) throws ObjectMappingException;
    }

    /**
     * Provider for object mappers.
     */
    interface Factory {

        /**
         * Get an object mapper for the provided type.
         *
         * <p>The provided type cannot be a <em>raw type</em>.</p>
         *
         * @param type token holding the mapped type
         * @param <V> mapped type
         * @return a mapper for the provided type
         * @throws ObjectMappingException if the type does not correspond to a
         *     mappable object
         */
        @SuppressWarnings("unchecked")
        default <V> ObjectMapper<V> get(TypeToken<V> type) throws ObjectMappingException {
            return (ObjectMapper<V>) get(type.getType());
        }

        /**
         * Get an object mapper for the unparameterized type {@code clazz}.
         *
         * <p>The provided type cannot be a <em>raw type</em>.</p>
         *
         * @param clazz class of the mapped type
         * @param <V> mapped type
         * @return a mapper for the provided type
         * @throws ObjectMappingException if the type does not correspond to a
         *     mappable object
         */
        @SuppressWarnings("unchecked")
        default <V> ObjectMapper<V> get(Class<V> clazz) throws ObjectMappingException {
            return (ObjectMapper<V>) get((Type) clazz);
        }

        /**
         * Get the object mapper for the provided type.
         *
         * <p>The provided type cannot be a <em>raw type</em>.</p>
         *
         * @param type object type.
         * @return a mapper for the provided type
         * @throws ObjectMappingException if the type does not correspond to a
         *     mappable object
         */
        ObjectMapper<?> get(Type type) throws ObjectMappingException;

        /**
         * A builder for settings to be applied to an object mapper.
         *
         * <p>In general, with multiple applicable resolvers, the one registered
         * last will take priority.</p>
         */
        interface Builder {

            /**
             * Set the naming scheme to use as a default for field names.
             *
             * <p>This can be overridden by other
             * {@link NodeResolver NodeResolvers} for specific nodes.</p>
             *
             * @param scheme naming scheme
             * @return this builder
             */
            Builder setDefaultNamingScheme(NamingScheme scheme);

            /**
             * Add a resolver that will locate a node for a field.
             *
             * @param resolver the resolver
             * @return this builder
             */
            Builder addNodeResolver(NodeResolver.Factory resolver);

            /**
             * Add a discoverer for a type of object.
             *
             * <p>Field discoverers will be tried in order until one can
             * produce the appropriate metadata.</p>
             *
             * @param discoverer field discoverer
             * @return this builder
             */
            Builder addDiscoverer(FieldDiscoverer<?> discoverer);

            /**
             * Register a {@link Processor} that will process fields after write.
             *
             * <p>Processors registered without a specific data type should be
             * able to operate on any value type.</p>
             *
             * @param definition annotation providing data
             * @param factory factory for callback function
             * @param <A> annotation type
             * @return this builder
             */
            default <A extends Annotation> Builder addProcessor(Class<A> definition, Processor.Factory<A, Object> factory) {
                return addProcessor(definition, Object.class, factory);
            }

            /**
             * Register a {@link Processor} that will process fields after write.
             *
             * <p>All value types will be tested against types normalized to
             * their boxed variants.</p>
             *
             * @param definition annotation providing data
             * @param valueType value types the processor will handle
             * @param factory factory for callback function
             * @param <A> annotation type
             * @param <T> data type
             * @return this builder
             */
            <A extends Annotation, T> Builder addProcessor(Class<A> definition, Class<T> valueType, Processor.Factory<A, T> factory);

            /**
             * Register a {@link Constraint} that will be used to validate fields.
             *
             * <p>Constraints registered without a specific data type will be
             * able to operate on any value type.</p>
             *
             * @param definition annotations providing data
             * @param factory factory for callback function
             * @param <A> annotation type
             * @return this builder
             */
            default <A extends Annotation> Builder addConstraint(Class<A> definition, Constraint.Factory<A, Object> factory) {
                return addConstraint(definition, Object.class, factory);
            }

            /**
             * Register a {@link Constraint} that will be used to validate fields.
             *
             * <p>All value types will be tested against types normalized to
             * their boxed variants.</p>
             *
             * @param definition annotations providing data
             * @param valueType value types the processor will handle
             * @param factory factory for callback function
             * @param <A> annotation type
             * @param <T> data type
             * @return this builder
             */
            <A extends Annotation, T> Builder addConstraint(Class<A> definition, Class<T> valueType, Constraint.Factory<A, T> factory);

            /**
             * Create a new factory using the current configuration.
             *
             * @return new factory instance
             */
            Factory build();

        }

    }

}
