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
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * A number of {@link TypeSerializer}s provided by configurate.
 */
public class TypeSerializers {
    static final TypeSerializerCollection DEFAULT_SERIALIZERS = new TypeSerializerCollection(null);

    /**
     * Gets the default {@link TypeSerializer}s.
     *
     * @return The default serializers
     * @deprecated see {@link TypeSerializerCollection#defaults()}
     */
    @Deprecated
    public static TypeSerializerCollection getDefaultSerializers() {
        return TypeSerializerCollection.defaults();
    }

    /**
     * Creates a new collection with the Configurate defaults as a parent
     *
     * @return the new collection
     * @deprecated see {@link TypeSerializerCollection#create()}
     */
    @Deprecated
    public static TypeSerializerCollection newCollection() {
        return TypeSerializerCollection.create();
    }

    static {
        DEFAULT_SERIALIZERS.register(TypeToken.of(URI.class), new URISerializer());
        DEFAULT_SERIALIZERS.register(TypeToken.of(URL.class), new URLSerializer());
        DEFAULT_SERIALIZERS.register(TypeToken.of(UUID.class), new UUIDSerializer());
        DEFAULT_SERIALIZERS.register(input -> input.getRawType().isAnnotationPresent(ConfigSerializable.class), new AnnotatedObjectSerializer());
        DEFAULT_SERIALIZERS.register(NumberSerializer.getPredicate(), new NumberSerializer());
        DEFAULT_SERIALIZERS.register(TypeToken.of(Character.class), new CharSerializer());
        DEFAULT_SERIALIZERS.register(TypeToken.of(String.class), new StringSerializer());
        DEFAULT_SERIALIZERS.register(TypeToken.of(Boolean.class), new BooleanSerializer());
        DEFAULT_SERIALIZERS.register(new TypeToken<Map<?, ?>>() {}, new MapSerializer());
        DEFAULT_SERIALIZERS.register(new TypeToken<List<?>>() {}, new ListSerializer());
        DEFAULT_SERIALIZERS.register(new TypeToken<Enum<?>>() {}, new EnumValueSerializer());
        DEFAULT_SERIALIZERS.register(TypeToken.of(Pattern.class), new PatternSerializer());
        DEFAULT_SERIALIZERS.register(ArraySerializer.Objects.predicate(), new ArraySerializer.Objects());
        DEFAULT_SERIALIZERS.register(TypeToken.of(boolean[].class), new ArraySerializer.Booleans());
        DEFAULT_SERIALIZERS.register(TypeToken.of(byte[].class), new ArraySerializer.Bytes());
        DEFAULT_SERIALIZERS.register(TypeToken.of(char[].class), new ArraySerializer.Chars());
        DEFAULT_SERIALIZERS.register(TypeToken.of(short[].class), new ArraySerializer.Shorts());
        DEFAULT_SERIALIZERS.register(TypeToken.of(int[].class), new ArraySerializer.Ints());
        DEFAULT_SERIALIZERS.register(TypeToken.of(long[].class), new ArraySerializer.Longs());
        DEFAULT_SERIALIZERS.register(TypeToken.of(float[].class), new ArraySerializer.Floats());
        DEFAULT_SERIALIZERS.register(TypeToken.of(double[].class), new ArraySerializer.Doubles());
        DEFAULT_SERIALIZERS.register(new TypeToken<Set<?>>() {}, new SetSerializer());
    }
}
