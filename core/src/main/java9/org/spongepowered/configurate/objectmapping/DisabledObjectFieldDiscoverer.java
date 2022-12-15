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
import java.lang.invoke.VarHandle;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

class DisabledObjectFieldDiscoverer implements FieldDiscoverer<Map<VarHandle, Object>> {

    private static final MethodHandles.Lookup OWN_LOOKUP = MethodHandles.lookup();

    static final ObjectFieldDiscoverer EMPTY_CONSTRUCTOR_INSTANCE = new ObjectFieldDiscoverer((type, lookup) -> {
        try {
            final MethodHandle constructor;
            final Class<?> erased = erase(type.getType());
            constructor = MethodHandles.privateLookupIn(erased, lookup == null ? OWN_LOOKUP : lookup)
                .findConstructor(erased, MethodType.methodType(void.class));
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

    DisabledObjectFieldDiscoverer(
        final CheckedFunction<AnnotatedType, @Nullable Supplier<Object>, SerializationException> instanceFactory,
        final @Nullable String instanceUnavailableErrorMessage,
        final boolean requiresInstanceCreation
    ) {
        this((type, lookup) -> instanceFactory.apply(type), instanceUnavailableErrorMessage, requiresInstanceCreation);
    }

    DisabledObjectFieldDiscoverer(
        final CheckedBiFunction<AnnotatedType, MethodHandles.@Nullable Lookup, @Nullable Supplier<Object>, SerializationException> instanceFactory,
        final @Nullable String instanceUnavailableErrorMessage,
        final boolean requiresInstanceCreation
    ) {
        this.instanceFactory = instanceFactory;
        this.instanceUnavailableErrorMessage = Objects.requireNonNullElse(
            instanceUnavailableErrorMessage,
            "Unable to create instances for this type!"
        );
        this.requiresInstanceCreation = requiresInstanceCreation;
    }

    @Override
    public <V> @Nullable InstanceFactory<Map<VarHandle, Object>> discover(
        final AnnotatedType target,
        final FieldCollector<Map<VarHandle, Object>, V> collector,
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
            try {
                collectFields(collectType, collector, lookup);
            } catch (final IllegalAccessException ex) {
                throw new SerializationException(collectType.getType(), "Unable to access field in type", ex);
            }

            collectClass = collectClass.getSuperclass();
            if (collectClass.equals(Object.class)) {
                break;
            }
            collectType = getExactSuperType(collectType, collectClass);
        }

        return new MutableInstanceFactory<>() {

            @Override
            public Map<VarHandle, Object> begin() {
                return new HashMap<>();
            }

            @Override
            public void complete(final Object instance, final Map<VarHandle, Object> intermediate) {
                for (final Map.Entry<VarHandle, Object> entry : intermediate.entrySet()) {
                    // Handle implicit field initialization by detecting any existing information in the object
                    if (entry.getValue() instanceof ImplicitProvider) {
                        final @Nullable Object implicit = ((ImplicitProvider) entry.getValue()).provider.get();
                        if (implicit != null) {
                            if (entry.getKey().get(instance) == null) {
                                entry.getKey().set(instance, implicit);
                            }
                        }
                    } else {
                        entry.getKey().set(instance, entry.getValue());
                    }
                }
            }

            @Override
            public Object complete(final Map<VarHandle, Object> intermediate) throws SerializationException {
                final @Nullable Object instance = maker == null ? null : maker.get();
                if (instance == null) {
                    throw new SerializationException(target.getType(), DisabledObjectFieldDiscoverer.this.instanceUnavailableErrorMessage);
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

    private void collectFields(
        final AnnotatedType clazz,
        final FieldCollector<Map<VarHandle, Object>, ?> fieldMaker,
        final MethodHandles.@Nullable Lookup source
    ) throws IllegalAccessException {
        final Class<?> erased = erase(clazz.getType());
        final MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(erased, source == null ? OWN_LOOKUP : source);
        for (final Field field : erased.getDeclaredFields()) {
            if ((field.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT)) != 0) {
                continue;
            }
            final VarHandle handle = lookup.unreflectVarHandle(field);
            final AnnotatedType fieldType = getFieldType(field, clazz);
            fieldMaker.accept(field.getName(), fieldType, Types.combinedAnnotations(fieldType, field),
                              (intermediate, val, implicitProvider) -> {
                    if (val != null) {
                        intermediate.put(handle, val);
                    } else {
                        intermediate.put(handle, new ImplicitProvider(implicitProvider));
                    }
                }, handle::get);
        }
    }

    static class ImplicitProvider {

        final Supplier<Object> provider;

        ImplicitProvider(final Supplier<Object> provider) {
            this.provider = provider;
        }

    }

}
