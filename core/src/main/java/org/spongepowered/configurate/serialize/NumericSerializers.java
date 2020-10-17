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

import java.util.function.BiFunction;

/**
 * Scalar serializers for numeric types
 *
 * <p>These serializers attempt to make the appropriate type conversions for
 * numeric values.
 *
 * <p>In general, for any numeric type, in addition to the rules
 * of {@link ScalarSerializer},
 *
 * <ul>
 *     <li>
 *         When serializing: <ul>
 *             <li>If any larger types are applicable for serialization, convert
 *             to one of those types</li>
 *             <li>Otherwise, write as a string</li>
 *         </ul>
 *     </li>
 *     <li>
 *         When deserializing decimal types ({@code float} and {@code double}):
 *         <ul>
 *             <li>If the input value is a {@link Number}, convert it to the
 *             applicable type, throwing an exception if it is out
 *             of bounds</li>
 *             <li>If the input value is a {@link CharSequence}, attempt to
 *             parse the numeric type, with an accepted suffix of {@code f} or
 *             {@code d}, aligned with the type of number being parsed</li>
 *         </ul>
 *     </li>
 *     <li>
 *         When deserializing whole number types ({@code byte}, {@code short},
 *         {@code int}, and {@code long}): <ul>
 *             <li>If the input value is a {@link Float} or {@link Double}, is a
 *             whole number, and is within the bounds of the return type, return
 *             the casted value</li>
 *             <li>If the input value is a {@link Number}, convert it to the
 *             applicable type, throwing an exception if it is out
 *             of bounds</li>
 *             <li>If the input value is a {@link CharSequence}, attempt to
 *             parse the numeric type with the following affixes:
 *             <ul>
 *                 <li>prefix {@code 0x} to interpret as a hex value</li>
 *                 <li>prefix {@code #} to interpret as a hex value</li>
 *                 <li>prefix {@code 0b} to interpret as a binary value</li>
 *                 <li>suffix {@code u} to interpret as an unsigned value</li>
 *                 <li>An accepted suffix of {@code b}, {@code s}, {@code i},
 *                 or {@code l} (after {@code u} if present), aligned with the
 *                 type of number being parsed</li>
 *             </ul>
 *             </li>
 *         </ul>
 *     </li>
 * </ul>
 */
final class NumericSerializers {

    private static final float EPSILON = Float.MIN_NORMAL;

    private NumericSerializers() {}

    static final ScalarSerializer<Float> FLOAT = TypeSerializer.of(Float.class, (v, pass) -> {
        if (pass.test(Double.class)) {
            return v.doubleValue();
        } else {
            return v.toString();
        }
    }, v -> {
            if (v instanceof Number) {
                final double d = ((Number) v).doubleValue();
                if (d > Float.MAX_VALUE || d < Float.MIN_VALUE) {
                    throw new SerializationException("Value " + d + " is out of bounds of a float");
                }
                return (float) d;
            } else if (v instanceof CharSequence) {
                String value = v.toString();
                if (value.endsWith("f") || value.endsWith("F")) {
                    value = value.substring(0, value.length() - 1);
                }
                try {
                    return Float.parseFloat(value);
                } catch (final NumberFormatException ex) {
                    throw new SerializationException(ex);
                }
            } else {
                throw new CoercionFailedException(v, "float");
            }
        });

    static final ScalarSerializer<Double> DOUBLE = TypeSerializer.of(Double.class, (v, pass) -> { // we don't want to lose precision
        return v.toString();
    }, v -> {
            if (v instanceof Number) {
                return ((Number) v).doubleValue();
            } else if (v instanceof CharSequence) {
                String value = v.toString();
                if (value.endsWith("d") || value.endsWith("D")) {
                    value = value.substring(0, value.length() - 1);
                }
                try {
                    return Double.parseDouble(value);
                } catch (final NumberFormatException ex) {
                    throw new SerializationException(ex);
                }
            } else {
                throw new CoercionFailedException(v, "double");
            }
        });

    static final ScalarSerializer<Byte> BYTE = TypeSerializer.of(Byte.class, (v, pass) -> {
        if (pass.test(Short.class)) {
            return v.shortValue();
        } else if (pass.test(Integer.class)) {
            return v.intValue();
        } else if (pass.test(Double.class)) {
            return v.doubleValue();
        } else if (pass.test(Long.class)) {
            return v.longValue();
        } else {
            return v.toString();
        }
    }, value -> {
            if (value instanceof Float
                    || value instanceof Double) {
                final double absVal = Math.abs(((Number) value).doubleValue());
                if ((absVal - Math.floor(absVal)) < EPSILON && absVal <= Byte.MAX_VALUE) {
                    return (byte) absVal;
                } else {
                    throw new CoercionFailedException(value, "byte");
                }
            }

            if (value instanceof Number) {
                final long full = ((Number) value).longValue();
                if (full > Byte.MAX_VALUE || full < Byte.MIN_VALUE) {
                    throw new SerializationException("Value " + full
                            + " is out of range for a byte ([" + Byte.MIN_VALUE + "," + Byte.MAX_VALUE + "])");
                }
                return (byte) full;
            }

            if (value instanceof CharSequence) {
                return parseNumber(value.toString(), Byte::parseByte, Byte::parseByte, "b");
            }
            throw new CoercionFailedException(value, "byte");
        });

