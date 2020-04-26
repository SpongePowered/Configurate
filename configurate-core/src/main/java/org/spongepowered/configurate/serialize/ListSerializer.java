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

import com.google.common.reflect.TypeToken;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.configurate.util.CheckedConsumer;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

class ListSerializer extends AbstractListChildSerializer<List<?>> {
    static TypeToken<List<?>> TYPE = new TypeToken<List<?>>() {};

    @Override
    TypeToken<?> getElementType(TypeToken<?> containerType) throws ObjectMappingException {
        if (!(containerType.getType() instanceof ParameterizedType)) {
            throw new ObjectMappingException("Raw types are not supported for collections");
        }
        return containerType.resolveType(List.class.getTypeParameters()[0]);
    }

    @Override
    List<?> createNew(int length, TypeToken<?> elementType) {
        return new ArrayList<>(length);
    }

    @Override
    void forEachElement(List<?> collection, CheckedConsumer<Object, ObjectMappingException> action) throws ObjectMappingException {
        for (Object el: collection) {
            action.accept(el);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    void deserializeSingle(int index, List<?> collection, Object deserialized) {
        ((List) collection).add(deserialized);
    }
}
