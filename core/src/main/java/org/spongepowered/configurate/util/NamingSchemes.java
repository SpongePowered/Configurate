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
package org.spongepowered.configurate.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Standard naming schemes.
 */
public enum NamingSchemes implements NamingScheme {

    /**
     * Passes through names unchanged.
     */
    PASSTHROUGH {
        @Override
        public String coerce(final String input) {
            return input;
        }
    },

    /**
     * Reformats names to {@code camelCase} style.
     */
    CAMEL_CASE {
        @Override
        public String coerce(final String input) {
            final Matcher match = DASH_UNDERSCORE.matcher(input);
            if (!match.find()) {
                return input;
            }
            final StringBuffer ret = new StringBuffer(input.length());
            do {
                match.appendReplacement(ret, "");
                ret.appendCodePoint(input.codePointAt(match.start()));
                ret.appendCodePoint(Character.toUpperCase(input.codePointBefore(match.end())));
            } while (match.find());
            match.appendTail(ret);
            return ret.toString();
        }
    },

    /**
     * Reformats names to {@code snake_case} format.
     */
    SNAKE_CASE {
        @Override
        public String coerce(final String input) {
            return NamingSchemes.enforceLowerCaseSeparatorChar(input, UNDERSCORE, DASH);
        }
    },

    /**
     * Reformats names to {@code lower-case-dashed} format.
     */
    LOWER_CASE_DASHED {
        @Override
        public String coerce(final String input) {
            return NamingSchemes.enforceLowerCaseSeparatorChar(input, DASH, UNDERSCORE);
        }
    };

    private static final Pattern DASH_UNDERSCORE = Pattern.compile(".[-_].");
    private static final char UNDERSCORE = '_';
    private static final char DASH = '-';

    // Common logic for snake_case and dash-separated
    private static String enforceLowerCaseSeparatorChar(final String input, final char preferredDelimiter, final char convertDelimiter) {
        final StringBuilder build = new StringBuilder(input);
        for (int i = 0; i < build.length(); i++) {
            final int ch = build.codePointAt(i);
            if (ch == convertDelimiter) {
                if (i != 0 && i != build.length() - 1) { // only convert actual separators
                    build.setCharAt(i, preferredDelimiter);
                }
            } else if (Character.isUpperCase(ch)) {
                build.insert(i++, preferredDelimiter);
                final int lower = Character.toLowerCase(ch);
                if (Character.isBmpCodePoint(lower)) {
                    build.setCharAt(i, (char) lower);
                } else {
                    build.setCharAt(i++, Character.highSurrogate(lower));
                    build.setCharAt(i, Character.lowSurrogate(lower));
                }
            }
        }
        return build.toString();

    }

}
