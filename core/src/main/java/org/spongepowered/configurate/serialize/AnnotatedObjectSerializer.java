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
package org.spongepowered.configurate.serialize;

import static io.leangen.geantyref.GenericTypeReflector.erase;

import io.leangen.geantyref.GenericTypeReflector;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.function.Predicate;

final class AnnotatedObjectSerializer implements TypeSerializer<Object> {

    public static final String CLASS_KEY = "__class__";

    static Predicate<Type> predicate() {
        return it -> GenericTypeReflector.annotate(it).isAnnotationPresent(ConfigSerializable.class);
    }

    @Override
    public Object deserialize(final Type type, final ConfigurationNode node) throws ObjectMappingException {
        final Type clazz = getInstantiableType(type, node.getNode(CLASS_KEY).getString());
        return node.getOptions().getObjectMapperFactory().get(clazz).load(node);
    }

    private Type getInstantiableType(final Type type, final @Nullable String configuredName) throws ObjectMappingException {
        final Type retClass;
        final Class<?> rawType = erase(type);
        if (rawType.isInterface() || Modifier.isAbstract(rawType.getModifiers())) {
            if (configuredName == null) {
                throw new ObjectMappingException("No available configured type for instances of " + type);
            } else {
                try {
                    retClass = Class.forName(configuredName);
                } catch (final ClassNotFoundException e) {
                    throw new ObjectMappingException("Unknown class of object " + configuredName, e);
                }
                if (!GenericTypeReflector.isSuperType(type, retClass)) {
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
    public void serialize(final Type type, final @Nullable Object obj, final ConfigurationNode node) throws ObjectMappingException {
        if (obj == null) {
            final ConfigurationNode clazz = node.getNode(CLASS_KEY);
            node.setValue(null);
            if (!clazz.isVirtual()) {
                node.getNode(CLASS_KEY).setValue(clazz);
            }
            return;
        }
        final Class<?> rawType = erase(type);
        final ObjectMapper<?> mapper;
        if (rawType.isInterface() || Modifier.isAbstract(rawType.getModifiers())) {
            // serialize obj's concrete type rather than the interface/abstract class
            node.getNode(CLASS_KEY).setValue(obj.getClass().getName());
            mapper = node.getOptions().getObjectMapperFactory().get(obj.getClass());
        } else {
            mapper = node.getOptions().getObjectMapperFactory().get(type);
        }
        ((ObjectMapper<Object>) mapper).save(obj, node);
    }

}
