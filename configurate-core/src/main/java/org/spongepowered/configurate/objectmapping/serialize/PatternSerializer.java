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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

class PatternSerializer implements TypeSerializer<Pattern> {
    @Override
    public <Node extends ScopedConfigurationNode<Node>> Pattern deserialize(@NonNull TypeToken<?> type, @NonNull Node node) throws ObjectMappingException {
        String value = node.getString();
        if (value == null) {
            throw new ObjectMappingException("Node must have a string value");
        }
        try {
            return Pattern.compile(value);
        } catch (PatternSyntaxException ex) {
            throw new ObjectMappingException(ex);
        }
    }

    @Override
    public <Node extends ScopedConfigurationNode<Node>> void serialize(@NonNull TypeToken<?> type, @Nullable Pattern obj, @NonNull Node node) throws ObjectMappingException {
        node.setValue(obj == null ? null : obj.pattern());
    }
}
