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

import org.spongepowered.configurate.objectmapping.ObjectMappingException;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Predicate;

final class UrlSerializer extends ScalarSerializer<URL> {

    UrlSerializer() {
        super(URL.class);
    }

    @Override
    public URL deserialize(final Type type, final Object obj) throws ObjectMappingException {
        final String plainUri = obj.toString();
        try {
            return new URL(plainUri);
        } catch (final MalformedURLException e) {
            throw new CoercionFailedException(obj, "URL");
        }
    }

    @Override
    public Object serialize(final URL item, final Predicate<Class<?>> typeSupported) {
        return item.toString();
    }

}
