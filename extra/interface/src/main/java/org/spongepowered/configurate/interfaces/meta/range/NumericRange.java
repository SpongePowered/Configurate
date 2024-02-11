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
package org.spongepowered.configurate.interfaces.meta.range;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation limits the values that a config node can have.
 * Because of annotation limits, there is an annotation for:
 * {@link DecimalRange decimals}, {@link NumericRange numerics} and
 * {@link StringRange String length}.
 *
 * @since 4.2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NumericRange {
    /**
     * The minimal value allowed (inclusive.)
     *
     * @return the minimal value allowed (inclusive.)
     * @since 4.2.0
     */
    long from();

    /**
     * The maximal value allowed (inclusive.)
     *
     * @return the maximal value allowed (inclusive.)
     * @since 4.2.0
     */
    long to();

}
