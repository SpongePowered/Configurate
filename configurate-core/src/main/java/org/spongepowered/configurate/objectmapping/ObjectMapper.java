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

import com.google.common.reflect.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.configurate.objectmapping.serialize.TypeSerializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * This is the object mapper. It handles conversion between configuration nodes and
 * fields annotated with {@link Setting} in objects.
 *
 * Values in the node not used by the mapped object will be preserved.
 *
 * @param <T> The type to work with
 */
public class ObjectMapper<T> {
    private final Class<T> clazz;
    private final Constructor<T> constructor;
    private final Map<String, FieldData> cachedFields = new LinkedHashMap<>();


    /**
     * Create a new object mapper that can work with objects of the given class using the
     * {@link DefaultObjectMapperFactory}.
     *
     * @param clazz The type of object
     * @param <T> The type
     * @return An appropriate object mapper instance. May be shared with other users.
     * @throws ObjectMappingException If invalid annotated fields are presented
     */
    public static <T> ObjectMapper<T> forClass(@NonNull Class<T> clazz) throws ObjectMappingException {
        return DefaultObjectMapperFactory.getInstance().getMapper(clazz);
    }

    /**
     * Creates a new object mapper bound to the given object.
     *
     * @param obj The object
     * @param <T> The object type
     * @return An appropriate object mapper instance.
     * @throws ObjectMappingException when an object is provided that is not suitable for object mapping.
     *                              Reasons may include but are not limited to:
     *                              <ul>
     *                                  <li>Not annotated with {@link org.spongepowered.configurate.objectmapping.serialize.ConfigSerializable} annotation</li>
     *                                  <li>Invalid field types</li>
     *                              </ul>
     */
    @SuppressWarnings("unchecked")
    public static <T> ObjectMapper<T>.BoundInstance forObject(@NonNull T obj) throws ObjectMappingException {
        return forClass((Class<T>) requireNonNull(obj).getClass()).bind(obj);
    }

    /**
     * Holder for field-specific information
     */
    protected static class FieldData {
        private final Field field;
        private final TypeToken<?> fieldType;
        private final String comment;

        public FieldData(Field field, String comment) {
            this.field = field;
            this.comment = comment;
            this.fieldType = TypeToken.of(field.getGenericType());
        }

