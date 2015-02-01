/**
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
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.Types;
import ninja.leaping.configurate.objectmapping.InvalidTypeException;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TypeSerializers {
    private static final LinkedList<TypeSerializer> SERIALIZERS = new LinkedList<>();

    /**
     * Register a new serializer with the serialization system. This method may be called at any time.
     * Newly registered serializers will take priority.
     *
     * @param serializer The serializer to register
     */
    public static void registerSerializer(TypeSerializer serializer) {
        Preconditions.checkNotNull(serializer, "serializer");
        SERIALIZERS.addFirst(serializer);
    }

    /**
     * Get an unmodifiable list of all registered type serializers.
     *
     * @return All registered serializers
     */
    public static List<TypeSerializer> getAllSerializers() {
        return Collections.unmodifiableList(SERIALIZERS);
    }

    /**
     * Returns the most recently registered serializer applicable to the given type
     *
     * @param type The type needed to be serialized
     * @return The appropriate serializer, or {@code null} if none is available
     */
    public static TypeSerializer getSerializer(TypeToken<?> type) {
        Preconditions.checkNotNull(type, "type");
        for (TypeSerializer serializer : SERIALIZERS) {
            if (serializer.isApplicable(type)) {
                return serializer;
            }
        }
        return null;
    }

    static {
        registerSerializer(new NumberSerializer());
        registerSerializer(new BooleanSerializer());
        registerSerializer(new MapSerializer());
        registerSerializer(new ListSerializer());
        registerSerializer(new AnnotatedObjectSerializer());
        registerSerializer(new StringSerializer());
    }


    private static class StringSerializer implements TypeSerializer {
        private final TypeToken<String> stringType = TypeToken.of(String.class);

        @Override
        public boolean isApplicable(TypeToken<?> type) {
            return stringType.isAssignableFrom(type);
        }

        @Override
        public Object deserialize(TypeToken<?> type, ConfigurationNode value) throws InvalidTypeException {
            if (!isApplicable(type)) {
                throw new InvalidTypeException(type);
            }
            return value.getString();
        }

        @Override
        public void serialize(TypeToken<?> type, Object obj, ConfigurationNode value) {
            value.setValue(obj == null ? null : obj.toString());
        }
    }

    private static class NumberSerializer implements TypeSerializer {
        private final TypeToken<Number> numberType = TypeToken.of(Number.class);

        @Override
        public boolean isApplicable(TypeToken<?> type) {
            return type != null && numberType.isAssignableFrom(type.wrap());
        }

        @Override
        public Object deserialize(TypeToken<?> type, ConfigurationNode value) throws InvalidTypeException {
            if (!isApplicable(type)) {
                throw new InvalidTypeException(type);
            }
            type = type.wrap();
            Class<?> clazz = type.getRawType();
            if (Integer.class.equals(clazz)) {
                return value.getInt();
            } else if (Long.class.equals(clazz)) {
                return value.getLong();
            } else if (Short.class.equals(clazz)) {
                return (short) value.getInt();
            } else if (Byte.class.equals(clazz)) {
                return (byte) value.getInt();
            } else if (Float.class.equals(clazz)) {
                return value.getFloat();
            } else if (Double.class.equals(clazz)) {
                return value.getDouble();
            }
            return null;
        }

        @Override
        public void serialize(TypeToken<?> type, Object obj, ConfigurationNode value) {
            value.setValue(obj);
        }
    }

    private static class BooleanSerializer implements TypeSerializer {

        @Override
        public boolean isApplicable(TypeToken<?> type) {
            return type != null && Boolean.class.equals(type.wrap().getRawType());
        }

        @Override
        public Object deserialize(TypeToken<?> type, ConfigurationNode value) throws InvalidTypeException {
            if (!isApplicable(type)) {
                throw new InvalidTypeException(type);
            }
            return value.getBoolean();
        }

        @Override
        public void serialize(TypeToken<?> type, Object obj, ConfigurationNode value) {
            value.setValue(Types.asBoolean(obj));
        }
    }

    private static class MapSerializer implements TypeSerializer {

        @Override
        public boolean isApplicable(TypeToken<?> type) {
            return TypeToken.of(Map.class).isAssignableFrom(type);
        }

        @Override
        public Object deserialize(TypeToken<?> type, ConfigurationNode node) throws ObjectMappingException {
            if (!isApplicable(type)) {
                throw new InvalidTypeException(type);
            }
            Map<Object, Object> ret = new LinkedHashMap<>();
            if (node.hasMapChildren()) {
                TypeToken<?> key = type.resolveType(Map.class.getTypeParameters()[0]);
                TypeToken<?> value = type.resolveType(Map.class.getTypeParameters()[1]);
                TypeSerializer keySerial = getSerializer(key);
                TypeSerializer valueSerial = getSerializer(value);
                for (Map.Entry<Object, ? extends ConfigurationNode> ent : node.getChildrenMap().entrySet()) {
                    Object keyValue = keySerial == null ? ent.getKey() : keySerial.deserialize(key, SimpleConfigurationNode.root().setValue(ent.getKey()));
                    Object valueValue = valueSerial == null ? ent.getValue().getValue() : valueSerial.deserialize(value, ent.getValue());
                    if (keyValue == null || valueValue == null) {
                        continue;
                    }

                    ret.put(keyValue, valueValue);
                }
            }
            return ret;
        }

        @Override
        public void serialize(TypeToken<?> type, Object obj, ConfigurationNode node) throws ObjectMappingException {
            Map<?, ?> origMap = (Map<?, ?>) obj;
            TypeToken<?> key = type.resolveType(Map.class.getTypeParameters()[0]);
            TypeToken<?> value = type.resolveType(Map.class.getTypeParameters()[1]);
            TypeSerializer keySerial = getSerializer(key);
            TypeSerializer valueSerial = getSerializer(value);
            for (Map.Entry<?, ?> ent : origMap.entrySet()) {
                SimpleConfigurationNode keyNode = SimpleConfigurationNode.root();
                keySerial.serialize(value, ent.getKey(), keyNode);
                valueSerial.serialize(value, ent.getValue(), node.getChild(keyNode.getValue()));
            }
        }
    }


    private static class ListSerializer implements TypeSerializer {

        @Override
        public boolean isApplicable(TypeToken<?> type) {
            return TypeToken.of(List.class).isAssignableFrom(type);
        }

        @Override
        public Object deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
            if (!isApplicable(type)) {
                throw new InvalidTypeException(type);
            }
            if (value.hasListChildren()) {
                List<? extends ConfigurationNode> values = value.getChildrenList();
                List<Object> ret = new ArrayList<>(values.size());
                TypeToken<?> entryType = type.resolveType(List.class.getTypeParameters()[0]);
                TypeSerializer entrySerial = getSerializer(entryType);
                for (ConfigurationNode ent : values) {
                    ret.add(entrySerial == null ? ent.getValue() : entrySerial.deserialize(entryType, ent));
                }
                return ret;
            }
            return new ArrayList<>();
        }

        @Override
        public void serialize(TypeToken<?> type, Object obj, ConfigurationNode value) throws ObjectMappingException {
            List<?> origList = (List<?>) obj;
            TypeToken<?> entryType = type.resolveType(List.class.getTypeParameters()[0]);
            TypeSerializer entrySerial = getSerializer(entryType);
            value.setValue(null);
            for (Object ent : origList) {
                entrySerial.serialize(entryType, ent, value.getAppendedChild());
            }
        }
    }

    private static class AnnotatedObjectSerializer implements TypeSerializer {

        @Override
        public boolean isApplicable(TypeToken<?> type) {
            return type.getRawType().isAnnotationPresent(ConfigSerializable.class);
        }

        @Override
        public Object deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
            if (!isApplicable(type)) {
                throw new InvalidTypeException(type);
            }
            Class<?> clazz = getInstantiableType(type, value.getNode("__class__").getString());
            return ObjectMapper.mapperForClass(clazz).newInstance(value);
        }

        private Class<?> getInstantiableType(TypeToken<?> type, String configuredName) throws ObjectMappingException {
            Class<?> retClass;
            if (type.getRawType().isInterface() || Modifier.isAbstract(type.getRawType().getModifiers())) {
                if (configuredName == null) {
                    throw new ObjectMappingException("No available configured type for instances of " + type);
                } else {
                    try {
                        retClass = Class.forName(configuredName);
                    } catch (ClassNotFoundException e) {
                        throw new ObjectMappingException("Unknown class of object " + configuredName, e);
                    }
                }
            } else {
                retClass = type.getRawType();
            }
            return retClass;
        }

        @Override
        public void serialize(TypeToken<?> type, Object obj, ConfigurationNode value) throws ObjectMappingException {
            value.getNode("__class__").setValue(type.getRawType().getCanonicalName());
            ObjectMapper.mapperForObject(obj).serializeObject(obj, value);
        }
    }
}
