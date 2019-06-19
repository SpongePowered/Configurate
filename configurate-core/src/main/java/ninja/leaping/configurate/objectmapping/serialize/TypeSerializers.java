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

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * A number of {@link TypeSerializer}s provided by configurate.
 */
public class TypeSerializers {
    private static final TypeSerializerCollection DEFAULT_SERIALIZERS = new TypeSerializerCollection(null);

    /**
     * Gets the default {@link TypeSerializer}s.
     *
     * @return The default serializers
     */
    public static TypeSerializerCollection getDefaultSerializers() {
        return DEFAULT_SERIALIZERS;
    }

    public static TypeSerializerCollection newCollection() {
        return DEFAULT_SERIALIZERS.newChild();
    }

    static {
        DEFAULT_SERIALIZERS.registerType(TypeToken.of(URI.class), new URISerializer());
        DEFAULT_SERIALIZERS.registerType(TypeToken.of(URL.class), new URLSerializer());
        DEFAULT_SERIALIZERS.registerType(TypeToken.of(UUID.class), new UUIDSerializer());
        DEFAULT_SERIALIZERS.registerPredicate(input -> input.getRawType().isAnnotationPresent(ConfigSerializable.class), new AnnotatedObjectSerializer());
        DEFAULT_SERIALIZERS.registerPredicate(NumberSerializer.getPredicate(), new NumberSerializer());
        DEFAULT_SERIALIZERS.registerType(TypeToken.of(Character.class), new CharSerializer());
        DEFAULT_SERIALIZERS.registerType(TypeToken.of(String.class), new StringSerializer());
        DEFAULT_SERIALIZERS.registerType(TypeToken.of(Boolean.class), new BooleanSerializer());
        DEFAULT_SERIALIZERS.registerType(new TypeToken<Map<?, ?>>() {}, new MapSerializer());
        DEFAULT_SERIALIZERS.registerType(new TypeToken<List<?>>() {}, new ListSerializer());
        DEFAULT_SERIALIZERS.registerType(new TypeToken<Enum<?>>() {}, new EnumValueSerializer());
        DEFAULT_SERIALIZERS.registerType(TypeToken.of(Pattern.class), new PatternSerializer());
        DEFAULT_SERIALIZERS.registerPredicate(ArraySerializer.Objects.predicate(), new ArraySerializer.Objects());
        DEFAULT_SERIALIZERS.registerType(TypeToken.of(boolean[].class), new ArraySerializer.Booleans());
        DEFAULT_SERIALIZERS.registerType(TypeToken.of(byte[].class), new ArraySerializer.Bytes());
        DEFAULT_SERIALIZERS.registerType(TypeToken.of(char[].class), new ArraySerializer.Chars());
        DEFAULT_SERIALIZERS.registerType(TypeToken.of(short[].class), new ArraySerializer.Shorts());
        DEFAULT_SERIALIZERS.registerType(TypeToken.of(int[].class), new ArraySerializer.Ints());
        DEFAULT_SERIALIZERS.registerType(TypeToken.of(long[].class), new ArraySerializer.Longs());
        DEFAULT_SERIALIZERS.registerType(TypeToken.of(float[].class), new ArraySerializer.Floats());
        DEFAULT_SERIALIZERS.registerType(TypeToken.of(double[].class), new ArraySerializer.Doubles());
    }
}
