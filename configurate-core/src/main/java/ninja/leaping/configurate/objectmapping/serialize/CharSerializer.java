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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;


class CharSerializer implements TypeSerializer<Character> {

    @Nullable
    @Override
    public Character deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws ObjectMappingException {
        if (value.isList() || value.isMap()) {
            return null;
        }

        Object val = value.getValue();
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
    public void serialize(@NonNull TypeToken<?> type, @Nullable Character obj, @NonNull ConfigurationNode value) throws ObjectMappingException {
        if (value.getOptions().acceptsType(char.class)) {
            value.setValue(obj);
        } else {
            value.setValue(obj == null ? null : obj.toString());
        }

    }
}
