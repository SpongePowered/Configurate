/**
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
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This is the object mapper. It handles conversion between configuration nodes and
 * fields annotated with {@link Setting} in objects.
 *
 * @param <T> The type to work with
 */
public class ObjectMapper<T> {
    private final Class<T> clazz;
    private final Constructor<T> constructor;
    private final List<Map.Entry<String[], FieldData>> cachedFields = new ArrayList<>();

    /**
     * Holder for field-specific information
     */
    private static class FieldData {
        private final Field field;
        private final TypeToken<?> fieldType;
        private final String comment;
        private TypeSerializer fieldSerializer;

        public FieldData(Field field, String comment) throws ObjectMappingException {
            this.field = field;
            this.comment = comment;
            this.fieldType = TypeToken.of(field.getGenericType());
            this.fieldSerializer = TypeSerializers.getSerializer(fieldType);
            if (this.fieldSerializer == null) {
                throw new ObjectMappingException("No serializer available for field " + field.getName() + " of type " + fieldType);
            }
        }

        public void deserializeFrom(Object instance, ConfigurationNode node) throws ObjectMappingException {
            Object newVal = this.fieldSerializer.deserialize(this.fieldType, node);
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

        public void serializeTo(Object instance, ConfigurationNode node) throws ObjectMappingException {
            try {
                this.fieldSerializer.serialize(this.fieldType, this.field.get(instance), node);
                if (node instanceof CommentedConfigurationNode && this.comment != null && !this.comment.isEmpty()) {
                    ((CommentedConfigurationNode) node).setComment(this.comment);
                }
            } catch (IllegalAccessException e) {
                throw new ObjectMappingException("Unable to serialize field " + field.getName(), e);
            }

        }
    }

    /**
     * Create a new object mapper of a given type
     *
     * @param clazz The type this object mapper will work with
     * @throws ObjectMappingException if the provided class is in someway invalid
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
            collectFields(collectClass);
        } while (!(collectClass = collectClass.getSuperclass()).equals(Object.class));
    }

    private void collectFields(Class<? super T> clazz) throws ObjectMappingException {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Setting.class)) {
                Setting setting = field.getAnnotation(Setting.class);
                String[] path = setting.value();
                FieldData data = new FieldData(field, setting.comment());
                field.setAccessible(true);
                cachedFields.add(Maps.immutableEntry(path, data));
            }
        }

    }

    /**
     * Create a new object mapper that can work with objects of the given class
     *
     * @param clazz The type of object
     * @param <T> The type
     * @return An appropriate object mapper instance. May be shared with other users.
     * @throws ObjectMappingException If invalid annotated fields are presented
     */
    public static <T> ObjectMapper<T> mapperForClass(Class<T> clazz) throws ObjectMappingException {
        Preconditions.checkNotNull(clazz);
        return new ObjectMapper<>(clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T> ObjectMapper<T> mapperForObject(T obj) throws ObjectMappingException {
        Preconditions.checkNotNull(obj);
        return new ObjectMapper<>((Class) obj.getClass());
    }

    /**
     * Populate the annotated fields in a pre-created object
     * @param target The target object to populate fields in
     * @param source The source to get data from
     * @return The object provided, for easier chaining
     * @throws ObjectMappingException If an error occurs while populating data
     */
    public T populateObject(T target, ConfigurationNode source) throws ObjectMappingException {
        for (Map.Entry<String[], FieldData> ent : cachedFields) {
            ConfigurationNode node = source.getNode((Object[]) ent.getKey());
            ent.getValue().deserializeFrom(target, node);
        }
        return target;
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
     * Creates a new instance of this object mapper's type and populates it with data from the given configuration node
     *
     * @param source The node to source data from
     * @return The newly created object
     * @throws ObjectMappingException if any errors occurred along the way.
     */
    public T newInstance(ConfigurationNode source) throws ObjectMappingException {
        return populateObject(constructObject(), source);
    }

    /**
     * Serialize the data contained in annotated fields to the configuration node.
     *
     * @param object The object to serialize
     * @param target The target node to serialize to
     * @throws ObjectMappingException if serialization was not possible due to some error.
     */
    public void serializeObject(T object, ConfigurationNode target) throws ObjectMappingException {
        for (Map.Entry<String[], FieldData> ent : cachedFields) {
            ConfigurationNode node = target.getNode((Object[]) ent.getKey());
            ent.getValue().serializeTo(object, node);
        }
    }

}
