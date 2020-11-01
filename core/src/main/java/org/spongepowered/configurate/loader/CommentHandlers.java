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

import com.google.errorprone.annotations.Immutable;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.util.Strings;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Defines a number of default {@link CommentHandler}s.
 *
 * @since 4.0.0
 */
@Immutable
public enum CommentHandlers implements CommentHandler {

    /**
     * {@link CommentHandler} for comments prefixed by the <code>#</code> character.
     */
    HASH(new AbstractPrefixHandler("#")),

    /**
     * {@link CommentHandler} for comments prefixed by a <code>//</code> escape.
     */
    DOUBLE_SLASH(new AbstractPrefixHandler("//")),

    /**
     * {@link CommentHandler} for comments delineated using <code>/*  *\</code>.
     */
    SLASH_BLOCK(new AbstractDelineatedHandler("/*", "*/", "*")),

    /**
     * {@link CommentHandler} for comments delineated using <code>&lt;!--  --&gt;</code>.
     */
    XML_STYLE(new AbstractDelineatedHandler("<!--", "-->", "~"));

    /**
     * Limit on the number of characters that may be read by a comment handler
     * while still preserving the mark.
     */
    private static final int READAHEAD_LEN = 4096;

    private final CommentHandler delegate;

    CommentHandlers(final CommentHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    public @Nullable String extractHeader(final BufferedReader reader) throws IOException {
        return this.delegate.extractHeader(reader);
    }

    @Override
    public Stream<String> toComment(final Stream<String> lines) {
        return this.delegate.toComment(lines);
    }

    /**
     * Uses provided comment handlers to extract a comment from the reader.
     *
     * @param reader to extract a comment from
     * @param allowedHeaderTypes handlers to try
     * @return extracted comment, or null if a comment could not be extracted
     * @throws IOException if an IO error occurs
     * @since 4.0.0
     */
    public static @Nullable String extractComment(final BufferedReader reader,
            final Iterable<CommentHandler> allowedHeaderTypes) throws IOException {
        reader.mark(READAHEAD_LEN);
        for (CommentHandler handler : allowedHeaderTypes) {
            final @Nullable String comment = handler.extractHeader(reader);
            if (comment == null) {
                reader.reset();
            } else {
                return comment;
            }
        }
        return null;
    }

    @Immutable
    private static final class AbstractDelineatedHandler implements CommentHandler {
        private final String startSequence;
        private final String endSequence;
        private final String lineIndentSequence;

        private AbstractDelineatedHandler(final String startSequence, final String endSequence, final String lineIndentSequence) {
            this.startSequence = startSequence;
            this.endSequence = endSequence;
            this.lineIndentSequence = lineIndentSequence;
        }

        @Override
        public @Nullable String extractHeader(final BufferedReader reader) throws IOException {
            if (!beginsWithPrefix(this.startSequence, reader)) {
                return null;
            }

            final StringBuilder build = new StringBuilder();
            String line = reader.readLine();
            if (line == null) {
                return null;
            }
            if (handleSingleLine(build, line)) {
                for (line = reader.readLine(); line != null; line = reader.readLine()) {
                    if (!handleSingleLine(build, line)) {
                        break;
                    }
                }
            }
            line = reader.readLine();
            if (!(line == null || line.trim().isEmpty())) { // Require a blank line after a comment to make it a header
                return null;
            }

            if (build.length() > 0) {
                return build.toString();
            } else {
                return null;
            }
        }

        private boolean handleSingleLine(final StringBuilder builder, String line) {
            boolean moreLines = true;
            if (line.trim().endsWith(this.endSequence)) {
                line = line.substring(0, line.lastIndexOf(this.endSequence));
                if (line.endsWith(" ")) {
                    line = line.substring(0, line.length() - 1);
                }

                moreLines = false;
                if (line.isEmpty()) {
                    return false;
                }
            }
            if (line.trim().startsWith(this.lineIndentSequence)) {
                line = line.substring(line.indexOf(this.lineIndentSequence) + 1);
            }

            if (line.length() > 0 && line.charAt(0) == ' ') {
                line = line.substring(1);
            }

            if (builder.length() > 0) {
                builder.append(AbstractConfigurationLoader.CONFIGURATE_LINE_SEPARATOR);
            }
            builder.append(line.replace("\r", "").replace("\n", "").replace("\r\n", ""));
            return moreLines;
        }

        @Override
        public Stream<String> toComment(final Stream<String> lines) {
            final Stream.Builder<String> build = Stream.builder();
            boolean first = true;
            for (Iterator<String> it = lines.iterator(); it.hasNext();) {
                final String next = it.next();
                if (first) {
                    if (!it.hasNext()) {
                        build.add(this.startSequence + " " + next + " " + this.endSequence);
                        return build.build();
                    } else {
                        build.add(this.startSequence);
                    }
                    first = false;
                }
                build.add(" " + this.lineIndentSequence + " " + next);
            }
            build.add(" " + this.endSequence);
            return build.build();
        }
    }

    @Immutable
    private static final class AbstractPrefixHandler implements CommentHandler {
        private final String commentPrefix;

        AbstractPrefixHandler(final String commentPrefix) {
            this.commentPrefix = commentPrefix;
        }

        @Override
        public @Nullable String extractHeader(final BufferedReader reader) throws IOException {
            if (!beginsWithPrefix(this.commentPrefix, reader)) {
                return null;
            }
            boolean firstLine = true;

            final StringBuilder build = new StringBuilder();
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (firstLine) {
                    if (line.length() > 0 && line.charAt(0) == ' ') {
                        line = line.substring(1);
                    }
                    build.append(line);
                    firstLine = false;
                } else if (line.trim().startsWith(this.commentPrefix)) {
                    line = line.substring(line.indexOf(this.commentPrefix) + 1);
                    if (line.length() > 0 && line.charAt(0) == ' ') {
                        line = line.substring(1);
                    }
                    if (build.length() > 0) {
                        build.append(AbstractConfigurationLoader.CONFIGURATE_LINE_SEPARATOR);
                    }
                    build.append(line);
                } else if (Strings.isBlank(line)) {
                    break;
                } else {
                    return null;
                }
            }
            // We've reached the end of the document?
            return build.length() > 0 ? build.toString() : null;
        }

        @Override
        public Stream<String> toComment(final Stream<String> lines) {
            return lines
                    .map(s -> {
                        if (s.length() > 0 && s.charAt(0) == ' ') {
                            return this.commentPrefix + s;
                        } else {
                            return this.commentPrefix + " " + s;
                        }
                    });
        }
    }

    /**
     * Consumes the length of the comment prefix from the reader and returns
     * whether or not the contents from the reader matches the expected prefix.
     */
    static boolean beginsWithPrefix(final String commentPrefix, final BufferedReader reader) throws IOException {
        final CharBuffer buf = CharBuffer.allocate(commentPrefix.length());
        if (reader.read(buf) != buf.limit()) {
            return false;
        }
        buf.flip();
        return commentPrefix.contentEquals(buf);
    }

}
