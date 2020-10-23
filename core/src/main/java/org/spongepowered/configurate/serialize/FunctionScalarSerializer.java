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

import net.kyori.coffee.function.Function1E;
import net.kyori.coffee.function.Function2;
import net.kyori.coffee.function.Predicate1;

import java.lang.reflect.Type;

final class FunctionScalarSerializer<T> extends ScalarSerializer<T> {

    private final Function1E<Object, T, SerializationException> deserializer;
    private final Function2<T, Predicate1<Class<?>>, Object> serializer;

    FunctionScalarSerializer(final Type type,
            final Function1E<Object, T, SerializationException> deserializer, final Function2<T, Predicate1<Class<?>>, Object> serializer) {
        super(type);
        this.deserializer = deserializer;
        this.serializer = serializer;
    }

    @Override
    public T deserialize(final Type type, final Object obj) throws SerializationException {
        try {
            return this.deserializer.apply(obj);
        } catch (final SerializationException ex) {
            ex.initType(type);
            throw ex;
        }
    }

    @Override
    public Object serialize(final T item, final Predicate1<Class<?>> typeSupported) {
        return this.serializer.apply(item, typeSupported);
    }

}
