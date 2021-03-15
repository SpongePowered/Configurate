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

import io.leangen.geantyref.GenericTypeReflector;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.util.CheckedConsumer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

final class SetSerializer extends AbstractListChildSerializer<Set<?>> {

    @SuppressWarnings("PMD")
    static boolean accepts(final Type type) {
        final Class<?> erased = GenericTypeReflector.erase(type);
        return Set.class.isAssignableFrom(erased) && (erased.isAssignableFrom(EnumSet.class) || erased.isAssignableFrom(LinkedHashSet.class));
    }

    SetSerializer() {
    }

    @Override
    protected Type elementType(final Type containerType) throws SerializationException {
        if (!(containerType instanceof ParameterizedType)) {
            throw new SerializationException("Raw types are not supported for collections");
        }
        return ((ParameterizedType) containerType).getActualTypeArguments()[0];
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Set<?> createNew(final int length, final Type elementType) {
        final Class<?> erased = GenericTypeReflector.erase(elementType);
        if (erased.isEnum()) {
            return EnumSet.noneOf(erased.asSubclass(Enum.class));
        }
        return new LinkedHashSet<>(length);
    }

    @Override
    protected void forEachElement(final Set<?> collection,
            final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
        for (Object el: collection) {
            action.accept(el);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected void deserializeSingle(final int index, final Set<?> collection, final @Nullable Object deserialized) {
        ((Set) collection).add(deserialized);
    }

}
