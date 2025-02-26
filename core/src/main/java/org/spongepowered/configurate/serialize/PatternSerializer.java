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

import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.AnnotatedType;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

final class PatternSerializer extends ScalarSerializer.Annotated<Pattern> {

    PatternSerializer() {
        super(Pattern.class);
    }

    @Override
    public Pattern deserialize(final AnnotatedType type, final Object obj) throws SerializationException {
        try {
            final @Nullable PatternFlags flags = type.getAnnotation(PatternFlags.class);
            if (flags != null) {
                return Pattern.compile(obj.toString(), flags.value());
            } else {
                return Pattern.compile(obj.toString());
            }
        } catch (final PatternSyntaxException ex) {
            throw new SerializationException(ex);
        }
    }

    @Override
    public Object serialize(final AnnotatedType type, final Pattern item, final Predicate<Class<?>> typeSupported) {
        return item.pattern();
    }

}
