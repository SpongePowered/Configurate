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
package org.spongepowered.configurate.util;

import io.leangen.geantyref.TypeFactory;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Utility methods for working with generic types.
 *
 * <p>Most of these utilities are designed to go along with
 * <a href="https://github.com/leangen/geantyref">GeAnTyRef</a>.</p>
 */
public final class Typing {

    private static final Map<Type, Type> BOXED_TO_PRIMITIVE = UnmodifiableCollections.buildMap(m -> {
        m.put(Boolean.class, boolean.class);
        m.put(Byte.class, byte.class);
        m.put(Short.class, short.class);
        m.put(Integer.class, int.class);
        m.put(Long.class, long.class);
        m.put(Float.class, float.class);
        m.put(Double.class, double.class);
        m.put(Void.class, void.class);
    });

    private static final Map<Type, Type> PRIMITIVE_TO_BOXED = UnmodifiableCollections.buildMap(m -> {
        m.put(boolean.class, Boolean.class);
        m.put(byte.class, Byte.class);
        m.put(short.class, Short.class);
        m.put(int.class, Integer.class);
        m.put(long.class, Long.class);
        m.put(float.class, Float.class);
        m.put(double.class, Double.class);
        m.put(void.class, Void.class);
    });

    private Typing() {
    }

    /**
     * Get if the provided type is an array type.
     *
     * <p>Being an array type means that the provided
     * type has a component type.</p>
     *
     * @param input input type
     * @return whether the type is an array
     */
    public static boolean isArray(final Type input) {
        if (input instanceof Class<?>) {
            return ((Class<?>) input).isArray();
        } else if (input instanceof ParameterizedType) {
            return isArray(((ParameterizedType) input).getRawType());
        } else if (input instanceof GenericArrayType) {
            return true;
        } else { // unknown type
            return false;
        }
    }

    /**
     * Get whether or not the provided input type is a boxed primitive type.
     *
     * <p>This check will <em>not</em> match unboxed primitives.</p>
     *
     * @param input type to check
     * @return if type is a boxed primitive
     */
    public static boolean isBoxedPrimitive(final Type input) {
        return BOXED_TO_PRIMITIVE.containsKey(input);
    }

    /**
     * Unbox the input type if it is a boxed primitive.
     *
     * @param input input type
     * @return the unboxed version of the input type,
     *          or the input type if it was already non-primitive
     */
    public static Type unbox(final Type input) {
        final Type ret = BOXED_TO_PRIMITIVE.get(input);
        return ret == null ? input : ret;
    }

    /**
     * Box the input type if it is an unboxed primitive {@link Class}.
     *
     * @param input input type
     * @return the unboxed version of the input type, or the input type if
     *          it was already a primitive, or had no primitive equivalent
     */
    public static Type box(final Type input) {
        final Type ret = PRIMITIVE_TO_BOXED.get(input);
        return ret == null ? input : ret;
    }

    /**
     * Given an element type, create a new list type.
     *
     * @param elementType Type token representing the element type
     * @param <T> type of element
     * @return new list type token
     */
    @SuppressWarnings("unchecked")
    public static <T> TypeToken<List<T>> makeListType(final TypeToken<T> elementType) {
        return (TypeToken<List<T>>) TypeToken.get(TypeFactory.parameterizedClass(List.class, elementType.getType()));
    }

    /**
     * Get an element containing the annotations of all the provided elements.
     *
     * <p>If multiple elements have the same annotation, only the first one
     * with an applicable type is returned.</p>
     *
     * @param elements elements to combine
     * @return new union element
     */
    public static AnnotatedElement combinedAnnotations(final AnnotatedElement... elements) {
        return new CombinedAnnotations(Arrays.copyOf(elements, elements.length));
    }

    static class CombinedAnnotations implements AnnotatedElement {
        private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];

        private final AnnotatedElement[] elements;

        CombinedAnnotations(final AnnotatedElement[] elements) {
            this.elements = elements;
        }

        @Override
        public boolean isAnnotationPresent(final Class<? extends Annotation> annotationClass) {
            for (AnnotatedElement element : this.elements) {
                if (element.isAnnotationPresent(annotationClass)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public <T extends Annotation> @Nullable T getAnnotation(final Class<T> annotationClass) {
            @Nullable T ret = null;
            for (AnnotatedElement element : this.elements) {
                ret = element.getAnnotation(annotationClass);
                if (ret != null) {
                    break;
                }
            }
            return ret;
        }

        @Override
        public Annotation[] getAnnotations() {
            final List<Annotation> annotations = new ArrayList<>();
            for (AnnotatedElement element : this.elements) {
                final Annotation[] annotation = element.getAnnotations();
                if (annotation.length > 0) {
                    annotations.addAll(Arrays.asList(annotation));
                }
            }
            return annotations.toArray(EMPTY_ANNOTATION_ARRAY);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends Annotation> T[] getAnnotationsByType(final Class<T> annotationClass) {
            final List<T> annotations = new ArrayList<>();
            for (AnnotatedElement element : this.elements) {
                final T[] annotation = element.getAnnotationsByType(annotationClass);
                if (annotation.length > 0) {
                    annotations.addAll(Arrays.asList(annotation));
                }
            }
            return annotations.toArray((T[]) EMPTY_ANNOTATION_ARRAY);
        }

        @Override
        public <T extends Annotation> T getDeclaredAnnotation(final Class<T> annotationClass) {
            @Nullable T ret = null;
            for (AnnotatedElement element : this.elements) {
                ret = element.getDeclaredAnnotation(annotationClass);
                if (ret != null) {
                    break;
                }
            }
            return ret;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends Annotation> T[] getDeclaredAnnotationsByType(final Class<T> annotationClass) {
            final List<T> annotations = new ArrayList<>();
            for (AnnotatedElement element : this.elements) {
                final T[] annotation = element.getDeclaredAnnotationsByType(annotationClass);
                if (annotation.length > 0) {
                    annotations.addAll(Arrays.asList(annotation));
                }
            }
            return annotations.toArray((T[]) EMPTY_ANNOTATION_ARRAY);
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            final List<Annotation> annotations = new ArrayList<>();
            for (AnnotatedElement element : this.elements) {
                final Annotation[] annotation = element.getDeclaredAnnotations();
                if (annotation.length > 0) {
                    annotations.addAll(Arrays.asList(annotation));
                }
            }
            return annotations.toArray(EMPTY_ANNOTATION_ARRAY);
        }
    }

}
