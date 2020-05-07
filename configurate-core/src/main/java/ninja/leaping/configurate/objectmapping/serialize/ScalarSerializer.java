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
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.function.Predicate;

/**
 * Serialize a value that can be represented as a scalar value within a node. Implementations must be able
 * to serialize when one of the accepted types is a {@link String}, and may support any other types as desired.
 * <p>
 * When serializing to a node, null values will be passed through directly. If the type serialized by this serializer is
 * one of the native types of the backing node, it will be written directly to the node without any transformation.
 * <p>
 * Any serialized value must be deserializable by the same serializer..
 *
 * @param <T> The object type to serialize
 */
public abstract class ScalarSerializer<T> implements TypeSerializer<T> {
    private final TypeToken<T> type;

    protected ScalarSerializer(TypeToken<T> type) {
        this.type = type.wrap();
    }

    protected ScalarSerializer(Class<T> type) {
        if (type.getTypeParameters().length > 0) {
            throw new IllegalArgumentException("Provided type " + type + " has type parameters but was not provided as a TypeToken!");
        }
        this.type = TypeToken.of(type);
    }

    /**
     * Get the general type token applicable for this serializer. This token may be parameterized.
     *
     * @return The type token for this serializer
     */
    public final TypeToken<T> type() {
        return this.type;
    }

    @Override
    public final @Nullable T deserialize(TypeToken<?> type, ConfigurationNode node) throws ObjectMappingException {
        ConfigurationNode deserializeFrom = node;
        if (node.isList()) {
            List<? extends ConfigurationNode> children = node.getChildrenList();
            if (children.size() == 1) {
                deserializeFrom = children.get(0);
            }
        }

        if (deserializeFrom.isList() || deserializeFrom.isMap()) {
            throw new ObjectMappingException("Value must be provided as a scalar!");
        }

        @Nullable Object value = deserializeFrom.getValue();
        if (value == null) {
            return null;
        }

        type = type.wrap(); // every primitive type should be boxed (cuz generics!)
        @Nullable T possible = cast(value);
        if (possible != null) {
            return possible;
        }

        return deserialize(type, value);
    }

    @Override
    public final void serialize(TypeToken<?> type, @Nullable T obj, ConfigurationNode node) {
        if (obj == null) {
            node.setValue(null);
            return;
        }

        if (node.getOptions().acceptsType(obj.getClass())) {
            node.setValue(obj);
            return;
        }

        node.setValue(serialize(obj, node.getOptions()::acceptsType));
    }

    /**
     * Given an object of unknown type, attempt to convert it into the given type.
     *
     * @param type The specific type of the type's usage
     * @param obj  The object to convert
     * @return A converted object
     * @throws ObjectMappingException If the object could not be converted for any reason
     */
    public abstract T deserialize(TypeToken<?> type, Object obj) throws ObjectMappingException;

    /**
     * @param item          The value to serialize
     * @param typeSupported A predicate to allow choosing which types are supported
     * @return A serialized form of this object
     */
    public abstract Object serialize(T item, Predicate<Class<?>> typeSupported);

    /**
     * Attempt to deserialize the provided object using an unspecialized type. This may fail on more complicated
     * deserialization processes
     *
     * @param value The object to deserialize.
     * @return The deserialized object, if possible
     * @throws ObjectMappingException If unable to coerce the value to the requested type
     */
    public final T deserialize(Object value) throws ObjectMappingException {
        @Nullable T possible = cast(value);
        if (possible != null) {
            return possible;
        }

        return this.deserialize(this.type(), value);
    }

    @SuppressWarnings("unchecked")
    private @Nullable T cast(Object value) {
        Class<?> rawType = this.type().getRawType();
        if (rawType.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    /**
     * Attempt to deserialize the provided object, but rather than throwing an exception when a parse error occurs,
     * return null instead.
     *
     * @param obj The object to try to deserialize
     * @return An instance of the appropriate type, or null
     * @see #deserialize(Object)
     */
    public final @Nullable T tryDeserialize(@Nullable Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            return deserialize(obj);
        } catch (ObjectMappingException ex) {
            return null;
        }
    }

    /**
     * Serialize the item to a {@link String}, in a representation that can be interpreted by this serializer again
     *
     * @param item The item to serialize
     * @return The serialized form of the item
     */
    public final String serializeToString(T item) {
        if (item instanceof CharSequence) {
            return item.toString();
        }
        // Otherwise, use the serializer
        return (String) serialize(item, clazz -> clazz.isAssignableFrom(String.class));
    }

}
