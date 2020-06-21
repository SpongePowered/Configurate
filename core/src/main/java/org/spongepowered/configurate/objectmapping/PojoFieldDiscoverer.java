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

import static io.leangen.geantyref.GenericTypeReflector.erase;
import static io.leangen.geantyref.GenericTypeReflector.getExactSuperType;
import static io.leangen.geantyref.GenericTypeReflector.getFieldType;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.util.CheckedFunction;
import org.spongepowered.configurate.util.Typing;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

class PojoFieldDiscoverer implements FieldDiscoverer<Map<Field, Object>> {

    static final PojoFieldDiscoverer EMPTY_CONSTRUCTOR_INSTANCE = new PojoFieldDiscoverer(type -> {
        try {
            final Constructor<?> constructor;
            constructor = erase(type.getType()).getDeclaredConstructor();
            constructor.setAccessible(true);
            return () -> {
                try {
                    return constructor.newInstance();
                } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            };
        } catch (final NoSuchMethodException e) {
            return null;
        }
    });

    private final CheckedFunction<AnnotatedType, @Nullable Supplier<Object>, ObjectMappingException> instanceFactory;

    PojoFieldDiscoverer(final CheckedFunction<AnnotatedType, @Nullable Supplier<Object>, ObjectMappingException> instanceFactory) {
        this.instanceFactory = instanceFactory;
    }

    @Override
    public <V> @Nullable InstanceFactory<Map<Field, Object>> discover(final AnnotatedType target,
            final FieldCollector<Map<Field, Object>, V> collector) throws ObjectMappingException {
        final Class<?> clazz = erase(target.getType());
        if (clazz.isInterface()) {
            throw new ObjectMappingException("ObjectMapper can only work with concrete types");
        }

        final @Nullable Supplier<Object> maker = this.instanceFactory.apply(target);

        AnnotatedType collectType = target;
        Class<?> collectClass = clazz;
        while (true) {
            collectFields(collectType, collector);
            collectClass = collectClass.getSuperclass();
            if (collectClass.equals(Object.class)) {
                break;
            }
            collectType = getExactSuperType(collectType, collectClass);
        }

        return new MutableInstanceFactory<Map<Field, Object>>() {

            @Override
            public Map<Field, Object> begin() {
                return new HashMap<>();
            }

            @Override
            public void complete(final Object instance, final Map<Field, Object> intermediate) throws ObjectMappingException {
                for (Map.Entry<Field, Object> entry : intermediate.entrySet()) {
                    try {
                        entry.getKey().set(instance, entry.getValue());
                    } catch (final IllegalAccessException e) {
                        throw new ObjectMappingException(e);
                    }
                }
            }

            @Override
            public Object complete(final Map<Field, Object> intermediate) throws ObjectMappingException {
                final Object instance = maker == null ? null : maker.get();
                if (instance == null) {
                    throw new ObjectMappingException("Unable to create instance with this populator");
                }
                complete(instance, intermediate);
                return instance;
            }

            @Override
            public boolean canCreateInstances() {
                return maker != null;
            }

        };
    }

    private void collectFields(final AnnotatedType clazz, final FieldCollector<Map<Field, Object>, ?> fieldMaker) {
        for (Field field : erase(clazz.getType()).getDeclaredFields()) {
            if ((field.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT)) != 0) {
                continue;
            }

            field.setAccessible(true);
            final AnnotatedType fieldType = getFieldType(field, clazz);
            fieldMaker.accept(field.getName(), fieldType, Typing.combinedAnnotations(fieldType, field),
                (intermediate, val) -> {
                    if (val != null) {
                        intermediate.put(field, val);
                    }
                }, field::get);
        }
    }

}
