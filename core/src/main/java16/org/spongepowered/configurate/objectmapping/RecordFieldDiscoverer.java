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
import static io.leangen.geantyref.GenericTypeReflector.resolveExactType;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.util.Types;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;

/**
 * Discovers fields in J16+ {@code Record}s.
 */
final class RecordFieldDiscoverer implements FieldDiscoverer<@Nullable Object[]> {

    static final RecordFieldDiscoverer INSTANCE = new RecordFieldDiscoverer();

    private RecordFieldDiscoverer() {
    }

    /**
     * Get data from a {@code record}.
     *
     * <p>These classes are quite a bit more limited than ordinary classes,
     * so we don't have to worry about traversing hierarchy.</p>.
     *
     * @param target containing record
     * @return an instance factory if this class is a record
     */
    @Override
    public <V> @Nullable InstanceFactory<@Nullable Object[]> discover(final AnnotatedType target,
            final FieldCollector<@Nullable Object[], V> collector) throws SerializationException {
        final Class<?> clazz = erase(target.getType());
        if (!clazz.isRecord()) {
            return null;
        }
        try {
            final RecordComponent[] recordComponents = clazz.getRecordComponents();
            final Class<?>[] constructorParams = new Class<?>[recordComponents.length];
            for (int i = 0, recordComponentsLength = recordComponents.length; i < recordComponentsLength; i++) {
                // each component is itself annotatable, plus attached backing field and accessor method, so we have to get them all
                final RecordComponent component = recordComponents[i];
                final Method accessor = component.getAccessor();
                accessor.setAccessible(true);

                final String name = component.getName();
                final AnnotatedType genericType = component.getAnnotatedType();
                constructorParams[i] = erase(genericType.getType()); // to add to the canonical constructor

                final Field backingField = clazz.getDeclaredField(name);
                backingField.setAccessible(true);

                // Then we put everything together: resolve the type, calculate annotations, and submit a field
                final AnnotatedType resolvedType = resolveExactType(genericType, target);
                final AnnotatedElement annotationContainer = Types.combinedAnnotations(component, backingField, accessor);
                final int targetIdx = i;
                collector.accept(name, resolvedType, annotationContainer,
                    (intermediate, el, implicitSupplier) -> {
                        if (el != null) {
                            intermediate[targetIdx] = el;
                        } else {
                            intermediate[targetIdx] = implicitSupplier.get();
                        }
                    }, accessor::invoke
                );
            }

            // canonical constructor, which we'll use to make new instances
            final Constructor<?> clazzConstructor = clazz.getDeclaredConstructor(constructorParams);
            clazzConstructor.setAccessible(true);

            return new InstanceFactory<>() {
                @Override
                public Object[] begin() {
                    return new Object[recordComponents.length];
                }

                @Override
                public Object complete(final @Nullable Object[] intermediate) throws SerializationException {
                    // Primitive values cannot be null, but we must pass a value for every parameter.
                    for (int i = 0, length = intermediate.length; i < length; ++i) {
                        if (intermediate[i] == null && constructorParams[i].isPrimitive()) {
                            intermediate[i] = Types.defaultValue(constructorParams[i]);
                        }
                    }

                    try {
                        return clazzConstructor.newInstance(intermediate);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        throw new SerializationException(target.getType(), e);
                    }
                }

                @Override
                public boolean canCreateInstances() {
                    return true;
                }
            };
        } catch (final NoSuchFieldException | NoSuchMethodException ex) {
            throw new SerializationException(target.getType(), "Record class did not have fields and accessors aligning specification", ex);
        }
    }

}
