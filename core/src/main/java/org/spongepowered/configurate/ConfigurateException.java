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
package org.spongepowered.configurate;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * Any sort of error thrown within Configurate.
 *
 * <p>Configurate's errors are designed to provide a view of as
 * many errors as possible within one configuration tree, through the
 * {@link Throwable#getSuppressed() suppressed exceptions}</p>
 *
 * @since 4.0.0
 */
public class ConfigurateException extends IOException {

    private static final long serialVersionUID = 1635526451813128733L;

    private @Nullable Supplier<NodePath> path;

    /**
     * Given an unknown {@link IOException}, return it as a Configurate type.
     *
     * <p>If the input {@code ex} is already a {@link ConfigurateException},
     * this method returns the input value.</p>
     *
     * @param source node where the source exception was thrown
     * @param ex the source exception
     * @return an exception, either casted or wrapped
     * @since 4.0.0
     */
    public static ConfigurateException wrap(final ConfigurationNode source, final IOException ex) {
        if (ex instanceof ConfigurateException) {
            return (ConfigurateException) ex;
        } else {
            return new ConfigurateException(source, ex);
        }
    }

    /**
     * Create a new unknown exception.
     *
     * @since 4.0.0
     */
    public ConfigurateException() {
    }

    /**
     * Create a new exception at unknown path with provided
     * informational message.
     *
     * @param message informational message
     * @since 4.0.0
     */
    public ConfigurateException(final String message) {
        super(message);
    }

    /**
     * Create a new exception with a cause and unknown message.
     *
     * @param cause the cause of this exception
     * @since 4.0.0
     */
    public ConfigurateException(final Throwable cause) {
        super(cause);
    }

    /**
     * Create a new exception with informational message and cause.
     *
     * @param message the informational message
     * @param cause the cause of the exception
     * @since 4.0.0
     */
    public ConfigurateException(final @Nullable String message, final @Nullable Throwable cause) {
        super(message, cause);
    }

    /**
     * Create a new exception pre-initialized with path and message.
     *
     * @param pos node where the error occurred
     * @param message message describing the error
     * @since 4.0.0
     */
    public ConfigurateException(final ConfigurationNode pos, final String message) {
        super(message);
        this.path = pos::path;
    }

    /**
     * Create a new exception pre-initialized with path and cause.
     *
     * @param pos node where the error occurred
     * @param cause direct cause of this exception
     * @since 4.0.0
     */
    public ConfigurateException(final ConfigurationNode pos, final Throwable cause) {
        super(cause);
        this.path = pos::path;
    }

    /**
     * Create a new exception pre-initialized with path, message, and cause.
     *
     * @param pos node where the error occurred
     * @param message message describing the error
     * @param cause direct cause of this exception
     * @since 4.0.0
     */
    public ConfigurateException(final ConfigurationNode pos, final @Nullable String message, final @Nullable Throwable cause) {
        super(message, cause);
        this.path = pos::path;
    }

    /**
     * Create a new exception pre-initialized with path, message, and cause.
     *
     * @param path path to the node where the error occurred
     * @param message message describing the error
     * @param cause direct cause of this exception
     * @since 4.0.0
     */
    public ConfigurateException(final NodePath path, final @Nullable String message, final @Nullable Throwable cause) {
        super(message, cause);
        this.path = () -> path;
    }

    /**
     * Get the path associated with this failure.
     *
     * @return the path
     * @since 4.0.0
     */
    public NodePath path() {
        final @Nullable Supplier<NodePath> path = this.path;
        return path == null ? NodePath.path() : path.get();
    }

    /**
     * Initialize path if none has been set.
     *
     * @param path new path
     * @since 4.0.0
     */
    public void initPath(final Supplier<NodePath> path) {
        if (this.path == null) {
            this.path = path;
        }
    }

    /**
     * Get the exception's message without any extra formatting.
     *
     * @return the raw message
     * @since 4.0.0
     */
    public @Nullable String rawMessage() {
        return super.getMessage();
    }

    /**
     * Get a description of the location of this error, with path included.
     *
     * @return message
     * @since 4.0.0
     */
    @Override
    public @Nullable String getMessage() {
        return this.path().toString() + ": " + super.getMessage();
    }

}
