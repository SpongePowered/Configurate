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

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableSet;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.AbstractCollection;
import java.util.Collection;

@SuppressWarnings("PMD.LooseCoupling") // specific types matter
class TypesTest {

    @Test
    void testObjectNoSuperclasses() {
        assertEquals(ImmutableSet.of(Object.class), Types.allSuperTypes(Object.class).collect(toImmutableSet()));
        assertEquals(ImmutableSet.of(Object.class), Types.allSuperTypesAndInterfaces(Object.class).collect(toImmutableSet()));
    }

    @Test
    void testPrimitiveNoSuperclasses() {
        assertEquals(ImmutableSet.of(long.class), Types.allSuperTypes(long.class).collect(toImmutableSet()));
        assertEquals(ImmutableSet.of(long.class), Types.allSuperTypesAndInterfaces(long.class).collect(toImmutableSet()));
    }

    @Test
    void testArraySupertypes() {
        // int[] -> (int[])
        assertEquals(ImmutableSet.of(int[].class), Types.allSuperTypes(int[].class).collect(toImmutableSet()));
        assertEquals(ImmutableSet.of(int[].class), Types.allSuperTypesAndInterfaces(int[].class).collect(toImmutableSet()));

        final ImmutableSet<Type> methodWithoutInterfaces = ImmutableSet.of(
                Method[].class, Executable[].class, AccessibleObject[].class, Object[].class);
        final ImmutableSet<Type> methodWithInterfaces = ImmutableSet.<Type>builder()
                .addAll(methodWithoutInterfaces)
                .add(Member[].class).add(GenericDeclaration[].class).add(AnnotatedElement[].class)
                .build();
        assertEquals(methodWithoutInterfaces, Types.allSuperTypes(Method[].class).collect(toImmutableSet()));
        assertEquals(methodWithInterfaces, Types.allSuperTypesAndInterfaces(Method[].class).collect(toImmutableSet()));
    }

    @Test
    void testGenericArrays() {
        // AbstractCollection<String>[] -> (AbstractCollection<String>[], {Collection<String>[], Iterable<String>[]}, Object[])
        final Type abstractCollectionString = new TypeToken<AbstractCollection<String>[]>() {}.getType();
        final Type collectionString = new TypeToken<Collection<String>[]>() {}.getType();
        final Type iterableString = new TypeToken<Iterable<String>[]>() {}.getType();
        final ImmutableSet<Type> collectionWithoutInterfaces = ImmutableSet.of(abstractCollectionString, Object[].class);
        final ImmutableSet<Type> collectionWithInterfaces = ImmutableSet.<Type>builder().addAll(collectionWithoutInterfaces)
                .add(collectionString, iterableString)
                .build();
        assertEquals(collectionWithoutInterfaces, Types.allSuperTypes(abstractCollectionString).collect(toImmutableSet()));
        assertEquals(collectionWithInterfaces, Types.allSuperTypesAndInterfaces(abstractCollectionString).collect(toImmutableSet()));
    }

    @Test
    void testParameterizedSupertypes() {
        // Parameterized<String, Integer> -> (Parameterized<String, Integer>, Parent<String>, {IOne, ITwo}, Object)
        final TypeToken<Parameterized<String, Integer>> parameterized = new TypeToken<Parameterized<String, Integer>>() {};
        final TypeToken<Parent<String>> parent = new TypeToken<Parent<String>>() {};
        assertEquals(ImmutableSet.of(parameterized.getType(), parent.getType(), Object.class),
                     Types.allSuperTypes(parameterized.getType()).collect(toImmutableSet()));
        assertEquals(ImmutableSet.of(parameterized.getType(), parent.getType(), IOne.class, ITwo.class, Object.class),
                     Types.allSuperTypesAndInterfaces(parameterized.getType()).collect(toImmutableSet()));
    }

    static class ParamHolder<A, B, C, D, E, F> {
    }

    private static final @Nullable ParamHolder<? extends Executable, ? extends AccessibleObject, ? extends Member, ? extends GenericDeclaration, ?
            extends AnnotatedElement, ?> wildcardHolder = null;

    @Test
    void testWildcardTypes() throws NoSuchFieldException {
        //noinspection ConstantConditions
        if (wildcardHolder != null) {
            // Make the variable appear as used
            throw new AssertionError();
        }
        final Type[] hierarchy =
                ((ParameterizedType) this.getClass().getDeclaredField("wildcardHolder").getGenericType()).getActualTypeArguments();
        final Type base = hierarchy[0];

        // Annoying to declare
        assertEquals(ImmutableSet.copyOf(hierarchy), Types.allSuperTypesAndInterfaces(base).collect(toImmutableSet()));
    }

    @Test
    void testTypeVariable() {
        final TypeVariable<? extends Class<?>> parameterizedB = Parameterized.class.getTypeParameters()[1];

        assertEquals(ImmutableSet.of(parameterizedB, Number.class, Object.class),
                     Types.allSuperTypes(parameterizedB).collect(toImmutableSet()));
        assertEquals(ImmutableSet.of(parameterizedB, Number.class, Serializable.class, Object.class),
                     Types.allSuperTypesAndInterfaces(parameterizedB).collect(toImmutableSet()));

    }

    static class Parameterized<A, B extends Number> extends Parent<A> implements IOne {}

    static class Parent<A> {}

    interface IOne extends ITwo {}

    interface ITwo {}

    interface IThree {}

}
