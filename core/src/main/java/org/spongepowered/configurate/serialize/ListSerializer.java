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

import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.util.CheckedConsumer;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.util.ArrayList;
import java.util.List;

final class ListSerializer extends AbstractListChildSerializer<List<?>> {

    static final TypeToken<List<?>> TYPE = new TypeToken<List<?>>() {};

    ListSerializer() {
    }

    @Override
    protected AnnotatedType elementType(final AnnotatedType containerType) throws SerializationException {
        if (!(containerType instanceof AnnotatedParameterizedType)) {
            throw new SerializationException(containerType, "Raw types are not supported for collections");
        }
        return ((AnnotatedParameterizedType) containerType).getAnnotatedActualTypeArguments()[0];
    }

    @Override
    protected List<?> createNew(final int length, final AnnotatedType elementType) {
        return new ArrayList<>(length);
    }

    @Override
    protected void forEachElement(final List<?> collection,
            final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
        for (final Object el: collection) {
            action.accept(el);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected void deserializeSingle(final int index, final List<?> collection, final @Nullable Object deserialized) {
        ((List) collection).add(deserialized);
    }

}
