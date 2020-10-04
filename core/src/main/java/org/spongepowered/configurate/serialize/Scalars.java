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

import java.net.URL;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Scalar value serializers available
 *
 * <p>Each of these serializers can be used through a configuration node, or
 * directly with a value.
 */
public final class Scalars {

    private Scalars() {}

    /**
     * Serializer for {@code boolean} values.
     *
     * <p>Case-insensitive true values are: {@code true}, {@code t},
     * {@code yes}, {@code y}, and {@code 1}.</p>
     *
     * <p>Case-insensitive false values are: {@code false}, {@code f},
     * {@code no}, {@code n}, and {@code 0}</p>
     */
    public static final ScalarSerializer<Boolean> BOOLEAN = new BooleanSerializer();

    /**
     * Serializer for {@link String} values.
     *
     * <p>Values that are not already strings are converted
     * using {@link Object#toString()}.</p>
     */
    public static final ScalarSerializer<String> STRING = new StringSerializer();

    /**
     * Serializer for {@code char} values.
     *
     * <p>A character can be converted from a 1-long {@link String}, or
     * a number.</p>
     */
    public static final ScalarSerializer<Character> CHAR = new CharSerializer();

    /**
     * Serializer for values in {@code enum} classes.
     *
     * <p>Value lookup is case-insensitive and ignores underscores.</p>
     */
    public static final ScalarSerializer<Enum<?>> ENUM = new EnumValueSerializer();

    /**
     * Serializer for {@link Pattern} values.
     *
     * <p>Patterns will be compiled with default options.</p>
     */
    public static final ScalarSerializer<Pattern> PATTERN = new PatternSerializer();

    /**
     * Serializer for {@link java.net.URI} values.
     */
    public static final ScalarSerializer<java.net.URI> URI = new UriSerializer();

    /**
     * Serializer for {@link URL} values.
     */
    public static final ScalarSerializer<URL> URL = new UrlSerializer();

    /**
     * Serializer for {@link UUID} values.
     *
     * <p>UUIDs will be accept in RFC format, and RFC format without
     * dashes (Mojang style).</p>
     */
    public static final ScalarSerializer<UUID> UUID = new UuidSerializer();

    /**
     * Serializer for {@link Byte} values.
     */
    public static final ScalarSerializer<Byte> BYTE = NumericSerializers.BYTE;

    /**
     * Serializer for {@link Short} values.
     */
    public static final ScalarSerializer<Short> SHORT = NumericSerializers.SHORT;

    /**
     * Serializer for {@link Integer} values.
     */
    public static final ScalarSerializer<Integer> INTEGER = NumericSerializers.INTEGER;

    /**
     * Serializer for {@link Long} values.
     */
    public static final ScalarSerializer<Long> LONG = NumericSerializers.LONG;

    /**
     * Serializer for {@link Float} values.
     */
    public static final ScalarSerializer<Float> FLOAT = NumericSerializers.FLOAT;

    /**
     * Serializer for {@link Double} values.
     */
    public static final ScalarSerializer<Double> DOUBLE = NumericSerializers.DOUBLE;

}
