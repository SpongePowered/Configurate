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
import org.spongepowered.configurate.Types;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.configurate.util.ThrowingConsumer;

import java.lang.reflect.Array;
import java.util.function.Predicate;

abstract class ArraySerializer<T> extends AbstractListChildSerializer<T> {


    @Override
    TypeToken<?> getElementType(TypeToken<?> containerType) throws ObjectMappingException {
        return containerType.getComponentType();
    }

    static class Objects extends ArraySerializer<Object[]> {

        public static Predicate<TypeToken<Object[]>> predicate() {
            return token -> {
                TypeToken<?> componentType = token.getComponentType();
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
        void deserializeSingle(int index, boolean[] collection, Object deserialized) throws ObjectMappingException {
            Boolean ret = Types.asBoolean(deserialized);
            collection[index] = ret == null ? false : ret;
        }
    }

    static class Bytes extends ArraySerializer<byte[]> {

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
        void deserializeSingle(int index, byte[] collection, Object deserialized) throws ObjectMappingException {
            Integer ret = Types.asInt(deserialized);
            collection[index] = ret == null ? 0 : ret.byteValue();
        }
    }

    static class Chars extends ArraySerializer<char[]> {

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
        void deserializeSingle(int index, char[] collection, Object deserialized) throws ObjectMappingException {
            if (deserialized instanceof Character) {
                collection[index] = (Character) deserialized;
            } else {
                throw new ObjectMappingException("Deserialized value must be a Character at index " + index);
            }
        }
    }

    static class Shorts extends ArraySerializer<short[]> {

        @Override
        TypeToken<?> getElementType(TypeToken<?> containerType) throws ObjectMappingException {
            return containerType.getComponentType();
        }

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
        void deserializeSingle(int index, short[] collection, Object deserialized) throws ObjectMappingException {
            Integer ret = Types.asInt(deserialized);
            collection[index] = ret == null ? 0 : ret.shortValue();
        }
    }

    static class Ints extends ArraySerializer<int[]> {

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
        void deserializeSingle(int index, int[] collection, Object deserialized) throws ObjectMappingException {
            Integer ret = Types.asInt(deserialized);
            collection[index] = ret == null ? 0 : ret;
        }
    }

    static class Longs extends ArraySerializer<long[]> {

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
        void deserializeSingle(int index, long[] collection, Object deserialized) throws ObjectMappingException {
            Long ret = Types.asLong(deserialized);
            collection[index] = ret == null ? 0 : ret;
        }
    }

    static class Floats extends ArraySerializer<float[]> {

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
        void deserializeSingle(int index, float[] collection, Object deserialized) throws ObjectMappingException {
            Float ret = Types.asFloat(deserialized);
            collection[index] = ret == null ? 0 : ret;
        }
    }

    static class Doubles extends ArraySerializer<double[]> {

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
        void deserializeSingle(int index, double[] collection, Object deserialized) throws ObjectMappingException {
            Double ret = Types.asDouble(deserialized);
            collection[index] = ret == null ? 0 : ret;
        }
    }
}
