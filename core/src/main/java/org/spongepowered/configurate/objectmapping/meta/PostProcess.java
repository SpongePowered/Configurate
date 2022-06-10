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
package org.spongepowered.configurate.objectmapping.meta;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import org.spongepowered.configurate.serialize.SerializationException;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicate that a method in an object-mappable object is intended for
 * post-processing.
 *
 * <p>The annotated method will be invoked after all fields in the object have
 * been populated by the object mapper.</p>
 *
 * <p>The annotated method must be non-static and may only declare a thrown type
 * of {@link SerializationException}.</p>
 *
 * @since 4.2.0
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface PostProcess {
}