        public <Node extends ConfigurationNode<Node>> void deserializeFrom(Object instance, Node node) throws ObjectMappingException {
            TypeSerializer<?> serial = node.getOptions().getSerializers().get(this.fieldType);
            if (serial == null) {
                throw new ObjectMappingException("No TypeSerializer found for field " + field.getName() + " of type "
                        + this.fieldType);
            }
            Object newVal = node.isVirtual() ? null : serial.deserialize(this.fieldType, node);
            try {
                if (newVal == null) {
                    Object existingVal = field.get(instance);
                    if (existingVal != null) {
                        serializeTo(instance, node);
                    }
                } else {
                    field.set(instance, newVal);
                }
            } catch (IllegalAccessException e) {
                throw new ObjectMappingException("Unable to deserialize field " + field.getName(), e);
            }
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        public void serializeTo(Object instance, ConfigurationNode<?> node) throws ObjectMappingException {
            try {
                Object fieldVal = this.field.get(instance);
                if (fieldVal == null) {
                    node.setValue(null);
                } else {
                    TypeSerializer serial = node.getOptions().getSerializers().get(this.fieldType);
                    if (serial == null) {
                        throw new ObjectMappingException("No TypeSerializer found for field " + field.getName() + " of type " + this.fieldType);
                    }
                    serial.serialize(this.fieldType, fieldVal, node);
                }

                if (node instanceof CommentedConfigurationNode && this.comment != null && !this.comment.isEmpty()) {
                    CommentedConfigurationNode<?> commentNode = ((CommentedConfigurationNode<?>) node);
                    if (!commentNode.getComment().isPresent()) {
                        commentNode.setComment(this.comment);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new ObjectMappingException("Unable to serialize field " + field.getName(), e);
            }
        }
    }

    /**
     * Represents an object mapper bound to a certain instance of the object
     */
    public class BoundInstance {
        private final T boundInstance;

        protected BoundInstance(T boundInstance) {
            this.boundInstance = boundInstance;
        }

        /**
         * Populate the annotated fields in a pre-created object
         *
         * @param <Node> The type of node being populated
         * @param source The source to get data from
         * @return The object provided, for easier chaining
         * @throws ObjectMappingException If an error occurs while populating data
         */
        public <Node extends ConfigurationNode<Node>> T populate(Node source) throws ObjectMappingException {
            for (Map.Entry<String, FieldData> ent : cachedFields.entrySet()) {
                Node node = source.getNode(ent.getKey());
                ent.getValue().deserializeFrom(boundInstance, node);
            }
            return boundInstance;
        }

        /**
         * Serialize the data contained in annotated fields to the configuration node.
         *
         * @param target The target node to serialize to
         * @throws ObjectMappingException if serialization was not possible due to some error.
         */
        public void serialize(ConfigurationNode<?> target) throws ObjectMappingException {
            for (Map.Entry<String, FieldData> ent : cachedFields.entrySet()) {
                ConfigurationNode<?> node = target.getNode(ent.getKey());
                ent.getValue().serializeTo(boundInstance, node);
            }
        }

        /**
         * Return the instance this mapper is bound to.
         *
         * @return The active instance
         */
        public T getInstance() {
            return boundInstance;
        }
    }

    /**
     * Create a new object mapper of a given type
     *
     * @param clazz The type this object mapper will work with
     * @throws ObjectMappingException When errors occur discovering fields in the class
     */
    protected ObjectMapper(Class<T> clazz) throws ObjectMappingException {
        this.clazz = clazz;
        Constructor<T> constructor = null;
        try {
            constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
        } catch (NoSuchMethodException ignore) {
        }
        this.constructor = constructor;
        Class<? super T> collectClass = clazz;
        do {
            collectFields(cachedFields, collectClass);
        } while (!(collectClass = collectClass.getSuperclass()).equals(Object.class));
    }

    protected void collectFields(Map<String, FieldData> cachedFields, Class<? super T> clazz) throws ObjectMappingException {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Setting.class)) {
                Setting setting = field.getAnnotation(Setting.class);
                String path = setting.value();
                if (path.isEmpty()) {
                    path = field.getName();
                }

                FieldData data = new FieldData(field, setting.comment());
                field.setAccessible(true);
                if (!cachedFields.containsKey(path)) {
                    cachedFields.put(path, data);
                }
            }
        }
    }

    /**
     * Create a new instance of an object of the appropriate type. This method is not
     * responsible for any population.
     *
     * @return The new object instance
     * @throws ObjectMappingException If constructing a new instance was not possible
     */
    protected T constructObject() throws ObjectMappingException {
        if (constructor == null) {
            throw new ObjectMappingException("No zero-arg constructor is available for class " + clazz + " but is required to construct new instances!");
        }
        try {
            return constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ObjectMappingException("Unable to create instance of target class " + clazz, e);
        }
    }

    /**
     * Returns whether this object mapper can create new object instances. This may be
     * false if the provided class has no zero-argument constructors.
     *
     * @return Whether new object instances can be created
     */
    public boolean canCreateInstances() {
        return constructor != null;
    }

    /**
     * Return a view on this mapper that is bound to a single object instance
     *
     * @param instance The instance to bind to
     * @return A view referencing this mapper and the bound instance
     */
    public BoundInstance bind(T instance) {
        return new BoundInstance(instance);
    }

    /**
     * Returns a view on this mapper that is bound to a newly created object instance
     *
     * @see #bind(Object)
     * @return Bound mapper attached to a new object instance
     * @throws ObjectMappingException If the object could not be constructed correctly
     */
    public BoundInstance bindToNew() throws ObjectMappingException {
        return new BoundInstance(constructObject());
    }

    public Class<T> getMappedType() {
        return this.clazz;
    }
}
