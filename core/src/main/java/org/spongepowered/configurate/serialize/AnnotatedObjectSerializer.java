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

import com.google.common.reflect.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ScopedConfigurationNode;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;

import java.lang.reflect.Modifier;
import java.util.function.Predicate;

class AnnotatedObjectSerializer implements TypeSerializer<Object> {

    public static final String CLASS_KEY = "__class__";

    static Predicate<TypeToken<Object>> predicate() {
        return it -> it.getRawType().isAnnotationPresent(ConfigSerializable.class);
    }

    @Override
    public <N extends ScopedConfigurationNode<N>> Object deserialize(final TypeToken<?> type, final N node) throws ObjectMappingException {
        final TypeToken<?> clazz = getInstantiableType(type, node.getNode(CLASS_KEY).getString());
        return node.getOptions().getObjectMapperFactory().getMapper(clazz).bindToNew().populate(node);
    }

    private TypeToken<?> getInstantiableType(final TypeToken<?> type, final @Nullable String configuredName) throws ObjectMappingException {
        final TypeToken<?> retClass;
        final Class<?> rawType = type.getRawType();
        if (rawType.isInterface() || Modifier.isAbstract(rawType.getModifiers())) {
            if (configuredName == null) {
                throw new ObjectMappingException("No available configured type for instances of " + type);
            } else {
                try {
                    retClass = TypeToken.of(Class.forName(configuredName));
                } catch (final ClassNotFoundException e) {
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
    public <N extends ScopedConfigurationNode<N>> void serialize(final TypeToken<?> type,
            final @Nullable Object obj, final N node) throws ObjectMappingException {
        if (obj == null) {
            final N clazz = node.getNode(CLASS_KEY);
            node.setValue(null);
            if (!clazz.isVirtual()) {
                node.getNode(CLASS_KEY).setValue(clazz);
            }
            return;
        }
        final Class<?> rawType = type.getRawType();
        final ObjectMapper<?> mapper;
        if (rawType.isInterface() || Modifier.isAbstract(rawType.getModifiers())) {
            // serialize obj's concrete type rather than the interface/abstract class
            node.getNode(CLASS_KEY).setValue(obj.getClass().getName());
            mapper = node.getOptions().getObjectMapperFactory().getMapper(obj.getClass());
        } else {
            mapper = node.getOptions().getObjectMapperFactory().getMapper(type);
        }
        ((ObjectMapper<Object>) mapper).bind(obj).serialize(node);
    }

}
