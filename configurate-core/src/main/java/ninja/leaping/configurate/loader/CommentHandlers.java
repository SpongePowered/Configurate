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
package ninja.leaping.configurate.loader;

import com.google.common.collect.Collections2;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Defines a number of default {@link CommentHandler}s.
 */
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
     * Limit on the number of characters that may be read by a comment handler while still
     * preserving the mark.
     */
    private static final int READAHEAD_LEN = 4096;

    private final CommentHandler delegate;

    CommentHandlers(CommentHandler delegate) {
        this.delegate = delegate;
    }

    @NonNull
    @Override
    public Optional<String> extractHeader(@NonNull BufferedReader reader) throws IOException {
        return delegate.extractHeader(reader);
    }

    @NonNull
    @Override
    public Collection<String> toComment(@NonNull Collection<String> lines) {
        return delegate.toComment(lines);
    }

    /**
     * Uses provided comment handlers to extract a comment from the reader.
     *
     * @param reader The reader
     * @param allowedHeaderTypes The handlers to try
     * @return The extracted comment, or null if a comment could not be extracted
     * @throws IOException If an IO error occurs
     */
    @Nullable
    public static String extractComment(@NonNull BufferedReader reader, @NonNull CommentHandler... allowedHeaderTypes) throws IOException {
        reader.mark(READAHEAD_LEN);
        for (CommentHandler handler : allowedHeaderTypes) {
            Optional<String> comment = handler.extractHeader(reader);
            if (!comment.isPresent()) {
                reader.reset();
            } else {
                return comment.get();
            }
        }
        return null;
    }

    private static final class AbstractDelineatedHandler implements CommentHandler {
        private final String startSequence;
        private final String endSequence;
        private final String lineIndentSequence;

        private AbstractDelineatedHandler(String startSequence, String endSequence, String lineIndentSequence) {
            this.startSequence = startSequence;
            this.endSequence = endSequence;
            this.lineIndentSequence = lineIndentSequence;
        }

        @NonNull
        @Override
        public Optional<String> extractHeader(@NonNull BufferedReader reader) throws IOException {
            final StringBuilder build = new StringBuilder();
            String line = reader.readLine();
            if (line == null) {
                return Optional.empty();
            }
            if (!line.trim().startsWith(startSequence)) {
                return Optional.empty();
            }
            line = line.substring(line.indexOf(startSequence) + startSequence.length());
            if (handleSingleLine(build, line)) {
                for (line = reader.readLine(); line != null; line = reader.readLine()) {
                    if (!handleSingleLine(build, line)) {
                        break;
                    }
                }
            }
            line = reader.readLine();
            if (!(line == null || line.trim().isEmpty())) { // Require a blank line after a comment to make it a header
                return Optional.empty();
            }

            if (build.length() > 0) {
                return Optional.of(build.toString());
            } else {
                return Optional.empty();
            }
        }

        private boolean handleSingleLine(StringBuilder builder, String line) {
            boolean moreLines = true;
            if (line.trim().endsWith(endSequence)) {
                line = line.substring(0, line.lastIndexOf(endSequence));
                if (line.endsWith(" ")) {
                    line = line.substring(0, line.length() - 1);
                }

                moreLines = false;
                if (line.isEmpty()) {
                    return false;
                }
            }
            if (line.trim().startsWith(lineIndentSequence)) {
                line = line.substring(line.indexOf(lineIndentSequence) + 1);
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
        public Collection<String> toComment(@NonNull Collection<String> lines) {
            if (lines.size() == 1) {
                return lines.stream().map(i -> startSequence + " " + i + " " + endSequence).collect(Collectors.toList());
            } else {
                Collection<String> ret = new ArrayList<>();
                ret.add(startSequence);
                ret.addAll(lines.stream().map(i -> " " + lineIndentSequence + " " + i).collect(Collectors.toList()));
                ret.add(" " + endSequence);
                return ret;
            }
        }
    }

    private static final class AbstractPrefixHandler implements CommentHandler {
        private final String commentPrefix;

        AbstractPrefixHandler(String commentPrefix) {
            this.commentPrefix = commentPrefix;
        }

        @NonNull
        @Override
        public Optional<String> extractHeader(@NonNull BufferedReader reader) throws IOException {
            StringBuilder build = new StringBuilder();
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (line.trim().startsWith(commentPrefix)) {
                    line = line.substring(line.indexOf(commentPrefix) + 1);
                    if (line.startsWith(" ")) {
                        line = line.substring(1);
                    }
                    if (build.length() > 0) {
                        build.append(AbstractConfigurationLoader.CONFIGURATE_LINE_SEPARATOR);
                    }
                    build.append(line);
                } else {
                    if (line.trim().isEmpty()) {
                        break;
                    } else {
                        return Optional.empty();
                    }
                }
            }
            // We've reached the end of the document?
            return build.length() > 0 ? Optional.of(build.toString()) : Optional.empty();
        }

        @NonNull
        @Override
        public Collection<String> toComment(@NonNull Collection<String> lines) {
            return Collections2.transform(lines, s -> {
                if (s.startsWith(" ")) {
                    return commentPrefix + s;
                } else {
                    return commentPrefix + " " + s;
                }
            });
        }
    }

}
