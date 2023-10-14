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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.util.UnmodifiableCollections;

@SuppressWarnings("UnnecessaryParentheses") // for casting negative number literals
class NumericSerializersTest {

    private <T> TypeSerializer<T> serializer(final Class<T> type) {
        final @Nullable TypeSerializer<T> ret = TypeSerializerCollection.defaults().get(type);
        assertNotNull(ret, "Serializer for " + type + " must be present!");
        return ret;
    }

    private final BasicConfigurationNode node = BasicConfigurationNode.root(ConfigurationOptions.defaults()
            .nativeTypes(UnmodifiableCollections.toSet(Byte.class, Float.class, String.class, Integer.class, Long.class, Double.class)));

    @Test
    void testSerializeCustomNumber() {
        final TypeToken<CustomNumber> customNumberType = TypeToken.get(CustomNumber.class);
        final @Nullable TypeSerializer<?> serializer = TypeSerializerCollection.defaults().get(customNumberType);
        assertNull(serializer, "Type serializer for custom number class should be null!");
    }

    private static final class CustomNumber extends Number {
        public static final long serialVersionUID = 4647727438607023527L;

        @Override
        public int intValue() {
            return 0;
        }

        @Override
        public long longValue() {
            return 0;
        }

        @Override
        public float floatValue() {
            return 0;
        }

        @Override
        public double doubleValue() {
            return 0;
        }
    }

    @Test
    void testByte() throws SerializationException {
        final TypeSerializer<Byte> serializer = this.serializer(Byte.class);

        final byte b = (byte) 65;

        // roundtrip actual value
        this.node.set(b);
        assertEquals((Byte) b, serializer.deserialize(Byte.class, this.node));

        // test negative
        this.node.set(-65);
        assertEquals(Byte.valueOf((byte) -65), serializer.deserialize(Byte.class, this.node));

        // test too large
        this.node.set(348);
        assertThrows(SerializationException.class, () -> serializer.deserialize(Byte.class, this.node));

        // from float
        this.node.set(65f);
        assertEquals((Byte) b, serializer.deserialize(Byte.class, this.node));

        // from string
        this.node.set("65");
        assertEquals((Byte) b, serializer.deserialize(Byte.class, this.node));

        // from hex
        this.node.set("0x41");
        assertEquals((Byte) b, serializer.deserialize(Byte.class, this.node));

        // from binary
        this.node.set("0b1000001");
        assertEquals((Byte) b, serializer.deserialize(Byte.class, this.node));
    }

    @Test
    void testShort() throws SerializationException {
        final TypeSerializer<Short> serializer = this.serializer(Short.class);

        final short b = (short) 32486;

        // roundtrip actual value
        this.node.set((int) b);
        assertEquals((Short) b, serializer.deserialize(Short.class, this.node));

        // test negative
        this.node.set(-32486);
        assertEquals(Short.valueOf((short) -32486), serializer.deserialize(Short.class, this.node));

        // test too large
        this.node.set(348333333);
        assertThrows(SerializationException.class, () -> serializer.deserialize(Short.class, this.node));

        // from float

        this.node.set(32486f);
        assertEquals((Short) b, serializer.deserialize(Short.class, this.node));

        // from string
        this.node.set("32486");
        assertEquals((Short) b, serializer.deserialize(Short.class, this.node));

        // from hex
        this.node.set("0x7ee6");
        assertEquals((Short) b, serializer.deserialize(Short.class, this.node));

        // from binary
        this.node.set("0b111111011100110");
        assertEquals((Short) b, serializer.deserialize(Short.class, this.node));

    }

