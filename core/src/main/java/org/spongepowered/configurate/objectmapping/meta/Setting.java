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

import com.google.errorprone.annotations.Keep;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field to be targeted by the object mapper.
 *
 * <p>Optionally, a path override can be provided.</p>
 *
 * <p>This annotation is not required on fields unless the
 * {@link NodeResolver#onlyWithSetting()} resolver filter has been applied to
 * the loading object mapper.</p>
 *
 * @since 4.0.0
 */
@Keep
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Setting {

    /**
     * The path this setting is located at.
     *
     * @return the path
     * @since 4.0.0
     */
    String value() default "";

    /**
     * Whether a field should use its containing node for its value.
     *
     * @return whether this field should source its data from the node of
     *     its container
     * @since 4.0.0
     */
    boolean nodeFromParent() default false;

}
