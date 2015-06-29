/**
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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Handlers for various comment formats
 */
public enum CommentHandlers implements CommentHandler {
    HASH("#"),
    DOUBLE_SLASH("//"),
    /**
     * Block comments delineated by
     */
    SLASH_BLOCK() {
        @Override
        public Optional<String> extractHeader(BufferedReader reader) throws IOException {
            final StringBuilder build = new StringBuilder();
            String line = reader.readLine();
            if (line == null) {
                return Optional.absent();
            }
            if (!line.trim().startsWith("/*")) {
                return Optional.absent();
            }
            line = line.substring(line.indexOf("/*") + 2);
            if (handleSingleLine(build, line)) {
                for (line = reader.readLine(); line != null; line = reader.readLine()) {
                    if (!handleSingleLine(build, line)) {
                        break;
                    }
                }
            }
            line = reader.readLine();
            if (!(line == null || line.trim().isEmpty())) { // Require a blank line after a comment to make it a header
                return Optional.absent();
            }

            if (build.length() > 0) {
                return Optional.of(build.toString());
            } else {
                return Optional.absent();
            }
        }

        private boolean handleSingleLine(StringBuilder builder, String line) {
            boolean moreLines = true;
            if (line.trim().endsWith("*/")) {
                line = line.substring(0, line.lastIndexOf("*/"));
                if (line.endsWith(" ")) {
                    line = line.substring(0, line.length() - 1);
                }

                moreLines = false;
                if (line.isEmpty()) {
                    return false;
                }
            }
            if (line.trim().startsWith("*")) {
                line = line.substring(line.indexOf("*") + 1);
            }

            if (line.startsWith(" ")) {
                line = line.substring(1);
            }

            if (builder.length() > 0) {
                builder.append(AbstractConfigurationLoader.LINE_SEPARATOR);
            }
            builder.append(line);
            return moreLines;
        }

        @Override
        public Collection<String> toComment(Collection<String> lines) {
            if (lines.size() == 1) {
                return Collections2.transform(lines, new Function<String, String>() {
                    @Override
                    public String apply(String input) {
                        return "/* " + input + " */";
                    }
                });
            } else {
                Collection<String> ret = new ArrayList<>();
                ret.add("/*");
                ret.addAll(Collections2.transform(lines, new Function<String, String>() {
                    @Override
                    public String apply(String input) {
                        return " * " + input;
                    }
                }));
                ret.add(" */");
                return ret;
            }
        }
    }
    ;

    private static final int READAHEAD_LEN = 4096;

    private final String commentPrefix;

    private CommentHandlers() {
        this.commentPrefix = null;
    }

    private CommentHandlers(String commentPrefix) {
        this.commentPrefix = commentPrefix;
    }

    public Optional<String> extractHeader(BufferedReader reader) throws IOException {
        StringBuilder build = new StringBuilder();
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            if (line.trim().startsWith(commentPrefix)) {
                line = line.substring(line.indexOf(commentPrefix) + 1);
                if (line.startsWith(" ")) {
                    line = line.substring(1);
                }
                if (build.length() > 0) {
                    build.append(AbstractConfigurationLoader.LINE_SEPARATOR);
                }
                build.append(line);
            } else {
                if (line.trim().isEmpty()) {
                    break;
                } else {
                    return Optional.absent();
                }
            }
        }
        // We've reached the end of the document?
        return build.length() > 0 ? Optional.fromNullable(build.toString()) : Optional.<String>absent();
    }

    @Override
    public Collection<String> toComment(Collection<String> lines) {
        return Collections2.transform(lines, new Function<String, String>() {
            @Override
            public String apply(String s) {
                if (s.startsWith(" ")) {
                    return commentPrefix + s;
                } else {
                    return commentPrefix + " " + s;
                }
            }
        });
    }

    public static String extractComment(BufferedReader reader, CommentHandler... allowedHeaderTypes) throws IOException {
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
}
