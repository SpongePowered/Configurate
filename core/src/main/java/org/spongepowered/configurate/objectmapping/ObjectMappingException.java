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
package org.spongepowered.configurate.objectmapping;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Exception thrown on errors encountered while mapping objects.
 */
public class ObjectMappingException extends Exception {

    public static final long serialVersionUID = 2310268704411616686L;

    /**
     * Create an exception with unknown message and cause.
     */
    public ObjectMappingException() {
        super();
    }

    /**
     * Create an exception without a cause.
     *
     * @param message message with information about the exception
     */
    public ObjectMappingException(final String message) {
        super(message);
    }

    /**
     * Create an exception with message and wrapped cause.
     *
     * @param message informational message
     * @param cause cause to be wrapped
     */
    public ObjectMappingException(final @Nullable String message, final @Nullable Throwable cause) {
        super(message, cause);
    }

    /**
     * Create an exception with a cause and no additonal information.
     *
     * @param cause wrapped causing throwable
     */
    public ObjectMappingException(final Throwable cause) {
        super(cause);
    }

}
