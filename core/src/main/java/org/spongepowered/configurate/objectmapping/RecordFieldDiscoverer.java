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
import org.spongepowered.configurate.util.Typing;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Discovers fields in J14+ {@code Record}s.
 */
final class RecordFieldDiscoverer implements FieldDiscoverer<Object[]> {

    static final RecordFieldDiscoverer INSTANCE = new RecordFieldDiscoverer();

    // We access record metadata reflectively to avoid a compile-time dependency on Java 14+ (and preview features)
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final MethodHandle CLASS_IS_RECORD;
    private static final MethodHandle CLASS_GET_RECORD_COMPONENTS;
    private static final MethodHandle RECORD_COMPONENT_GET_ANNOTATED_TYPE;
    private static final MethodHandle RECORD_COMPONENT_GET_NAME;
    private static final @Nullable MethodHandle RECORD_COMPONENT_GET_ACCESSOR;

    static {
        @Nullable MethodHandle classIsRecord = null;
        @Nullable MethodHandle classGetRecordComponents = null;
        @Nullable MethodHandle recordComponentGetAnnotatedType = null;
        @Nullable MethodHandle recordComponentGetName = null;
        @Nullable MethodHandle recordComponentGetAccessor = null;
        try {
            classIsRecord = LOOKUP.findVirtual(Class.class, "isRecord", MethodType.methodType(boolean.class));
            final Class<?> recordComponent = Class.forName("java.lang.reflect.RecordComponent");
            final Class<?> recordComponentArray = Array.newInstance(recordComponent, 0).getClass();
            classGetRecordComponents = LOOKUP.findVirtual(Class.class, "getRecordComponents", MethodType.methodType(recordComponentArray));
            recordComponentGetAnnotatedType = LOOKUP.findVirtual(recordComponent, "getAnnotatedType", MethodType.methodType(AnnotatedType.class));
            recordComponentGetAccessor = LOOKUP.findVirtual(recordComponent, "getAccessor", MethodType.methodType(Method.class));
            recordComponentGetName = LOOKUP.findVirtual(recordComponent, "getName", MethodType.methodType(String.class));

        } catch (final Exception ex) {
            // ignore, not J14+
        }

        CLASS_IS_RECORD = classIsRecord;
        CLASS_GET_RECORD_COMPONENTS = classGetRecordComponents;
        RECORD_COMPONENT_GET_ANNOTATED_TYPE = recordComponentGetAnnotatedType;
        RECORD_COMPONENT_GET_NAME = recordComponentGetName;
        RECORD_COMPONENT_GET_ACCESSOR = recordComponentGetAccessor;
    }

    private RecordFieldDiscoverer() {
    }

    /**
     * Get data from a {@code record}.
     *
     * <p>These classes are quite a bit more limited than ordinary classes,
     * so we don't have to worry about traversing hierarchy.</p>.
     *
     * @param target Containing record
     * @return an instance factory if this class is a record
     */
    @Override
    public <V> @Nullable InstanceFactory<Object[]> discover(final AnnotatedType target, final FieldCollector<Object[], V> collector)
            throws ObjectMappingException {
        if (RECORD_COMPONENT_GET_ACCESSOR != null) {
            final Class<?> clazz = erase(target.getType());
            try {
                if ((boolean) CLASS_IS_RECORD.invoke(clazz)) { // clazz.isRecord()
                    final AnnotatedElement[] recordComponents =
                            (AnnotatedElement[]) CLASS_GET_RECORD_COMPONENTS.invoke(clazz); // clazz.getRecordComponents()
                    final Class<?>[] constructorParams = new Class<?>[recordComponents.length];
                    for (int i = 0, recordComponentsLength = recordComponents.length; i < recordComponentsLength; i++) {
                        // each component is itself annotatable, plus attached backing field and accessor method, so we have to get them all
                        final AnnotatedElement component = recordComponents[i];
                        final Method accessor = (Method) RECORD_COMPONENT_GET_ACCESSOR.invoke(component); // component.getAccessor()
                        accessor.setAccessible(true);

                        final String name = (String) RECORD_COMPONENT_GET_NAME.invoke(component); // component.getName()
                        final AnnotatedType genericType = (AnnotatedType) RECORD_COMPONENT_GET_ANNOTATED_TYPE.invoke(component); // .getAnnotatedType
                        constructorParams[i] = erase(genericType.getType()); // to add to the canonical constructor

                        final Field backingField = clazz.getDeclaredField(name);
                        backingField.setAccessible(true);

                        // Then we put everything together: resolve the type, calculate annotations, and submit a field
                        final AnnotatedType resolvedType = resolveExactType(genericType, target);
                        final AnnotatedElement annotationContainer = Typing.combinedAnnotations(component, backingField, accessor);
                        final int targetIdx = i;
                        collector.accept(name, resolvedType, annotationContainer,
                            (intermediate, el) -> intermediate[targetIdx] = el, accessor::invoke);
                    }

                    // canonical constructor, which we'll use to make new instances
                    final Constructor<?> clazzConstructor = clazz.getDeclaredConstructor(constructorParams);
                    clazzConstructor.setAccessible(true);

                    return new InstanceFactory<Object[]>() {
                        @Override
                        public Object[] begin() {
                            return new Object[recordComponents.length];
                        }

                        @Override public Object complete(final Object[] intermediate) throws ObjectMappingException {
                            try {
                                return clazzConstructor.newInstance(intermediate);
                            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                                throw new ObjectMappingException(e);
                            }
                        }

                        @Override public boolean canCreateInstances() {
                            return true;
                        }
                    };
                }
            } catch (final ObjectMappingException ex) {
                throw ex;
            } catch (final Throwable ex) {
                // suppress, we just won't handle as a record
            }
        }
        return null;
    }

}
