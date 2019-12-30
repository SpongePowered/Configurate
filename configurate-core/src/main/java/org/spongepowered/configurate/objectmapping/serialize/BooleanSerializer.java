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
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.Types;
import org.spongepowered.configurate.objectmapping.InvalidTypeException;

class BooleanSerializer implements TypeSerializer<Boolean> {
    @Override
    public Boolean deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode<?> value) throws InvalidTypeException {
        return value.getBoolean();
    }

    @Override
    public void serialize(@NonNull TypeToken<?> type, @Nullable Boolean obj, @NonNull ConfigurationNode<?> value) {
        value.setValue(Types.asBoolean(obj));
    }
}
