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
import ninja.leaping.configurate.objectmapping.InvalidTypeException;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Predicate;

class NumberSerializer implements TypeSerializer<Number> {

    public static Predicate<TypeToken<Number>> getPredicate() {
        return (type) -> {
            type = type.wrap();
            Class<?> clazz = type.getRawType();
            return Integer.class.equals(clazz)
                    || Long.class.equals(clazz)
                    || Short.class.equals(clazz)
                    || Byte.class.equals(clazz)
                    || Float.class.equals(clazz)
                    || Double.class.equals(clazz);
        };
    }

    @Override
    public Number deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws InvalidTypeException {
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
    public void serialize(@NonNull TypeToken<?> type, @Nullable Number obj, @NonNull ConfigurationNode value) throws ObjectMappingException {
        if (obj == null) {
            value.setValue(null);
            return;
        }

        if (value.getOptions().acceptsType(obj.getClass())) {
            value.setValue(obj);
            return;
        }

        // We have to coerce :( hack fix until 4.0 allows what may be more breaking changes
        // any conversion will work as long as there's no data loss
        if (obj instanceof Float && value.getOptions().acceptsType(Double.class)) {
            value.setValue(obj.doubleValue());
        } else if (obj instanceof Byte) {
            if (value.getOptions().acceptsType(Short.class)) {
                value.setValue(obj.shortValue());
            } else if (value.getOptions().acceptsType(Integer.class)) {
                value.setValue(obj.intValue());
            } else if (value.getOptions().acceptsType(Long.class)) {
                value.setValue(obj.longValue());
            }
        } else if (obj instanceof Short) {
            if (value.getOptions().acceptsType(Integer.class)) {
                value.setValue(obj.intValue());
            } else if (value.getOptions().acceptsType(Long.class)) {
                value.setValue(obj.longValue());
            }
        } else if (obj instanceof Integer) {
            if (value.getOptions().acceptsType(Long.class)) {
                value.setValue(obj.longValue());
            }
        } else {
            throw new ObjectMappingException("Unable to coerce value of type " + obj.getClass() + " to one accepted by node " + value);
        }
    }
}
