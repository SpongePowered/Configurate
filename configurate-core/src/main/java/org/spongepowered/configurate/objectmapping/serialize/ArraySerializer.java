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
package org.spongepowered.configurate.objectmapping.serialize;

import com.google.common.reflect.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.configurate.util.ThrowingConsumer;

import java.lang.reflect.Array;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * A serializer for array classes. Primitive arrays need special handling because they don't have autoboxing like single
 * primitives do.
 *
 * @param <T>
 */
abstract class ArraySerializer<T> extends AbstractListChildSerializer<T> {

    @Override
    TypeToken<?> getElementType(TypeToken<?> containerType) throws ObjectMappingException {
        return requireNonNull(containerType.getComponentType(), "Must be array type");
    }

    static class Objects extends ArraySerializer<Object[]> {

        public static Predicate<TypeToken<Object[]>> predicate() {
            return token -> {
                @Nullable TypeToken<?> componentType = token.getComponentType();
                return componentType != null && !componentType.isPrimitive();
            };
        }

        @Override
        Object[] createNew(int length, TypeToken<?> elementType) throws ObjectMappingException {
            return (Object[]) Array.newInstance(elementType.getRawType(), length);
        }

        @Override
        void forEachElement(Object[] collection, ThrowingConsumer<Object, ObjectMappingException> action) throws ObjectMappingException {
            for (Object o : collection) {
                action.accept(o);
            }
        }

        @Override
        void deserializeSingle(int index, Object[] collection, Object deserialized) throws ObjectMappingException {
            collection[index] = deserialized;
        }

    }

    static class Booleans extends ArraySerializer<boolean[]> {
        static final TypeToken<boolean[]> TYPE = TypeToken.of(boolean[].class);

        @Override
        boolean[] createNew(int length, TypeToken<?> elementType) throws ObjectMappingException {
            return new boolean[length];
        }

        @Override
        void forEachElement(boolean[] collection, ThrowingConsumer<Object, ObjectMappingException> action) throws ObjectMappingException {
            for (boolean b : collection) {
                action.accept(b);
            }
        }

        @Override
        void deserializeSingle(int index, boolean[] collection, @Nullable Object deserialized) throws ObjectMappingException {
            collection[index] = deserialized == null ? false : Scalars.BOOLEAN.deserialize(deserialized);
        }
    }

    static class Bytes extends ArraySerializer<byte[]> {
        static final TypeToken<byte[]> TYPE = TypeToken.of(byte[].class);

        @Override
        byte[] createNew(int length, TypeToken<?> elementType) throws ObjectMappingException {
            return new byte[length];
        }

        @Override
        void forEachElement(byte[] collection, ThrowingConsumer<Object, ObjectMappingException> action) throws ObjectMappingException {
            for (byte b : collection) {
                action.accept(b);
            }
        }

        @Override
        void deserializeSingle(int index, byte[] collection, @Nullable Object deserialized) throws ObjectMappingException {
            collection[index] = deserialized == null ? 0 : Scalars.INTEGER.deserialize(deserialized).byteValue();
        }
    }

    static class Chars extends ArraySerializer<char[]> {
        static final TypeToken<char[]> TYPE = TypeToken.of(char[].class);

        @Override
        char[] createNew(int length, TypeToken<?> elementType) throws ObjectMappingException {
            return new char[length];
        }

        @Override
        void forEachElement(char[] collection, ThrowingConsumer<Object, ObjectMappingException> action) throws ObjectMappingException {
            for (char b : collection) {
                action.accept(b);
            }
        }

        @Override
        void deserializeSingle(int index, char[] collection, @Nullable Object deserialized) throws ObjectMappingException {
            collection[index] = deserialized == null ? 0 : Scalars.CHAR.deserialize(deserialized);
        }
    }

    static class Shorts extends ArraySerializer<short[]> {
        static final TypeToken<short[]> TYPE = TypeToken.of(short[].class);

        @Override
        short[] createNew(int length, TypeToken<?> elementType) throws ObjectMappingException {
            return new short[length];
        }

        @Override
        void forEachElement(short[] collection, ThrowingConsumer<Object, ObjectMappingException> action) throws ObjectMappingException {
            for (short b : collection) {
                action.accept(b);
            }
        }

        @Override
        void deserializeSingle(int index, short[] collection, @Nullable Object deserialized) throws ObjectMappingException {
            collection[index] = deserialized == null ? 0 : Scalars.INTEGER.deserialize(deserialized).shortValue();
        }
    }

    static class Ints extends ArraySerializer<int[]> {
        static final TypeToken<int[]> TYPE = TypeToken.of(int[].class);

        @Override
        int[] createNew(int length, TypeToken<?> elementType) throws ObjectMappingException {
            return new int[length];
        }

        @Override
        void forEachElement(int[] collection, ThrowingConsumer<Object, ObjectMappingException> action) throws ObjectMappingException {
            for (int b : collection) {
                action.accept(b);
            }
        }

        @Override
        void deserializeSingle(int index, int[] collection, @Nullable Object deserialized) throws ObjectMappingException {
            collection[index] = deserialized == null ? 0 : Scalars.INTEGER.deserialize(deserialized);
        }
    }

    static class Longs extends ArraySerializer<long[]> {
        static final TypeToken<long[]> TYPE = TypeToken.of(long[].class);

        @Override
        long[] createNew(int length, TypeToken<?> elementType) throws ObjectMappingException {
            return new long[length];
        }

        @Override
        void forEachElement(long[] collection, ThrowingConsumer<Object, ObjectMappingException> action) throws ObjectMappingException {
            for (long b : collection) {
                action.accept(b);
            }
        }

        @Override
        void deserializeSingle(int index, long[] collection, @Nullable Object deserialized) throws ObjectMappingException {
            collection[index] = deserialized == null ? 0 : Scalars.LONG.deserialize(deserialized);
        }
    }

    static class Floats extends ArraySerializer<float[]> {
        static final TypeToken<float[]> TYPE = TypeToken.of(float[].class);

        @Override
        float[] createNew(int length, TypeToken<?> elementType) throws ObjectMappingException {
            return new float[length];
        }

        @Override
        void forEachElement(float[] collection, ThrowingConsumer<Object, ObjectMappingException> action) throws ObjectMappingException {
            for (float b : collection) {
                action.accept(b);
            }
        }

        @Override
        void deserializeSingle(int index, float[] collection, @Nullable Object deserialized) throws ObjectMappingException {
            collection[index] = deserialized == null ? 0 : Scalars.FLOAT.deserialize(deserialized);
        }
    }

    static class Doubles extends ArraySerializer<double[]> {
        static final TypeToken<double[]> TYPE = TypeToken.of(double[].class);

        @Override
        double[] createNew(int length, TypeToken<?> elementType) throws ObjectMappingException {
            return new double[length];
        }

        @Override
        void forEachElement(double[] collection, ThrowingConsumer<Object, ObjectMappingException> action) throws ObjectMappingException {
            for (double b : collection) {
                action.accept(b);
            }
        }

        @Override
        void deserializeSingle(int index, double[] collection, @Nullable Object deserialized) throws ObjectMappingException {
            collection[index] = deserialized == null ? 0 : Scalars.DOUBLE.deserialize(deserialized);
        }
    }
}
