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

import java.net.URL;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Scalar value serializers available
 *
 * Each of these serializers
 */
public interface Scalars {
    ScalarSerializer<Boolean> BOOLEAN = new BooleanSerializer();
    ScalarSerializer<String> STRING = new StringSerializer();
    ScalarSerializer<Character> CHAR = new CharSerializer();
    ScalarSerializer<Enum<?>> ENUM = new EnumValueSerializer();
    ScalarSerializer<Pattern> PATTERN = new PatternSerializer();
    ScalarSerializer<java.net.URI> URI = new URISerializer();
    ScalarSerializer<URL> URL = new URLSerializer();
    ScalarSerializer<UUID> UUID = new UUIDSerializer();
    ScalarSerializer<Byte> BYTE = NumericSerializers.BYTE;
    ScalarSerializer<Short> SHORT = NumericSerializers.SHORT;
    ScalarSerializer<Integer> INTEGER = NumericSerializers.INTEGER;
    ScalarSerializer<Long> LONG = NumericSerializers.LONG;
    ScalarSerializer<Float> FLOAT = NumericSerializers.FLOAT;
    ScalarSerializer<Double> DOUBLE = NumericSerializers.DOUBLE;
}
