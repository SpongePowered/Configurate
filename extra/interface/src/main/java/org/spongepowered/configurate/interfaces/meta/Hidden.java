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
package org.spongepowered.configurate.interfaces.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation allows you to read the config node as normal, but it only
 * writes the node when the value is not the default value. This is to ensure
 * that when a user manually adds the entry, it remains there (as long as it's
 * not the default value.)
 *
 * <p>Without a default value the annotated node will be read, but will never
 * be written even if the user explicitly added it to their config.</p>
 *
 * <b>Note that Hidden doesn't work with default method getters due to a
 * limitation, and Hidden will function like it doesn't have a default
 * value.</b>
 *
 * @since 4.2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface Hidden {
}
