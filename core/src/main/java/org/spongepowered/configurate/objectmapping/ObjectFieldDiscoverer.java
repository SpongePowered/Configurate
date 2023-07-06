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
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.util.CheckedBiFunction;
import org.spongepowered.configurate.util.CheckedFunction;
import org.spongepowered.configurate.util.Types;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

class ObjectFieldDiscoverer implements FieldDiscoverer<Map<ObjectFieldDiscoverer.FieldHandles, Object>> {

    private static final MethodHandles.Lookup OWN_LOOKUP = MethodHandles.lookup();

    static final ObjectFieldDiscoverer EMPTY_CONSTRUCTOR_INSTANCE = new ObjectFieldDiscoverer((type, lookup) -> {
        try {
            final MethodHandle constructor;
            final Class<?> erased = erase(type.getType());
            if (lookup == null) { // legacy
                final Constructor<?> construct = erased.getDeclaredConstructor();
                construct.setAccessible(true);
                constructor = OWN_LOOKUP.unreflectConstructor(construct);
            } else {
                constructor = LookupShim.privateLookupIn(erased, lookup)
                    .findConstructor(erased, MethodType.methodType(void.class));
            }

            return () -> {
                try {
                    return constructor.invoke();
                } catch (final RuntimeException ex) {
                    throw ex;
                } catch (final Throwable thr) {
                    throw new RuntimeException(thr);
                }
            };
        } catch (final NoSuchMethodException | IllegalAccessException e) {
            return null;
        }
    }, "Objects must have a zero-argument constructor to be able to create new instances", false);

    private final CheckedBiFunction<
        AnnotatedType,
        MethodHandles.@Nullable Lookup,
        @Nullable Supplier<Object>,
        SerializationException
        > instanceFactory;
    private final String instanceUnavailableErrorMessage;
    private final boolean requiresInstanceCreation;

    ObjectFieldDiscoverer(
        final CheckedFunction<AnnotatedType, @Nullable Supplier<Object>, SerializationException> instanceFactory,
        final @Nullable String instanceUnavailableErrorMessage,
        final boolean requiresInstanceCreation
    ) {
        this((type, lookup) -> instanceFactory.apply(type), instanceUnavailableErrorMessage, requiresInstanceCreation);
    }

    ObjectFieldDiscoverer(
        final CheckedBiFunction<AnnotatedType, MethodHandles.@Nullable Lookup, @Nullable Supplier<Object>, SerializationException> instanceFactory,
        final @Nullable String instanceUnavailableErrorMessage,
        final boolean requiresInstanceCreation
    ) {
        this.instanceFactory = instanceFactory;
        if (instanceUnavailableErrorMessage == null) {
            this.instanceUnavailableErrorMessage = "Unable to create instances for this type!";
        } else {
            this.instanceUnavailableErrorMessage = instanceUnavailableErrorMessage;
        }
        this.requiresInstanceCreation = requiresInstanceCreation;
    }

    @Override
    public <V> @Nullable InstanceFactory<Map<FieldHandles, Object>> discover(
        final AnnotatedType target,
        final FieldCollector<Map<FieldHandles, Object>, V> collector,
        final MethodHandles.@Nullable Lookup lookup
    ) throws SerializationException {
        final Class<?> clazz = erase(target.getType());
        if (clazz.isInterface()) {
            throw new SerializationException(target.getType(), "ObjectMapper can only work with concrete types");
        }

        final @Nullable Supplier<Object> maker = this.instanceFactory.apply(target, lookup);
        if (maker == null && this.requiresInstanceCreation) {
            return null;
        }

        AnnotatedType collectType = target;
        Class<?> collectClass = clazz;
        while (true) {
            collectFields(collectType, collector, lookup);
            collectClass = collectClass.getSuperclass();
            if (collectClass.equals(Object.class)) {
                break;
            }
            collectType = getExactSuperType(collectType, collectClass);
        }

        return new MutableInstanceFactory<Map<FieldHandles, Object>>() {

            @Override
            public Map<FieldHandles, Object> begin() {
                return new HashMap<>();
            }

            @Override
            public void complete(final Object instance, final Map<FieldHandles, Object> intermediate) throws SerializationException {
                for (final Map.Entry<FieldHandles, Object> entry : intermediate.entrySet()) {
                    try {
                        // Handle implicit field initialization by detecting any existing information in the object
                        if (entry.getValue() instanceof ImplicitProvider) {
                            final @Nullable Object implicit = ((ImplicitProvider) entry.getValue()).provider.get();
                            if (implicit != null) {
                                if (entry.getKey().getter.invoke(instance) == null) {
                                    entry.getKey().setter.invoke(instance, implicit);
                                }
                            }
                        } else {
                            entry.getKey().setter.invoke(instance, entry.getValue());
                        }
                    } catch (final IllegalAccessException e) {
                        throw new SerializationException(target.getType(), e);
                    } catch (final Throwable thr) {
                        throw new SerializationException(target.getType(), "An unexpected error occurred while trying to set a field", thr);
                    }
                }
            }

            @Override
            public Object complete(final Map<FieldHandles, Object> intermediate) throws SerializationException {
                final @Nullable Object instance = maker == null ? null : maker.get();
                if (instance == null) {
                    throw new SerializationException(target.getType(), ObjectFieldDiscoverer.this.instanceUnavailableErrorMessage);
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

    private <V> void collectFields(
        final AnnotatedType clazz,
        final FieldCollector<Map<FieldHandles, Object>, V> fieldMaker,
        final MethodHandles.@Nullable Lookup lookup
    ) throws SerializationException {
        for (final Field field : erase(clazz.getType()).getDeclaredFields()) {
            if ((field.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT)) != 0) {
                continue;
            }

            final AnnotatedType fieldType = getFieldType(field, clazz);
            final FieldData.Deserializer<Map<FieldHandles, Object>> deserializer;
            final CheckedFunction<V, @Nullable Object, Exception> serializer;
            final FieldHandles handles;
            try {
                if (lookup != null) {
                    handles = new FieldHandles(field, lookup);
                } else {
                    handles = new FieldHandles(field);
                }
            } catch (final IllegalAccessException ex) {
                throw new SerializationException(fieldType, ex);
            }
            deserializer = (intermediate, val, implicitProvider) -> {
                if (val != null) {
                    intermediate.put(handles, val);
                } else {
                    intermediate.put(handles, new ImplicitProvider(implicitProvider));
                }
            };
            serializer = inst -> {
                try {
                    return handles.getter.invoke(inst);
                } catch (final Exception ex) {
                    throw ex;
                } catch (final Throwable thr) {
                    throw new Exception(thr);
                }
            };
            fieldMaker.accept(
                field.getName(),
                fieldType,
                Types.combinedAnnotations(fieldType, field),
                deserializer,
                serializer
            );
        }
    }

    static class FieldHandles {
        final MethodHandle getter;
        final MethodHandle setter;

        FieldHandles(final Field field) throws IllegalAccessException {
            field.setAccessible(true);
            final MethodHandles.Lookup lookup = MethodHandles.publicLookup();

            this.getter = lookup.unreflectGetter(field);
            this.setter = lookup.unreflectSetter(field);
        }

        FieldHandles(final Field field, final MethodHandles.Lookup lookup) throws IllegalAccessException {
            this.getter = lookup.unreflectGetter(field);
            this.setter = lookup.unreflectSetter(field);
        }
    }

    static class ImplicitProvider {

        final Supplier<Object> provider;

        ImplicitProvider(final Supplier<Object> provider) {
            this.provider = provider;
        }

    }

}
