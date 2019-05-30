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

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A calculated collection of {@link TypeSerializer}s
 */
public class TypeSerializerCollection {
    private final TypeSerializerCollection parent;
    private final SerializerList serializers = new SerializerList();
    private final Map<TypeToken<?>, TypeSerializer<?>> typeMatches = new ConcurrentHashMap<>();

    TypeSerializerCollection(TypeSerializerCollection parent) {
        this.parent = parent;
    }

    @SuppressWarnings("unchecked")
    public <T> TypeSerializer<T> get(TypeToken<T> type) {
        Preconditions.checkNotNull(type, "type");
        type = type.wrap();

        TypeSerializer<?> serial = typeMatches.computeIfAbsent(type, serializers);
        if (serial == null && parent != null) {
            serial = parent.get(type);
        }

        return (TypeSerializer) serial;
    }

    /**
     * Register a type serializer for a given type. Serializers registered will match all subclasses of the provided
     * type, as well as unwrapped primitive equivalents of the type.
     *
     * @param type The type to accept
     * @param serializer The serializer that will be serialized with
     * @param <T> The type to generify around
     * @return this
     */
    public <T> TypeSerializerCollection registerType(TypeToken<T> type, TypeSerializer<? super T> serializer) {
        Preconditions.checkNotNull(type, "type");
        Preconditions.checkNotNull(serializer, "serializer");
        serializers.add(new RegisteredSerializer(type, serializer));
        typeMatches.clear();
        return this;
    }

    /**
     * Register a type serializer matching against a given predicate.
     *
     * @param test The predicate to match types against
     * @param serializer The serializer to serialize matching types with
     * @param <T> The type parameter
     * @return this
     */
    @SuppressWarnings("unchecked")
    public <T> TypeSerializerCollection registerPredicate(Predicate<TypeToken<T>> test, TypeSerializer<? super T> serializer) {
        Preconditions.checkNotNull(test, "test");
        Preconditions.checkNotNull(serializer, "serializer");
        serializers.add(new RegisteredSerializer((Predicate) test, serializer));
        typeMatches.clear();
        return this;
    }

    public TypeSerializerCollection newChild() {
        return new TypeSerializerCollection(this);
    }

    private static final class RegisteredSerializer {
        private final Predicate<TypeToken<?>> predicate;
        private final TypeSerializer<?> serializer;

        private RegisteredSerializer(Predicate<TypeToken<?>> predicate, TypeSerializer<?> serializer) {
            this.predicate = predicate;
            this.serializer = serializer;
        }

        private RegisteredSerializer(TypeToken<?> type, TypeSerializer<?> serializer) {
            this(new SuperTypePredicate(type), serializer);
        }
    }

    /**
     * Effectively a predicate which is <code>type::isSupertypeOf</code>.
     *
     * <p>The isSupertypeOf method was only added in Guava 19.0, and was previously named
     * isAssignableFrom.</p>
     */
    private static final class SuperTypePredicate implements Predicate<TypeToken<?>> {
        private static final Method SUPERTYPE_TEST;
        static {
            Method supertypeTest;
            try {
                supertypeTest = TypeToken.class.getMethod("isSupertypeOf", TypeToken.class);
            } catch (NoSuchMethodException e1) {
                try {
                    supertypeTest = TypeToken.class.getMethod("isAssignableFrom", TypeToken.class);
                } catch (NoSuchMethodException e2) {
                    throw new RuntimeException("Unable to get TypeToken#isSupertypeOf or TypeToken#isAssignableFrom method");
                }
            }
            SUPERTYPE_TEST = supertypeTest;
        }

        private final TypeToken<?> type;

        SuperTypePredicate(TypeToken<?> type) {
            this.type = type;
        }

        @Override
        public boolean test(TypeToken<?> t) {
            try {
                return (boolean) SUPERTYPE_TEST.invoke(type, t);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private static final class SerializerList extends CopyOnWriteArrayList<RegisteredSerializer> implements Function<TypeToken<?>, TypeSerializer<?>> {

        @Override
        public TypeSerializer<?> apply(TypeToken<?> type) {
            for (RegisteredSerializer ent : this) {
                if (ent.predicate.test(type)) {
                    return ent.serializer;
                }
            }
            return null;
        }
    }

}
