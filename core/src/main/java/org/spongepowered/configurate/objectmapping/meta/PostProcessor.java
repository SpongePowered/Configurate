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
package org.spongepowered.configurate.objectmapping.meta;

import io.leangen.geantyref.GenericTypeReflector;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.util.Types;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * A callback executed after all field serialization has been performed.
 *
 * @since 4.2.0
 */
@FunctionalInterface
public interface PostProcessor {

    /**
     * Perform post-processing on the fully deserialized instance.
     *
     * @param instance the instance to post-process
     * @throws SerializationException if the underlying operation
     *     detects an error
     * @since 4.2.0
     */
    void postProcess(Object instance) throws SerializationException;

    /**
     * A factory to produce object post-processors.
     *
     * @since 4.2.0
     */
    @FunctionalInterface
    interface Factory {

        /**
         * Return a post-processor if any is applicable to the provided type.
         *
         * @param type the type to post-process
         * @return a potential post-processor
         * @throws SerializationException if there is a declared post-processor
         *     handled by this factory with an invalid type
         * @since 4.2.0
         */
        @Nullable PostProcessor createProcessor(Type type) throws SerializationException;

    }

    /**
     * Discover methods annotated with the designated post-processor annotation
     * in object-mapped types and their supertypes.
     *
     * <p>Annotated methods must be non-static, take no parameters, and can have
     * no declared thrown exceptions
     * except for {@link SerializationException}.</p>
     *
     * @param annotation the annotation that will mark post-processor methods
     * @return a factory for annotated methods
     * @since 4.2.0
     */
    static Factory methodsAnnotated(final Class<? extends Annotation> annotation) {
        return type -> {
            List<Method> methods = null;
            for (final Method method : Types.allDeclaredMethods(GenericTypeReflector.erase(type))) {
                if (method.isAnnotationPresent(annotation)) {
                    // Validate method
                    final int modifiers = method.getModifiers();
                    if (Modifier.isAbstract(modifiers)) {
                        continue;
                    }

                    if (Modifier.isStatic(modifiers)) {
                        throw new SerializationException(
                            type,
                            "Post-processor method " + method.getName() + "() annotated @" + annotation.getSimpleName()
                                + " must not be static."
                        );
                    }
                    if (method.getParameterCount() != 0) {
                        throw new SerializationException(
                            type,
                            "Post-processor method " + method.getName() + "() annotated @" + annotation.getSimpleName()
                                + " must not take any parameters."
                        );
                    }

                    for (final Class<?> exception : method.getExceptionTypes()) {
                        if (!SerializationException.class.isAssignableFrom(exception)) {
                            throw new SerializationException(
                                type,
                                "Post-processor method " + method.getName() + "() annotated @" + annotation.getSimpleName()
                                    + " must only throw SerializationException or its subtypes, but is declared to throw "
                                    + exception.getSimpleName() + "."
                            );
                        }
                    }
                    method.setAccessible(true);

                    // Then add it
                    if (methods == null) {
                        methods = new ArrayList<>();
                    }
                    methods.add(method);
                }
            }

            if (methods != null) {
                final List<Method> finalMethods = methods;
                return instance -> {
                    SerializationException aggregateException = null;
                    for (final Method postProcessorMethod : finalMethods) {
                        SerializationException exc = null;
                        try {
                            postProcessorMethod.invoke(instance);
                        } catch (final InvocationTargetException ex) {
                            if (ex.getCause() instanceof SerializationException) {
                                exc = (SerializationException) ex.getCause();
                                exc.initType(type);
                            } else if (ex.getCause() != null) {
                                exc = new SerializationException(
                                    type,
                                    "Failure occurred in post-processor method " + postProcessorMethod.getName() + "()", ex.getCause()
                                );
                            } else {
                                exc = new SerializationException(
                                    type,
                                    "Unknown error occurred attempting to invoke post-processor method " + postProcessorMethod.getName() + "()",
                                    ex
                                );
                            }
                        } catch (final IllegalAccessException | IllegalArgumentException ex) {
                            exc = new SerializationException(
                                type, "Failed to invoke post-processor method " + postProcessorMethod.getName() + "()", ex
                            );
                        }

                        // Capture all relevant exceptions
                        if (exc != null) {
                            if (aggregateException == null) {
                                aggregateException = exc;
                            } else {
                                aggregateException.addSuppressed(exc);
                            }
                        }
                    }

                    // If anybody threw an exception, rethrow
                    if (aggregateException != null) {
                        throw aggregateException;
                    }
                };
            }

            return null;
        };
    }

    /**
     * Discover methods annotated with the {@link PostProcess} annotation.
     *
     * <p>All restrictions from {@link #methodsAnnotated(Class)} apply to these
     * annotated methods.</p>
     *
     * @return a new factory for discovering annotated methods
     * @see #methodsAnnotated(Class)
     * @since 4.2.0
     */
    static Factory methodsAnnotatedPostProcess() {
        return methodsAnnotated(PostProcess.class);
    }

}
