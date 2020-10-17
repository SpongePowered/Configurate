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

import java.lang.reflect.Type;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

final class PatternSerializer extends ScalarSerializer<Pattern> {

    PatternSerializer() {
        super(Pattern.class);
    }

    @Override
    public Pattern deserialize(final Type type, final Object obj) throws SerializationException {
        try {
            return Pattern.compile(obj.toString());
        } catch (final PatternSyntaxException ex) {
            throw new SerializationException(ex);
        }
    }

    @Override
    public Object serialize(final Pattern item, final Predicate<Class<?>> typeSupported) {
        return item.pattern();
    }

}
