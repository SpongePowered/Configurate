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
package org.spongepowered.configurate.serialize;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.NodePath;

import java.lang.reflect.Type;

/**
 * Exception thrown on errors encountered while using type serializers.
 *
 * @since 4.0.0
 */
public class SerializationException extends ConfigurateException {

    public static final long serialVersionUID = -910568375387191784L;
    private transient @Nullable Type expectedType;

    /**
     * Create an exception with unknown message and cause.
     *
     * @since 4.0.0
     */
    public SerializationException() {
    }

    /**
     * Create an exception without a cause.
     *
     * @param message message with information about the exception
     * @since 4.0.0
     */
    public SerializationException(final String message) {
        super(message);
    }

    /**
     * Create an exception with a cause and no additional information.
     *
     * @param cause wrapped causing throwable
     * @since 4.0.0
     */
    public SerializationException(final Throwable cause) {
        super(cause);
    }

    /**
     * Create an exception without a cause.
     *
     * @param expectedType declared type being processed
     * @param message message with information about the exception
     * @since 4.0.0
     */
    public SerializationException(final Type expectedType, final String message) {
        super(message);
        this.expectedType = expectedType;
    }

    /**
     * Create an exception with a cause and no additional information.
     *
     * @param expectedType declared type being processed
     * @param cause wrapped causing throwable
     * @since 4.0.0
     */
    public SerializationException(final Type expectedType, final Throwable cause) {
        super(cause);
        this.expectedType = expectedType;
    }

    /**
     * Create an exception with message and wrapped cause.
     *
     * @param expectedType declared type being processed
     * @param message informational message
     * @param cause cause to be wrapped
     * @since 4.0.0
     */
    public SerializationException(final Type expectedType, final String message, final Throwable cause) {
        super(message, cause);
        this.expectedType = expectedType;
    }

    /**
     * Create an exception with a message and unknown cause.
     *
     * @param node node being processed
     * @param message informational message
     * @param expectedType declared type being processed
     * @since 4.0.0
     */
    public SerializationException(final ConfigurationNode node, final Type expectedType, final String message) {
        this(node, expectedType, message, null);
    }

    /**
     * Create an exception with wrapped cause.
     *
     * @param node node being processed
     * @param expectedType declared type being processed
     * @param cause cause to be wrapped
     * @since 4.0.0
     */
    public SerializationException(final ConfigurationNode node, final Type expectedType, final Throwable cause) {
        this(node, expectedType, null, cause);
    }

    /**
     * Create an exception with message and wrapped cause.
     *
     * @param node node being processed
     * @param expectedType declared type being processed
     * @param message informational message
     * @param cause cause to be wrapped
     * @since 4.0.0
     */
    public SerializationException(final ConfigurationNode node, final Type expectedType,
            final @Nullable String message, final @Nullable Throwable cause) {
        super(node, message, cause);
        this.expectedType = expectedType;
    }

    /**
     * Create an exception with message and wrapped cause.
     *
     * @param path path to node being processed
     * @param expectedType declared type being processed
     * @param message informational message
     * @since 4.0.0
     */
    public SerializationException(final NodePath path, final Type expectedType, final String message) {
        super(path, message, null);

        this.expectedType = expectedType;
    }

    /**
     * Get the desired type associated with the failed object mapping operation.
     *
     * @return type
     * @since 4.0.0
     */
    public @Nullable Type expectedType() {
        return this.expectedType;
    }

    @Override
    public @Nullable String getMessage() {
        if (this.expectedType == null) {
            return super.getMessage();
        } else {
            return path() + " of type " + this.expectedType.getTypeName() + ": " + rawMessage();
        }
    }

    /**
     * Initialize the expected type.
     *
     * <p>If a type has already been set, it will not be overridden.</p>
     *
     * @param type expected type
     * @since 4.0.0
     */
    public void initType(final Type type) {
        if (this.expectedType == null) {
            this.expectedType = type;
        }
    }

}
