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
package ninja.leaping.configurate.objectmapping;

import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
Instructs the {@link ObjectMapper} that the serialization and deserialization value associated with the annotated field must be handled by the supplied {@link TypeSerializer} class.

<p>Consumers should note the following:</p>
<ul>
<li>The annotated field must also be annotated with the {@link Setting} attribute in order for it to be considered by the object mapper;</li>
<li>The supplied {@link TypeSerializer} class <strong>must</strong> have a public no-args constructor - violating this restriction will cause an {@link ObjectMappingException} during object mapper creation;</li>
<li>The {@link TypeSerializer} must deserialize into a type that the field can implicity accept (for example, you cannot use a {@link String} {@link TypeSerializer} on a field that is of type {@code int}) - violating this restriction will case an {@link ObjectMappingException} during serialization or deserialization.
</ul>

<p>Note that these restrictions may be different when using derived object mappers. Consult with the documentation provided by those mappers to determine if this is the case.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Adapter {
    Class<? extends TypeSerializer<?>> value();
}
