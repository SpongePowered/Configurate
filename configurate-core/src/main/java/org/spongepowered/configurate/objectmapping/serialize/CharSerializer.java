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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ScopedConfigurationNode;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;

import java.util.function.Predicate;

class CharSerializer implements TypeSerializer<Character> {
    static Predicate<TypeToken<Character>> predicate() {
        return it -> {
            Class<?> rawType = it.getRawType();
            return rawType.equals(char.class) || rawType.equals(Character.class);
        };
    }

    @Nullable
    @Override
    public <Node extends ScopedConfigurationNode<Node>> Character deserialize(@NonNull TypeToken<?> type, @NonNull Node node) throws ObjectMappingException {
        if (node.isList() || node.isMap()) {
            return null;
        }

        Object val = node.getValue();
        if (val instanceof String) {
            String strVal = ((String) val);
            if (strVal.length() == 1) {
                return strVal.charAt(0);
            }
        } else if (val instanceof Character) {
            return ((Character) val);
        } else if (val instanceof Number) {
            return (char) ((Number) val).shortValue();
        }
        return null;
    }

    @Override
    public <T extends ScopedConfigurationNode<T>> void serialize(@NonNull TypeToken<?> type, @Nullable Character obj, @NonNull T node) throws ObjectMappingException {
        if (node.getOptions().acceptsType(char.class)) {
            node.setValue(obj);
        } else {
            node.setValue(obj == null ? null : obj.toString());
        }

    }
}
