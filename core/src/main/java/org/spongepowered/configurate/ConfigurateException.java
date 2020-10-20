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
import org.spongepowered.configurate.transformation.NodePath;

import java.util.function.Supplier;

/**
 * Any sort of error thrown within Configurate.
 *
 * <p>Configurate's errors are designed to provide a view of as
 * many errors as possible within one configuration tree, though the
 * {@link Throwable#getSuppressed() suppressed exceptions}</p>
 */
public class ConfigurateException extends Exception {

    private static final long serialVersionUID = 1635526451813128733L;

    private @Nullable Supplier<NodePath> path;

    /**
     * Create a new unknown exception.
     */
    public ConfigurateException() {
        this.path = null;
    }

    /**
     * Create a new exception at unknown path with provided
     * informational message.
     *
     * @param message informational message
     */
    public ConfigurateException(final String message) {
        super(message);
        this.path = null;
    }

    /**
     * Create a new exception with a cause and unknown message.
     *
     * @param cause the cause of this exception
     */
    public ConfigurateException(final Throwable cause) {
        super(cause);
        this.path = null;
    }

    /**
     * Create a new exception with informational message and cause.
     *
     * @param message the informational message
     * @param cause the cause of the exception
     */
    public ConfigurateException(final @Nullable String message, final @Nullable Throwable cause) {
        super(message, cause);
        this.path = null;
    }

    /**
     * Create a new exception pre-initialized with path and message.
     *
     * @param pos node where the error occurred
     * @param message message describing the error
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
     */
    public ConfigurateException(final NodePath path, final @Nullable String message, final @Nullable Throwable cause) {
        super(message, cause);
        this.path = () -> path;
    }

    /**
     * Get the path associated with this failure.
     *
     * @return the path
     */
    public NodePath path() {
        final @Nullable Supplier<NodePath> path = this.path;
        return path == null ? NodePath.path() : path.get();
    }

    /**
     * Initialize path if none has been set.
     *
     * @param path new path
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
     */
    public @Nullable String rawMessage() {
        return super.getMessage();
    }

    /**
     * Get a description of the location of this error, with path included.
     *
     * @return message
     */
    @Override
    public @Nullable String getMessage() {
        return this.path().toString() + ": " + super.getMessage();
    }

}