    @Test
    void testInt() throws Exception {
        final TypeSerializer<Integer> serializer = this.serializer(Integer.class);

        final int i = 48888333;

        // roundtrip actual value
        this.node.set(i);
        assertEquals((Integer) i, serializer.deserialize(Integer.class, this.node));

        // test negative
        this.node.set(-595959595);
        assertEquals((Integer) (-595959595), serializer.deserialize(Integer.class, this.node));

        // test too large
        this.node.set(333339003003030L);
        assertThrows(SerializationException.class, () -> serializer.deserialize(Integer.class, this.node));

        // from double
        this.node.set(48888333d);
        assertEquals((Integer) i, serializer.deserialize(Integer.class, this.node));

        // with fraction
        this.node.set(48888333.4d);
        assertThrows(CoercionFailedException.class, () -> serializer.deserialize(Integer.class, this.node));

        // from string
        this.node.set("48888333");
        assertEquals((Integer) i, serializer.deserialize(Integer.class, this.node));

        // from hex
        this.node.set("0x2E9FA0D");
        assertEquals((Integer) i, serializer.deserialize(Integer.class, this.node));

        // from hex but lowercase
        this.node.set("0x2e9fa0d");
        assertEquals((Integer) i, serializer.deserialize(Integer.class, this.node));

        // from binary
        this.node.set("0b10111010011111101000001101");
        assertEquals((Integer) i, serializer.deserialize(Integer.class, this.node));
    }

    @Test
    void testLong() throws Exception {
        final TypeSerializer<Long> serializer = this.serializer(Long.class);

        final long i = 48888333494404L;

        // roundtrip actual value
        this.node.set(i);
        assertEquals((Long) i, serializer.deserialize(Long.class, this.node));

        // test negative
        this.node.set(-595959595);
        assertEquals((Long) (-595959595L), serializer.deserialize(Long.class, this.node));

        // from float
        this.node.set(48888333494404d);
        assertEquals((Long) i, serializer.deserialize(Long.class, this.node));

        // from string
        this.node.set("48888333494404");
        assertEquals((Long) i, serializer.deserialize(Long.class, this.node));

        // from hex
        this.node.set("0x2c76b3c06884");
        assertEquals((Long) i, serializer.deserialize(Long.class, this.node));

        // from binary
        this.node.set("0b1011000111011010110011110000000110100010000100");
        assertEquals((Long) i, serializer.deserialize(Long.class, this.node));
    }

    @Test
    void testFloat() throws Exception {
        final TypeSerializer<Float> serializer = this.serializer(Float.class);

        final float i = 3.1415f;

        // roundtrip actual value
        this.node.set(i);
        assertEquals((Float) i, serializer.deserialize(Float.class, this.node));

        // test negative
        this.node.set(-595.34f);
        assertEquals((Float) (-595.34f), serializer.deserialize(Float.class, this.node));

        // test too large
        this.node.set(13.4e129d);
        assertThrows(SerializationException.class, () -> serializer.deserialize(Float.class, this.node));

        // from int
        this.node.set(448);
        assertEquals((Float) 448f, serializer.deserialize(Float.class, this.node));

        // from string
        this.node.set("3.1415");
        assertEquals((Float) i, serializer.deserialize(Float.class, this.node));
    }

    // https://github.com/SpongePowered/Configurate/issues/198
    @Test
    void testFloatPrecision() throws SerializationException {
        final TypeSerializer<Float> serializer = this.serializer(Float.class);

        final float expected = -34.050217f;
        final double expectedAsFloatAsDouble = -34.050217f;
        final double expectedAsDouble = -34.050217d;

        this.node.set(expected);
        assertEquals(expected, serializer.deserialize(Float.class, this.node));

        this.node.set(expectedAsFloatAsDouble);
        assertEquals(expected, serializer.deserialize(Float.class, this.node));

        this.node.set(expectedAsDouble);
        assertEquals(expected, serializer.deserialize(Float.class, this.node));
    }

    @Test
    void testDouble() throws Exception {
        final TypeSerializer<Double> serializer = this.serializer(Double.class);

        final double i = 3.1415e180d;

        // roundtrip actual value
        this.node.set(i);
        assertEquals((Double) i, serializer.deserialize(Double.class, this.node));

        // test negative
        this.node.set(-595.34e180d);
        assertEquals((Double) (-595.34e180d), serializer.deserialize(Double.class, this.node));

        // from int
        this.node.set(448);
        assertEquals((Double) 448d, serializer.deserialize(Double.class, this.node));

        // from string
        this.node.set("3.1415e180");
        assertEquals((Double) i, serializer.deserialize(Double.class, this.node));
    }

    @Test
    void testFloatFromDoubleZeroes() throws Exception {
        final TypeSerializer<Float> serializer = this.serializer(Float.class);

        this.node.set(0d);
        assertEquals(0f, serializer.deserialize(Float.class, this.node));

        this.node.set(-0d);
        assertEquals(-0f, serializer.deserialize(Float.class, this.node));
    }

}
