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

import static io.leangen.geantyref.GenericTypeReflector.erase;
import static io.leangen.geantyref.GenericTypeReflector.resolveType;
import static java.util.Objects.requireNonNull;

import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeFactory;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Utility methods for working with generic types.
 *
 * <p>Most of these utilities are designed to go along with
 * <a href="https://github.com/leangen/geantyref">GeAnTyRef</a>.</p>
 *
 * @see GenericTypeReflector for other tools to work with types
 * @see TypeFactory for methods to construct types
 */
public final class Types {

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

    private Types() {
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
        } else {
            return input instanceof GenericArrayType;
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
     * @param elementType type token representing the element type
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

    /**
     * Throw an exception if the passed type is raw (missing parameters)..
     *
     * @param input input type
     * @return type, passed through
     */
    public static Type requireCompleteParameters(final Type input) {
        if (GenericTypeReflector.isMissingTypeParameters(input)) {
            throw new IllegalArgumentException("Provided type " + input + " is a raw type, which is not accepted.");
        }
        return input;
    }

    /**
     * Get all supertypes of this object with type parameters.
     *
     * <p>The iteration order is undefined. The returned stream will include the
     * base type plus superclasses, but not superinterfaces.</p>
     *
     * @param type base type
     * @return stream of supertypes
     */
    public static Stream<Type> allSuperTypes(final Type type) {
        return calculateSuperTypes(type, false);
    }

    /**
     * Get all supertypes and interfaces of the provided type.
     *
     * <p>The iteration order is undefined. The returned stream will include the
     * base type plus superclasses and superinterfaces.</p>
     *
     * @param type base type
     * @return stream of supertypes
     */
    public static Stream<Type> allSuperTypesAndInterfaces(final Type type) {
        return calculateSuperTypes(type, true);
    }

    private static Stream<Type> calculateSuperTypes(final Type type, final boolean includeInterfaces) {
        requireNonNull(type, "type");
        return StreamSupport.stream(Spliterators.spliterator(new SuperTypesIterator(type, includeInterfaces), Long.MAX_VALUE,
                                                             Spliterator.NONNULL | Spliterator.IMMUTABLE), false);
    }

    /**
     * Recursively iterate through supertypes.
     */
    static class SuperTypesIterator implements Iterator<Type> {
        private final boolean includeInterfaces;
        private final Deque<Type> types = new ArrayDeque<>();
        private final Set<Type> seen = new HashSet<>();

        SuperTypesIterator(final Type base, final boolean includeInterfaces) {
            this.types.add(base);
            this.includeInterfaces = includeInterfaces;
        }

        @Override
        public boolean hasNext() {
            return !this.types.isEmpty();
        }

        @Override
        public Type next() {
            // Get current type, throws the correct exception if empty
            final Type head = this.types.removeLast();

            // Calculate the next step depending on the type of Type seen
            // Arrays, covariant based on component type
            if ((head instanceof Class<?> && ((Class<?>) head).isArray()) || head instanceof GenericArrayType) {
                // find a super component-type
                final Type componentType;
                if (head instanceof Class<?>) {
                    componentType = ((Class<?>) head).getComponentType();
                } else {
                    componentType = ((GenericArrayType) head).getGenericComponentType();
                }

                addSuperClassAndInterface(componentType, erase(componentType), TypeFactory::arrayOf);
            } else if (head instanceof Class<?> || head instanceof ParameterizedType) {
                final Class<?> clazz;
                if (head instanceof ParameterizedType) {
                    final ParameterizedType parameterized = (ParameterizedType) head;
                    clazz = (Class<?>) parameterized.getRawType();
                } else {
                    clazz = (Class<?>) head;
                }
                addSuperClassAndInterface(head, clazz, null);
            } else if (head instanceof TypeVariable<?>) {
                addAllIfUnseen(head, ((TypeVariable<?>) head).getBounds());
            } else if (head instanceof WildcardType) {
                final Type[] upperBounds = ((WildcardType) head).getUpperBounds();
                if (upperBounds.length == 1) { // single type
                    final Type upperBound = upperBounds[0];
                    addSuperClassAndInterface(head, erase(upperBound), TypeFactory::wildcardExtends);
                } else { // for each bound, add as a single supertype
                    addAllIfUnseen(head, ((WildcardType) head).getUpperBounds());
                }
            }
            return head;
        }

        private void addAllIfUnseen(final Type base, final Type[] types) {
            for (final Type type : types) {
                addIfUnseen(resolveType(type, base));
            }
        }

        private void addIfUnseen(final Type type) {
            if (this.seen.add(type)) {
                this.types.addLast(type);
            }
        }

        private void addSuperClassAndInterface(final Type base, final Class<?> actualClass, final @Nullable UnaryOperator<Type> postProcess) {
            if (this.includeInterfaces) {
                for (Type itf : actualClass.getGenericInterfaces()) {
                    if (postProcess != null) {
                        addIfUnseen(postProcess.apply(resolveType(itf, base)));
                    } else {
                        addIfUnseen(resolveType(itf, base));
                    }
                }
            }

            if (actualClass.getSuperclass() != null) {
                final Type resolved = resolveType(actualClass.getGenericSuperclass(), base);
                addIfUnseen(postProcess == null ? resolved : postProcess.apply(resolved));
            }
        }
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
        public <T extends Annotation> @Nullable T getDeclaredAnnotation(final Class<T> annotationClass) {
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
