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

    public static final ScalarSerializer<Boolean> BOOLEAN = new BooleanSerializer();
    public static final ScalarSerializer<String> STRING = new StringSerializer();
    public static final ScalarSerializer<Character> CHAR = new CharSerializer();
    public static final ScalarSerializer<Enum<?>> ENUM = new EnumValueSerializer();
    public static final ScalarSerializer<Pattern> PATTERN = new PatternSerializer();
    public static final ScalarSerializer<java.net.URI> URI = new UriSerializer();
    public static final ScalarSerializer<URL> URL = new UrlSerializer();
    public static final ScalarSerializer<UUID> UUID = new UuidSerializer();
    public static final ScalarSerializer<Byte> BYTE = NumericSerializers.BYTE;
    public static final ScalarSerializer<Short> SHORT = NumericSerializers.SHORT;
    public static final ScalarSerializer<Integer> INTEGER = NumericSerializers.INTEGER;
    public static final ScalarSerializer<Long> LONG = NumericSerializers.LONG;
    public static final ScalarSerializer<Float> FLOAT = NumericSerializers.FLOAT;
    public static final ScalarSerializer<Double> DOUBLE = NumericSerializers.DOUBLE;

}
