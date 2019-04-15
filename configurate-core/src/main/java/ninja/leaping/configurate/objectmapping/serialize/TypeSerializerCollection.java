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
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A calculated collection of {@link TypeSerializer}s
 */
public class TypeSerializerCollection {
    /**
     * Indicates that, when {@link TypeSerializer}s are scanned for a type
     * match, the first serializer that was added to the collection will
     * be selected.
     *
     * <p>This is the default strategy, and prevents serializers from being
     * overriden without the use of {@link #newChild()}.</p>
     */
    public static final AdditionStrategy FIRST_WINS =
            (registeredSerializer, serializerList) -> serializerList.add(registeredSerializer);

    /**
     * Indicates that, when {@link TypeSerializer}s are scanned for a type
     * match, the last serializer that was added to the collection will
     * be selected.
     *
     * <p>This allows for any serializer to be overriden.</p>
     */
    public static final AdditionStrategy LAST_WINS =
            (registeredSerializer, serializerList) -> serializerList.add(0, registeredSerializer);

    /**
     * A strategy that:
     * <ul>
     *     <li>places any serializers that have predicate conditions at the end
     *     of the collection, and</li>
     *     <li>attempts to place serializers that have {@link TypeToken} based
     *     conditions at an appropriate point in the collection such that a
     *     serializer that works on a subtype of another serializer will be
     *     given priority.</li>
     * </ul>
     *
     * <p>The default behaviour is the same as {@link #FIRST_WINS}</p>
     */
    public static final AdditionStrategy DEPENDENCY_ORDER_OR_FIRST_WINS =
            (registeredSerializer, serializerList) -> {
                if (registeredSerializer.predicate instanceof SuperTypePredicate) {
                    // We can get information out of this
                    TypeToken<?> token = ((SuperTypePredicate) registeredSerializer.predicate).type;
                    ListIterator<RegisteredSerializer> serializerIterator = serializerList.listIterator();
                    while (serializerIterator.hasNext()) {
                        RegisteredSerializer serializer = serializerIterator.next();
                        boolean willSerialize = serializer.predicate.test(token);
                        if (willSerialize) {
                            // Now, check to see if it's the same type (if we can), if it is,
                            // we don't override, else we do.
                            if (serializer.predicate instanceof SuperTypePredicate) {
                                if (((SuperTypePredicate) serializer.predicate).type.equals(token)) {
                                    continue;
                                }
                            }

                            // This needs to act before
                            int indexToInsertAt = serializerIterator.previousIndex();
                            serializerList.add(indexToInsertAt, registeredSerializer);
                            return;
                        }
                    }
                }

                FIRST_WINS.addSerializer(registeredSerializer, serializerList);
            };

    private final TypeSerializerCollection parent;
    private final AdditionStrategy strategy;
    private final SerializerList serializers = new SerializerList();
    private final Map<TypeToken<?>, TypeSerializer<?>> typeMatches = new ConcurrentHashMap<>();

    TypeSerializerCollection(TypeSerializerCollection parent) {
        this(parent, FIRST_WINS);
    }

    TypeSerializerCollection(TypeSerializerCollection parent, AdditionStrategy strategy) {
        this.parent = parent;
        this.strategy = strategy;
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
        this.strategy.addSerializer(new RegisteredSerializer(type, serializer), this.serializers);
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
        this.strategy.addSerializer(new RegisteredSerializer((Predicate) test, serializer), this.serializers);
        typeMatches.clear();
        return this;
    }

    /**
     * Creates a new {@link TypeSerializerCollection} that uses the current
     * collection as a parent, and uses the {@link #FIRST_WINS} strategy
     * when adding new serializers.
     *
     * <p>Any {@link TypeSerializer}s that are added to the returned child
     * will take precedence over any serializer registered in this parent
     * collection.</p>
     *
     * <p>See also {@link #newChild(AdditionStrategy)}.</p>
     *
     * @return A new {@link TypeSerializerCollection} that uses this
     *      {@link TypeSerializerCollection} as its parent.
     */
    public TypeSerializerCollection newChild() {
        return new TypeSerializerCollection(this);
    }

    /**
     * Creates a new {@link TypeSerializerCollection} that uses the current
     * collection as a parent.
     *
     * <p>Any {@link TypeSerializer}s that are added to the returned child
     * will take precedence over any serializer registered in this parent
     * collection.</p>
     *
     * @param strategy The {@link AdditionStrategy} to use when adding
     *                 new {@link TypeSerializer}s to this collection.
     * @return A new {@link TypeSerializerCollection} that uses this
     *      {@link TypeSerializerCollection} as its parent.
     */
    public TypeSerializerCollection newChild(AdditionStrategy strategy) {
        return new TypeSerializerCollection(this, strategy);
    }

    /**
     * Provides logic that enables a user to select how newly registered
     * {@link TypeSerializer}s are registered. This influences which
     * serializer takes precedence if more than one matches.
     *
     * <p>Note that this cannot be implemented outside of the
     * {@link TypeSerializerCollection} class due to the use of private classes.
     * </p>
     */
    @FunctionalInterface
    public interface AdditionStrategy {

        void addSerializer(RegisteredSerializer serializer, List<RegisteredSerializer> list);
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
