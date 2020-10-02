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

import org.checkerframework.checker.regex.qual.Regex;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Constrains a field value to ensure it matches the provided expression.
 *
 * <p>This constraint will always pass with an empty field. See {@link Required}
 * to enforce a non-null value.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Matches {

    /**
     * Pattern to test string value against.
     *
     * @return pattern to test against
     */
    @Regex String value();

    /**
     * Message to throw in an exception when a match fails.
     *
     * <p>This message will be formatted as a MessageFormat with two
     * parameters:</p>
     * <ol start="0">
     *     <li>the input string</li>
     *     <li>the pattern being matched</li>
     * </ol>
     *
     * @return message format.
     */
    String failureMessage() default "";

}
