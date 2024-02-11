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
package org.spongepowered.configurate.interfaces.meta.defaults;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation provides a default value for the annotated method.
 * Because of annotation limits, there is an annotation for:
 * {@link DefaultBoolean booleans}, {@link DefaultDecimal decimals},
 * {@link DefaultNumeric numerics} and {@link DefaultString Strings}.
 *
 * @since 4.2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface DefaultBoolean {

    /**
     * The default value for the annotated method.
     *
     * @return the default value for the annotated method.
     * @since 4.2.0
     */
    boolean value() default false;

}
