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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ResourceBundle;

/**
 * A comment that will be applied to a configuration node if possible.
 *
 * <p>By default, this node will not override any user-defined comments.</p>
 *
 * <p>When used with an object mapper with a {@link Processor#comments()} or
 * {@link Processor#localizedComments(ResourceBundle)} processor applied,
 * the comment in {@link #value()} will be applied to the node upon save.</p>
 *
 * @since 4.0.0
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Comment {

    /**
     * The comment to use.
     *
     * @return comment
     * @since 4.0.0
     */
    String value();

    /**
     * Whether or not to override existing comments on a node.
     *
     * @return if we should override.
     * @since 4.0.0
     */
    boolean override() default false;

}
