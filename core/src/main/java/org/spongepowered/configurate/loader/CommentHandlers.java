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

import com.google.common.collect.Collections2;
import com.google.errorprone.annotations.Immutable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Defines a number of default {@link CommentHandler}s.
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
    public @Nullable String extractHeader(final @NonNull BufferedReader reader) throws IOException {
        return this.delegate.extractHeader(reader);
    }

    @NonNull
    @Override
    public Collection<String> toComment(final @NonNull Collection<String> lines) {
        return this.delegate.toComment(lines);
    }

    /**
     * Uses provided comment handlers to extract a comment from the reader.
     *
     * @param reader to extract a comment from
     * @param allowedHeaderTypes handlers to try
     * @return extracted comment, or null if a comment could not be extracted
     * @throws IOException If an IO error occurs
     */
    public static @Nullable String extractComment(final @NonNull BufferedReader reader,
            final @NonNull Iterable<CommentHandler> allowedHeaderTypes) throws IOException {
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
        public @Nullable String extractHeader(final @NonNull BufferedReader reader) throws IOException {
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

            if (line.startsWith(" ")) {
                line = line.substring(1);
            }

            if (builder.length() > 0) {
                builder.append(AbstractConfigurationLoader.CONFIGURATE_LINE_SEPARATOR);
            }
            builder.append(line.replace("\r", "").replace("\n", "").replace("\r\n", ""));
            return moreLines;
        }

        @NonNull
        @Override
        public Collection<String> toComment(final @NonNull Collection<String> lines) {
            if (lines.size() == 1) {
                return lines.stream().map(i -> this.startSequence + " " + i + " " + this.endSequence).collect(Collectors.toList());
            } else {
                final Collection<String> ret = new ArrayList<>();
                ret.add(this.startSequence);
                ret.addAll(lines.stream().map(i -> " " + this.lineIndentSequence + " " + i).collect(Collectors.toList()));
                ret.add(" " + this.endSequence);
                return ret;
            }
        }
    }

    @Immutable
    private static final class AbstractPrefixHandler implements CommentHandler {
        private final String commentPrefix;

        AbstractPrefixHandler(final String commentPrefix) {
            this.commentPrefix = commentPrefix;
        }

        @Override
        public @Nullable String extractHeader(final @NonNull BufferedReader reader) throws IOException {
            if (!beginsWithPrefix(this.commentPrefix, reader)) {
                return null;
            }
            boolean firstLine = true;

            final StringBuilder build = new StringBuilder();
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (firstLine) {
                    if (line.startsWith(" ")) {
                        line = line.substring(1);
                    }
                    build.append(line);
                    firstLine = false;
                } else if (line.trim().startsWith(this.commentPrefix)) {
                    line = line.substring(line.indexOf(this.commentPrefix) + 1);
                    if (line.startsWith(" ")) {
                        line = line.substring(1);
                    }
                    if (build.length() > 0) {
                        build.append(AbstractConfigurationLoader.CONFIGURATE_LINE_SEPARATOR);
                    }
                    build.append(line);
                } else if (line.trim().isEmpty()) {
                    break;
                } else {
                    return null;
                }
            }
            // We've reached the end of the document?
            return build.length() > 0 ? build.toString() : null;
        }

        @Override
        public @NonNull Collection<String> toComment(final @NonNull Collection<String> lines) {
            return Collections2.transform(lines, s -> {
                if (s.startsWith(" ")) {
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
