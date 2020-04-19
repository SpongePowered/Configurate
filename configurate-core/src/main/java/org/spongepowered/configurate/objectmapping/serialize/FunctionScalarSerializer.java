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

import com.google.common.reflect.TypeToken;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.configurate.util.CheckedFunction;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public final class FunctionScalarSerializer<T> extends ScalarSerializer<T> {
    private final CheckedFunction<Object, T, ObjectMappingException> deserializer;
    private final BiFunction<T, Predicate<Class<?>>, Object> serializer;

    FunctionScalarSerializer(TypeToken<T> type, CheckedFunction<Object, T, ObjectMappingException> deserializer, BiFunction<T, Predicate<Class<?>>, Object> serializer) {
        super(type);
        this.deserializer = deserializer;
        this.serializer = serializer;
    }

    @Override
    public T deserialize(TypeToken<?> type, Object obj) throws ObjectMappingException {
        return deserializer.apply(obj);
    }

    @Override
    public Object serialize(T item, Predicate<Class<?>> typeSupported) {
        return serializer.apply(item, typeSupported);
    }
}
