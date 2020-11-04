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
package org.spongepowered.configurate.loader;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.IOException;
import java.util.Arrays;

/**
 * Indicates an error that occurred while parsing the configuration.
 *
 * <p>These exceptions can include a specific position reference
 * within a file.</p>
 *
 * @since 4.0.0
 */
public class ParsingException extends ConfigurateException {

    /**
     * Indicates a line or column is unknown.
     */
    public static final int UNKNOWN_POS = -1;

    private static final char POSITION_MARKER = '^';
    private static final long serialVersionUID = 8379206111053602577L;

    /**
     * Given an unknown {@link IOException}, return it as a Configurate type.
     *
     * <p>If the input {@code ex} is already a {@link ParsingException},
     * this method returns the input value.</p>
     *
     * @param source node where the source exception was thrown
     * @param ex the source exception
     * @return an exception, either casted or wrapped
     * @since 4.0.0
     */
    public static ParsingException wrap(final ConfigurationNode source, final IOException ex) {
        if (ex instanceof ParsingException) {
            return (ParsingException) ex;
        } else {
            return new ParsingException(source, -1, -1, null, null, ex);
        }
    }

    private final int line;
    private final int column;
    private final @Nullable String context;

    /**
     * Create a new parsing exception.
     *
     * @param position position in the node structure where the error occurred
     * @param line line with issue
     * @param column column in the line
     * @param context the line in a file where the error occurred
     * @param message message describing the error
     * @since 4.0.0
     */
    public ParsingException(
        final ConfigurationNode position,
        final int line,
        final int column,
        final String context,
        final @Nullable String message
    ) {
        this(position, line, column, context, message, null);
    }

    /**
     * Create a new parsing exception.
     *
     * @param line line with issue
     * @param column column in the line
     * @param context the line in a file where the error occurred
     * @param message message describing the error
     * @param cause direct cause
     * @since 4.0.0
     */
    public ParsingException(
        final int line,
        final int column,
        final @Nullable String context,
        final @Nullable String message,
        final @Nullable Throwable cause
    ) {
        super(message, cause);
        this.line = line;
        this.column = column;
        this.context = context;
    }

    /**
     * Create a new parsing exception.
     *
     * @param position position in the node structure where the error occurred
     * @param line line with issue
     * @param column column in the line
     * @param context the line in a file where the error occurred
     * @param message message describing the error
     * @param cause direct cause
     * @since 4.0.0
     */
    public ParsingException(
        final ConfigurationNode position,
        final int line,
        final int column,
        final @Nullable String context,
        final @Nullable String message,
        final @Nullable Throwable cause
    ) {
        super(position, message, cause);
        this.line = line;
        this.column = column;
        this.context = context;
    }

    /**
     * Line most closely associated with this error.
     *
     * @return line, or {@code -1} for unknown
     * @since 4.0.0
     */
    public int line() {
        return this.line;
    }

    /**
     * Column most closely associated with the error.
     *
     * @return column, or {@code -1} for unknown
     * @since 4.0.0
     */
    public int column() {
        return this.column;
    }

    /**
     * A context line from the source, if available.
     *
     * @return context line
     * @since 4.0.0
     */
    public @Nullable String context() {
        return this.context;
    }

    @Override
    public @Nullable String getMessage() {
        if (this.line == UNKNOWN_POS || this.column == UNKNOWN_POS) {
            return super.getMessage();
        }
        final @Nullable String rawMessage = this.rawMessage();
        final StringBuilder message = new StringBuilder(rawMessage == null ? 0 : (rawMessage.length() + 20));
        message.append(this.path())
                .append("@(line ").append(this.line).append(", col ").append(this.column).append("): ")
                .append(rawMessage);

        if (this.context != null) {
            message.append(System.lineSeparator()).append(this.context);
            if (this.column >= 0 && this.column < this.context.length()) {
                message.append(System.lineSeparator());
                if (this.column > 0) {
                    final char[] spaces = new char[this.column - 1];
                    Arrays.fill(spaces, ' ');
                    message.append(spaces);
                }

                message.append(POSITION_MARKER);
            }
        }

        return message.toString();
    }

}
