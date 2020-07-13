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
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;

import java.lang.reflect.Type;

public class PassthroughSerializer implements TypeSerializer<Object> {

    @Override
    public Object deserialize(final Type type, final ConfigurationNode node) throws ObjectMappingException {
        final @Nullable Object o = node.getValue();
        if (o == null) {
            throw new ObjectMappingException("No value present for node");
        }
        if (!GenericTypeReflector.isSuperType(type, o.getClass())) {
            throw new ObjectMappingException("Value returned was of type '" + o.getClass() + "', but was expected to be a subtype of '" + type + "'");
        }
        return o;
    }

    @Override
    public void serialize(final Type type, final @Nullable Object obj, final ConfigurationNode node) throws ObjectMappingException {
        node.setValue(obj);
    }

}
