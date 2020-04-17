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
package ninja.leaping.configurate.objectmapping.serialize;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Modifier;

class AnnotatedObjectSerializer implements TypeSerializer<Object> {
    public static final String CLASS_KEY = "__class__";

    @Override
    public Object deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws ObjectMappingException {
        TypeToken<?> clazz = getInstantiableType(type, value.getNode(CLASS_KEY).getString());
        return value.getOptions().getObjectMapperFactory().getMapper(clazz).bindToNew().populate(value);
    }

    private TypeToken<?> getInstantiableType(TypeToken<?> type, String configuredName) throws ObjectMappingException {
        TypeToken<?> retClass;
        Class<?> rawType = type.getRawType();
        if (rawType.isInterface() || Modifier.isAbstract(rawType.getModifiers())) {
            if (configuredName == null) {
                throw new ObjectMappingException("No available configured type for instances of " + type);
            } else {
                try {
                    retClass = TypeToken.of(Class.forName(configuredName));
                } catch (ClassNotFoundException e) {
                    throw new ObjectMappingException("Unknown class of object " + configuredName, e);
                }
                if (!retClass.isSubtypeOf(type)) {
                    throw new ObjectMappingException("Configured type " + configuredName + " does not extend "
                            + rawType.getCanonicalName());
                }
            }
        } else {
            retClass = type;
        }
        return retClass;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void serialize(@NonNull TypeToken<?> type, @Nullable Object obj, @NonNull ConfigurationNode value) throws ObjectMappingException {
        if (obj == null) {
            ConfigurationNode clazz = value.getNode(CLASS_KEY);
            value.setValue(null);
            if (!clazz.isVirtual()) {
                value.getNode(CLASS_KEY).setValue(clazz);
            }
            return;
        }
        Class<?> rawType = type.getRawType();
        ObjectMapper<?> mapper;
        if (rawType.isInterface() || Modifier.isAbstract(rawType.getModifiers())) {
            // serialize obj's concrete type rather than the interface/abstract class
            value.getNode(CLASS_KEY).setValue(obj.getClass().getName());
            mapper = value.getOptions().getObjectMapperFactory().getMapper((obj.getClass()));
        } else {
            mapper = value.getOptions().getObjectMapperFactory().getMapper(type);
        }
        ((ObjectMapper<Object>) mapper).bind(obj).serialize(value);
    }
}