    static final ScalarSerializer<Short> SHORT = TypeSerializer.of(Short.class, (v, pass) -> {
        if (pass.test(Integer.class)) {
            return v.intValue();
        } else if (pass.test(Long.class)) {
            return v.longValue();
        } else if (pass.test(Double.class)) {
            return v.doubleValue();
        } else {
            return v.toString();
        }
    }, value -> {
            if (value instanceof Float
                    || value instanceof Double) {
                final double absVal = Math.abs(((Number) value).doubleValue());
                if ((absVal - Math.floor(absVal)) < EPSILON && absVal <= Short.MAX_VALUE) {
                    return (short) absVal;
                } else {
                    throw new CoercionFailedException(value, "short");
                }
            }

            if (value instanceof Number) {
                final long full = ((Number) value).longValue();
                if (full > Short.MAX_VALUE || full < Short.MIN_VALUE) {
                    throw new SerializationException("Value " + full
                            + " is out of range for a short ([" + Short.MIN_VALUE + "," + Short.MAX_VALUE + "])");
                }
                return (short) full;
            }

            if (value instanceof CharSequence) {
                return parseNumber(value.toString(), Short::parseShort, Short::parseShort, "s");
            }
            throw new CoercionFailedException(value, "short");
        });

    static final ScalarSerializer<Integer> INTEGER = TypeSerializer.of(Integer.class, (v, pass) -> {
        if (pass.test(Double.class)) {
            return v.doubleValue();
        } else if (pass.test(Long.class)) {
            return v.longValue();
        } else {
            return v.toString();
        }
    }, value -> {
            if (value instanceof Float
                    || value instanceof Double) {
                final double absVal = Math.abs(((Number) value).doubleValue());
                if ((absVal - Math.floor(absVal)) < EPSILON && absVal <= Integer.MAX_VALUE) {
                    return (int) absVal;
                } else {
                    throw new CoercionFailedException(value, "int");
                }
            }

            if (value instanceof Number) {
                final long full = ((Number) value).longValue();
                if (full > Integer.MAX_VALUE || full < Integer.MIN_VALUE) {
                    throw new SerializationException("Value " + full
                            + " is out of range for an integer ([" + Integer.MIN_VALUE + "," + Integer.MAX_VALUE + "])");
                }
                return (int) full;
            }

            if (value instanceof CharSequence) {
                return parseNumber(value.toString(), Integer::parseInt, Integer::parseUnsignedInt, "i");
            }
            throw new CoercionFailedException(value, "int");
        });

    static final ScalarSerializer<Long> LONG = TypeSerializer.of(Long.class, (v, pass) -> { // serialize
        return v.toString();
    }, value -> {
            if (value instanceof Float
                    || value instanceof Double) {
                final double absVal = Math.abs(((Number) value).doubleValue());
                if ((absVal - Math.floor(absVal)) < EPSILON && absVal <= Long.MAX_VALUE) {
                    return (long) absVal;
                } else {
                    throw new CoercionFailedException(value, "long");
                }
            }

            if (value instanceof Number) {
                return ((Number) value).longValue();
            }

            if (value instanceof CharSequence) {
                return parseNumber(value.toString(), Long::parseLong, Long::parseUnsignedLong, "l");
            }
            throw new CoercionFailedException(value, "long");
        });

    /**
     * Parse a number, resolving hex and binary values, as well as a type
     * suffix, and unsigned values.
     *
     * @param input the input string
     * @param parseFunc the function to parse as a signed number
     * @param unsignedParseFunc the function to parse as an unsigned number
     * @param suffix the numeric suffix, in lowercase
     * @param <T> the number type
     * @return the parsed number
     * @throws SerializationException if unable to interpret an appropriate
     *                                number from the input string.
     */
    static <T extends Number> T parseNumber(String input,
            final BiFunction<String, Integer, T> parseFunc, final BiFunction<String, Integer, T> unsignedParseFunc,
            final String suffix) throws SerializationException {
        boolean unsigned = false;
        boolean negative = false;

        int startIdx = 0;
        int endIdx = input.length();

        // type suffix
        if (input.endsWith(suffix) || input.endsWith(suffix.toUpperCase())) {
            --endIdx;
        }

        // unsigned
        if (endIdx > 0 && input.charAt(endIdx - 1) == 'u') {
            unsigned = true;
            --endIdx;
        }

        if (input.startsWith("-", startIdx)) {
            if (unsigned) {
                throw new SerializationException("Negative numbers cannot be unsigned! (both - prefix and u suffix were used)");
            }
            negative = true;
            ++startIdx;
        } else if (input.startsWith("+", startIdx)) { // skip, positive is the default
            ++startIdx;
        }

        // bases
        int radix = 10;
        if (input.startsWith("0x", startIdx)) { // hex
            radix = 16;
            startIdx += 2;
        } else if (input.startsWith("#", startIdx)) { // hex
            radix = 16;
            ++startIdx;
        } else if (input.startsWith("0b", startIdx)) { // binary
            radix = 2;
            startIdx += 2;
        }

        input = input.substring(startIdx, endIdx);

        if (negative) { // ugly but not super avoidable without knowing the number type
            input = "-" + input;
        }
        try {
            return (unsigned ? unsignedParseFunc : parseFunc).apply(input, radix);
        } catch (final IllegalArgumentException ex) {
            throw new SerializationException(ex);
        }
    }

}
