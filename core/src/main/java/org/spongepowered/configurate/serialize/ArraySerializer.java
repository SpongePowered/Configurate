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
package org.spongepowered.configurate.serialize;

import io.leangen.geantyref.GenericTypeReflector;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.util.CheckedConsumer;
import org.spongepowered.configurate.util.Types;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Array;
import java.lang.reflect.Type;

/**
 * A serializer for array classes. Primitive arrays need special handling
 * because they don't have autoboxing like single primitives do.
 *
 * @param <T> array type
 */
abstract class ArraySerializer<T> extends AbstractListChildSerializer<T> {

    ArraySerializer() {
    }

    @Override
    protected AnnotatedType elementType(final AnnotatedType containerType) throws SerializationException {
        final AnnotatedType componentType = GenericTypeReflector.getArrayComponentType(containerType);
        if (componentType == null) {
            throw new SerializationException(containerType, "Must be array type");
        }
        return componentType;
    }

    static final class Objects extends ArraySerializer<Object[]> {

        public static boolean accepts(final Type token) {
            if (!Types.isArray(token)) {
                return false;
            }

            final Type componentType = GenericTypeReflector.getArrayComponentType(token);
            // require that the component type is non-primitive, by comparing with its `box`-ed value
            // this works because `box` is a only a no-op on non-primitive types
            return componentType.equals(GenericTypeReflector.box(componentType));
        }

        @Override
        protected Object[] createNew(final int length, final AnnotatedType elementType) {
            return (Object[]) Array.newInstance(GenericTypeReflector.erase(elementType.getType()), length);
        }

        @Override
        protected void forEachElement(final Object[] collection,
                final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
            for (final Object o : collection) {
                action.accept(o);
            }
        }

        @Override
        protected void deserializeSingle(final int index, final Object[] collection, final @Nullable Object deserialized) {
            collection[index] = deserialized;
        }

    }

    static final class Booleans extends ArraySerializer<boolean[]> {

        static final Class<boolean[]> TYPE = boolean[].class;

        @Override
        protected boolean[] createNew(final int length, final AnnotatedType elementType) {
            return new boolean[length];
        }

        @Override
        protected void forEachElement(final boolean[] collection,
                final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
            for (final boolean b : collection) {
                action.accept(b);
            }
        }

        @Override
        protected void deserializeSingle(final int index, final boolean[] collection,
                final @Nullable Object deserialized) throws SerializationException {
            collection[index] = deserialized == null ? false : Scalars.BOOLEAN.deserialize(deserialized);
        }

    }

    static final class Bytes extends ArraySerializer<byte[]> {

        static final Class<byte[]> TYPE = byte[].class;

        @Override
        protected byte[] createNew(final int length, final AnnotatedType elementType) {
            return new byte[length];
        }

        @Override
        protected void forEachElement(final byte[] collection,
                final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
            for (final byte b : collection) {
                action.accept(b);
            }
        }

        @Override
        protected void deserializeSingle(final int index, final byte[] collection,
                final @Nullable Object deserialized) throws SerializationException {
            collection[index] = deserialized == null ? 0 : Scalars.INTEGER.deserialize(deserialized).byteValue();
        }

    }

    static final class Chars extends ArraySerializer<char[]> {

        static final Class<char[]> TYPE = char[].class;

        @Override
        protected char[] createNew(final int length, final AnnotatedType elementType) {
            return new char[length];
        }

        @Override
        protected void forEachElement(final char[] collection,
                final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
            for (final char b : collection) {
                action.accept(b);
            }
        }

        @Override
        protected void deserializeSingle(final int index, final char[] collection,
                final @Nullable Object deserialized) throws SerializationException {
            collection[index] = deserialized == null ? 0 : Scalars.CHAR.deserialize(deserialized);
        }

    }

    static final class Shorts extends ArraySerializer<short[]> {

        static final Class<short[]> TYPE = short[].class;

        @Override
        protected short[] createNew(final int length, final AnnotatedType elementType) {
            return new short[length];
        }

        @Override
        protected void forEachElement(final short[] collection,
                final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
            for (final short b : collection) {
                action.accept(b);
            }
        }

        @Override
        protected void deserializeSingle(final int index, final short[] collection,
                final @Nullable Object deserialized) throws SerializationException {
            collection[index] = deserialized == null ? 0 : Scalars.INTEGER.deserialize(deserialized).shortValue();
        }

    }

    static final class Ints extends ArraySerializer<int[]> {

        static final Class<int[]> TYPE = int[].class;

        @Override
        protected int[] createNew(final int length, final AnnotatedType elementType) {
            return new int[length];
        }

        @Override
        protected void forEachElement(final int[] collection,
                final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
            for (final int b : collection) {
                action.accept(b);
            }
        }

        @Override
        protected void deserializeSingle(final int index, final int[] collection,
                final @Nullable Object deserialized) throws SerializationException {
            collection[index] = deserialized == null ? 0 : Scalars.INTEGER.deserialize(deserialized);
        }

    }

    static final class Longs extends ArraySerializer<long[]> {

        static final Class<long[]> TYPE = long[].class;

        @Override
        protected long[] createNew(final int length, final AnnotatedType elementType) {
            return new long[length];
        }

        @Override
        protected void forEachElement(final long[] collection,
                final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
            for (final long b : collection) {
                action.accept(b);
            }
        }

        @Override
        protected void deserializeSingle(final int index, final long[] collection,
                final @Nullable Object deserialized) throws SerializationException {
            collection[index] = deserialized == null ? 0 : Scalars.LONG.deserialize(deserialized);
        }

    }

    static final class Floats extends ArraySerializer<float[]> {

        static final Class<float[]> TYPE = float[].class;

        @Override
        protected float[] createNew(final int length, final AnnotatedType elementType) {
            return new float[length];
        }

        @Override
        protected void forEachElement(final float[] collection,
                final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
            for (final float b : collection) {
                action.accept(b);
            }
        }

        @Override
        protected void deserializeSingle(final int index, final float[] collection,
                final @Nullable Object deserialized) throws SerializationException {
            collection[index] = deserialized == null ? 0 : Scalars.FLOAT.deserialize(deserialized);
        }

    }

    static final class Doubles extends ArraySerializer<double[]> {

        static final Class<double[]> TYPE = double[].class;

        @Override
        protected double[] createNew(final int length, final AnnotatedType elementType) {
            return new double[length];
        }

        @Override
        protected void forEachElement(final double[] collection,
                final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
            for (final double b : collection) {
                action.accept(b);
            }
        }

        @Override
        protected void deserializeSingle(final int index, final double[] collection,
                final @Nullable Object deserialized) throws SerializationException {
            collection[index] = deserialized == null ? 0 : Scalars.DOUBLE.deserialize(deserialized);
        }

    }

}
