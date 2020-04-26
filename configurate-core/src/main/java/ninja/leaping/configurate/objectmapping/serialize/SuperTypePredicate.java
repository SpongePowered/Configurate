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
package ninja.leaping.configurate.objectmapping.serialize;

import com.google.common.reflect.TypeToken;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Predicate;

/**
 * Effectively a predicate which is <code>type::isSupertypeOf</code>.
 *
 * <p>The isSupertypeOf method was only added in Guava 19.0, and was previously named
 * isAssignableFrom.</p>
 */
final class SuperTypePredicate implements Predicate<TypeToken<?>> {
    private static final MethodHandle SUPERTYPE_TEST;
    static {
        MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        MethodType type = MethodType.methodType(boolean.class, TypeToken.class);
        MethodHandle supertypeTest;
        try {
            try {
                supertypeTest = lookup.findVirtual(TypeToken.class, "isSupertypeOf", type);
            } catch (NoSuchMethodException e1) {
                try {
                    supertypeTest = lookup.findVirtual(TypeToken.class, "isAssignableFrom", type);
                } catch (NoSuchMethodException e2) {
                    throw new RuntimeException("Unable to get TypeToken#isSupertypeOf or TypeToken#isAssignableFrom method");
                }
            }
        } catch (IllegalAccessException e) {
            throw new ExceptionInInitializerError("Could not access isSupertypeOf/isAssignableFrom method in TypeToken");
        }

        SUPERTYPE_TEST = supertypeTest;
    }

    private final TypeToken<?> type;

    SuperTypePredicate(TypeToken<?> type) {
        this.type = type;
    }

    @Override
    public boolean test(TypeToken<?> t) {
        try {
            return (boolean) SUPERTYPE_TEST.invokeExact(type, t);
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }
}
